package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, UUID> {

    @Query(value = """
            SELECT * FROM project_comments
            WHERE project_id = :projectId AND deleted_at IS NULL
            ORDER BY created_at DESC, id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<ProjectComment> findActiveByProject(@Param("projectId") UUID projectId,
                                             @Param("limit") int limit,
                                             @Param("offset") int offset);

    @Query("""
            SELECT c FROM ProjectComment c
            LEFT JOIN FETCH c.author
            WHERE c.id = :id AND c.deletedAt IS NULL
            """)
    Optional<ProjectComment> findActiveById(@Param("id") UUID id);
}
