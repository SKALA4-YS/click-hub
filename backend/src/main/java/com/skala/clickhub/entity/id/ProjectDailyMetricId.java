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

/** schema.sql: project_daily_metrics PK (project_id, metric_date) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectDailyMetricId implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "metric_date")
    private LocalDate metricDate;
}
