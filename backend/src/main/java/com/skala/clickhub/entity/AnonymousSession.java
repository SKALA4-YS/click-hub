package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: anonymous_sessions — 비로그인 사용자의 삭제 후에도 유지되는 비식별 액터 키.
 * interaction_events/search_requests의 actor_key(actor_kind=ANONYMOUS)가 이 id를 가리킨다
 * (SQL에는 명시적 FK 제약이 없는 논리적 참조).
 */
@Getter
@Entity
@Table(name = "anonymous_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnonymousSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime lastSeenAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;
}
