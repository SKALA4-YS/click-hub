package com.skala.clickhub.security.oauth2;

import com.skala.clickhub.service.OAuth2UserSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
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

        syncService.syncGoogleUser(googleSubject, displayName, avatarUrl);

        return oidcUser;
    }
}
