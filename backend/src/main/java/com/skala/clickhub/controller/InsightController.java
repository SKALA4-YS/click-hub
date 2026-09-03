package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.insight.InsightDtos.WeeklyInsightResponse;
import com.skala.clickhub.service.InsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    // 기획서 9.1 — 주간 트렌드. 인증: 선택
    @GetMapping("/v1/insights/weekly")
    public ApiResponse<WeeklyInsightResponse> getWeeklyInsight() {
        return ApiResponse.success(insightService.getLatestWeekly());
    }
}
