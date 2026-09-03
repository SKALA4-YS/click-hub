package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest;
import com.skala.clickhub.dto.reaction.ReactionDtos.CommentResponse;
import com.skala.clickhub.dto.reaction.ReactionDtos.LikeResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects/{id}")
public class ReactionController {

    // 인증: 로그인 - "사용자당 프로젝트 1회" 정책은 서비스 레이어에서 검증
    @PutMapping("/like")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable UUID id) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 로그인
    @PostMapping("/comments")
    public ApiResponse<CommentResponse> createComment(@PathVariable UUID id, @RequestBody CommentCreateRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }
}
