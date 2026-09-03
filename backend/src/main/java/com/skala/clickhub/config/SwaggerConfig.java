package com.skala.clickhub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 우측 상단의 "Authorize" 버튼에 Bearer 토큰을 입력할 수 있게 하는 설정.
 * 이게 없으면 인증이 필요한 API를 Swagger에서 호출할 때마다 매번 Authorization 헤더를
 * 수동으로 넣어야 한다 — "Authorize" 한 번으로 이후 모든 요청에 자동으로 실린다.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI clickHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Click HUB API")
                        .description("Click HUB 백엔드 스캐폴딩 API 문서 (컨트롤러 로직 미구현 상태)")
                        .version("v0.0.1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
