package com.skala.clickhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * db/migration/V1__initial_schema.sql: users.
 * authProvider/googleSubject 두 필드는 V1이 아니라
 * db/migration/V2__add_social_login_and_onboarding.sql이 추가하는 컬럼이다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "social_login_provider", nullable = false)
    private SocialLoginProvider authProvider;

    /** GOOGLE 로그인 사용자의 OIDC subject — GITHUB 로그인 사용자는 null (V2 CHECK 참고) */
    private String googleSubject;

    private Long githubUserId;

    private String githubLogin;

    private OffsetDateTime githubConnectedAt;

    @Column(nullable = false)
    private String displayName;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "user_role", nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "theme_preference", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    private boolean newProjectNotifications;

    private OffsetDateTime deletedAt;

    @Builder
    private User(SocialLoginProvider authProvider, String googleSubject, Long githubUserId,
                 String githubLogin, OffsetDateTime githubConnectedAt,
                 String displayName, String avatarUrl, UserRole role, Theme theme,
                 boolean newProjectNotifications) {
        this.authProvider = authProvider;
        this.googleSubject = googleSubject;
        this.githubUserId = githubUserId;
        this.githubLogin = githubLogin;
        this.githubConnectedAt = githubConnectedAt;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.theme = theme;
        this.newProjectNotifications = newProjectNotifications;
    }

    public void updateProfile(String displayName, Theme theme, Boolean newProjectNotifications) {
        if (displayName != null) {
            this.displayName = displayName;
        }
        if (theme != null) {
            this.theme = theme;
        }
        if (newProjectNotifications != null) {
            this.newProjectNotifications = newProjectNotifications;
        }
    }
}
