package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.community.CommunityDtos.BoardResponse;
import com.skala.clickhub.dto.community.CommunityDtos.CommentCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.CommentResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.PostCreateResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostDetailResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostSummaryResponse;
import com.skala.clickhub.dto.community.CommunityDtos.PostUpdateRequest;
import com.skala.clickhub.service.CommunityService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 커뮤니티 게시판 — 기획서 3장 "포함" 항목.
 *
 * 게시판은 목록/상세/댓글 조회까지 전부 로그인 사용자 전용이다(2026-09-03 확정).
 * 게시판 식별자는 UUID가 아니라 slug(notice/free/share/qna)를 쓴다 — 시드 데이터 기준
 * 고정값이라 프론트가 URL에 그대로 쓸 수 있고, 환경마다 달라지는 UUID를 노출하지 않아도 된다.
 */
@RestController
@RequestMapping("/v1/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    // 인증: 로그인
    @GetMapping("/boards")
    public ApiResponse<List<BoardResponse>> getBoards() {
        return ApiResponse.success(communityService.getBoards());
    }

    // 인증: 로그인
    @GetMapping("/boards/{boardSlug}/posts")
    public ApiResponse<CursorPageResponse<PostSummaryResponse>> getPosts(
            @PathVariable String boardSlug,
            @RequestParam(required = false) String cursor
    ) {
        return ApiResponse.success(communityService.getPosts(boardSlug, cursor));
    }

    // 인증: 로그인
    @PostMapping("/boards/{boardSlug}/posts")
    public ApiResponse<PostCreateResponse> createPost(
            @PathVariable String boardSlug,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        return ApiResponse.success(HttpStatus.CREATED, "등록되었습니다.",
                communityService.createPost(boardSlug, userId, request));
    }

    // 인증: 로그인
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable UUID postId,
                                                          @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(communityService.getPostDetail(postId, userId));
    }

    // 인증: 작성자
    @PatchMapping("/posts/{postId}")
    public ApiResponse<Void> updatePost(@PathVariable UUID postId,
                                        @AuthenticationPrincipal UUID userId,
                                        @Valid @RequestBody PostUpdateRequest request) {
        communityService.updatePost(postId, userId, request);
        return ApiResponse.success(HttpStatus.OK, "수정되었습니다.", null);
    }

    // 인증: 작성자 — 소프트 삭제(status=DELETED)
    @DeleteMapping("/posts/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable UUID postId,
                                        @AuthenticationPrincipal UUID userId) {
        communityService.deletePost(postId, userId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }

    // 인증: 로그인
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        return ApiResponse.success(communityService.getComments(postId));
    }

    // 인증: 로그인 (1단계 대댓글까지 허용)
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable UUID postId,
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return ApiResponse.success(HttpStatus.CREATED, "등록되었습니다.",
                communityService.createComment(postId, userId, request));
    }
}
