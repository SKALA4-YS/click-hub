package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.dashboard.DashboardDtos.DashboardResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class DashboardController {

    /**
     * 기획서 8장 — 원본 데이터와 AI 분석 결과를 같은 화면에서 구분해 제공.
     * 인증: 소유자 - 컨트롤러/서비스 레이어에서 NOT_PROJECT_OWNER 검증 필요
     */
    @GetMapping("/v1/dashboard/projects/{id}")
    public ApiResponse<DashboardResponse> getProjectDashboard(
            @PathVariable UUID id,
            @RequestParam(required = false) String period
    ) {
        throw new UnsupportedOperationException("not implemented");
    }
}
