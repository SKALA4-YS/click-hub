package com.skala.clickhub;

import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.user.UserDtos.OnboardingUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProfileUpdateRequest;
import com.skala.clickhub.service.CatalogService;
import com.skala.clickhub.service.EngagementService;
import com.skala.clickhub.service.SubscriptionService;
import com.skala.clickhub.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class MvpPersistenceIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");
    private static final UUID CREATOR_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID VIEWER_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID PROJECT_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE)
            .withDatabaseName("clickhub")
            .withUsername("clickhub")
            .withPassword("test-only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "?stringtype=unspecified");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.connect-retries", () -> "0");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EngagementService engagementService;

    @Autowired
    private UserService userService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private CatalogService catalogService;

    @BeforeEach
    void seedUsersAndProject() {
        insertUser(CREATOR_ID, "creator-subject", "프로젝트 제작자");
        insertUser(VIEWER_ID, "viewer-subject", "방문 사용자");
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id, owner_id, primary_category_id, title, description, site_url, status, tags, screenshots
                )
                VALUES (?, ?, (SELECT id FROM categories ORDER BY slug LIMIT 1),
                        'Click HUB', 'MVP integration project', 'https://clickhub.example',
                        'DRAFT', ARRAY['vue', 'spring'], '[]'::jsonb)
                """, PROJECT_ID, CREATOR_ID);
        jdbcTemplate.queryForObject(
                "SELECT record_project_url_validation(?, ?, true, 200, ?)",
                Object.class, PROJECT_ID, "https://clickhub.example", "https://clickhub.example");
        jdbcTemplate.update("UPDATE projects SET status = 'PENDING_REVIEW' WHERE id = ?", PROJECT_ID);
        jdbcTemplate.update("UPDATE projects SET status = 'PUBLISHED' WHERE id = ?", PROJECT_ID);
    }

    @Test
    void persistsLikesFavoritesAndCommentsAcrossReads() {
        assertThat(engagementService.toggleLike(VIEWER_ID, PROJECT_ID).liked()).isTrue();
        assertThat(engagementService.toggleLike(VIEWER_ID, PROJECT_ID).likeCount()).isZero();
        assertThat(engagementService.toggleLike(VIEWER_ID, PROJECT_ID).likeCount()).isOne();

        assertThat(engagementService.toggleFavorite(VIEWER_ID, PROJECT_ID).favorited()).isTrue();
        assertThat(userService.getMyFavorites(VIEWER_ID))
                .extracting(project -> project.id())
                .containsExactly(PROJECT_ID);

        var created = engagementService.createComment(
                VIEWER_ID, PROJECT_ID, new CommentCreateRequest(" 실제 DB 댓글 "));
        assertThat(created.authorId()).isEqualTo(VIEWER_ID);
        assertThat(created.body()).isEqualTo("실제 DB 댓글");
        assertThat(engagementService.getComments(PROJECT_ID))
                .extracting(comment -> comment.id())
                .containsExactly(created.id());
    }

    @Test
    void persistsProfileOnboardingCatalogAndSubscriptions() {
        var updated = userService.updateProfile(
                VIEWER_ID, new ProfileUpdateRequest("Click HUB 사용자", "dark", false));
        assertThat(updated.displayName()).isEqualTo("Click HUB 사용자");
        assertThat(updated.theme()).isEqualTo("DARK");
        assertThat(updated.newProjectNotifications()).isFalse();

        var categories = catalogService.getCategories();
        var technologies = catalogService.getTechnologies();
        assertThat(categories).hasSize(14);
        assertThat(technologies).hasSize(15);

        var onboarding = userService.updateOnboarding(VIEWER_ID, new OnboardingUpdateRequest(
                List.of("프로젝트 탐색"),
                List.of(categories.getFirst().slug()),
                List.of(technologies.getFirst().slug())
        ));
        assertThat(onboarding.goals()).containsExactly("프로젝트 탐색");
        assertThat(userService.getMe(VIEWER_ID).onboardingCompleted()).isTrue();
        assertThat(count("SELECT count(*) FROM user_onboarding_interest_categories WHERE user_id = ?", VIEWER_ID))
                .isOne();
        assertThat(count("SELECT count(*) FROM user_onboarding_interest_technologies WHERE user_id = ?", VIEWER_ID))
                .isOne();

        assertThat(subscriptionService.toggle(VIEWER_ID, CREATOR_ID).subscribed()).isTrue();
        assertThat(userService.getMySubscriptions(VIEWER_ID))
                .extracting(creator -> creator.id())
                .containsExactly(CREATOR_ID);
        var creator = userService.getCreator(CREATOR_ID, VIEWER_ID);
        assertThat(creator.subscriberCount()).isOne();
        assertThat(creator.subscribedByMe()).isTrue();
    }

    private void insertUser(UUID id, String subject, String displayName) {
        jdbcTemplate.update("""
                INSERT INTO users (id, auth_provider, google_subject, display_name)
                VALUES (?, 'GOOGLE', ?, ?)
                """, id, subject, displayName);
    }

    private Integer count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }
}
