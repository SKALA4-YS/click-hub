package com.skala.clickhub.dto.ranking;

import java.util.UUID;

/** schema.sql: project_top100_7d / developer_top100_7d 뷰 기준. */
public final class RankingDtos {

    private RankingDtos() {}

    public record ProjectRankingItem(
            int rank,
            UUID projectId,
            String title,
            double score
    ) {}

    public record DeveloperRankingItem(
            int rank,
            UUID creatorId,
            String displayName,
            double score
    ) {}
}
