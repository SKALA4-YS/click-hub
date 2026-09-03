package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: project_search_documents — sync_project_search_document() 트리거가 유지하는
 * 비정규화 검색 read model. PK가 projects.id를 그대로 공유한다 (대리키 없음).
 * search_document(tsvector)는 STORED GENERATED 컬럼이라 매핑하지 않는다.
 * embedding(vector(1536))은 pgvector 전용 타입으로, Hibernate에 매핑하려면
 * com.pgvector:pgvector 의존성과 커스텀 UserType이 추가로 필요해 이번 스켈레톤에서는 제외했다.
 */
@Getter
@Entity
@Table(name = "project_search_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectSearchDocument {

    @Id
    private UUID projectId;

    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String siteUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingType pricing;

    private String categorySlug;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] tags;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] technologySlugs;

    private OffsetDateTime publishedAt;

    @Column(nullable = false)
    private float engagementQuality;

    // embedding vector(1536) — 매핑 제외 (위 클래스 주석 참고)

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(64)")
    private String contentHash;

    private String modelName;

    private OffsetDateTime embeddingGeneratedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
