package com.skala.clickhub.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.skala.clickhub.entity.converter.EventTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: interaction_events — actor_kind/actor_key로 로그인 사용자(User.id)와
 * 익명 세션(AnonymousSession.id)을 함께 표현하는 폴리모픽 액터 모델이라 FK 매핑을 두지 않는다.
 * event_date는 DB GENERATED ALWAYS 컬럼이므로 읽기 전용.
 */
@Getter
@Entity
@Table(name = "interaction_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InteractionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // interaction_event_type만 스키마에서 소문자 라벨이라 전용 컨버터를 쓴다(EventTypeConverter 주석 참고).
    @Convert(converter = EventTypeConverter.class)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActorKind actorKind;

    @Column(nullable = false)
    private UUID actorKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private OffsetDateTime occurredAt;

    @Column(insertable = false, updatable = false)
    private LocalDate eventDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode context;

    @Builder
    private InteractionEvent(EventType eventType, ActorKind actorKind, UUID actorKey,
                             Project project, OffsetDateTime occurredAt, JsonNode context) {
        this.eventType = eventType;
        this.actorKind = actorKind;
        this.actorKey = actorKey;
        this.project = project;
        this.occurredAt = occurredAt == null ? OffsetDateTime.now() : occurredAt;
        this.context = context;
    }
}
