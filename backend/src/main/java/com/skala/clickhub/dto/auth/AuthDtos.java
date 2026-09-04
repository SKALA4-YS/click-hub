package com.skala.clickhub.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

    public record AdminLoginRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(max = 200) String password
    ) {}

    public record AdminLoginResponse(String accessToken) {}

    public record MeResponse(
            UUID id,
            String displayName,
            String avatarUrl,
            String role,
            String theme,
            String authProvider,
            boolean newProjectNotifications,
            boolean onboardingCompleted
    ) {}
}
