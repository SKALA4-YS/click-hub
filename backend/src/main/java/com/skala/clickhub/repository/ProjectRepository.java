package com.skala.clickhub.repository;

import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Optional<Project> findByIdAndStatus(UUID id, ProjectStatus status);

    @Query("""
            SELECT project FROM Project project
            JOIN FETCH project.owner
            LEFT JOIN FETCH project.primaryCategory
            WHERE project.owner.id = :ownerId
            ORDER BY project.createdAt DESC
            """)
    List<Project> findAllByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("""
            SELECT project FROM Project project
            JOIN FETCH project.owner
            LEFT JOIN FETCH project.primaryCategory
            WHERE project.owner.id = :ownerId AND project.status = :status
            ORDER BY project.publishedAt DESC, project.id ASC
            """)
    List<Project> findAllByOwnerIdAndStatus(@Param("ownerId") UUID ownerId,
                                             @Param("status") ProjectStatus status);

    /**
     * 통합 검색 — 기획서 5장 기준으로 "AI 없이도 항상 동작"하는 키워드 + 메타데이터 필터.
     *
     * 벡터 유사도(pgvector) 결합은 project_search_documents.embedding을 채우는 임베딩 파이프라인이
     * 아직 없어서 여기서는 제외했다. 임베딩이 생성되기 시작하면 이 쿼리에 유사도 항을 더하면 된다.
     *
     * 배열 파라미터(tags/tech)는 JDBC 바인딩이 까다로워 CSV 문자열로 받아 string_to_array로 변환한다.
     * 빈 문자열("")이면 해당 필터를 적용하지 않는다.
     */
    @Query(value = """
            SELECT p.* FROM projects p
            LEFT JOIN categories c ON c.id = p.primary_category_id
            WHERE p.status = 'PUBLISHED'
              AND (:keyword = '' OR p.title ILIKE ('%' || :keyword || '%')
                                 OR p.description ILIKE ('%' || :keyword || '%'))
              AND (:categorySlug = '' OR c.slug = :categorySlug)
              AND (:tagsCsv = '' OR p.tags && string_to_array(:tagsCsv, ','))
              AND (:techCsv = '' OR EXISTS (
                    SELECT 1 FROM project_technologies pt
                    JOIN technologies t ON t.id = pt.technology_id
                    WHERE pt.project_id = p.id
                      AND t.slug = ANY(string_to_array(:techCsv, ','))
              ))
            ORDER BY p.published_at DESC NULLS LAST, p.id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Project> search(@Param("keyword") String keyword,
                         @Param("categorySlug") String categorySlug,
                         @Param("tagsCsv") String tagsCsv,
                         @Param("techCsv") String techCsv,
                         @Param("limit") int limit,
                         @Param("offset") int offset);

    /**
     * 홈 종합 피드 — 기획서 4장의 "인기·최신·다양성 혼합".
     *
     * 개인화(협업 필터링 등)는 사용자 행동 데이터가 쌓인 뒤 적용할 영역이라, 지금은 최근 7일
     * 집계(project_daily_metrics)의 인기 점수와 최신성만 조합한다. 집계 행이 아직 없는 신규
     * 프로젝트도 최신성 덕분에 노출된다(콜드 스타트).
     */
    @Query(value = """
            SELECT p.* FROM projects p
            LEFT JOIN (
                SELECT m.project_id,
                       sum(m.unique_visitors) AS visitors,
                       sum(m.unique_likes) AS likes
                FROM project_daily_metrics m
                WHERE m.metric_date >= current_date - 6
                GROUP BY m.project_id
            ) recent ON recent.project_id = p.id
            WHERE p.status = 'PUBLISHED'
            ORDER BY (
                0.6 * ln(1 + coalesce(recent.visitors, 0))
              + 0.4 * ln(1 + coalesce(recent.likes, 0))
              + 2.0 * exp(-greatest(0, extract(epoch FROM (now() - coalesce(p.published_at, p.created_at))) / 86400) / 14)
            ) DESC, p.id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Project> findHomeFeed(@Param("limit") int limit, @Param("offset") int offset);
}
