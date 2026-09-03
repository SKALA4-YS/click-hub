package com.skala.clickhub.repository;

import com.skala.clickhub.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 알림 목록 — 화면(기획서 10.1)에 개발자명/프로젝트명/썸네일이 함께 필요해서
     * actor/project를 함께 가져온다(N+1 방지).
     */
    @Query("""
            SELECT n FROM Notification n
            LEFT JOIN FETCH n.actor
            LEFT JOIN FETCH n.project
            WHERE n.recipient.id = :recipientId
            ORDER BY n.createdAt DESC
            """)
    List<Notification> findAllForRecipient(@Param("recipientId") UUID recipientId);
}
