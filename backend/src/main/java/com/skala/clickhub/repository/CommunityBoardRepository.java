package com.skala.clickhub.repository;

import com.skala.clickhub.entity.CommunityBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * is_active 조건은 파생 쿼리(findByIsActiveTrue...) 대신 명시적 JPQL로 쓴다.
 * 필드명이 isActive라 Lombok 게터는 isActive()가 되는데, JavaBeans 규약상 이 게터의 프로퍼티명은
 * "active"라서 파생 쿼리 이름 해석이 구현체/접근방식에 따라 엇갈릴 수 있다.
 * JPQL에 Hibernate 속성명(=필드명)을 직접 쓰면 그 모호함이 사라진다.
 */
public interface CommunityBoardRepository extends JpaRepository<CommunityBoard, UUID> {

    @Query("SELECT b FROM CommunityBoard b WHERE b.isActive = true ORDER BY b.displayOrder ASC, b.id ASC")
    List<CommunityBoard> findActiveBoards();

    @Query("SELECT b FROM CommunityBoard b WHERE b.slug = :slug AND b.isActive = true")
    Optional<CommunityBoard> findActiveBySlug(@Param("slug") String slug);
}
