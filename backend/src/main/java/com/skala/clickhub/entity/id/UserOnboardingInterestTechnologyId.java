package com.skala.clickhub.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserOnboardingInterestTechnologyId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "technology_id")
    private UUID technologyId;
}
