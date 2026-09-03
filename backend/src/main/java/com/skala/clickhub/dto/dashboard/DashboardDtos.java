package com.skala.clickhub.dto.dashboard;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** schema.sql: project_daily_metrics 컬럼명에 맞춰 동기화. */
public final class DashboardDtos {

    private DashboardDtos() {}

    public record RawMetrics(
            long uniqueVisitors,
            long impressions,
            long detailViews,
            long validOutboundClicks,
            long uniqueLikes,
            long uniqueCommenters,
            double ctr
    ) {}

    /**
     * 기획서 8장 "표시 원칙" — AI 해석 결과는 원본 지표와 구분해서 내려주고,
     * 분석 기간·생성 시각을 항상 함께 표시한다. 분석 결과가 아직 없으면 이 필드는 null이다.
     */
    public record AiSummary(
            String changeSummary,
            String sourcePeriod,
            String modelName,
            OffsetDateTime generatedAt
    ) {}

    public record DashboardResponse(
            LocalDate from,
            LocalDate to,
            RawMetrics rawMetrics,
            AiSummary aiSummary
    ) {}
}
