package com.skala.clickhub.dto.feed;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class FeedDtos {

    private FeedDtos() {}

    public record FeedItem(
            UUID id,
            String title,
            String description,
            String thumbnailUrl,
            String categorySlug,
            String categoryName,
            List<String> tags,
            String ownerName,
            OffsetDateTime publishedAt,
            long likeCount
    ) {}
}
