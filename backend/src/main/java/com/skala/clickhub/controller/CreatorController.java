package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.user.UserDtos.CreatorDetailResponse;
import com.skala.clickhub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<CreatorDetailResponse> getCreator(@PathVariable UUID id,
                                                          @AuthenticationPrincipal UUID viewerId) {
        return ApiResponse.success(userService.getCreator(id, viewerId));
    }
}
