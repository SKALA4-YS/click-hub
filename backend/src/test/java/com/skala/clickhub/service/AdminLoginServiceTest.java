package com.skala.clickhub.service;

import com.skala.clickhub.config.AdminLoginProperties;
import com.skala.clickhub.dto.auth.AuthDtos.AdminLoginRequest;
import com.skala.clickhub.entity.SocialLoginProvider;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.UserRole;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.UserRepository;
import com.skala.clickhub.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminLoginServiceTest {

    private static final UUID ADMIN_ID = UUID.fromString("90000000-0000-0000-0000-000000000001");

    private final AdminLoginProperties properties = new AdminLoginProperties();
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtUtils jwtUtils = mock(JwtUtils.class);
    private final AdminLoginService service = new AdminLoginService(properties, userRepository, jwtUtils);

    @BeforeEach
    void configure() {
        properties.setEnabled(true);
        properties.setUsername("admin");
        properties.setPassword("admin");
    }

    @Test
    void createsTheLocalAdminOnTheFirstSuccessfulLogin() {
        when(userRepository.findByLocalLoginIdIgnoreCase("admin")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", ADMIN_ID);
            return user;
        });
        when(jwtUtils.generateAccessToken(ADMIN_ID.toString())).thenReturn("admin.jwt");

        var response = service.login(new AdminLoginRequest("admin", "admin"));

        assertThat(response.accessToken()).isEqualTo("admin.jwt");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void reusesOnlyAnActiveLocalAdmin() {
        User admin = mock(User.class);
        when(admin.getId()).thenReturn(ADMIN_ID);
        when(admin.getAuthProvider()).thenReturn(SocialLoginProvider.LOCAL);
        when(admin.getRole()).thenReturn(UserRole.ADMIN);
        when(userRepository.findByLocalLoginIdIgnoreCase("admin")).thenReturn(Optional.of(admin));
        when(jwtUtils.generateAccessToken(ADMIN_ID.toString())).thenReturn("admin.jwt");

        assertThat(service.login(new AdminLoginRequest("admin", "admin")).accessToken())
                .isEqualTo("admin.jwt");

        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsInvalidCredentialsBeforeAccessingTheDatabase() {
        assertThatThrownBy(() -> service.login(new AdminLoginRequest("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ADMIN_CREDENTIALS);

        verify(userRepository, never()).findByLocalLoginIdIgnoreCase(any());
    }

    @Test
    void hidesTheEndpointWhenAdminLoginIsDisabled() {
        properties.setEnabled(false);

        assertThatThrownBy(() -> service.login(new AdminLoginRequest("admin", "admin")))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode())
                .isEqualTo(ErrorCode.ADMIN_LOGIN_DISABLED);

        verify(userRepository, never()).findByLocalLoginIdIgnoreCase(any());
    }
}
