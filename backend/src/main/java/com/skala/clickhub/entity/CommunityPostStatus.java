package com.skala.clickhub.entity;

/** V1__initial_schema.sql: community_posts.status — 네이티브 enum 타입이 아니라 varchar+CHECK 제약이다. */
public enum CommunityPostStatus {
    PUBLISHED,
    HIDDEN,
    DELETED
}
