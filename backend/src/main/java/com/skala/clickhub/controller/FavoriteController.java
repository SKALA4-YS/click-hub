package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import com.skala.clickhub.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final EngagementService engagementService;

    // 인증: 로그인 - 즐겨찾기는 개인 저장/선호 신호로 비공개
    @PutMapping("/v1/projects/{id}/favorite")
    public ApiResponse<FavoriteResponse> toggleFavorite(@PathVariable UUID id,
                                                         @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(engagementService.toggleFavorite(userId, id));
    }
}
