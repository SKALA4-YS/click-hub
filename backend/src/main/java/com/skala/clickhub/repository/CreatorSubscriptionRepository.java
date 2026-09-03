package com.skala.clickhub.repository;

import com.skala.clickhub.entity.CreatorSubscription;
import com.skala.clickhub.entity.id.CreatorSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorSubscriptionRepository extends JpaRepository<CreatorSubscription, CreatorSubscriptionId> {
}
