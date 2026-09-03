package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.skala.clickhub.dto.notification.NotificationDtos.NotificationResponse;
import com.skala.clickhub.dto.notification.NotificationDtos.ReadResponse;
import com.skala.clickhub.entity.Notification;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 구독 알림 (기획서 10.1).
 *
 * 알림 행은 notification_outbox를 처리하는 process_notification_outbox() 함수가 생성한다
 * (프로젝트가 PUBLISHED로 바뀔 때 트리거가 outbox에 적재). 여기서는 조회/읽음 처리만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID recipientId) {
        return notificationRepository.findAllForRecipient(recipientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReadResponse markAsRead(Long notificationId, UUID recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 남의 알림을 읽음 처리하지 못하게 막는다. 존재 자체를 숨기기 위해 404로 응답한다.
        if (!notification.getRecipient().getId().equals(recipientId)) {
            throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notification.markRead();
        return new ReadResponse(notification.getId(), notification.getReadAt());
    }

    /**
     * payload(JSONB)에는 알림 생성 시점의 값이 스냅샷으로 들어있다.
     * 프로젝트가 나중에 삭제되면 project 연관관계는 NULL이 되지만(ON DELETE SET NULL)
     * payload는 남아 있어 알림 목록이 깨지지 않는다 — 그래서 payload를 우선 사용한다.
     */
    private NotificationResponse toResponse(Notification notification) {
        JsonNode payload = notification.getPayload();

        return new NotificationResponse(
                notification.getId(),
                text(payload, "creator_name", notification.getActor() == null
                        ? null : notification.getActor().getDisplayName()),
                text(payload, "project_title", notification.getProject() == null
                        ? null : notification.getProject().getTitle()),
                text(payload, "thumbnail_url", notification.getProject() == null
                        ? null : notification.getProject().getThumbnailUrl()),
                notification.getProject() == null ? null : notification.getProject().getId(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private String text(JsonNode payload, String field, String fallback) {
        if (payload == null || !payload.hasNonNull(field)) {
            return fallback;
        }
        return payload.get(field).asText();
    }
}
