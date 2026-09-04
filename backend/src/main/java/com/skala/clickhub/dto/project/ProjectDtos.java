package com.skala.clickhub.dto.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** schema.sql: projects / project_technologies 기준으로 동기화. */
public final class ProjectDtos {

    private ProjectDtos() {}

    public record ScreenshotItem(
            @NotBlank String url,
            String alt
    ) {}

    /**
     * 등록 시 카탈로그(technologies)에서 슬러그로 선택 — 자유 텍스트 기술명 아님.
     * technologies.id는 gen_random_uuid()라 환경마다 값이 달라 프론트가 미리 알 수 없다.
     * 반면 slug는 시드 데이터 기준 고정값이라 프론트가 상수로 들고 있을 수 있다.
     */
    public record TechStackSelection(
            @NotBlank String technologySlug,
            String group,
            String version
    ) {}

    public record TechStackItem(
            String technologyName,
            String technologySlug,
            String group,
            String version
    ) {}

    public record CreateRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank String description,
            @NotBlank String siteUrl,
            String repositoryUrl,
            String pricing,
            List<String> tags,
            String thumbnailUrl,
            @Valid List<ScreenshotItem> screenshots,
            @Valid List<TechStackSelection> techStacks,
            /** categories.slug — id(UUID)가 아니라 슬러그로 받는다(위 TechStackSelection과 동일한 이유). */
            String categorySlug
    ) {}

    public record UpdateRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank String description,
            @NotBlank String siteUrl,
            String repositoryUrl,
            String pricing,
            List<String> tags,
            String thumbnailUrl,
            @Valid List<ScreenshotItem> screenshots,
            @Valid List<TechStackSelection> techStacks,
            String categorySlug
    ) {}

    public record CreateResponse(
            UUID id,
            String status
    ) {}

    public record SummaryResponse(
            UUID id,
            String title,
            String description,
            String thumbnailUrl,
            String categorySlug,
            String categoryName,
            String pricing,
            List<String> tags,
            String ownerName,
            OffsetDateTime publishedAt
    ) {}

    public record DetailResponse(
            UUID id,
            String title,
            String description,
            String siteUrl,
            String repositoryUrl,
            String pricing,
            String status,
            String categorySlug,
            String categoryName,
            List<String> tags,
            String thumbnailUrl,
            List<ScreenshotItem> screenshots,
            List<TechStackItem> techStacks,
            String ownerName,
            UUID ownerId,
            OffsetDateTime publishedAt,
            long likeCount,
            long favoriteCount,
            boolean likedByMe,
            boolean favoritedByMe
    ) {}

    public record OutboundClickResponse(
            boolean recorded
    ) {}

    public record StatusResponse(
            UUID id,
            String status
    ) {}

    /** 관리자 승인 대기 목록 — 이름/설명/URL/현재 상태만 보여주면 되는 요약. */
    public record AdminPendingItem(
            UUID id,
            String title,
            String description,
            String siteUrl,
            String status,
            String ownerName,
            OffsetDateTime createdAt
    ) {}

    public record AdminRejectRequest(
            @NotBlank String reason
    ) {}
}
