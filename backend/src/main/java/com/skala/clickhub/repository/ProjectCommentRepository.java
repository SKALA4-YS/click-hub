package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, UUID> {

    @Query("""
            SELECT c FROM ProjectComment c
            JOIN FETCH c.author
            WHERE c.project.id = :projectId AND c.deletedAt IS NULL
            ORDER BY c.createdAt ASC, c.id ASC
            """)
    List<ProjectComment> findActiveByProjectId(@Param("projectId") UUID projectId);
}
