package com.skala.clickhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import com.skala.clickhub.dto.project.ProjectDtos.SummaryResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.service.ReactionService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * §5 — 좋아요/즐겨찾기 토글, 내 즐겨찾기 목록, 프로젝트 댓글 CRUD.
 *
 * 좋아요/즐겨찾기/댓글은 project_reactions/project_comments 테이블에 상태 제약이 없어(랭킹 뷰와
 * 달리 PUBLISHED 트리거 요건이 없음) DRAFT 프로젝트로도 충분하다 — RankingIntegrationTest처럼
 * URL 검증/상태 전이 시딩까지 갈 필요는 없다.
 *
 * 서비스 메서드를 직접 호출해 실제 Postgres/트리거/제약을 통과하는지 검증하고, 인증 여부에 따른
 * 접근 제어만 MockMvc로 별도 확인한다(로그인 필요 API에 실제 JWT를 발급해 태우는 인프라가 아직
 * 없어, "토큰 없이 호출" 케이스만 MockMvc로 검증).
 */
@Testcontainers
@SpringBootTest
@Transactional
class ReactionIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE)
            .withDatabaseName("clickhub")
            .withUsername("clickhub")
            .withPassword("test-only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        // application.properties의 기본 datasource.url은 "?stringtype=unspecified"를 붙여서
        // Postgres 네이티브 enum 컬럼(예: interaction_events.actor_kind)에 문자열 파라미터를
        // 캐스트 없이 바인딩할 수 있게 해준다. Testcontainers가 주는 URL로 통째로 덮어쓰면 이
        // 옵션이 사라져 InteractionEventRecorder의 INSERT가 "column is of type X but expression
        // is of type character varying"으로 깨진다(실측 확인 — 이 테스트가 InteractionEventRecorder를
        // 거치는 첫 테스트라 지금까지 드러나지 않았던 문제).
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "&stringtype=unspecified");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.connect-retries", () -> "0");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private UUID ownerId;
    private UUID reactorId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        seedProjectAndUsers();
    }

    @Test
    void toggleLikeTogglesOnAndOff() {
        LikeResponse liked = reactionService.toggleLike(projectId, reactorId);
        assertTrue(liked.liked());
        assertEquals(1, liked.likeCount());

        LikeResponse unliked = reactionService.toggleLike(projectId, reactorId);
        assertFalse(unliked.liked());
        assertEquals(0, unliked.likeCount());
    }

    @Test
    void toggleFavoriteTogglesOnAndOff() {
        FavoriteResponse favorited = reactionService.toggleFavorite(projectId, reactorId);
        assertTrue(favorited.favorited());

        FavoriteResponse unfavorited = reactionService.toggleFavorite(projectId, reactorId);
        assertFalse(unfavorited.favorited());
    }

    @Test
    void listFavoritesReturnsFavoritedProject() {
        reactionService.toggleFavorite(projectId, reactorId);

        CursorPageResponse<SummaryResponse> page = reactionService.listFavorites(reactorId, null);

        assertEquals(1, page.items().size());
        assertEquals(projectId, page.items().get(0).id());
        assertFalse(page.hasNext());
    }

    @Test
    void createListAndDeleteComment() {
        CommentResponse created =
                reactionService.createComment(projectId, reactorId, new CommentCreateRequest("좋은 프로젝트네요!"));
        assertEquals("좋은 프로젝트네요!", created.body());

        CursorPageResponse<CommentResponse> page = reactionService.listComments(projectId, null);
        assertEquals(1, page.items().size());
        assertEquals(created.id(), page.items().get(0).id());

        BusinessException forbidden = assertThrows(BusinessException.class,
                () -> reactionService.deleteComment(projectId, created.id(), ownerId));
        assertEquals(ErrorCode.NOT_COMMENT_AUTHOR, forbidden.getErrorCode());

        reactionService.deleteComment(projectId, created.id(), reactorId);

        CursorPageResponse<CommentResponse> afterDelete = reactionService.listComments(projectId, null);
        assertTrue(afterDelete.items().isEmpty());
    }

    @Test
    void likeEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(put("/v1/projects/{id}/like", projectId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void commentsEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v1/projects/{id}/comments", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    private void seedProjectAndUsers() {
        ownerId = jdbcTemplate.queryForObject(
                """
                INSERT INTO users (github_user_id, github_login, github_connected_at,
                                    display_name, role, theme, new_project_notifications, auth_provider)
                VALUES (?, ?, now(), ?, 'USER', 'SYSTEM', true, 'GITHUB')
                RETURNING id
                """,
                UUID.class, 555001L, "reaction-owner", "Reaction Owner");

        reactorId = jdbcTemplate.queryForObject(
                """
                INSERT INTO users (github_user_id, github_login, github_connected_at,
                                    display_name, role, theme, new_project_notifications, auth_provider)
                VALUES (?, ?, now(), ?, 'USER', 'SYSTEM', true, 'GITHUB')
                RETURNING id
                """,
                UUID.class, 555002L, "reaction-reactor", "Reactor User");

        UUID categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM categories ORDER BY id LIMIT 1", UUID.class);

        projectId = jdbcTemplate.queryForObject(
                """
                INSERT INTO projects (owner_id, primary_category_id, title, description, site_url,
                                       pricing, tags, screenshots, status)
                VALUES (?, ?, 'Reaction Test Project', 'used for §5 reaction tests',
                        'https://reaction-test.example.com', 'FREE', ARRAY['test'], '[]'::jsonb, 'DRAFT')
                RETURNING id
                """,
                UUID.class, ownerId, categoryId);
    }
}
