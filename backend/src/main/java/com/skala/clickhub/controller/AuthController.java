package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.auth.AuthDtos.MeResponse;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * "/v1/auth/github", "/v1/auth/google"은 별도 메서드로 만들지 않는다 — SecurityConfig의
 * oauth2Login().authorizationEndpoint().baseUri("/v1/auth")가 이 두 경로를 직접 가로채
 * GitHub/Google 로그인 화면으로 리다이렉트한다. 로그인 성공 후 콜백 처리와 JWT 발급은
 * security/oauth2/OAuth2AuthenticationSuccessHandler가 담당한다.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    // 인증: 로그인
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return ApiResponse.success(new MeResponse(
                user.getId(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getTheme().name(),
                user.getAuthProvider().name()
        ));
    }
}
