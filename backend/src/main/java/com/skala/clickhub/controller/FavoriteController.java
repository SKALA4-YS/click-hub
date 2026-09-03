package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import com.skala.clickhub.dto.project.ProjectDtos.SummaryResponse;
import com.skala.clickhub.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final ReactionService reactionService;

    // 인증: 로그인 - 즐겨찾기는 개인 저장/선호 신호로 비공개
    @PutMapping("/v1/projects/{id}/favorite")
    public ApiResponse<FavoriteResponse> toggleFavorite(@PathVariable UUID id, @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(reactionService.toggleFavorite(id, userId));
    }

    // 인증: 로그인 - 마이페이지 즐겨찾기 목록
    @GetMapping("/v1/me/favorites")
    public ApiResponse<CursorPageResponse<SummaryResponse>> getFavorites(
            @AuthenticationPrincipal UUID userId, @RequestParam(required = false) String cursor) {
        return ApiResponse.success(reactionService.listFavorites(userId, cursor));
    }
}
