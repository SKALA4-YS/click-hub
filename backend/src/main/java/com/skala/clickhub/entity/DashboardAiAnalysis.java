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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: dashboard_ai_analyses — 8장 "원본 데이터와 AI 분석 결과를 함께 제공" 원칙의 근거 저장소.
 * source_period(daterange)는 Hibernate 기본 매핑이 없는 PostgreSQL 전용 range 타입이라
 * 문자열로 임시 매핑했다 — 실제 조회/저장에는 커스텀 UserType이 필요하다 (스켈레톤 단계 한계로 명시).
 */
@Getter
@Entity
@Table(name = "dashboard_ai_analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardAiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "analysis_type", nullable = false)
    private AnalysisType analysisType;

    /** TODO: daterange 커스텀 UserType 필요 — 현재는 원문 텍스트로만 다룸 */
    @Column(columnDefinition = "daterange", nullable = false)
    private String sourcePeriod;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode sourceMetricSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode evidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode result;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private OffsetDateTime generatedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
