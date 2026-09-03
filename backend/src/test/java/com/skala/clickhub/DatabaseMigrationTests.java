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
	void appliesSchemaMigrationsWithExtensionsTablesAndSeedData() {
		assertEquals(1, count("SELECT count(*) FROM flyway_schema_history WHERE version = '1' AND success"));
		assertEquals(1, count("SELECT count(*) FROM flyway_schema_history WHERE version = '2' AND success"));
		assertEquals(2, count("SELECT count(*) FROM pg_extension WHERE extname IN ('pgcrypto', 'vector')"));
		assertEquals(25, count("SELECT count(*) FROM pg_tables "
				+ "WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'"));
		assertEquals(14, count("SELECT count(*) FROM categories"));
		assertEquals(15, count("SELECT count(*) FROM technologies"));
		assertEquals(5, count("SELECT count(*) FROM community_boards"));
	}

	private int count(String sql) {
		Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
		return result == null ? 0 : result;
	}
}
