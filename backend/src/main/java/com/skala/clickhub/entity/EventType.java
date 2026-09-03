package com.skala.clickhub.entity;

/** schema.sql: interaction_event_type (기획서 15장 표준 이벤트 목록과 다름 — SQL 기준으로 맞춤) */
public enum EventType {
    PROJECT_IMPRESSION,
    PROJECT_CARD_CLICK,
    PROJECT_DETAIL_VIEW,
    OUTBOUND_CLICK,
    LIKE_SET,
    FAVORITE_SET,
    COMMENT_CREATED,
    CREATOR_SUBSCRIBED,
    SEARCH_RESULT_CLICKED,
    PROJECT_REGISTERED,
    PROJECT_PUBLISHED,
    NOTIFICATION_CLICKED,
    TUTORIAL_CLICKED,
    INSIGHT_VIEWED
}
