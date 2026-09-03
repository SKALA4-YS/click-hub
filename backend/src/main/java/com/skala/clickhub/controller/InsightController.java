package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.insight.InsightDtos.WeeklyInsightResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InsightController {

    // 기획서 9.1 — 주간 트렌드. 인증: 선택
    @GetMapping("/v1/insights/weekly")
    public ApiResponse<WeeklyInsightResponse> getWeeklyInsight() {
        throw new UnsupportedOperationException("not implemented");
    }
}
