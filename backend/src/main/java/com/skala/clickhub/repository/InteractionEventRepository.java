package com.skala.clickhub.repository;

import com.skala.clickhub.entity.InteractionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteractionEventRepository extends JpaRepository<InteractionEvent, Long> {
}
