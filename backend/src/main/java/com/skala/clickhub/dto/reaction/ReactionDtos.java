package com.skala.clickhub.dto.reaction;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ReactionDtos {

    private ReactionDtos() {}

    public record LikeResponse(
            boolean liked,
            long likeCount
    ) {}

    public record CommentCreateRequest(
            String body
    ) {}

    public record CommentResponse(
            UUID id,
            String authorName,
            String body,
            OffsetDateTime createdAt
    ) {}
}
