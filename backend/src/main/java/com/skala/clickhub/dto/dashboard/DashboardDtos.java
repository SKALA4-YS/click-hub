package com.skala.clickhub.dto.dashboard;

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

    public record AiSummary(
            String changeSummary,
            String generatedAt
    ) {}

    public record DashboardResponse(
            String period,
            RawMetrics rawMetrics,
            AiSummary aiSummary
    ) {}
}
