package com.skala.clickhub.repository;

import com.skala.clickhub.entity.ProjectTechnology;
import com.skala.clickhub.entity.id.ProjectTechnologyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectTechnologyRepository extends JpaRepository<ProjectTechnology, ProjectTechnologyId> {

    List<ProjectTechnology> findByProjectId(UUID projectId);

    void deleteByProjectId(UUID projectId);
}
