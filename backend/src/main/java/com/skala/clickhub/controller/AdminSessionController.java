package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.auth.AuthDtos.AdminLoginRequest;
import com.skala.clickhub.dto.auth.AuthDtos.AdminLoginResponse;
import com.skala.clickhub.service.AdminLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/session")
@RequiredArgsConstructor
public class AdminSessionController {

    private final AdminLoginService adminLoginService;

    @PostMapping
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminLoginService.login(request));
    }
}
