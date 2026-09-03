package com.skala.clickhub.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** schema.sql: creator_daily_metrics PK (creator_id, metric_date) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreatorDailyMetricId implements Serializable {

    @Column(name = "creator_id")
    private UUID creatorId;

    @Column(name = "metric_date")
    private LocalDate metricDate;
}
