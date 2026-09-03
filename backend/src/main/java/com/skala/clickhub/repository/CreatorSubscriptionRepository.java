package com.skala.clickhub.repository;

import com.skala.clickhub.entity.CreatorSubscription;
import com.skala.clickhub.entity.id.CreatorSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CreatorSubscriptionRepository extends JpaRepository<CreatorSubscription, CreatorSubscriptionId> {

    long countByIdCreatorId(UUID creatorId);

    @Query("""
            SELECT subscription FROM CreatorSubscription subscription
            JOIN FETCH subscription.creator
            WHERE subscription.subscriber.id = :subscriberId
            ORDER BY subscription.createdAt DESC
            """)
    List<CreatorSubscription> findAllBySubscriberId(@Param("subscriberId") UUID subscriberId);
}
