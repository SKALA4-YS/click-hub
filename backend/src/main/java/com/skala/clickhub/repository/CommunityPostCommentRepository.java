package com.skala.clickhub.repository;

import com.skala.clickhub.entity.CommunityPostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, UUID> {

    /** 대댓글 트리 구성을 위해 작성 시각 오름차순으로 전부 가져온다(1단계 대댓글까지만 허용). */
    @Query("""
            SELECT c FROM CommunityPostComment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.parent
            WHERE c.post.id = :postId AND c.deletedAt IS NULL
            ORDER BY c.createdAt ASC
            """)
    List<CommunityPostComment> findActiveByPostId(@Param("postId") UUID postId);
}
