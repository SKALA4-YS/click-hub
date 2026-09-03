package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.feed.FeedDtos.FeedItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {

    /**
     * 기획서 4장 — 홈은 인기·최신·개인화를 하나의 종합 피드로 제공.
     * 인증: 선택 (로그인 시 개인화 반영, 비로그인 시 인기·최신·다양성 혼합)
     */
    @GetMapping("/v1/feed")
    public ApiResponse<CursorPageResponse<FeedItem>> getHomeFeed(@RequestParam(required = false) String cursor) {
        throw new UnsupportedOperationException("not implemented");
    }
}
