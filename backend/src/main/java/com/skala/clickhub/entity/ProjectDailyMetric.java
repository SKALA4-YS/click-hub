package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.ProjectDailyMetricId;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * schema.sql: project_daily_metrics — refresh_project_daily_metrics()가 채우는 집계 테이블.
 * 애플리케이션은 이 테이블에 직접 insert하지 않고 조회 전용으로 사용하는 것을 전제로 한다.
 */
@Getter
@Entity
@Table(name = "project_daily_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectDailyMetric {

    @EmbeddedId
    private ProjectDailyMetricId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false)
    private int uniqueVisitors;

    @Column(nullable = false)
    private int impressions;

    @Column(nullable = false)
    private int detailViews;

    @Column(nullable = false)
    private int validOutboundClicks;

    @Column(nullable = false)
    private int uniqueLikes;

    @Column(nullable = false)
    private int uniqueCommenters;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal abuseFactor;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
