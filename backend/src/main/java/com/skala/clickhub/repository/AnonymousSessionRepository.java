package com.skala.clickhub.repository;

import com.skala.clickhub.entity.AnonymousSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnonymousSessionRepository extends JpaRepository<AnonymousSession, UUID> {
}
