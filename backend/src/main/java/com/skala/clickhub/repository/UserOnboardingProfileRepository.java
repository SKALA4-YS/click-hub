package com.skala.clickhub.repository;

import com.skala.clickhub.entity.UserOnboardingProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserOnboardingProfileRepository extends JpaRepository<UserOnboardingProfile, UUID> {
}
