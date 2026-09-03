package com.skala.clickhub.repository;

import com.skala.clickhub.entity.WeeklyInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WeeklyInsightRepository extends JpaRepository<WeeklyInsight, UUID> {

    /** 발행(published_at)된 것 중 가장 최근 주차. 미발행 초안은 노출하지 않는다. */
    Optional<WeeklyInsight> findFirstByPublishedAtIsNotNullOrderByWeekStartDesc();
}
