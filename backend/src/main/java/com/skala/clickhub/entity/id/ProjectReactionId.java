package com.skala.clickhub.entity.id;

import com.skala.clickhub.entity.ReactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

/** schema.sql: project_reactions PK (user_id, project_id, type) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectReactionId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "project_id")
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", columnDefinition = "reaction_type")
    private ReactionType type;
}
