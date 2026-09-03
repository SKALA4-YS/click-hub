package com.skala.clickhub.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {}

    public record ProfileUpdateRequest(
            @Size(min = 1, max = 100) String displayName,
            String theme,
            Boolean newProjectNotifications
    ) {}

    public record OnboardingUpdateRequest(
            @Size(max = 10) List<@NotBlank @Size(max = 100) String> goals,
            @Size(max = 14) List<@NotBlank String> categorySlugs,
            @Size(max = 15) List<@NotBlank String> technologySlugs
    ) {}

    public record OnboardingResponse(
            List<String> goals,
            List<String> categorySlugs,
            List<String> technologySlugs,
            OffsetDateTime completedAt
    ) {}

    public record ProjectItem(
            UUID id,
            String title,
            String description,
            String thumbnailUrl,
            String categorySlug,
            String categoryName,
            String status,
            List<String> tags,
            String ownerName,
            UUID ownerId,
            OffsetDateTime publishedAt,
            long likeCount,
            long favoriteCount
    ) {}

    public record CreatorSummary(
            UUID id,
            String displayName,
            String avatarUrl,
            long subscriberCount,
            long projectCount
    ) {}

    public record CreatorDetailResponse(
            UUID id,
            String displayName,
            String avatarUrl,
            long subscriberCount,
            boolean subscribedByMe,
            List<ProjectItem> projects
    ) {}
}
