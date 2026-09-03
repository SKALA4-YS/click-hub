package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.auth.AuthDtos.MeResponse;
import com.skala.clickhub.dto.user.UserDtos.CreatorSummary;
import com.skala.clickhub.dto.user.UserDtos.OnboardingResponse;
import com.skala.clickhub.dto.user.UserDtos.OnboardingUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProfileUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProjectItem;
import com.skala.clickhub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

/**
 * "현재 로그인한 사용자" 조회는 "/v1/auth/**"가 아니라 "/v1/users/**"에 둔다.
 * 이유(실측으로 발견한 버그): SecurityConfig의 oauth2Login().authorizationEndpoint()가
 * baseUri("/v1/auth")로 설정돼 있으면, Spring Security의 DefaultOAuth2AuthorizationRequestResolver는
 * "/v1/auth/{한 단계 경로}" 형태의 요청을 전부 "그 이름의 OAuth2 registrationId로 로그인을
 * 시작하라"는 요청으로 가로챈다. 그래서 이전에 "/v1/auth/me"로 뒀을 때 실제 컨트롤러에
 * 도달하지도 못하고 "Invalid Client Registration with Id: me" 예외가 났다(구글 로그인 테스트 중
 * 실측 확인). "/v1/auth/**" 밑에는 절대 이 두 개(github, google) 외의 엔드포인트를 추가하면 안 된다.
 *
 * "/v1/auth/github", "/v1/auth/google" 자체는 별도 메서드로 만들지 않는다 — SecurityConfig의
 * oauth2Login().authorizationEndpoint().baseUri("/v1/auth")가 이 두 경로를 직접 가로채
 * GitHub/Google 로그인 화면으로 리다이렉트한다. 로그인 성공 후 콜백 처리와 JWT 발급은
 * security/oauth2/OAuth2AuthenticationSuccessHandler가 담당한다.
 */
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 인증: 로그인
    @GetMapping("/me")
    public ApiResponse<MeResponse> me(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(userService.getMe(userId));
    }

    @PatchMapping("/me")
    public ApiResponse<MeResponse> updateProfile(@AuthenticationPrincipal UUID userId,
                                                  @Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(userService.updateProfile(userId, request));
    }

    @PutMapping("/me/onboarding")
    public ApiResponse<OnboardingResponse> updateOnboarding(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody OnboardingUpdateRequest request) {
        return ApiResponse.success(userService.updateOnboarding(userId, request));
    }

    @GetMapping("/me/projects")
    public ApiResponse<List<ProjectItem>> getMyProjects(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(userService.getMyProjects(userId));
    }

    @GetMapping("/me/favorites")
    public ApiResponse<List<ProjectItem>> getMyFavorites(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(userService.getMyFavorites(userId));
    }

    @GetMapping("/me/subscriptions")
    public ApiResponse<List<CreatorSummary>> getMySubscriptions(@AuthenticationPrincipal UUID userId) {
        return ApiResponse.success(userService.getMySubscriptions(userId));
    }
}
