package com.skala.clickhub.security.oauth2;

import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.service.OAuth2UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * Google 로그인은 OIDC(ID 토큰 포함)라서 GitHub와 별도 서비스로 분리했다.
 * SecurityConfig의 oauth2Login().userInfoEndpoint().oidcUserService(...)에 등록해서 쓴다.
 */
@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserSyncService syncService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleSubject = oidcUser.getSubject();
        String fullName = oidcUser.getFullName();
        String displayName = (fullName != null && !fullName.isBlank()) ? fullName : googleSubject;
        String avatarUrl = oidcUser.getPicture();

        try {
            syncService.syncGoogleUser(googleSubject, displayName, avatarUrl);
        } catch (BusinessException e) {
            // BusinessException은 AuthenticationException이 아니라서 그냥 던지면 Spring Security의
            // OAuth2LoginAuthenticationFilter가 인증 실패로 인식하지 못하고 그대로 서블릿 밖으로
            // 전파돼 OAuth2AuthenticationFailureHandler를 거치지 않는다 — 실측 확인(탈퇴한 계정으로
            // 재로그인 시도 시 프론트 콜백으로 리다이렉트되지 않고 500이 나며, 그 직후 내부 /error
            // 포워딩이 보안 필터를 다시 타면서 엉뚱하게 401 "인증이 필요합니다" JSON이 노출됐다).
            // OAuth2AuthenticationException(AuthenticationException의 하위 타입)으로 감싸서
            // 던져야 실패 핸들러가 정상적으로 프론트로 리다이렉트한다.
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null), e);
        }

        return oidcUser;
    }
}
