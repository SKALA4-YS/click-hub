package com.skala.clickhub.security.oauth2;

import com.skala.clickhub.entity.SocialLoginProvider;
import com.skala.clickhub.entity.Theme;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.UserRole;
import com.skala.clickhub.repository.UserRepository;
import com.skala.clickhub.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2RedirectHandlerTest {

    @Test
    void redirectsWithOnlyAnAccessTokenInTheFragment() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtils jwtUtils = mock(JwtUtils.class);
        User user = User.builder()
                .authProvider(SocialLoginProvider.GOOGLE)
                .googleSubject("google-user")
                .displayName("Click HUB 사용자")
                .role(UserRole.USER)
                .theme(Theme.SYSTEM)
                .newProjectNotifications(true)
                .build();
        UUID userId = UUID.fromString("10000000-0000-0000-0000-000000000001");
        ReflectionTestUtils.setField(user, "id", userId);
        when(userRepository.findByGoogleSubject("google-user")).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(userId.toString())).thenReturn("signed.access.token");

        var principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", "google-user"),
                "sub");
        var authentication = new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), "google");
        var response = new MockHttpServletResponse();
        var handler = new OAuth2AuthenticationSuccessHandler(userRepository, jwtUtils);
        ReflectionTestUtils.setField(handler, "redirectUri", "https://click-hub.vercel.app/oauth/callback");

        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("https://click-hub.vercel.app/oauth/callback#accessToken=signed.access.token")
                .doesNotContain("refreshToken", "?");
        verify(jwtUtils).generateAccessToken(userId.toString());
        verify(jwtUtils, never()).generateRefreshToken(userId.toString());
    }

    @Test
    void redirectsFailuresInTheFragment() throws Exception {
        var handler = new OAuth2AuthenticationFailureHandler();
        ReflectionTestUtils.setField(handler, "redirectUri", "http://localhost:5173/oauth/callback");
        var response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(
                new MockHttpServletRequest(),
                response,
                new org.springframework.security.core.AuthenticationException("failed") {});

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:5173/oauth/callback#error=oauth2_login_failed")
                .doesNotContain("?");
    }
}
