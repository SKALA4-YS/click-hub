package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    /**
     * created_at은 @CreationTimestamp로 두지 않고 생성자에서 직접 채운다.
     * 스키마에 CHECK (last_seen_at >= created_at)가 걸려 있는데, @CreationTimestamp가 만드는 시각과
     * 생성자에서 만든 last_seen_at이 마이크로초 단위로 어긋나면서 실제로 이 제약을 위반했다(실측).
     * 두 값을 같은 인스턴스에서 한 번에 확정하면 순서 문제가 원천적으로 사라진다.
     */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime lastSeenAt;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Builder
    private AnonymousSession(OffsetDateTime expiresAt) {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.lastSeenAt = now;
        this.expiresAt = expiresAt == null ? now.plusDays(30) : expiresAt;
    }
}
