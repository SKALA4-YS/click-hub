package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.search.SearchDtos.SearchResultItem;
import com.skala.clickhub.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 통합 검색 — 키워드 + 메타데이터 필터 기반. AI/LLM은 사용하지 않는다(2026-09-03 확정).
     * 인증: 선택
     */
    @GetMapping("/v1/search")
    public ApiResponse<CursorPageResponse<SearchResultItem>> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> tech,
            @RequestParam(required = false) String cursor
    ) {
        return ApiResponse.success(searchService.search(q, category, tags, tech, cursor));
    }
}
