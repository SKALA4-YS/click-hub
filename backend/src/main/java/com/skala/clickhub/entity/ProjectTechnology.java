package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.ProjectTechnologyId;
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

/** schema.sql: project_technologies (PK = 복합키, id 대리키 없음) */
@Getter
@Entity
@Table(name = "project_technologies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTechnology {

    @EmbeddedId
    private ProjectTechnologyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("technologyId")
    @JoinColumn(name = "technology_id")
    private Technology technology;

    private String version;

    @Builder
    private ProjectTechnology(Project project, Technology technology, TechGroup technologyGroup, String version) {
        this.project = project;
        this.technology = technology;
        this.id = new ProjectTechnologyId(project.getId(), technology.getId(), technologyGroup);
        this.version = version;
    }
}
