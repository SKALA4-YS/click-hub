package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectRankingView;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * project_top100_7d 뷰 전용 조회 리포지토리. JpaRepository가 아니라 Repository 마커
 * 인터페이스만 확장해 save()/delete()를 아예 노출하지 않는다 — 뷰는 쓸 수 없으므로.
 */
public interface ProjectRankingRepository extends Repository<ProjectRankingView, UUID> {

    List<ProjectRankingView> findTop100ByOrderByScoreDesc();
}
