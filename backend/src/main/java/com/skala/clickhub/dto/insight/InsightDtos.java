package com.skala.clickhub.dto.insight;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** schema.sql: weekly_insights (week_start date, model_name 컬럼 반영). */
public final class InsightDtos {

    private InsightDtos() {}

    public record TrendItem(
            String topic,
            String direction,
            double changeRate
    ) {}

    public record WeeklyInsightResponse(
            LocalDate weekStart,
            String headline,
            List<TrendItem> trends,
            List<String> watchlist,
            String modelName,
            OffsetDateTime generatedAt
    ) {}
}
