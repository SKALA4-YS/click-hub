package com.skala.clickhub.dto.search;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SearchDtos {

    private SearchDtos() {}

    /** 프로젝트 카드 렌더링에 필요한 최소 필드 (홈/검색/랭킹 카드 공통 모양). */
    public record SearchResultItem(
            UUID id,
            String title,
            String description,
            String thumbnailUrl,
            String categorySlug,
            String categoryName,
            List<String> tags,
            String ownerName,
            OffsetDateTime publishedAt
    ) {}
}
