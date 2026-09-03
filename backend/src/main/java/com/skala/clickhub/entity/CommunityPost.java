package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * V1__initial_schema.sql: community_posts.
 * author_id는 ON DELETE SET NULL이라 작성자 탈퇴 후에도 글은 남는다 —
 * author가 null이거나 author.deletedAt이 있으면 화면에는 "알 수 없는 사용자"로 표시해야 한다(스키마 주석).
 */
@Getter
@Entity
@Table(name = "community_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private CommunityBoard board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommunityPostStatus status;

    @Column(nullable = false)
    private int viewCount;

    private OffsetDateTime deletedAt;
}
