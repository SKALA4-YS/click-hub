package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.subscribe.SubscribeDtos.SubscriptionResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class SubscribeController {

    // 인증: 로그인 - 사용자는 프로젝트가 아니라 "제작자"를 구독한다
    @PutMapping("/v1/creators/{id}/subscription")
    public ApiResponse<SubscriptionResponse> toggleSubscription(@PathVariable UUID id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
