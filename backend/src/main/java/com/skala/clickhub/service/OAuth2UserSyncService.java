package com.skala.clickhub.service;

import com.skala.clickhub.entity.SocialLoginProvider;
import com.skala.clickhub.entity.Theme;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.UserRole;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * GitHub/Google OAuth2 로그인 성공 시 "있으면 로그인, 없으면 회원가입"을 한 번에 처리한다.
 * V1의 users.auth_provider(V2에서 추가)는 최초 가입 시 어떤 프로바이더로 가입했는지를 고정하고,
 * 이후에는 같은 프로바이더의 고유 ID(githubUserId/googleSubject)로만 사용자를 다시 찾는다.
 */
@Service
@RequiredArgsConstructor
public class OAuth2UserSyncService {

    private final UserRepository userRepository;

    @Transactional
    public User syncGithubUser(Long githubUserId, String githubLogin, String displayName, String avatarUrl) {
        return userRepository.findByGithubUserId(githubUserId)
                .map(this::assertNotDeleted)
                .orElseGet(() -> userRepository.save(User.builder()
                        .authProvider(SocialLoginProvider.GITHUB)
                        .githubUserId(githubUserId)
                        .githubLogin(githubLogin)
                        .githubConnectedAt(OffsetDateTime.now())
                        .displayName(displayName)
                        .avatarUrl(avatarUrl)
                        .role(UserRole.USER)
                        .theme(Theme.SYSTEM)
                        .newProjectNotifications(true)
                        .build()));
    }

    @Transactional
    public User syncGoogleUser(String googleSubject, String displayName, String avatarUrl) {
        return userRepository.findByGoogleSubject(googleSubject)
                .map(this::assertNotDeleted)
                .orElseGet(() -> userRepository.save(User.builder()
                        .authProvider(SocialLoginProvider.GOOGLE)
                        .googleSubject(googleSubject)
                        .displayName(displayName)
                        .avatarUrl(avatarUrl)
                        .role(UserRole.USER)
                        .theme(Theme.SYSTEM)
                        .newProjectNotifications(true)
                        .build()));
    }

    private User assertNotDeleted(User user) {
        if (user.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.ACCOUNT_DELETED);
        }
        return user;
    }
}
