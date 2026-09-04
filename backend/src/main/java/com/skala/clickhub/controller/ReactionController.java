package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import com.skala.clickhub.service.EngagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects/{id}")
@RequiredArgsConstructor
public class ReactionController {

    private final EngagementService engagementService;

    // 인증: 로그인 - "사용자당 프로젝트 1회" 정책은 서비스 레이어에서 검증
    @PutMapping("/like")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable UUID id,
                                                 @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(engagementService.toggleLike(userId, id));
    }

    // 인증: 선택
    @GetMapping("/comments")
    public ApiResponse<java.util.List<CommentResponse>> getComments(@PathVariable UUID id) {
        return ApiResponse.success(engagementService.getComments(id));
    }

    // 인증: 로그인
    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@PathVariable UUID id,
                                                       @AuthenticationPrincipal UUID userId,
                                                       @Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED, "등록되었습니다.",
                engagementService.createComment(userId, id, request)));
    }
}
