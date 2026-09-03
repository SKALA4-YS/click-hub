package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.CreatorDailyMetricId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/** schema.sql: creator_daily_metrics — 개발자 랭킹 집계용, 조회 전용 테이블. */
@Getter
@Entity
@Table(name = "creator_daily_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorDailyMetric {

    @EmbeddedId
    private CreatorDailyMetricId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("creatorId")
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(nullable = false)
    private int subscriberGrowth;

    @Column(nullable = false)
    private int activeProjects;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
