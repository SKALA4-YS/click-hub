package com.skala.clickhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity의 @CreatedDate/@LastModifiedDate를 동작시키는 스위치.
 * ClickHubApplication에 직접 @EnableJpaAuditing을 붙이지 않고 별도 설정으로 뺀 이유:
 * "nodb" 프로필(DataSource/Hibernate 자동 구성 제외, DB 없는 Context 테스트용)에서
 * JPA 메타모델이 아예 없는 상태로 JPA Auditing 인프라가 초기화를 시도하면서
 * "JPA metamodel must not be empty" 오류로 컨텍스트 로딩 자체가 실패했다(실측 확인).
 * @Profile("!nodb")로 DB가 실제로 붙는 프로필에서만 활성화해 이 문제를 피한다.
 */
@Profile("!nodb")
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
