package com.skala.clickhub.dto.auth;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {}

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
