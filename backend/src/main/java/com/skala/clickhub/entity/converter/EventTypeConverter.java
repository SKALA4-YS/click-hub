package com.skala.clickhub.entity.converter;

import com.skala.clickhub.entity.EventType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * interaction_event_type ↔ EventType 변환기.
 *
 * 스키마의 다른 enum 타입은 전부 대문자 라벨인데(user_role, project_status, reaction_type 등),
 * interaction_event_type만 소문자 라벨이다:
 *   CREATE TYPE interaction_event_type AS ENUM ('project_impression', 'outbound_click', ...)
 *
 * 그래서 @Enumerated(EnumType.STRING)을 그대로 쓰면 Java 상수명("PROJECT_REGISTERED")이 그대로
 * 나가면서 'invalid input value for enum interaction_event_type' 로 INSERT가 전부 실패한다
 * (실제 이벤트를 저장해 보고서야 드러난 문제 — 스키마 검증만으로는 잡히지 않았다).
 *
 * Java 쪽은 자바 관례대로 대문자 상수를 유지하고, DB 경계에서만 소문자로 바꾼다.
 */
@Converter(autoApply = false)
public class EventTypeConverter implements AttributeConverter<EventType, String> {

    @Override
    public String convertToDatabaseColumn(EventType attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public EventType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EventType.valueOf(dbData.toUpperCase());
    }
}
