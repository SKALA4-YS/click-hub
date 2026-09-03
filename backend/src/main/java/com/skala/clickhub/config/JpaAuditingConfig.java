package com.skala.clickhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * BaseTimeEntity의 @CreatedDate/@LastModifiedDate를 동작시키는 스위치.
 * ClickHubApplication에 직접 @EnableJpaAuditing을 붙이지 않고 별도 설정으로 뺀 이유:
 * "nodb" 프로필(DataSource/Hibernate 자동 구성 제외, DB 없는 Context 테스트용)에서
 * JPA 메타모델이 아예 없는 상태로 JPA Auditing 인프라가 초기화를 시도하면서
 * "JPA metamodel must not be empty" 오류로 컨텍스트 로딩 자체가 실패했다(실측 확인).
 * @Profile("!nodb")로 DB가 실제로 붙는 프로필에서만 활성화해 이 문제를 피한다.
 *
 * dateTimeProviderRef가 왜 필요한가: Spring Data JPA Auditing의 기본
 * CurrentDateTimeProvider는 "현재 시각"을 java.time.LocalDateTime으로 만들어 넘긴다.
 * 이 프로젝트의 BaseTimeEntity.createdAt/updatedAt은 PostgreSQL timestamptz에 맞춰
 * 전부 OffsetDateTime으로 선언했는데, Hibernate가 LocalDateTime → OffsetDateTime 암묵
 * 변환을 거부해서 "Cannot convert unsupported date type java.time.LocalDateTime to
 * java.time.OffsetDateTime" 오류로 INSERT 자체가 실패했다 — 실제 Google 로그인으로
 * User를 최초 생성하려는 순간 이 예외가 났다(회원가입이 100% 실패하는 상태였음, 실측 확인).
 * OffsetDateTime.now()를 직접 공급하는 DateTimeProvider로 교체해 타입 불일치를 원천 차단한다.
 */
@Profile("!nodb")
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
