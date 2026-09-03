package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.ProjectReactionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/** schema.sql: project_reactions — 좋아요(LIKE)/즐겨찾기(FAVORITE) 통합 테이블, 복합키. */
@Getter
@Entity
@Table(name = "project_reactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectReaction {

    @EmbeddedId
    private ProjectReactionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private ProjectReaction(User user, Project project, ReactionType type) {
        this.user = user;
        this.project = project;
        this.id = new ProjectReactionId(user.getId(), project.getId(), type);
    }
}
