package com.skala.clickhub.dto.search;

import java.util.List;
import java.util.UUID;

public final class SearchDtos {

    private SearchDtos() {}

    public record SearchResultItem(
            UUID id,
            String title,
            String thumbnailUrl,
            String category,
            List<String> tags
    ) {}
}
