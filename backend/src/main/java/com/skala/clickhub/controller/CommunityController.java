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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * V1__initial_schema.sql의 community_boards/community_posts/community_post_comments 반영.
 * §12 API 명세에는 이 도메인이 없어 엔드포인트는 REST 컨벤션에 맞춰 추정했다 — 실제 화면 설계가
 * 나오면 재확인 필요.
 */
@RestController
@RequestMapping("/v1/community")
public class CommunityController {

    // 인증: 없음
    @GetMapping("/boards")
    public ApiResponse<List<BoardResponse>> getBoards() {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 없음
    @GetMapping("/boards/{boardId}/posts")
    public ApiResponse<CursorPageResponse<PostSummaryResponse>> getPosts(
            @PathVariable UUID boardId,
            @RequestParam(required = false) String cursor
    ) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 로그인
    @PostMapping("/boards/{boardId}/posts")
    public ApiResponse<PostCreateResponse> createPost(
            @PathVariable UUID boardId,
            @RequestBody PostCreateRequest request
    ) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 없음
    @GetMapping("/posts/{postId}")
    public ApiResponse<PostDetailResponse> getPostDetail(@PathVariable UUID postId) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 없음
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable UUID postId) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 로그인
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable UUID postId,
            @RequestBody CommentCreateRequest request
    ) {
        throw new UnsupportedOperationException("not implemented");
    }
}
