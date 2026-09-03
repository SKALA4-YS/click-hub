package com.skala.clickhub.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** schema.sql: search_requests — 5장 검색 로그(키워드 추출 결과·fallback 여부 기록). */
@Getter
@Entity
@Table(name = "search_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActorKind actorKind;

    @Column(nullable = false)
    private UUID actorKey;

    @Column(nullable = false)
    private String rawQuery;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode parsedFilters;

    @Column(nullable = false)
    private boolean usedFallback;

    @Column(nullable = false)
    private int resultCount;

    @Column(nullable = false)
    private OffsetDateTime searchedAt;
}
