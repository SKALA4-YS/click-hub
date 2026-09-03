package com.skala.clickhub.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** schema.sql: weekly_insights (created_at/updated_at 컬럼 없음 — generated_at/published_at으로 대체) */
@Getter
@Entity
@Table(name = "weekly_insights")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private LocalDate weekStart;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode rawMetrics;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode aiSummary;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private OffsetDateTime generatedAt;

    private OffsetDateTime publishedAt;
}
