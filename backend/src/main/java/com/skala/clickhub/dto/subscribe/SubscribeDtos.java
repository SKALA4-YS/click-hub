package com.skala.clickhub.dto.subscribe;

public final class SubscribeDtos {

    private SubscribeDtos() {}

    public record SubscriptionResponse(
            boolean subscribed
    ) {}
}
