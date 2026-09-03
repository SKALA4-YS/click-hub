package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * V1__initial_schema.sql: community_post_comments.
 * SQL은 FOREIGN KEY (post_id, parent_id) REFERENCES community_post_comments(post_id, id)로
 * "부모 댓글은 반드시 같은 게시글에 속한다"를 DB 레벨에서 강제한다.
 * 이 복합 FK는 JPA로 자연스럽게 표현할 수 없어, 여기서는 parent를 단순 자기참조 FK로만
 * 매핑했다 — 같은 게시글 소속 검증은 서비스 레이어 또는 DB 제약에 위임 (스켈레톤 한계로 명시).
 */
@Getter
@Entity
@Table(name = "community_post_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommunityPostComment parent;

    @Column(nullable = false)
    private String body;

    private OffsetDateTime deletedAt;
}
