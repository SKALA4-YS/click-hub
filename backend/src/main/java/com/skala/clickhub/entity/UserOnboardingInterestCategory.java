package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.UserOnboardingInterestCategoryId;
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

/**
 * db/migration/V2__add_social_login_and_onboarding.sql: user_onboarding_interest_categories —
 * 기획서 2장 "최초 로그인 후 관심 카테고리 온보딩 설문(건너뛰기 가능)"의 저장소.
 * 복합키(user_id, category_id). V1에는 없고 V2에서 추가된 테이블이다.
 */
@Getter
@Entity
@Table(name = "user_onboarding_interest_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOnboardingInterestCategory {

    @EmbeddedId
    private UserOnboardingInterestCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private UserOnboardingInterestCategory(User user, Category category) {
        this.user = user;
        this.category = category;
        this.id = new UserOnboardingInterestCategoryId(user.getId(), category.getId());
    }
}
