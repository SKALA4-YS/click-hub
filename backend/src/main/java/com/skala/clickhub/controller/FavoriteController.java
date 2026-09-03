package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.favorite.FavoriteDtos.FavoriteResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class FavoriteController {

    // 인증: 로그인 - 즐겨찾기는 개인 저장/선호 신호로 비공개
    @PutMapping("/v1/projects/{id}/favorite")
    public ApiResponse<FavoriteResponse> toggleFavorite(@PathVariable UUID id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
