package com.skala.clickhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * db/migration/V1__initial_schema.sql: developer_top100_7d (읽기 전용 뷰).
 * 대리키가 없는 뷰라 creator_id를 그대로 @Id로 쓴다. @Immutable로 Hibernate가
 * 이 엔티티에 대해 UPDATE/dirty-checking을 시도하지 않게 막는다(뷰라 쓰기 자체가 불가능).
 */
@Getter
@Entity
@Immutable
@Table(name = "developer_top100_7d")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeveloperRankingView {

    @Id
    private UUID creatorId;

    private String displayName;

    private double score;
}
