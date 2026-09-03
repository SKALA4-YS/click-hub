package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.skala.clickhub.dto.dashboard.DashboardDtos.AiSummary;
import com.skala.clickhub.dto.dashboard.DashboardDtos.DashboardResponse;
import com.skala.clickhub.dto.dashboard.DashboardDtos.RawMetrics;
import com.skala.clickhub.entity.DashboardAiAnalysis;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.DashboardAiAnalysisRepository;
import com.skala.clickhub.repository.ProjectDailyMetricRepository;
import com.skala.clickhub.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * 제작자 대시보드 (기획서 8장).
 *
 * 표시 원칙: AI가 해석한 결과와 AI가 관여하지 않은 원본 데이터를 같은 응답에서 분리해 내려준다.
 * 원본 지표는 project_daily_metrics 집계에서만 나오고, AI 요약은 dashboard_ai_analyses에
 * 이미 적재된 것을 그대로 읽는다 — 이 서비스가 AI를 호출하지는 않는다.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DEFAULT_PERIOD_DAYS = 7;

    private final ProjectRepository projectRepository;
    private final ProjectDailyMetricRepository projectDailyMetricRepository;
    private final DashboardAiAnalysisRepository dashboardAiAnalysisRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getProjectDashboard(UUID projectId, UUID requesterId, String period) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.isOwnedBy(requesterId)) {
            throw new BusinessException(ErrorCode.NOT_PROJECT_OWNER);
        }

        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(parsePeriodDays(period) - 1L);

        var sum = projectDailyMetricRepository.sumForPeriod(projectId, from, to);

        long detailViews = sum.getDetailViews();
        long outboundClicks = sum.getValidOutboundClicks();
        // 상세 조회 → 외부 클릭 전환율. 북극성 지표(유효 외부 클릭)의 분모가 0이면 0으로 둔다.
        double ctr = detailViews == 0 ? 0d : (double) outboundClicks / detailViews;

        RawMetrics rawMetrics = new RawMetrics(
                sum.getUniqueVisitors(),
                sum.getImpressions(),
                detailViews,
                outboundClicks,
                sum.getUniqueLikes(),
                sum.getUniqueCommenters(),
                ctr
        );

        return new DashboardResponse(from, to, rawMetrics, findAiSummary(projectId));
    }

    /** "7d" / "30d" / "7" 형태를 허용한다. 값이 없거나 해석 불가면 기본 7일. */
    private int parsePeriodDays(String period) {
        if (period == null || period.isBlank()) {
            return DEFAULT_PERIOD_DAYS;
        }
        String digits = period.trim().toLowerCase().replace("d", "");
        try {
            int days = Integer.parseInt(digits);
            if (days < 1 || days > 365) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return days;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private AiSummary findAiSummary(UUID projectId) {
        Optional<DashboardAiAnalysis> latest =
                dashboardAiAnalysisRepository.findFirstByProjectIdOrderByGeneratedAtDesc(projectId);

        return latest.map(analysis -> {
            JsonNode result = analysis.getResult();
            String summaryText = result != null && result.hasNonNull("summary")
                    ? result.get("summary").asText()
                    : null;
            return new AiSummary(
                    summaryText,
                    analysis.getSourcePeriod(),
                    analysis.getModelName(),
                    analysis.getGeneratedAt());
        }).orElse(null);
    }
}
