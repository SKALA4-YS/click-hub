package com.skala.clickhub.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/** schema.sql: user_onboarding_interest_categories PK (user_id, category_id) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserOnboardingInterestCategoryId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "category_id")
    private UUID categoryId;
}
