package com.skala.clickhub.dto.project;

import java.util.List;
import java.util.UUID;

/** schema.sql: projects / project_technologies 기준으로 동기화. */
public final class ProjectDtos {

    private ProjectDtos() {}

    public record ScreenshotItem(
            String url,
            String alt
    ) {}

    /** 등록 시 카탈로그(technologies)에서 슬러그로 선택 — 자유 텍스트 기술명 아님. */
    public record TechStackSelection(
            String technologySlug,
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
            String title,
            String description,
            String siteUrl,
            String repositoryUrl,
            String pricing,
            List<String> tags,
            String thumbnailUrl,
            List<ScreenshotItem> screenshots,
            List<TechStackSelection> techStacks,
            UUID categoryId
    ) {}

    public record CreateResponse(
            UUID id
    ) {}

    public record DetailResponse(
            UUID id,
            String title,
            String description,
            String siteUrl,
            String repositoryUrl,
            String pricing,
            String status,
            String categoryName,
            List<String> tags,
            String thumbnailUrl,
            List<ScreenshotItem> screenshots,
            List<TechStackItem> techStacks,
            long likeCount,
            long favoriteCount,
            boolean likedByMe,
            boolean favoritedByMe
    ) {}

    public record OutboundClickResponse(
            boolean recorded
    ) {}
}
