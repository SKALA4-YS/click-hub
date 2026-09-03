package com.skala.clickhub.entity.id;

import com.skala.clickhub.entity.TechGroup;
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

/** schema.sql: project_technologies PK (project_id, technology_id, technology_group) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectTechnologyId implements Serializable {

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "technology_id")
    private UUID technologyId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "technology_group", columnDefinition = "tech_group")
    private TechGroup technologyGroup;
}
