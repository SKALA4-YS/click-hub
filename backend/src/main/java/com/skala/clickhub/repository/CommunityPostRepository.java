package com.skala.clickhub.repository;

import com.skala.clickhub.entity.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, UUID> {

    /**
     * 게시글 목록. 작성자 탈퇴 시 author_id가 NULL이 될 수 있어(ON DELETE SET NULL)
     * LEFT JOIN FETCH로 가져오고, 화면에서는 "알 수 없는 사용자"로 표시한다.
     * status는 V1(varchar+CHECK)/V2(네이티브 enum) 어느 쪽이든 문자열 비교가 통하도록 네이티브 쿼리를 쓴다.
     */
    @Query(value = """
            SELECT p.* FROM community_posts p
            WHERE p.board_id = :boardId
              AND p.status = 'PUBLISHED'
              AND p.deleted_at IS NULL
            ORDER BY p.created_at DESC, p.id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<CommunityPost> findPublishedByBoard(@Param("boardId") UUID boardId,
                                             @Param("limit") int limit,
                                             @Param("offset") int offset);

    @Query("""
            SELECT p FROM CommunityPost p
            LEFT JOIN FETCH p.author
            LEFT JOIN FETCH p.board
            WHERE p.id = :id AND p.deletedAt IS NULL
            """)
    Optional<CommunityPost> findActiveById(@Param("id") UUID id);

    /**
     * 조회수 증가는 엔티티 더티체킹 대신 단일 UPDATE로 처리한다 —
     * 상세 조회 때마다 낙관적 충돌 없이 카운트만 올리면 되기 때문이다.
     */
    @Modifying
    @Query("UPDATE CommunityPost p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") UUID id);
}
