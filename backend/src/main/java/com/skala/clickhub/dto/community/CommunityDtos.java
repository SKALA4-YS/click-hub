package com.skala.clickhub.dto.community;

import java.time.OffsetDateTime;
import java.util.UUID;

/** V1__initial_schema.sql: community_boards / community_posts / community_post_comments */
public final class CommunityDtos {

    private CommunityDtos() {}

    public record BoardResponse(
            UUID id,
            String slug,
            String name,
            String description,
            int displayOrder
    ) {}

    public record PostCreateRequest(
            String title,
            String body
    ) {}

    public record PostCreateResponse(
            UUID id
    ) {}

    /** author가 null이거나 탈퇴 사용자면 "알 수 없는 사용자"로 표시 (V1 스키마 주석 기준) */
    public record PostSummaryResponse(
            UUID id,
            String title,
            String authorName,
            int viewCount,
            OffsetDateTime createdAt
    ) {}

    public record PostDetailResponse(
            UUID id,
            String boardSlug,
            String title,
            String body,
            String authorName,
            int viewCount,
            OffsetDateTime createdAt
    ) {}

    public record CommentCreateRequest(
            String body,
            UUID parentId
    ) {}

    public record CommentResponse(
            UUID id,
            UUID parentId,
            String authorName,
            String body,
            OffsetDateTime createdAt
    ) {}
}
