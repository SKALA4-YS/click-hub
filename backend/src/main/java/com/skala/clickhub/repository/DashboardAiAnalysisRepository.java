package com.skala.clickhub.repository;

import com.skala.clickhub.entity.DashboardAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DashboardAiAnalysisRepository extends JpaRepository<DashboardAiAnalysis, UUID> {

    Optional<DashboardAiAnalysis> findFirstByProjectIdOrderByGeneratedAtDesc(UUID projectId);
}
