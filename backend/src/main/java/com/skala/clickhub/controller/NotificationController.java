package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.notification.NotificationDtos.NotificationResponse;
import com.skala.clickhub.dto.notification.NotificationDtos.ReadResponse;
import com.skala.clickhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 인증: 로그인
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(notificationService.getNotifications(userId));
    }

    // 인증: 로그인
    @PatchMapping("/{id}/read")
    public ApiResponse<ReadResponse> markAsRead(@PathVariable Long id,
                                                 @AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(notificationService.markAsRead(id, userId));
    }
}
