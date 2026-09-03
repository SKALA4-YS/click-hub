package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.feed.FeedDtos.FeedItem;
import com.skala.clickhub.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     * 기획서 4장 — 홈은 인기·최신·개인화를 하나의 종합 피드로 제공.
     * 인증: 선택 (개인화 반영은 행동 데이터 축적 후 확장 예정)
     */
    @GetMapping("/v1/feed")
    public ApiResponse<CursorPageResponse<FeedItem>> getHomeFeed(
            @RequestParam(required = false) String cursor) {
        return ApiResponse.success(feedService.getHomeFeed(cursor));
    }
}
