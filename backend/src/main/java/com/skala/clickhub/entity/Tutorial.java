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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** schema.sql: tutorials */
@Getter
@Entity
@Table(name = "tutorials")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tutorial extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TutorialDifficulty difficulty;

    @Column(nullable = false)
    private int estimatedMinutes;

    @Column(nullable = false)
    private String sourceUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] categorySlugs;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]", nullable = false)
    private String[] technologySlugs;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]", nullable = false)
    private UUID[] relatedProjectIds;

    @Column(nullable = false)
    private boolean isPublished;

    private OffsetDateTime publishedAt;
}
