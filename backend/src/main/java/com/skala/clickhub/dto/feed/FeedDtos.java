package com.skala.clickhub.dto.feed;

import java.util.UUID;

public final class FeedDtos {

    private FeedDtos() {}

    public record FeedItem(
            UUID id,
            String title,
            String thumbnailUrl,
            String category,
            long likeCount
    ) {}
}
