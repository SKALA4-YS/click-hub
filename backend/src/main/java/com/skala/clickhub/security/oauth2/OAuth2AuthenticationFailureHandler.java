package com.skala.clickhub.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Value("${clickhub.oauth2.redirect-uri:http://localhost:5173/oauth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        // CustomOAuth2UserService/CustomOidcUserService가 탈퇴 계정 등 비즈니스 예외를
        // OAuth2AuthenticationException(OAuth2Error)으로 감싸 던지면 errorCode가 여기로 온다
        // (예: "AUTH_003") — 없으면(진짜 OAuth2 프로토콜 실패) 기존 기본값을 유지한다.
        String errorCode = (exception instanceof OAuth2AuthenticationException oAuth2Exception
                && oAuth2Exception.getError().getErrorCode() != null)
                ? oAuth2Exception.getError().getErrorCode()
                : "oauth2_login_failed";

        String redirectUrl = redirectUri + "#error=" + java.net.URLEncoder.encode(
                errorCode, java.nio.charset.StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}
