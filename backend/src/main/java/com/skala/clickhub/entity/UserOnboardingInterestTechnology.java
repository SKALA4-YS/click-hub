package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.UserOnboardingInterestTechnologyId;
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

@Getter
@Entity
@Table(name = "user_onboarding_interest_technologies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOnboardingInterestTechnology {

    @EmbeddedId
    private UserOnboardingInterestTechnologyId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("technologyId")
    @JoinColumn(name = "technology_id")
    private Technology technology;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private UserOnboardingInterestTechnology(User user, Technology technology) {
        this.user = user;
        this.technology = technology;
        this.id = new UserOnboardingInterestTechnologyId(user.getId(), technology.getId());
    }
}
