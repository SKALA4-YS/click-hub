package com.skala.clickhub.repository;

import com.skala.clickhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByGithubUserId(Long githubUserId);

    Optional<User> findByGoogleSubject(String googleSubject);
}
