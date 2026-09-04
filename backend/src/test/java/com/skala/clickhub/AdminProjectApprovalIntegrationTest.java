package com.skala.clickhub;

import com.skala.clickhub.dto.project.ProjectDtos.AdminPendingItem;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.service.ProjectService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 관리자 게시물 승인/거절(ProjectService.approve/reject/listPendingReview/getDetailForAdmin) 검증.
 * approve()의 실제 URL 재검증 "성공" 경로(record_project_url_validation까지 이어지는 전체 흐름)는
 * is_public_http_url() 제약상 로컬/사설 IP를 승인 대상 URL로 쓸 수 없어 외부 공인 도메인
 * (https://example.com, IANA 예약 도메인)에 대한 실제 네트워크 호출이 필요하다 — 기존
 * ApiE2ETests와 동일하게 이 저장소는 이미 네트워크 의존 테스트를 허용하는 편이다.
 * "URL 접속 불가" 실패 경로는 checkUrl()이 record_project_url_validation 호출 전에 막으므로
 * 네트워크·DB 제약 없이 로컬 포트로 검증한다.
 */
@Testcontainers
@SpringBootTest
@Transactional
class AdminProjectApprovalIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");
    private static final UUID OWNER_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");

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
    private ProjectService projectService;

    // approve()/reject()의 상태 변경은 트랜잭션이 끝날 때 flush된다. 같은 트랜잭션 안에서
    // jdbcTemplate으로 바로 조회하면 그 flush 이전 값을 보게 되므로, 조회 직전에 명시적으로 flush한다.
    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void seedOwner() {
        jdbcTemplate.update("""
                INSERT INTO users (id, auth_provider, google_subject, display_name)
                VALUES (?, 'GOOGLE', 'admin-approval-owner', '검토 대상 제작자')
                """, OWNER_ID);
    }

    @Test
    void approveMarksAsPublishedAfterRevalidatingTheRealUrl() {
        UUID projectId = seedPendingReview("https://example.com", true);

        var response = projectService.approve(projectId);
        entityManager.flush();

        assertThat(response.status()).isEqualTo("PUBLISHED");
        assertThat(count("SELECT COUNT(*) FROM projects WHERE id = ? AND status = 'PUBLISHED' "
                + "AND url_is_reachable AND url_checked_at IS NOT NULL", projectId)).isEqualTo(1);
    }

    @Test
    void approveFailsWithoutHittingDbWhenSiteUrlIsUnreachable() {
        UUID projectId = seedPendingReview("http://127.0.0.1:1", true);

        assertThatThrownBy(() -> projectService.approve(projectId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_URL_UNREACHABLE);
        assertThat(count("SELECT COUNT(*) FROM projects WHERE id = ? AND status = 'PENDING_REVIEW'", projectId))
                .isEqualTo(1);
    }

    @Test
    void approveRequiresPrimaryCategory() {
        UUID projectId = seedPendingReview("https://example.com", false);

        assertThatThrownBy(() -> projectService.approve(projectId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_CATEGORY_REQUIRED);
    }

    @Test
    void approveRejectsProjectsNotAwaitingReview() {
        UUID projectId = seedDraft("https://example.com");

        assertThatThrownBy(() -> projectService.approve(projectId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PROJECT_STATE);
    }

    @Test
    void rejectRecordsTheReasonAndStatus() {
        UUID projectId = seedPendingReview("https://example.com", true);

        var response = projectService.reject(projectId, "스크린샷과 실제 화면이 일치하지 않습니다.");
        entityManager.flush();

        assertThat(response.status()).isEqualTo("REJECTED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT rejection_reason FROM projects WHERE id = ?", String.class, projectId))
                .isEqualTo("스크린샷과 실제 화면이 일치하지 않습니다.");
    }

    @Test
    void listPendingReviewOnlyReturnsProjectsAwaitingReview() {
        UUID pendingId = seedPendingReview("https://example.com", true);
        seedDraft("https://example.com");

        List<AdminPendingItem> pending = projectService.listPendingReview();

        assertThat(pending).extracting(AdminPendingItem::id).containsExactly(pendingId);
        assertThat(pending.get(0).ownerName()).isEqualTo("검토 대상 제작자");
    }

    @Test
    void getDetailForAdminIgnoresTheOwnerOnlyVisibilityRuleOfGetDetail() {
        UUID projectId = seedPendingReview("https://example.com", true);

        assertThat(projectService.getDetailForAdmin(projectId).status()).isEqualTo("PENDING_REVIEW");
    }

    private UUID seedPendingReview(String siteUrl, boolean withCategory) {
        UUID projectId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id, owner_id, primary_category_id, title, description, site_url, status, tags, screenshots
                )
                VALUES (?, ?, %s,
                        '검토 대기 프로젝트', '관리자 승인 테스트용', ?, 'DRAFT', ARRAY['vue'], '[]'::jsonb)
                """.formatted(withCategory ? "(SELECT id FROM categories ORDER BY slug LIMIT 1)" : "NULL"),
                projectId, OWNER_ID, siteUrl);
        jdbcTemplate.update("UPDATE projects SET status = 'PENDING_REVIEW' WHERE id = ?", projectId);
        return projectId;
    }

    /** DRAFT는 PENDING_REVIEW를 거치지 않고 바로 만든다 — 트리거가 PENDING_REVIEW → DRAFT 역행은 막는다. */
    private UUID seedDraft(String siteUrl) {
        UUID projectId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO projects (
                    id, owner_id, primary_category_id, title, description, site_url, status, tags, screenshots
                )
                VALUES (?, ?, (SELECT id FROM categories ORDER BY slug LIMIT 1),
                        '검토 대기 프로젝트', '관리자 승인 테스트용', ?, 'DRAFT', ARRAY['vue'], '[]'::jsonb)
                """, projectId, OWNER_ID, siteUrl);
        return projectId;
    }

    private Integer count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }
}
