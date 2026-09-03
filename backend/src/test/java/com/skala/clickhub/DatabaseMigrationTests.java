package com.skala.clickhub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * db/migration의 모든 마이그레이션(V1, V2, ...)이 순서대로 실제 PostgreSQL(+pgvector)에
 * 적용되는지 검증한다. 새 버전 파일을 추가할 때마다 이 테스트의 기대값도 함께 갱신해야 한다.
 */
@Testcontainers
@SpringBootTest
class DatabaseMigrationTests {

	private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
			.asCompatibleSubstituteFor("postgres");

	@Container
	private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE)
			.withDatabaseName("clickhub")
			.withUsername("clickhub")
			.withPassword("test-only");

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.flyway.connect-retries", () -> "0");
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void appliesMigrationsWithExtensionsTablesAndSeedData() {
		assertEquals(3, count("SELECT count(*) FROM flyway_schema_history WHERE version IN ('1', '2', '3') AND success"));
		assertEquals(2, count("SELECT count(*) FROM pg_extension WHERE extname IN ('pgcrypto', 'vector')"));
		// V1 22개 + V2 관심 카테고리 1개 + V3 온보딩 프로필/관심 기술 2개 = 25개
		assertEquals(25, count("SELECT count(*) FROM pg_tables "
				+ "WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'"));
		assertEquals(14, count("SELECT count(*) FROM categories"));
		assertEquals(15, count("SELECT count(*) FROM technologies"));
		// V2가 users.auth_provider를 NOT NULL로 확정할 수 있는지 (백필 로직) 확인
		assertEquals(0, count("SELECT count(*) FROM users WHERE auth_provider IS NULL"));
	}

	private int count(String sql) {
		Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
		return result == null ? 0 : result;
	}
}
