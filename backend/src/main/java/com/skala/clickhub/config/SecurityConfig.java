package com.skala.clickhub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.security.jwt.JwtAuthenticationFilter;
import com.skala.clickhub.security.oauth2.CustomOAuth2UserService;
import com.skala.clickhub.security.oauth2.CustomOidcUserService;
import com.skala.clickhub.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.skala.clickhub.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * §12 API 명세 기준 인증 "없음"/"선택" 엔드포인트 + 인프라용/OAuth2 공개 경로.
     * "/v1/auth/github", "/v1/auth/google"은 oauth2Login()의 authorizationEndpoint가
     * 직접 가로채 리다이렉트하고, "/login/oauth2/code/**"는 그 콜백(각 프로바이더가 되돌아오는 경로)이다.
     * "/api/v1/ping"과 "/actuator/health"는 도메인 API와 경로 체계가 달라 보이지만,
     * db/migration 통합 브랜치에서 Docker Compose/Render 헬스체크용으로 이미 이 경로를 쓰고
     * 있어(backend/Dockerfile의 HEALTHCHECK, PingControllerTests) 그대로 맞췄다.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v1/auth/github",
            "/v1/auth/google",
            "/login/oauth2/code/**",
            "/api/v1/ping",
            "/actuator/health",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // 앱 전역 ObjectMapper 빈을 주입받지 않고 여기 전용으로 직접 만든다 — @SpringBootTest를
    // MOCK 웹 환경 + MockMvc.webAppContextSetup(...)으로 띄우는 테스트(PingControllerTests 등)에서
    // JacksonAutoConfiguration이 완전히 초기화되지 않아 ObjectMapper 빈을 못 찾는 경우가
    // 실제로 있었다(NoSuchBeanDefinitionException 실측). 에러 응답은 boolean/int/String 몇 개뿐이라
    // 앱의 날짜 포맷 등 커스텀 직렬화 설정과 무관하므로, 별도 인스턴스로 두는 편이 더 안전하다.
    private static final ObjectMapper ERROR_RESPONSE_MAPPER = new ObjectMapper();

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    // "oauth2" 프로필(실제 GitHub/Google client-id·secret)이 켜져 있지 않으면 Spring Boot가
    // 이 빈을 아예 만들지 않는다 — ObjectProvider로 선택적으로 받아서, 없으면 oauth2Login() 자체를
    // 건너뛴다. 그렇지 않으면 credential 없는 로컬/CI 환경에서 앱이 통째로 기동 실패한다(실측 확인).
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        // §12 "인증: 선택" — 토큰이 있으면 개인화, 없어도 접근은 허용해야 하는 조회 API
                        .requestMatchers(HttpMethod.GET,
                                "/v1/projects/{id}", "/v1/search", "/v1/feed",
                                "/v1/rankings/**", "/v1/insights/weekly", "/v1/tutorials"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/projects/{id}/outbound-clicks").permitAll()
                        // 커뮤니티 게시판(V1 스키마 도메인) 조회는 인증 없이 열람 가능
                        .requestMatchers(HttpMethod.GET, "/v1/community/**").permitAll()
                        .anyRequest().authenticated()
                )
                // 인증/인가 실패도 컨트롤러 예외와 동일하게 ApiResponse 포맷으로 내려준다.
                // 이게 없으면 Security가 필터 단계에서 바로 401/403을 응답해버려서
                // GlobalExceptionHandler를 거치지 않고, 프론트가 받는 에러 응답 모양이
                // 컨트롤러 예외와 완전히 달라진다. (브라우저가 직접 접근한 OAuth2 로그인
                // 리다이렉트 흐름 자체에는 영향 없음 — 그 흐름은 이 엔트리포인트에 도달하기 전에
                // oauth2Login 필터가 먼저 처리한다.)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // GitHub/Google OAuth2 로그인 — "oauth2" 프로필로 실제 credential이 주입됐을 때만 켠다.
        // 인가 요청 시작 경로를 "/v1/auth"로 맞춰서 "/v1/auth/{registrationId}"(github, google)를
        // §12의 "GET /v1/auth/github" 규격과 일치시킨다. 로그인 성공 시 실제 회원가입/조회와
        // JWT 발급은 OAuth2AuthenticationSuccessHandler가, 사용자 upsert는 CustomOAuth2UserService/
        // CustomOidcUserService가 담당한다.
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/v1/auth"))
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOidcUserService)
                    )
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            );
        }

        return http.build();
    }

    private void writeErrorResponse(jakarta.servlet.http.HttpServletResponse response,
                                     HttpStatus status, String message) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ERROR_RESPONSE_MAPPER.writeValue(response.getWriter(), ApiResponse.error(status, message));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
