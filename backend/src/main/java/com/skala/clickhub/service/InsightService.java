package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.skala.clickhub.dto.insight.InsightDtos.TrendItem;
import com.skala.clickhub.dto.insight.InsightDtos.WeeklyInsightResponse;
import com.skala.clickhub.entity.WeeklyInsight;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.WeeklyInsightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 주간 AI 트렌드 (기획서 9.1).
 *
 * 인사이트 본문은 AI 파이프라인이 ai_summary(JSONB)에 적재한다 — 이 서비스는 그 결과를
 * 화면 계약(DTO)으로 옮기기만 하고 직접 생성하지 않는다.
 * AI 결과 스키마는 기획서 14.3 기준: { headline, trends[{topic, direction, change_rate}], watchlist[] }
 */
@Service
@RequiredArgsConstructor
public class InsightService {

    private final WeeklyInsightRepository weeklyInsightRepository;

    @Transactional(readOnly = true)
    public WeeklyInsightResponse getLatestWeekly() {
        WeeklyInsight insight = weeklyInsightRepository.findFirstByPublishedAtIsNotNullOrderByWeekStartDesc()
                .orElseThrow(() -> new BusinessException(ErrorCode.INSIGHT_NOT_FOUND));

        JsonNode summary = insight.getAiSummary();

        return new WeeklyInsightResponse(
                insight.getWeekStart(),
                summary != null && summary.hasNonNull("headline") ? summary.get("headline").asText() : null,
                readTrends(summary),
                readWatchlist(summary),
                insight.getModelName(),
                insight.getGeneratedAt()
        );
    }

    private List<TrendItem> readTrends(JsonNode summary) {
        List<TrendItem> trends = new ArrayList<>();
        if (summary == null || !summary.path("trends").isArray()) {
            return trends;
        }
        summary.get("trends").forEach(node -> trends.add(new TrendItem(
                node.path("topic").asText(null),
                node.path("direction").asText(null),
                node.path("change_rate").asDouble(0)
        )));
        return trends;
    }

    private List<String> readWatchlist(JsonNode summary) {
        List<String> watchlist = new ArrayList<>();
        if (summary == null || !summary.path("watchlist").isArray()) {
            return watchlist;
        }
        summary.get("watchlist").forEach(node -> watchlist.add(node.asText()));
        return watchlist;
    }
}
