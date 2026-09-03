package com.skala.clickhub.dto.tutorial;

import java.util.List;
import java.util.UUID;

/** schema.sql: tutorials 컬럼(estimated_minutes, category_slugs, technology_slugs)에 맞춰 동기화. */
public final class TutorialDtos {

    private TutorialDtos() {}

    public record TutorialResponse(
            UUID id,
            String title,
            String description,
            String type,
            String difficulty,
            int estimatedMinutes,
            String sourceUrl,
            List<String> categorySlugs,
            List<String> technologySlugs
    ) {}
}
