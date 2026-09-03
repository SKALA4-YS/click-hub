package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * schema.sql: technologies — 프로젝트별 자유 텍스트가 아닌 공용 기술 카탈로그.
 * 화면에서 선택하는 기술 스택은 이 카탈로그를 참조한다 (project_technologies 통해).
 */
@Getter
@Entity
@Table(name = "technologies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Technology {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechGroup defaultGroup;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private Technology(String name, String slug, TechGroup defaultGroup) {
        this.name = name;
        this.slug = slug;
        this.defaultGroup = defaultGroup;
    }
}
