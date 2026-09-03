package com.skala.clickhub.dto.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: notifications.id는 bigint(Long) — projects/users의 uuid와 다르다.
 * projectId만 화면 상세 이동을 위해 UUID로 함께 내려준다.
 */
public final class NotificationDtos {

    private NotificationDtos() {}

    public record NotificationResponse(
            Long id,
            String creatorName,
            String projectTitle,
            String thumbnailUrl,
            UUID projectId,
            OffsetDateTime publishedAt,
            OffsetDateTime readAt
    ) {}

    public record ReadResponse(
            Long id,
            OffsetDateTime readAt
    ) {}
}
