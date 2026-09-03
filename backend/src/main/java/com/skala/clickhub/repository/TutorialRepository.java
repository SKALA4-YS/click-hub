package com.skala.clickhub.repository;

import com.skala.clickhub.entity.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TutorialRepository extends JpaRepository<Tutorial, UUID> {

    /**
     * 기획서 9.2 — 카테고리·난이도·기술 스택으로 필터링. 빈 문자열이면 해당 필터 미적용.
     * technology_slugs가 text[]라 배열 연산이 필요해 네이티브 쿼리를 쓴다.
     *
     * type/difficulty는 네이티브 enum 컬럼이지만 CAST(:param AS tutorial_type)을 쓰지 않는다.
     * PostgreSQL은 OR 단축평가와 무관하게 캐스팅을 평가할 수 있어서, 필터를 안 쓰려고 빈 문자열을
     * 넘기면 'invalid input value for enum tutorial_type: ""'로 실패한다(실측 확인).
     * 컬럼 쪽을 ::text로 내려 비교하면 이 문제가 사라진다.
     */
    @Query(value = """
            SELECT t.* FROM tutorials t
            WHERE t.is_published = true
              AND (:type = '' OR t.type::text = :type)
              AND (:difficulty = '' OR t.difficulty::text = :difficulty)
              AND (:techSlug = '' OR :techSlug = ANY(t.technology_slugs))
            ORDER BY t.published_at DESC NULLS LAST, t.id
            """, nativeQuery = true)
    List<Tutorial> findPublished(@Param("type") String type,
                                 @Param("difficulty") String difficulty,
                                 @Param("techSlug") String techSlug);
}
