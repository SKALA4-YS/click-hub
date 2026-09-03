package com.skala.clickhub.repository;

import com.skala.clickhub.entity.UserOnboardingInterestCategory;
import com.skala.clickhub.entity.id.UserOnboardingInterestCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserOnboardingInterestCategoryRepository
        extends JpaRepository<UserOnboardingInterestCategory, UserOnboardingInterestCategoryId> {

    List<UserOnboardingInterestCategory> findByIdUserId(UUID userId);

    void deleteByIdUserId(UUID userId);
}
