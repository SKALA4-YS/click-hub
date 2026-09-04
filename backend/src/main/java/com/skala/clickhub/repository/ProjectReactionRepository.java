package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectReaction;
import com.skala.clickhub.entity.ReactionType;
import com.skala.clickhub.entity.id.ProjectReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * 좋아요/즐겨찾기 토글 자체(§5)는 이번 구현 범위가 아니지만, 프로젝트 상세(§2)의
 * likeCount/favoriteCount/likedByMe/favoritedByMe를 채우려면 조회가 필요하다.
 */
public interface ProjectReactionRepository extends JpaRepository<ProjectReaction, ProjectReactionId> {

    long countByIdProjectIdAndIdType(UUID projectId, ReactionType type);

    boolean existsByIdUserIdAndIdProjectIdAndIdType(UUID userId, UUID projectId, ReactionType type);

    /**
     * 피드처럼 여러 프로젝트를 한 번에 나열할 때, 행마다 countByIdProjectIdAndIdType를
     * 부르면 N+1이 된다. 페이지 단위로 한 번에 묶어 세기 위한 배치 조회.
     */
    @Query("""
            SELECT reaction.id.projectId AS projectId, COUNT(reaction) AS count
            FROM ProjectReaction reaction
            WHERE reaction.id.projectId IN :projectIds AND reaction.id.type = :type
            GROUP BY reaction.id.projectId
            """)
    List<ProjectReactionCount> countGroupedByProjectIds(@Param("projectIds") List<UUID> projectIds,
                                                          @Param("type") ReactionType type);

    interface ProjectReactionCount {
        UUID getProjectId();

        long getCount();
    }

    @Query("""
            SELECT reaction FROM ProjectReaction reaction
            JOIN FETCH reaction.project project
            JOIN FETCH project.owner
            LEFT JOIN FETCH project.primaryCategory
            WHERE reaction.user.id = :userId AND reaction.id.type = :type
            ORDER BY reaction.createdAt DESC
            """)
    List<ProjectReaction> findAllByUserAndType(@Param("userId") UUID userId,
                                                @Param("type") ReactionType type);
}
