package com.skala.clickhub.entity;

/**
 * community_posts.status.
 * V1은 varchar+CHECK(PUBLISHED/HIDDEN/DELETED)였지만, V3__align_onboarding_and_community.sql이
 * 컬럼을 네이티브 enum(community_post_status: PUBLISHED/DELETED)으로 변경하며 HIDDEN을
 * DELETED로 백필했다. 그래서 이 enum도 두 값만 남긴다.
 */
public enum CommunityPostStatus {
    PUBLISHED,
    DELETED
}
