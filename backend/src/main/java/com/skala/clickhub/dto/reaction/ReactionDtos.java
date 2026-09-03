package com.skala.clickhub.dto.reaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ReactionDtos {

    private ReactionDtos() {}

    public record LikeResponse(
            boolean liked,
            long likeCount
    ) {}

    public record CommentCreateRequest(
            @NotBlank @Size(max = 3000) String body
    ) {}

    public record CommentResponse(
            UUID id,
            UUID authorId,
            String authorName,
            String body,
            OffsetDateTime createdAt
    ) {}
}
