package com.skala.clickhub.repository;

import com.skala.clickhub.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TechnologyRepository extends JpaRepository<Technology, UUID> {

    Optional<Technology> findBySlug(String slug);
}
