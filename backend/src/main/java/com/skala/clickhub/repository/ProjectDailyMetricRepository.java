package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectDailyMetric;
import com.skala.clickhub.entity.id.ProjectDailyMetricId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface ProjectDailyMetricRepository extends JpaRepository<ProjectDailyMetric, ProjectDailyMetricId> {

    /**
     * 대시보드 원본 지표 합계 (기획서 8장 "AI가 관여하지 않은 원본 데이터").
     * 집계 행이 하나도 없어도 coalesce로 0이 내려간다.
     * 컬럼 별칭을 큰따옴표로 감싼 이유: Postgres는 따옴표 없는 식별자를 소문자로 접어버려서
     * 인터페이스 프로젝션의 camelCase 게터와 이름이 어긋난다.
     */
    @Query(value = """
            SELECT coalesce(sum(m.unique_visitors), 0) AS "uniqueVisitors",
                   coalesce(sum(m.impressions), 0) AS "impressions",
                   coalesce(sum(m.detail_views), 0) AS "detailViews",
                   coalesce(sum(m.valid_outbound_clicks), 0) AS "validOutboundClicks",
                   coalesce(sum(m.unique_likes), 0) AS "uniqueLikes",
                   coalesce(sum(m.unique_commenters), 0) AS "uniqueCommenters"
            FROM project_daily_metrics m
            WHERE m.project_id = :projectId
              AND m.metric_date BETWEEN :from AND :to
            """, nativeQuery = true)
    MetricSum sumForPeriod(@Param("projectId") UUID projectId,
                           @Param("from") LocalDate from,
                           @Param("to") LocalDate to);

    /** 네이티브 쿼리 결과 매핑용 프로젝션. */
    interface MetricSum {
        long getUniqueVisitors();

        long getImpressions();

        long getDetailViews();

        long getValidOutboundClicks();

        long getUniqueLikes();

        long getUniqueCommenters();
    }
}
