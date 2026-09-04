package com.skala.clickhub.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: projects.
 * tags/screenshots는 조인 테이블이 아니라 projects 테이블의 배열/JSONB 컬럼이다.
 * url 검증 상태(url_checked_at 등)는 트리거(validate_project_write)가 채우는 값이라
 * 애플리케이션에서 직접 세팅하지 않는다.
 *
 * @DynamicUpdate: ProjectService.approve()가 승인 직전 record_project_url_validation()을
 * 네이티브 SQL로 직접 호출해 url_checked_at 등을 갱신한다. 이 엔티티는 그 값을 모르는 채로
 * 메모리에 남아있는데, 동적 업데이트가 없으면 이후 project.approve()로 status만 바꿔도
 * Hibernate가 매핑된 컬럼 전체를 엔티티의 (stale) 값으로 UPDATE에 실어 보내 방금 쓴 검증
 * 결과를 덮어써 버린다. 변경된 컬럼만 SET하도록 강제해 이 경합을 없앤다.
 */
@Getter
@Entity
@DynamicUpdate
@Table(name = "projects")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // 홈피드/검색은 native query라 JOIN FETCH를 못 쓴다. User/Category 쪽에 걸어둔
    // 클래스 레벨 @BatchSize(엔티티 자체 배치 로딩)가 이 지연로딩을 묶어준다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_category_id")
    private Category primaryCategory;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String siteUrl;

    private String repositoryUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "pricing_type", nullable = false)
    private PricingType pricing;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags", columnDefinition = "text[]", nullable = false)
    private String[] tags;

    private String thumbnailUrl;

    /** [{"url": "...", "alt": "..."}] 형태의 JSONB 배열 (schema.sql is_screenshot_array 제약) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode screenshots;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "project_status", nullable = false)
    private ProjectStatus status;

    private String rejectionReason;

    private OffsetDateTime publishedAt;

    private OffsetDateTime archivedAt;

    // --- URL 검증 상태: record_project_url_validation() 함수/트리거가 채움 (앱에서 직접 쓰기 금지) ---
    private OffsetDateTime urlCheckedAt;

    @Column(nullable = false)
    private boolean urlIsReachable;

    private Integer urlHttpStatus;

    private String urlFinalUrl;

    private String urlErrorCode;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(64)")
    private String urlValidationHash;

    @Builder
    private Project(User owner, Category primaryCategory, String title, String description,
                     String siteUrl, String repositoryUrl, PricingType pricing, String[] tags,
                     String thumbnailUrl, JsonNode screenshots, ProjectStatus status) {
        this.owner = owner;
        this.primaryCategory = primaryCategory;
        this.title = title;
        this.description = description;
        this.siteUrl = siteUrl;
        this.repositoryUrl = repositoryUrl;
        this.pricing = pricing;
        this.tags = tags;
        this.thumbnailUrl = thumbnailUrl;
        this.screenshots = screenshots;
        this.status = status;
    }

    public void update(Category primaryCategory, String title, String description, String siteUrl,
                       String repositoryUrl, PricingType pricing, String[] tags,
                       String thumbnailUrl, JsonNode screenshots) {
        this.primaryCategory = primaryCategory;
        this.title = title;
        this.description = description;
        this.siteUrl = siteUrl;
        this.repositoryUrl = repositoryUrl;
        this.pricing = pricing;
        this.tags = tags;
        this.thumbnailUrl = thumbnailUrl;
        this.screenshots = screenshots;
    }

    /**
     * 게시 요청 (DRAFT → PENDING_REVIEW).
     * 실제 상태 전이 규칙은 DB 트리거(validate_project_write)가 최종 검증하므로,
     * 여기서는 화면에서 곧바로 안내할 수 있는 선행 조건만 확인한다.
     */
    public void submitForReview() {
        this.status = ProjectStatus.PENDING_REVIEW;
    }

    /** 관리자 승인 (PENDING_REVIEW → PUBLISHED). URL 재검증은 승인 이전에 완료되어 있어야 한다. */
    public void approve() {
        this.status = ProjectStatus.PUBLISHED;
    }

    /** 관리자 거절 (PENDING_REVIEW → REJECTED). schema.sql 제약상 사유가 비어있으면 안 된다. */
    public void reject(String reason) {
        this.status = ProjectStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public boolean isOwnedBy(UUID userId) {
        return this.owner != null && this.owner.getId().equals(userId);
    }
}
