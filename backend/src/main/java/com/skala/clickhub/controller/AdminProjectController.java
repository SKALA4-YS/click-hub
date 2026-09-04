package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.project.ProjectDtos.AdminPendingItem;
import com.skala.clickhub.dto.project.ProjectDtos.AdminRejectRequest;
import com.skala.clickhub.dto.project.ProjectDtos.DetailResponse;
import com.skala.clickhub.dto.project.ProjectDtos.StatusResponse;
import com.skala.clickhub.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 인증: ADMIN 전용 (SecurityConfig의 "/v1/admin/**" → hasRole("ADMIN") 규칙으로 강제).
 * 게시물 승인 심사 — 승인 대기(PENDING_REVIEW) 목록 조회, 등록 폼과 동일한 상세 열람, 승인/거절.
 */
@RestController
@RequestMapping("/v1/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ApiResponse<List<AdminPendingItem>> listPending() {
        return ApiResponse.success(projectService.listPendingReview());
    }

    @GetMapping("/{id}")
    public ApiResponse<DetailResponse> getDetail(@PathVariable UUID id) {
        return ApiResponse.success(projectService.getDetailForAdmin(id));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<StatusResponse> approve(@PathVariable UUID id) {
        return ApiResponse.success(projectService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<StatusResponse> reject(@PathVariable UUID id,
                                               @Valid @RequestBody AdminRejectRequest request) {
        return ApiResponse.success(projectService.reject(id, request.reason()));
    }
}
