package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import com.skala.clickhub.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects/{id}")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    // 인증: 로그인
    @PutMapping("/like")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(reactionService.toggleLike(id, userId));
    }

    // 인증: 선택
    @GetMapping("/comments")
    public ApiResponse<CursorPageResponse<CommentResponse>> getComments(
            @PathVariable UUID id, @RequestParam(required = false) String cursor) {
        return ApiResponse.success(reactionService.listComments(id, cursor));
    }

    // 인증: 로그인
    @PostMapping("/comments")
    public ApiResponse<CommentResponse> createComment(@PathVariable UUID id, @AuthenticationPrincipal UUID userId,
                                                        @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(reactionService.createComment(id, userId, request));
    }

    // 인증: 소유자(댓글 작성자)
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable UUID id, @PathVariable UUID commentId,
                                            @AuthenticationPrincipal UUID userId) {
        reactionService.deleteComment(id, commentId, userId);
        return ApiResponse.success(HttpStatus.OK, "삭제되었습니다.", null);
    }
}
