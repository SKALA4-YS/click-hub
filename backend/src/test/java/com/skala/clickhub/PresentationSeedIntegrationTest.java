package com.skala.clickhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("demo")
@SpringBootTest
class PresentationSeedIntegrationTest {

    private static final DockerImageName PGVECTOR_IMAGE = DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres");

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE)
            .withDatabaseName("clickhub")
            .withUsername("clickhub")
            .withPassword("test-only");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "?stringtype=unspecified");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.connect-retries", () -> "0");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Test
    void loadsCompletePresentationDataset() {
        assertThat(count("SELECT count(*) FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(8);
        assertThat(count("SELECT count(*) FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%' AND status = 'PUBLISHED'"))
                .isEqualTo(12);
        assertThat(count("SELECT count(*) FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%' AND thumbnail_url IS NOT NULL"))
                .isEqualTo(12);
        assertThat(count("SELECT count(*) FROM project_daily_metrics WHERE project_id::text LIKE '20000000-0000-0000-0000-0000000000%' AND metric_date >= current_date - 6"))
                .isEqualTo(84);
        assertThat(count("SELECT count(*) FROM project_top100_7d WHERE project_id::text LIKE '20000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(12);
        assertThat(count("SELECT count(*) FROM developer_top100_7d WHERE creator_id::text LIKE '10000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(8);
        assertThat(count("SELECT count(*) FROM project_comments WHERE id::text LIKE '21000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(18);
        assertThat(count("SELECT count(*) FROM community_posts WHERE id::text LIKE '50000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(8);
        assertThat(count("SELECT count(*) FROM community_post_comments WHERE id::text LIKE '51000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(12);
        assertThat(count("SELECT count(*) FROM tutorials WHERE id::text LIKE '30000000-0000-0000-0000-0000000000%' AND is_published"))
                .isEqualTo(6);
    }

    @Test
    void keepsRankingOrderAndPresentationImagesDeterministic() {
        assertThat(text("SELECT title FROM project_top100_7d ORDER BY score DESC, project_id LIMIT 1"))
                .isEqualTo("Click HUB");
        assertThat(text("SELECT display_name FROM developer_top100_7d ORDER BY score DESC, creator_id LIMIT 1"))
                .isEqualTo("Flow Maker");
        assertThat(text("SELECT thumbnail_url FROM projects WHERE id = '20000000-0000-0000-0000-000000000001'"))
                .contains("images.unsplash.com/photo-1498050108023-c5249f4df085");
    }

    @Test
    void canRunAgainWithoutRemovingARealUser() {
        jdbcTemplate.update("""
                INSERT INTO users (id, display_name, auth_provider, google_subject)
                VALUES ('90000000-0000-0000-0000-000000000001', '실제 가입자', 'GOOGLE', 'real-user-test')
                """);

        new ResourceDatabasePopulator(
                new ClassPathResource("db/demo/R__presentation_content.sql"))
                .execute(dataSource);

        assertThat(count("SELECT count(*) FROM users WHERE id = '90000000-0000-0000-0000-000000000001'"))
                .isOne();
        assertThat(count("SELECT count(*) FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(12);
        assertThat(count("SELECT count(*) FROM project_comments WHERE id::text LIKE '21000000-0000-0000-0000-0000000000%'"))
                .isEqualTo(18);
    }

    private int count(String sql) {
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return result == null ? 0 : result;
    }

    private String text(String sql) {
        return jdbcTemplate.queryForObject(sql, String.class);
    }
}
