package com.skala.clickhub.repository;

import com.skala.clickhub.entity.UserOnboardingInterestTechnology;
import com.skala.clickhub.entity.id.UserOnboardingInterestTechnologyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserOnboardingInterestTechnologyRepository
        extends JpaRepository<UserOnboardingInterestTechnology, UserOnboardingInterestTechnologyId> {

    List<UserOnboardingInterestTechnology> findByIdUserId(UUID userId);

    void deleteByIdUserId(UUID userId);
}
