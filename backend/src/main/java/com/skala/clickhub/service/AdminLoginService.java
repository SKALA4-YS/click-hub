package com.skala.clickhub.service;

import com.skala.clickhub.config.AdminLoginProperties;
import com.skala.clickhub.dto.auth.AuthDtos.AdminLoginRequest;
import com.skala.clickhub.dto.auth.AuthDtos.AdminLoginResponse;
import com.skala.clickhub.entity.SocialLoginProvider;
import com.skala.clickhub.entity.Theme;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.UserRole;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.UserRepository;
import com.skala.clickhub.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminLoginProperties properties;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!properties.isEnabled()
                || properties.getUsername().isBlank()
                || properties.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.ADMIN_LOGIN_DISABLED);
        }

        if (!secureEquals(request.username(), properties.getUsername())
                || !secureEquals(request.password(), properties.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_CREDENTIALS);
        }

        User admin = userRepository.findByLocalLoginIdIgnoreCase(properties.getUsername())
                .map(this::requireActiveAdmin)
                .orElseGet(this::createAdmin);

        return new AdminLoginResponse(jwtUtils.generateAccessToken(admin.getId().toString()));
    }

    private User requireActiveAdmin(User user) {
        if (user.getDeletedAt() != null
                || user.getAuthProvider() != SocialLoginProvider.LOCAL
                || user.getRole() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.INVALID_ADMIN_CREDENTIALS);
        }
        return user;
    }

    private User createAdmin() {
        return userRepository.save(User.builder()
                .authProvider(SocialLoginProvider.LOCAL)
                .localLoginId(properties.getUsername())
                .displayName("관리자")
                .role(UserRole.ADMIN)
                .theme(Theme.SYSTEM)
                .newProjectNotifications(false)
                .build());
    }

    private boolean secureEquals(String actual, String expected) {
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
