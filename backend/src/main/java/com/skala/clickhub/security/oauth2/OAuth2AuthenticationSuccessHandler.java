package com.skala.clickhub.security.oauth2;

import com.skala.clickhub.entity.User;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.UserRepository;
import com.skala.clickhub.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * OAuth2 로그인이 성공하면(=회원가입 또는 로그인 완료) 우리 자체 JWT를 발급해서
 * 프론트엔드(SPA) 콜백 URL로 302 리다이렉트한다. 프론트는 이 콜백에서 쿼리 파라미터의
 * 토큰을 꺼내 저장한 뒤, 이후 모든 API 호출에 Authorization: Bearer 헤더로 실어 보내면 된다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    @Value("${clickhub.oauth2.redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) authentication;
        String registrationId = oAuth2Token.getAuthorizedClientRegistrationId();
        Map<String, Object> attributes = ((OAuth2User) authentication.getPrincipal()).getAttributes();

        User user = resolveUser(registrationId, attributes);

        String accessToken = jwtUtils.generateAccessToken(user.getId().toString());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId().toString());

        String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private User resolveUser(String registrationId, Map<String, Object> attributes) {
        if ("github".equals(registrationId)) {
            Long githubUserId = ((Number) attributes.get("id")).longValue();
            return userRepository.findByGithubUserId(githubUserId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }
        if ("google".equals(registrationId)) {
            String googleSubject = (String) attributes.get("sub");
            return userRepository.findByGoogleSubject(googleSubject)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        }
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
}
