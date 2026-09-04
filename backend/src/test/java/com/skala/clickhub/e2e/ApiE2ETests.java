package com.skala.clickhub.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skala.clickhub.dto.community.CommunityDtos.CommentCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.PostCreateRequest;
import com.skala.clickhub.dto.community.CommunityDtos.PostUpdateRequest;
import com.skala.clickhub.dto.project.ProjectDtos.CreateRequest;
import com.skala.clickhub.dto.project.ProjectDtos.ScreenshotItem;
import com.skala.clickhub.dto.project.ProjectDtos.TechStackSelection;
import com.skala.clickhub.dto.user.UserDtos.OnboardingUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProfileUpdateRequest;
import com.skala.clickhub.security.jwt.JwtUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 명세 1~9장(4장 랭킹·5장 반응/즐겨찾기/댓글 포함) 전체 엔드포인트를 실제 HTTP로 호출해
 * Method/Path/Status Code가 RESTful 규격대로 동작하는지 검증하는 E2E 테스트.
 *
 * 목적: 발표자료의 "Mock API 엔드포인트 구성 완성도 및 RESTful 규격 준수 여부" 항목을
 * 실측으로 뒷받침하기 위함. 각 호출 결과를 {@link #REPORT}에 모아 build/e2e-report에
 * 마크다운 표로 남긴다. TestRestTemplate 대신 JDK HttpClient를 쓰는 이유는 Spring Boot
 * 4.1의 모듈 분리로 spring-boot-resttestclient가 spring-boot-restclient 없이는
 * RestTemplateBuilder를 못 찾아 컨텍스트 로딩이 깨지기 때문이다(실측 확인) — 새 gradle
 * 의존성을 추가하는 대신 표준 JDK API로 우회했다.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiE2ETests {

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

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private record EndpointCheck(String domain, String method, String path, String scenario,
                                  int expected, int actual, boolean pass) {}

    private record HttpResult(int status, String body) {}

    private static final List<EndpointCheck> REPORT = new CopyOnWriteArrayList<>();

    private SoftAssertions soft;

    private static UUID ownerId;
    private static UUID otherId;
    private static String ownerToken;
    private static String otherToken;
    private static String categorySlug;
    private static String technologySlug;
    private static UUID projectId;
    private static UUID postId;
    private static UUID topCommentId;

    @BeforeAll
    static void seedUsersAndTokens(@Autowired JdbcTemplate jdbcTemplate, @Autowired JwtUtils jwtUtils) {
        ownerId = UUID.randomUUID();
        otherId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO users (id, auth_provider, google_subject, display_name)
                VALUES (?, 'GOOGLE', ?, ?)
                """, ownerId, "e2e-owner-subject", "E2E 오너");
        jdbcTemplate.update("""
                INSERT INTO users (id, auth_provider, google_subject, display_name)
                VALUES (?, 'GOOGLE', ?, ?)
                """, otherId, "e2e-other-subject", "E2E 어나더");
        ownerToken = jwtUtils.generateAccessToken(ownerId.toString());
        otherToken = jwtUtils.generateAccessToken(otherId.toString());
        categorySlug = jdbcTemplate.queryForObject("SELECT slug FROM categories ORDER BY slug LIMIT 1", String.class);
        technologySlug = jdbcTemplate.queryForObject("SELECT slug FROM technologies ORDER BY slug LIMIT 1", String.class);
    }

    @BeforeEach
    void newSoftAssertions() {
        soft = new SoftAssertions();
    }

    @AfterEach
    void verifySoft() {
        soft.assertAll();
    }

    @AfterAll
    static void writeReport() throws IOException {
        Path dir = Path.of("build", "e2e-report");
        Files.createDirectories(dir);
        Path file = dir.resolve("api-e2e-report.md");

        StringBuilder sb = new StringBuilder();
        sb.append("# Click HUB API E2E 검증 리포트\n\n");
        sb.append("생성 시각: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        long total = REPORT.size();
        long passed = REPORT.stream().filter(EndpointCheck::pass).count();
        sb.append(String.format("**전체 %d건 중 %d건 통과 (%d건 실패)**\n\n", total, passed, total - passed));
        sb.append("| 결과 | 도메인 | Method | Path | 시나리오 | 기대 | 실제 |\n");
        sb.append("|---|---|---|---|---|---|---|\n");
        for (EndpointCheck c : REPORT) {
            sb.append(String.format("| %s | %s | %s | `%s` | %s | %d | %d |\n",
                    c.pass() ? "✅" : "❌", c.domain(), c.method(), c.path(), c.scenario(), c.expected(), c.actual()));
        }
        Files.writeString(file, sb.toString());
        System.out.println("=== Click HUB API E2E 리포트 (" + passed + "/" + total + ") ===");
        System.out.println(sb);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpResult call(String method, String path, String token, Object body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url(path)));
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            HttpRequest.BodyPublisher publisher = body == null
                    ? BodyPublishers.noBody()
                    : BodyPublishers.ofString(JSON.writeValueAsString(body));
            if (body != null) {
                builder.header("Content-Type", "application/json");
            }
            builder.method(method, publisher);
            HttpResponse<String> response = HTTP.send(builder.build(), BodyHandlers.ofString());
            return new HttpResult(response.statusCode(), response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP 호출 실패: " + method + " " + path, e);
        }
    }

    private void check(String domain, String method, String path, String scenario,
                        HttpResult response, int expectedStatus) {
        int actual = response.status();
        boolean pass = actual == expectedStatus;
        REPORT.add(new EndpointCheck(domain, method, path, scenario, expectedStatus, actual, pass));
        soft.assertThat(actual)
                .as("[%s] %s %s - %s (응답 바디: %s)", domain, method, path, scenario, response.body())
                .isEqualTo(expectedStatus);
    }

    // ================= 1. 공개/선택 인증 엔드포인트 (토큰 없이 호출) =================

    @Test
    @Order(10)
    void publicAndOptionalAuthEndpoints_workWithoutToken() {
        String domain = "공개/선택인증";
        check(domain, "GET", "/api/v1/ping", "헬스체크",
                call("GET", "/api/v1/ping", null, null), 200);
        check(domain, "GET", "/v1/catalog/categories", "카테고리 목록",
                call("GET", "/v1/catalog/categories", null, null), 200);
        check(domain, "GET", "/v1/catalog/technologies", "기술스택 목록",
                call("GET", "/v1/catalog/technologies", null, null), 200);
        check(domain, "GET", "/v1/feed", "홈 종합 피드",
                call("GET", "/v1/feed", null, null), 200);
        check(domain, "GET", "/v1/search?q=test", "통합 검색",
                call("GET", "/v1/search?q=test", null, null), 200);
        check(domain, "GET", "/v1/rankings/projects", "Top 100 프로젝트",
                call("GET", "/v1/rankings/projects", null, null), 200);
        check(domain, "GET", "/v1/rankings/developers", "개발자 랭킹",
                call("GET", "/v1/rankings/developers", null, null), 200);
        check(domain, "GET", "/v1/tutorials", "튜토리얼 목록",
                call("GET", "/v1/tutorials", null, null), 200);
        // 시드 데이터에 발행된 주간 인사이트가 없어 404가 정상 동작이다.
        check(domain, "GET", "/v1/insights/weekly", "주간 인사이트(미발행 -> 404)",
                call("GET", "/v1/insights/weekly", null, null), 404);
        // 존재하지 않는 프로젝트/제작자 -> 404 (permitAll이지만 서비스 레이어에서 404)
        check(domain, "GET", "/v1/projects/{id}", "존재하지 않는 프로젝트 상세",
                call("GET", "/v1/projects/" + UUID.randomUUID(), null, null), 404);
        check(domain, "GET", "/v1/creators/{id}", "존재하지 않는 제작자",
                call("GET", "/v1/creators/" + UUID.randomUUID(), null, null), 404);
    }

    // ================= 2. 로그인 필요 엔드포인트 -> 토큰 없이 호출 시 전부 401 =================

    @Test
    @Order(20)
    void authenticatedEndpoints_rejectAnonymousAccess() {
        String domain = "인증게이트";
        UUID randomId = UUID.randomUUID();
        Object[][] endpoints = {
                {"POST", "/v1/projects", "프로젝트 생성"},
                {"PATCH", "/v1/projects/" + randomId, "프로젝트 수정"},
                {"POST", "/v1/projects/" + randomId + "/submit", "프로젝트 게시 요청"},
                {"DELETE", "/v1/projects/" + randomId, "프로젝트 삭제"},
                {"PUT", "/v1/projects/" + randomId + "/favorite", "즐겨찾기 토글"},
                {"PUT", "/v1/projects/" + randomId + "/like", "좋아요 토글"},
                {"POST", "/v1/projects/" + randomId + "/comments", "프로젝트 댓글 작성"},
                {"PUT", "/v1/creators/" + randomId + "/subscription", "구독 토글"},
                {"GET", "/v1/community/boards", "게시판 목록"},
                {"GET", "/v1/community/boards/free/posts", "게시글 목록"},
                {"POST", "/v1/community/boards/free/posts", "게시글 작성"},
                {"GET", "/v1/community/posts/" + randomId, "게시글 상세"},
                {"PATCH", "/v1/community/posts/" + randomId, "게시글 수정"},
                {"DELETE", "/v1/community/posts/" + randomId, "게시글 삭제"},
                {"GET", "/v1/community/posts/" + randomId + "/comments", "댓글 목록"},
                {"POST", "/v1/community/posts/" + randomId + "/comments", "댓글 작성"},
                {"GET", "/v1/dashboard/projects/" + randomId, "대시보드 조회"},
                {"GET", "/v1/notifications", "알림 목록"},
                {"PATCH", "/v1/notifications/1/read", "알림 읽음 처리"},
                {"GET", "/v1/users/me", "내 프로필"},
                {"PATCH", "/v1/users/me", "프로필 수정"},
                {"PUT", "/v1/users/me/onboarding", "온보딩 수정"},
                {"GET", "/v1/users/me/projects", "내 프로젝트 목록"},
                {"GET", "/v1/users/me/favorites", "내 즐겨찾기 목록"},
                {"GET", "/v1/users/me/subscriptions", "내 구독 목록"},
        };
        for (Object[] e : endpoints) {
            String method = (String) e[0];
            String path = (String) e[1];
            String scenario = (String) e[2] + " - 토큰 없이 호출";
            check(domain, method, path, scenario, call(method, path, null, null), 401);
        }
    }

    // ================= 3. 사용자 프로필 / 온보딩 =================

    @Test
    @Order(30)
    void userProfileAndOnboardingFlow() {
        String domain = "사용자프로필";
        check(domain, "GET", "/v1/users/me", "내 프로필 조회(owner)",
                call("GET", "/v1/users/me", ownerToken, null), 200);
        check(domain, "PATCH", "/v1/users/me", "프로필 수정(owner)",
                call("PATCH", "/v1/users/me", ownerToken,
                        new ProfileUpdateRequest("E2E 오너(수정됨)", "DARK", true)), 200);
        check(domain, "PUT", "/v1/users/me/onboarding", "온보딩 수정(owner)",
                call("PUT", "/v1/users/me/onboarding", ownerToken,
                        new OnboardingUpdateRequest(List.of("프로젝트 탐색"), List.of(categorySlug), List.of(technologySlug))),
                200);
        check(domain, "GET", "/v1/users/me/projects", "내 프로젝트 목록(owner)",
                call("GET", "/v1/users/me/projects", ownerToken, null), 200);
        check(domain, "GET", "/v1/users/me/favorites", "내 즐겨찾기 목록(owner)",
                call("GET", "/v1/users/me/favorites", ownerToken, null), 200);
        check(domain, "GET", "/v1/users/me/subscriptions", "내 구독 목록(owner)",
                call("GET", "/v1/users/me/subscriptions", ownerToken, null), 200);
    }

    // ================= 4. 프로젝트 라이프사이클 (명세 2장) =================

    @Test
    @Order(40)
    void projectLifecycle_createSubmitOwnershipAndDelete() {
        String domain = "프로젝트(2장)";

        CreateRequest createRequest = new CreateRequest(
                "E2E 테스트 프로젝트",
                "실제 HTTP로 검증하는 프로젝트입니다",
                "https://e2e.example.com",
                "https://github.com/example/e2e",
                "FREE",
                List.of("e2e", "테스트"),
                null,
                List.of(new ScreenshotItem("https://e2e.example.com/1.png", "스크린샷")),
                List.of(new TechStackSelection(technologySlug, null, null)),
                categorySlug
        );
        HttpResult created = call("POST", "/v1/projects", ownerToken, createRequest);
        // 수정 전: 바디는 status=201이라 표시하면서 실제 HTTP 응답은 200이었던 결함을 여기서 확인/고정한다.
        check(domain, "POST", "/v1/projects", "프로젝트 생성(owner) - 실제 HTTP 상태코드",
                created, 201);
        projectId = extractUuid(created.body(), "id");

        check(domain, "GET", "/v1/projects/{id}", "생성 직후 상세조회(owner, DRAFT)",
                call("GET", "/v1/projects/" + projectId, ownerToken, null), 200);
        check(domain, "GET", "/v1/projects/{id}", "DRAFT 상태 상세조회(비소유자 -> 존재 자체를 숨김)",
                call("GET", "/v1/projects/" + projectId, otherToken, null), 404);

        check(domain, "PATCH", "/v1/projects/{id}", "프로젝트 수정(비소유자 -> 403)",
                call("PATCH", "/v1/projects/" + projectId, otherToken, createRequest), 403);
        check(domain, "PATCH", "/v1/projects/{id}", "프로젝트 수정(owner)",
                call("PATCH", "/v1/projects/" + projectId, ownerToken, createRequest), 200);

        check(domain, "POST", "/v1/projects/{id}/submit", "게시 요청(비소유자 -> 403)",
                call("POST", "/v1/projects/" + projectId + "/submit", otherToken, null), 403);
        check(domain, "POST", "/v1/projects/{id}/submit", "게시 요청(owner, DRAFT->PENDING_REVIEW)",
                call("POST", "/v1/projects/" + projectId + "/submit", ownerToken, null), 200);
        check(domain, "POST", "/v1/projects/{id}/submit", "이미 PENDING_REVIEW 상태에서 재요청 -> 409",
                call("POST", "/v1/projects/" + projectId + "/submit", ownerToken, null), 409);

        // 관리자 승인/게시 API가 별도로 없어(이번 스코프 밖), 반응/즐겨찾기/댓글(5장) 검증을 위해
        // 테스트에서 직접 PUBLISHED로 전환한다. 실제 서비스에는 이 상태전이를 만드는 정식 엔드포인트가 없다.
        // validate_project_write() 트리거가 "최근 성공한 URL 검증"을 요구하므로 먼저 기록해준다.
        jdbcTemplate.queryForObject("SELECT record_project_url_validation(?, ?, true, 200, ?)",
                Object.class, projectId, "https://e2e.example.com", "https://e2e.example.com");
        jdbcTemplate.update("UPDATE projects SET status = 'PUBLISHED', published_at = now() WHERE id = ?", projectId);

        check(domain, "GET", "/v1/projects/{id}", "PUBLISHED 이후 상세조회(토큰 없이도 공개)",
                call("GET", "/v1/projects/" + projectId, null, null), 200);
        check(domain, "POST", "/v1/projects/{id}/outbound-clicks", "외부 클릭 기록(익명)",
                call("POST", "/v1/projects/" + projectId + "/outbound-clicks", null, null), 200);

        // ---- 명세 5장: 반응(좋아요)/즐겨찾기/댓글 ----
        check(domain, "PUT", "/v1/projects/{id}/favorite", "즐겨찾기 토글 ON(other)",
                call("PUT", "/v1/projects/" + projectId + "/favorite", otherToken, null), 200);
        check(domain, "PUT", "/v1/projects/{id}/favorite", "즐겨찾기 토글 OFF(other)",
                call("PUT", "/v1/projects/" + projectId + "/favorite", otherToken, null), 200);
        check(domain, "PUT", "/v1/projects/{id}/like", "좋아요 토글 ON(other)",
                call("PUT", "/v1/projects/" + projectId + "/like", otherToken, null), 200);
        check(domain, "GET", "/v1/projects/{id}/comments", "프로젝트 댓글 목록(빈 배열)",
                call("GET", "/v1/projects/" + projectId + "/comments", null, null), 200);
        HttpResult comment = call("POST", "/v1/projects/" + projectId + "/comments", otherToken,
                new com.skala.clickhub.dto.reaction.ReactionDtos.CommentCreateRequest("실제 HTTP로 남긴 댓글"));
        check(domain, "POST", "/v1/projects/{id}/comments", "프로젝트 댓글 작성(other) - 실제 HTTP 상태코드",
                comment, 201);
        check(domain, "GET", "/v1/projects/{id}/comments", "프로젝트 댓글 목록(1건)",
                call("GET", "/v1/projects/" + projectId + "/comments", null, null), 200);

        // ---- 명세 7장: 대시보드 ----
        check(domain, "GET", "/v1/dashboard/projects/{id}", "대시보드 조회(owner)",
                call("GET", "/v1/dashboard/projects/" + projectId, ownerToken, null), 200);
        check(domain, "GET", "/v1/dashboard/projects/{id}", "대시보드 조회(비소유자 -> 403)",
                call("GET", "/v1/dashboard/projects/" + projectId, otherToken, null), 403);

        // ---- 삭제 ----
        check(domain, "DELETE", "/v1/projects/{id}", "프로젝트 삭제(비소유자 -> 403)",
                call("DELETE", "/v1/projects/" + projectId, otherToken, null), 403);
        check(domain, "DELETE", "/v1/projects/{id}", "프로젝트 삭제(owner)",
                call("DELETE", "/v1/projects/" + projectId, ownerToken, null), 200);
        check(domain, "GET", "/v1/projects/{id}", "삭제 후 상세조회 -> 404",
                call("GET", "/v1/projects/" + projectId, null, null), 404);
    }

    // ================= 5. 커뮤니티 게시판 (명세 9장) =================

    @Test
    @Order(50)
    void communityBoardPostAndCommentFlow() {
        String domain = "커뮤니티(9장)";

        check(domain, "GET", "/v1/community/boards", "게시판 목록(owner)",
                call("GET", "/v1/community/boards", ownerToken, null), 200);
        check(domain, "GET", "/v1/community/boards/free/posts", "게시글 목록(owner)",
                call("GET", "/v1/community/boards/free/posts", ownerToken, null), 200);

        HttpResult postRes = call("POST", "/v1/community/boards/free/posts", ownerToken,
                new PostCreateRequest("E2E 테스트 게시글", "실제 HTTP로 검증하는 게시글 본문입니다."));
        check(domain, "POST", "/v1/community/boards/{slug}/posts", "게시글 작성(owner) - 실제 HTTP 상태코드",
                postRes, 201);
        postId = extractUuid(postRes.body(), "id");

        check(domain, "GET", "/v1/community/posts/{id}", "게시글 상세(other, 조회수 1)",
                call("GET", "/v1/community/posts/" + postId, otherToken, null), 200);
        check(domain, "GET", "/v1/community/posts/{id}", "게시글 상세(owner, 조회수 2)",
                call("GET", "/v1/community/posts/" + postId, ownerToken, null), 200);

        check(domain, "PATCH", "/v1/community/posts/{id}", "게시글 수정(작성자 아님 -> 403)",
                call("PATCH", "/v1/community/posts/" + postId, otherToken,
                        new PostUpdateRequest("수정 시도", "실패해야 함")), 403);
        check(domain, "PATCH", "/v1/community/posts/{id}", "게시글 수정(작성자)",
                call("PATCH", "/v1/community/posts/" + postId, ownerToken,
                        new PostUpdateRequest("E2E 테스트 게시글(수정됨)", "본문도 수정했습니다.")), 200);

        HttpResult topComment = call("POST", "/v1/community/posts/" + postId + "/comments", otherToken,
                new CommentCreateRequest("최상위 댓글", null));
        check(domain, "POST", "/v1/community/posts/{id}/comments", "최상위 댓글 작성(other) - 실제 HTTP 상태코드",
                topComment, 201);
        topCommentId = extractUuid(topComment.body(), "id");

        check(domain, "POST", "/v1/community/posts/{id}/comments", "1단계 대댓글 작성(owner)",
                call("POST", "/v1/community/posts/" + postId + "/comments", ownerToken,
                        new CommentCreateRequest("대댓글", topCommentId)), 201);

        check(domain, "GET", "/v1/community/posts/{id}/comments", "댓글 목록(2건)",
                call("GET", "/v1/community/posts/" + postId + "/comments", ownerToken, null), 200);

        check(domain, "DELETE", "/v1/community/posts/{id}", "게시글 삭제(작성자 아님 -> 403)",
                call("DELETE", "/v1/community/posts/" + postId, otherToken, null), 403);
        check(domain, "DELETE", "/v1/community/posts/{id}", "게시글 삭제(작성자)",
                call("DELETE", "/v1/community/posts/" + postId, ownerToken, null), 200);
        check(domain, "GET", "/v1/community/posts/{id}", "삭제 후 상세조회 -> 404",
                call("GET", "/v1/community/posts/" + postId, ownerToken, null), 404);
    }

    // ================= 6. 제작자 구독 (명세 6장) =================

    @Test
    @Order(60)
    void subscriptionToggleAndSelfBlock() {
        String domain = "구독(6장)";
        check(domain, "GET", "/v1/creators/{id}", "제작자 상세(공개, 토큰없음)",
                call("GET", "/v1/creators/" + ownerId, null, null), 200);
        check(domain, "PUT", "/v1/creators/{id}/subscription", "구독 ON(other -> owner)",
                call("PUT", "/v1/creators/" + ownerId + "/subscription", otherToken, null), 200);
        check(domain, "PUT", "/v1/creators/{id}/subscription", "구독 OFF(토글)",
                call("PUT", "/v1/creators/" + ownerId + "/subscription", otherToken, null), 200);
        check(domain, "PUT", "/v1/creators/{id}/subscription", "자기 자신 구독 시도 -> 400",
                call("PUT", "/v1/creators/" + ownerId + "/subscription", ownerToken, null), 400);
    }

    // ================= 7. 알림 (명세 6장) =================

    @Test
    @Order(70)
    void notificationsListAndMarkReadNotFound() {
        String domain = "알림(6장)";
        check(domain, "GET", "/v1/notifications", "알림 목록(owner)",
                call("GET", "/v1/notifications", ownerToken, null), 200);
        check(domain, "PATCH", "/v1/notifications/{id}/read", "존재하지 않는 알림 읽음 처리 -> 404",
                call("PATCH", "/v1/notifications/999999999/read", ownerToken, null), 404);
    }

    // ================= 8. 최종 집계 =================

    @Test
    @Order(999)
    void allChecksSummary() {
        List<EndpointCheck> failed = new ArrayList<>(REPORT.stream().filter(c -> !c.pass()).toList());
        assertThat(failed)
                .as("실패한 엔드포인트 체크 목록: " + failed)
                .isEmpty();
    }

    private UUID extractUuid(String jsonBody, String field) {
        // {"data":{"id":"...","..."}} 형태에서 최초 등장하는 "<field>":"<uuid>" 값만 뽑아낸다.
        String marker = "\"" + field + "\":\"";
        int start = jsonBody.indexOf(marker) + marker.length();
        int end = jsonBody.indexOf('"', start);
        return UUID.fromString(jsonBody.substring(start, end));
    }
}
