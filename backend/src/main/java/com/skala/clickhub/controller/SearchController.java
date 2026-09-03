package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.common.response.CursorPageResponse;
import com.skala.clickhub.dto.search.SearchDtos.SearchResultItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchController {

    /**
     * 기획서 5장 — 키워드 추출 → 메타데이터 필터링 → 벡터 유사도 검색 순서.
     * 인증: 선택 (로그인 시 개인화 랭킹 반영)
     */
    @GetMapping("/v1/search")
    public ApiResponse<CursorPageResponse<SearchResultItem>> search(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) List<String> tech,
            @RequestParam(required = false) String cursor
    ) {
        throw new UnsupportedOperationException("not implemented");
    }
}
