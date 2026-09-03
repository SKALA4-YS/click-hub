package com.skala.clickhub.entity;

import com.skala.clickhub.entity.id.CreatorSubscriptionId;
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

/** schema.sql: creator_subscriptions — 사용자는 프로젝트가 아니라 제작자를 구독한다. */
@Getter
@Entity
@Table(name = "creator_subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorSubscription {

    @EmbeddedId
    private CreatorSubscriptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subscriberId")
    @JoinColumn(name = "subscriber_id")
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("creatorId")
    @JoinColumn(name = "creator_id")
    private User creator;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Builder
    private CreatorSubscription(User subscriber, User creator) {
        this.subscriber = subscriber;
        this.creator = creator;
        this.id = new CreatorSubscriptionId(subscriber.getId(), creator.getId());
    }
}
