package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.project.ProjectDtos.CreateRequest;
import com.skala.clickhub.dto.project.ProjectDtos.CreateResponse;
import com.skala.clickhub.dto.project.ProjectDtos.DetailResponse;
import com.skala.clickhub.dto.project.ProjectDtos.OutboundClickResponse;
import com.skala.clickhub.dto.project.ProjectDtos.StatusResponse;
import com.skala.clickhub.dto.project.ProjectDtos.UpdateRequest;
import com.skala.clickhub.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 인증: 로그인 — 항상 DRAFT로 생성된다(스키마 트리거 규칙)
    @PostMapping
    public ApiResponse<CreateResponse> createProject(@AuthenticationPrincipal UUID userId,
                                                     @Valid @RequestBody CreateRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "생성되었습니다.", projectService.create(userId, request));
    }

    // 인증: 선택 - 로그인 시 likedByMe/favoritedByMe 개인화 필드 채움
    @GetMapping("/{id}")
    public ApiResponse<DetailResponse> getProjectDetail(@PathVariable UUID id,
                                                        @AuthenticationPrincipal UUID viewerId) {
        return ApiResponse.success(projectService.getDetail(id, viewerId));
    }

    // 인증: 소유자
    @PatchMapping("/{id}")
    public ApiResponse<StatusResponse> updateProject(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UUID userId,
                                                     @Valid @RequestBody UpdateRequest request) {
        return ApiResponse.success(projectService.update(id, userId, request));
    }

    // 인증: 소유자 — 게시 요청 (DRAFT → PENDING_REVIEW)
    @PostMapping("/{id}/submit")
    public ApiResponse<StatusResponse> submitProject(@PathVariable UUID id,
                                                     @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(projectService.submitForReview(id, userId));
    }

    // 인증: 소유자
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProject(@PathVariable UUID id,
                                           @AuthenticationPrincipal UUID userId) {
        projectService.delete(id, userId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }

    // 인증: 선택 — 북극성 지표(유효 외부 클릭) 기록
    @PostMapping("/{id}/outbound-clicks")
    public ApiResponse<OutboundClickResponse> recordOutboundClick(@PathVariable UUID id,
                                                                   @AuthenticationPrincipal UUID viewerId) {
        return ApiResponse.success(projectService.recordOutboundClick(id, viewerId));
    }
}
