package com.skala.clickhub.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/** schema.sql: creator_subscriptions PK (subscriber_id, creator_id) */
@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CreatorSubscriptionId implements Serializable {

    @Column(name = "subscriber_id")
    private UUID subscriberId;

    @Column(name = "creator_id")
    private UUID creatorId;
}
