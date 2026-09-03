package com.skala.clickhub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.skala.clickhub.entity.DeveloperRankingView;
import com.skala.clickhub.entity.ProjectRankingView;
import com.skala.clickhub.repository.DeveloperRankingRepository;
import com.skala.clickhub.repository.ProjectRankingRepository;
import java.util.List;
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
 * project_top100_7d / developer_top100_7d 뷰(V1__initial_schema.sql)가 실제로 값을 내려주는지,
 * 그리고 RankingController가 그 값을 정상적으로 노출하는지 검증한다.
 *
 * 뷰가 값을 내려면 프로젝트가 PUBLISHED 상태여야 하는데, validate_project_write() 트리거가
 * 이 상태 전이를 엄격히 강제한다(DRAFT로만 INSERT 가능, PUBLISHED 직전엔 최근 URL 검증 필요 등).
 * 그래서 트리거가 요구하는 순서 그대로 JdbcTemplate으로 시드 데이터를 만든다.
 *
 * @Transactional로 각 테스트 메서드를 롤백시킨다 — 그렇지 않으면 @BeforeEach가 매번 넣는
 * github_login 등 유니크 제약 값이 이전 테스트가 커밋한 행과 충돌한다(실측 확인:
 * DuplicateKeyException on users_github_login_uq).
 */
@Testcontainers
@SpringBootTest
@Transactional
class RankingIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE)
            .withDatabaseName("clickhub")
            .withUsername("clickhub")
            .withPassword("test-only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.connect-retries", () -> "0");
    }

    private static final String SITE_URL = "https://example.com";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProjectRankingRepository projectRankingRepository;

    @Autowired
    private DeveloperRankingRepository developerRankingRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private UUID ownerId;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        seedPublishedProjectWithMetrics();
    }

    @Test
    void projectRankingViewReflectsPublishedProjectMetrics() {
        List<ProjectRankingView> ranking = projectRankingRepository.findTop100ByOrderByScoreDesc();

        assertEquals(1, ranking.size());
        assertEquals(projectId, ranking.get(0).getProjectId());
        assertEquals("Test Project", ranking.get(0).getTitle());
        assertTrue(ranking.get(0).getScore() > 0);
    }

    @Test
    void developerRankingViewReflectsPublishedProjectMetrics() {
        List<DeveloperRankingView> ranking = developerRankingRepository.findTop100ByOrderByScoreDesc();

        assertEquals(1, ranking.size());
        assertEquals(ownerId, ranking.get(0).getCreatorId());
        assertEquals("Test Owner", ranking.get(0).getDisplayName());
        assertTrue(ranking.get(0).getScore() > 0);
    }

    @Test
    void projectRankingEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v1/rankings/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.data[0].rank").value(1));
    }

    @Test
    void developerRankingEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v1/rankings/developers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].creatorId").value(ownerId.toString()))
                .andExpect(jsonPath("$.data[0].rank").value(1));
    }

    private void seedPublishedProjectWithMetrics() {
        ownerId = jdbcTemplate.queryForObject(
                """
                INSERT INTO users (github_user_id, github_login, github_connected_at,
                                    display_name, role, theme, new_project_notifications, auth_provider)
                VALUES (?, ?, now(), ?, 'USER', 'SYSTEM', true, 'GITHUB')
                RETURNING id
                """,
                UUID.class, 123456789L, "test-owner", "Test Owner");

        UUID categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM categories ORDER BY id LIMIT 1", UUID.class);

        projectId = jdbcTemplate.queryForObject(
                """
                INSERT INTO projects (owner_id, primary_category_id, title, description, site_url,
                                       pricing, tags, screenshots, status)
                VALUES (?, ?, 'Test Project', 'A project used for ranking tests', ?,
                        'FREE', ARRAY['test'], '[]'::jsonb, 'DRAFT')
                RETURNING id
                """,
                UUID.class, ownerId, categoryId, SITE_URL);

        jdbcTemplate.update("UPDATE projects SET status = 'PENDING_REVIEW' WHERE id = ?", projectId);
        // record_project_url_validation()은 void를 반환하는 함수라 executeUpdate()가 아니라
        // (Postgres 드라이버가 SELECT에 executeUpdate를 쓰면 예외를 던진다) 쿼리로 호출해야 한다.
        jdbcTemplate.queryForObject(
                "SELECT record_project_url_validation(?, ?, true, 200, ?)",
                Object.class, projectId, SITE_URL, SITE_URL);
        jdbcTemplate.update("UPDATE projects SET status = 'PUBLISHED' WHERE id = ?", projectId);

        jdbcTemplate.update(
                """
                INSERT INTO project_daily_metrics (project_id, metric_date, unique_visitors,
                                                     valid_outbound_clicks, unique_likes, unique_commenters)
                VALUES (?, current_date, 100, 40, 20, 5)
                """,
                projectId);

        jdbcTemplate.update(
                """
                INSERT INTO creator_daily_metrics (creator_id, metric_date, subscriber_growth, active_projects)
                VALUES (?, current_date, 3, 1)
                """,
                ownerId);
    }
}
