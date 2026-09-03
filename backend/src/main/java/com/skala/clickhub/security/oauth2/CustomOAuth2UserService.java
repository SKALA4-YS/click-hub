package com.skala.clickhub.security.oauth2;

import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.service.OAuth2UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * GitHub는 OIDC가 아니라 순수 OAuth2라서 이 서비스가 처리한다 (Google은 CustomOidcUserService).
 * SecurityConfig의 oauth2Login().userInfoEndpoint().userService(...)에 등록해서 쓴다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserSyncService syncService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long githubUserId = ((Number) attributes.get("id")).longValue();
        String githubLogin = (String) attributes.get("login");
        String name = (String) attributes.get("name");
        String displayName = (name != null && !name.isBlank()) ? name : githubLogin;
        String avatarUrl = (String) attributes.get("avatar_url");

        try {
            syncService.syncGithubUser(githubUserId, githubLogin, displayName, avatarUrl);
        } catch (BusinessException e) {
            // CustomOidcUserService와 동일한 이유(실측 확인) — Google 로그인 경로에서 발견한 버그를
            // GitHub 경로에도 동일하게 적용해 둔다.
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null), e);
        }

        return oAuth2User;
    }
}
