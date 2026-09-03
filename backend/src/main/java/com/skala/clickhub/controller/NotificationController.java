package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.notification.NotificationDtos.NotificationResponse;
import com.skala.clickhub.dto.notification.NotificationDtos.ReadResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    // 인증: 로그인
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications() {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 로그인
    @PatchMapping("/{id}/read")
    public ApiResponse<ReadResponse> markAsRead(@PathVariable Long id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
