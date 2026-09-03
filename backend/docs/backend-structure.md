# Click HUB Backend 구조 요약 — SQL 스키마 동기화

기준: `db/migration/V1__initial_schema.sql` + `db/migration/V2__add_social_login_and_onboarding.sql`
(Flyway, 저장소 루트 `db/migration/` 기준 — 팀 공식 마이그레이션 경로). 로컬에 있던 `ClickHUB/schema (1).sql` 등은
이제 참고용 스냅샷일 뿐 실제 마이그레이션 소스가 아니다. 아래 5차 항목 참고.
스냅샷 문서 — 스키마가 바뀔 때마다 "0. 변경 이력"에 diff를 추가하고 본문을 갱신한다.

## 0. 변경 이력

### 2026-09-03 (6차) — GitHub/Google OAuth2 로그인 + 회원가입 실제 구현
"회원가입/로그인이 되어 있는지 확인하고 안 되어 있으면 구현해달라"는 요청에 따라, 지금까지 전부
`UnsupportedOperationException`이던 인증 흐름을 실제로 동작하게 만들었다. 이번이 이 프로젝트에서
처음으로 `repository`/`service` 레이어에 실제 코드가 들어간 케이스다(그 전까지는 의도적으로 비워둠).

**설계**: Spring Security의 `oauth2Login()`(OAuth2 Client)을 사용해 GitHub(순수 OAuth2)와
Google(OIDC)을 각각 처리한다. "회원가입"은 별도 폼이 없고, 최초 로그인 시 자동으로 `User` 행을
생성하는 "upsert" 패턴이다(있으면 로그인, 없으면 가입). 로그인 성공 후에는 우리 자체 JWT를 발급해
프론트엔드(SPA) 콜백 URL로 302 리다이렉트한다 — 이후 API 호출은 전부 `Authorization: Bearer` 토큰
기반 무상태 인증이고, OAuth2 세션 자체는 로그인 순간에만 관여한다.

**신규 파일**:
- `repository/UserRepository.java` — `findByGithubUserId`, `findByGoogleSubject`
- `service/OAuth2UserSyncService.java` — "있으면 로그인, 없으면 회원가입" upsert 로직. 소프트
  삭제된 계정(`deletedAt`)이면 `ErrorCode.ACCOUNT_DELETED`로 거부
- `security/oauth2/CustomOAuth2UserService.java` — GitHub 프로필(`id`/`login`/`name`/`avatar_url`)
  동기화
- `security/oauth2/CustomOidcUserService.java` — Google OIDC 클레임(`sub`/이름/사진) 동기화
- `security/oauth2/OAuth2AuthenticationSuccessHandler.java` — JWT 발급 + 프론트 콜백 리다이렉트
- `security/oauth2/OAuth2AuthenticationFailureHandler.java` — 실패 시 `?error=` 쿼리로 리다이렉트

**기존 파일 완성**:
- `JwtAuthenticationFilter` — 그동안 남아있던 TODO(실제 사용자 조회 미구현)를 완성. 토큰 subject로
  `UserRepository` 조회 → 소프트 삭제 계정 제외 → principal을 `User.id`(UUID)로 세팅. 컨트롤러는
  `@AuthenticationPrincipal UUID userId`로 바로 받는다
- `AuthController` — `GET /v1/auth/github`(스켈레톤)를 제거하고 `GET /v1/auth/me`로 교체. github/google
  로그인 시작은 이제 컨트롤러가 아니라 Security의 `authorizationEndpoint(baseUri="/v1/auth")`가 직접
  가로채 처리한다(§12의 "GET /v1/auth/github" 규격과 경로가 그대로 일치)
- `ErrorCode` — `ACCOUNT_DELETED`, `USER_NOT_FOUND` 추가
- `build.gradle` — `spring-boot-starter-oauth2-client` 추가

**⚠️ 실제로 앱을 통째로 못 띄울 뻔한 설정 문제 (실측으로 발견·수정)**:
Spring Boot의 `OAuth2ClientAutoConfiguration`은 `spring.security.oauth2.client.registration.{id}.*`
프로퍼티가 하나라도 "존재"하면(값이 빈 문자열이어도) 그 registration을 즉시 검증하고, client-id가
비어 있으면 `"Client id of registration 'github' must not be empty"`로 **부팅 자체를 실패**시킨다.
즉 처음에 `application.properties`에 `${GITHUB_CLIENT_ID:}`처럼 빈 기본값으로 넣어뒀던 접근은,
아직 GitHub/Google OAuth App을 안 만든 로컬/CI 환경에서 앱을 통째로 못 띄우게 만드는 결함이었다.
해결: client-id/secret 4줄을 전부 `application-oauth2.properties`(별도 프로필)로 옮기고,
`SecurityConfig`는 `ObjectProvider<ClientRegistrationRepository>`로 그 빈이 실제로 있을 때만
`oauth2Login()`을 켜도록 수정 — credential이 없는 기본 상태에서는 로그인 기능만 비활성화되고
나머지 앱은 정상 기동한다.

**테스트 인프라 관련 추가 수정**:
- `nodb` 프로필(DataSource/Hibernate 제외)에는 `UserRepository` 빈이 없는데
  `JwtAuthenticationFilter`/`AuthController`가 이제 이걸 필요로 해서, `ClickHubApplicationTests`/
  `PingControllerTests`에 `@MockitoBean private UserRepository userRepository;`를 추가했다.
  (이 Boot/Spring 버전엔 옛 `@MockBean`이 없고 `org.springframework.test.context.bean.override.mockito.MockitoBean`로
  교체됐다는 것도 이번에 확인)

**검증**: `./gradlew clean test`(Docker 기동 상태, `DatabaseMigrationTests` 포함) 5개 테스트 전부
통과. `bootRun`으로 기본 프로필(OAuth2 credential 없음) 상태에서 `UserRepository` 빈이 정상 등록되고
Flyway DB 연결 시도 단계까지 정상 도달하는 것도 재확인. `./gradlew build`(bootJar 포함) 통과.

**아직 안 된 것 / 다음 단계**:
- 실제 GitHub OAuth App, Google Cloud OAuth Client를 만들어 `GITHUB_CLIENT_ID/SECRET`,
  `GOOGLE_CLIENT_ID/SECRET`을 발급받고 `SPRING_PROFILES_ACTIVE`에 `oauth2`를 추가해야 실제 로그인이
  동작한다 — 이건 코드가 아니라 각 프로바이더 콘솔에서 앱 등록을 해야 하는 외부 작업
  (redirect URI는 `{baseUrl}/login/oauth2/code/{github|google}`)
- refresh token 재발급(`/v1/auth/refresh`), 로그아웃 엔드포인트는 아직 없음 — "로그인이 되는지"
  확인 범위에 맞춰 이번엔 만들지 않았다
- `GET /v1/projects/{id}` 등 "인증 선택" API에서 `likedByMe` 같은 개인화 필드를 채우는 로직은
  이제 `@AuthenticationPrincipal UUID userId`로 로그인 여부를 판단할 수 있게 됐지만, 실제 구현은
  아직 컨트롤러가 `UnsupportedOperationException`인 상태 그대로다

### 2026-09-03 (5차) — Flyway/PostgreSQL 인프라 브랜치와 병합 조정 (BE 담당자 확인 대응)
DevOps가 별도 브랜치(`feature/postgresql-integration`, PR #14로 `origin/develop`/`origin/main`에 이미 병합됨)에서
Flyway 기반 PostgreSQL 인프라를 구축했다는 변경내역서(`BE_POSTGRES_CHANGELOG.md`)를 받아 반영했다.
로컬 작업 브랜치는 그 인프라 브랜치가 갈라진 이후 독립적으로 entity/dto/controller 전체를 만들어온 상태라,
`git merge` 대신 원격 브랜치의 실제 파일을 직접 대조해 수동으로 합쳤다(우발적 충돌·미검증 병합을 피하기 위해).

**가장 중요한 발견 — 실제 V1은 3차 시점의 `schema (1).sql`이 아니라 그 이전 버전이었다.**
`db/migration/V1__initial_schema.sql`(이미 배포됨, 되돌릴 수 없음)을 열어보니 2차 시점의 22테이블
버전(커뮤니티 게시판 포함, Google 로그인·온보딩 없음)과 동일했다. 즉 로컬에서 3차 때 진행한
"커뮤니티 제거 + Google 로그인/온보딩 추가"는 Flyway 브랜치가 갈라져 나간 뒤에 로컬에서만 더 진행된
변경이라, 실제 배포된 V1과 어긋나 있었다.

**처리 원칙**: "V1은 수정하지 않고 필요하면 V2로 추가한다"는 담당자 확인사항을 그대로 따랐다.

| 항목 | 조치 |
|---|---|
| 커뮤니티 게시판(`CommunityBoard`/`CommunityPost`/`CommunityPostComment`/`CommunityPostStatus`, DTO, 컨트롤러) | V1에 이미 존재하므로 **복원**. 4차에서 지웠던 게 오히려 V1과 어긋난 것이었음 |
| Google 로그인(`SocialLoginProvider`, `User.authProvider`/`googleSubject`) | 엔티티는 유지하되, 이를 뒷받침하는 DDL을 V1이 아니라 신규 `db/migration/V2__add_social_login_and_onboarding.sql`로 분리 (기존 행은 `GITHUB`로 백필 후 `NOT NULL` 확정) |
| 온보딩 관심 카테고리(`UserOnboardingInterestCategory`) | 동일하게 V2로 분리 |
| `backend/build.gradle` | Flyway(`spring-boot-starter-flyway`, `flyway-database-postgresql`) + Testcontainers 의존성과 `db/migration`을 JAR 리소스에 포함하는 `processResources` 설정 추가. 기존에 있던 springdoc/jackson/postgres/jjwt는 유지 |
| `backend/src/main/resources/application.properties` | DB 접속을 `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USERNAME`/`DB_PASSWORD`/`DB_POOL_MAX_SIZE` 환경변수 계약으로 전환, `spring.flyway.*` 추가, CORS 환경변수명을 팀 컨벤션(`CORS_ALLOWED_ORIGINS`)에 맞춤. 기존에 있던 `stringtype=unspecified`, 명시적 dialect, JWT, springdoc 설정은 유지 |
| `backend/src/main/resources/application-nodb.properties` | 신규 — DB 없이 Context만 띄우는 테스트용 프로필 |
| `backend/Dockerfile`, `backend/.dockerignore` | 신규 — 저장소 루트를 빌드 컨텍스트로 삼아 `db/migration`을 JAR에 포함 |
| `backend/.../api/PingController.java` + 테스트 | 신규 — `/api/v1/ping` 헬스핑, CORS 실동작 검증용 |
| `ClickHubApplicationTests`, `PingControllerTests` | `@ActiveProfiles("nodb")` 적용 |
| `DatabaseMigrationTests` | 신규 — Testcontainers(pgvector/pg16)로 V1+V2 마이그레이션 실제 적용 검증. V2가 테이블을 1개 더 추가하므로 기대 테이블 수를 22 → **23**으로 조정 |

**반영 과정에서 실제로 잡은 버그 3건** (전부 로컬에서 컴파일+부팅 테스트로 실측 확인):
1. `SecurityConfig`의 `SecurityConfig.PUBLIC_ENDPOINTS`가 `/api/auth/**`였는데 실제 컨트롤러는 `/v1/**` — "인증 없음" API까지 401 나던 문제 (이전 감사에서 발견, 이번에 원격 버전에도 동일 버그가 있음을 재확인 — 아직 원격에는 반영 안 됨)
2. `SecurityConfig`가 앱 전역 `ObjectMapper` 빈을 주입받아 에러 응답을 직렬화했는데, `PingControllerTests`처럼 `MockMvc.webAppContextSetup(...)`로 띄우는 MOCK 웹 컨텍스트에서는 그 빈이 없어 `NoSuchBeanDefinitionException` 발생 — 전용 `ObjectMapper` 인스턴스로 교체해 해결
3. `ClickHubApplication`에 붙어있던 `@EnableJpaAuditing`이 `nodb` 프로필(DataSource/Hibernate 제외)에서 `JPA metamodel must not be empty`로 컨텍스트 로딩 자체를 실패시킴 — `@Profile("!nodb")`가 붙은 별도 `JpaAuditingConfig`로 분리해 해결. DevOps 브랜치는 엔티티가 없어서 이 문제가 없었다

**Docker 설치 후 Testcontainers로 추가로 잡은 엔티티↔V1 컬럼 타입 불일치 2건** (`DatabaseMigrationTests`를 실제 pgvector Postgres 컨테이너에 대해 돌려서 `ddl-auto=validate`가 직접 잡아냄):
4. `@Lob private String body/description` — `Project.description`, `ProjectComment.body`, `CommunityPost.body`, `CommunityPostComment.body`, `Tutorial.description` 5곳. `@Lob`이 붙은 `String`은 Hibernate가 기본적으로 PostgreSQL `oid`(large object) 타입으로 매핑하는데, V1의 실제 컬럼은 전부 평범한 `text`다. `@Lob`을 전부 제거해서 해결 — PostgreSQL의 `text`는 길이 제한이 없어 애초에 `@Lob`이 필요 없다.
5. `char(64)` 컬럼(`Project.urlValidationHash`, `ProjectSearchDocument.contentHash`) — `@Column(length = 64)`만으로는 Hibernate가 `varchar(64)`로 검증한다. `columnDefinition`은 DDL 생성에만 쓰이고 검증 로직에는 반영되지 않는다는 걸 실측으로 확인. `@JdbcTypeCode(SqlTypes.CHAR)`를 추가해서 실제 `bpchar` 타입과 맞춰 해결.

**검증 (Docker 설치 후 실측)**: Docker Desktop을 설치·기동한 뒤 `./gradlew clean test`로 전체 5개 테스트(`ClickHubApplicationTests` 1개, `PingControllerTests` 3개, `DatabaseMigrationTests` 1개)를 전부 실행해 **BUILD SUCCESSFUL** 확인. `DatabaseMigrationTests`는 `pgvector/pgvector:pg16` Testcontainer에 V1+V2 마이그레이션을 실제로 적용한 뒤 — flyway_schema_history에 두 버전 모두 성공 기록, pgcrypto/vector 확장 존재, 23개 테이블, categories 14건·technologies 15건 seed, `users.auth_provider IS NULL` 0건(V2 백필 정상) — 전부 통과. 즉 엔티티 레이어가 실제 배포 스키마와 완전히 일치함을 코드 리뷰가 아니라 실행으로 증명했다.

**남은 조치**:
- 원격 `origin/develop`의 `SecurityConfig`는 아직 `/api/auth/**` 버그를 갖고 있음 — 이 로컬 브랜치를 PR로 올릴 때 함께 고쳐질 것
- `V2__add_social_login_and_onboarding.sql`은 로컬에서만 존재 — 실제 배포 전 DB 담당자 리뷰 필요
- CI 파이프라인에도 Docker(Testcontainers) 실행 환경이 있는지 확인 — 없다면 `DatabaseMigrationTests`가 CI에서 스킵/실패할 수 있음

---

### 2026-09-03 (4차) — schema (1).sql: 커뮤니티 게시판 롤백 + Google 로그인/온보딩 추가
`schema (1).sql`을 직전 `schema.sql`(22개 테이블, 3차 시점)과 diff. 19개 테이블로 돌아감 (22 → 19). 세 가지 변경이 한 번에 들어왔다.

| 변경 | 내용 | 코드 반영 |
|---|---|---|
| 커뮤니티 게시판 **삭제** | `community_boards`/`community_posts`/`community_post_comments` 테이블, 관련 인덱스·트리거·seed data 전부 제거. 2차 변경 이력에서 추가했던 것이 다시 빠짐 | `CommunityBoard`/`CommunityPost`/`CommunityPostComment`/`CommunityPostStatus` 엔티티, `CommunityDtos`, `CommunityController` **삭제** |
| Google 소셜 로그인 **추가** | `social_login_provider` enum(`GOOGLE`/`GITHUB`) 신설. `users`에 `auth_provider`(NOT NULL) + `google_subject`(unique, nullable) 컬럼과 CHECK(`GOOGLE`이면 subject 필수) 추가. 3차 항목에서 flag했던 SQL 공백이 채워짐 | `SocialLoginProvider` enum 신규, `User`에 `authProvider`/`googleSubject` 필드 추가 |
| 온보딩 관심 카테고리 **추가** | `user_onboarding_interest_categories(user_id, category_id, created_at)` 신규, PK 복합키, `categories` FK는 `ON DELETE RESTRICT`. 3차 항목에서 flag했던 SQL 공백이 채워짐 | `UserOnboardingInterestCategory` 엔티티 + `entity/id/UserOnboardingInterestCategoryId` 복합키 추가 |

**⚠️ SQL ↔ ERD 불일치 (처음 발견됨)**: `erd (1).drawio`는 파일명만 바뀌었을 뿐 MD5가 이전 `erd.drawio`와 동일한 22-테이블(커뮤니티 포함) 버전 그대로다. 즉 지금 `schema (1).sql`(19 테이블, Google/온보딩 포함)과 ERD(22 테이블, 커뮤니티 포함, Google/온보딩 없음)가 서로 다른 스냅샷을 가리키고 있다. **코드는 지시대로 SQL을 기준으로 반영했지만, ERD 갱신이 필요하다.**

**보류한 항목 (스펙 없어 임의 추가 안 함)**:
- `AuthController`에 Google 로그인 진입점을 추가하지 않았다 — `§12 API 명세`는 여전히 `GET /v1/auth/github`만 정의하고 Google용 엔드포인트가 없다. `AuthDtos.OAuthStartResponse`도 provider 파라미터 없이 그대로 뒀다.
- 온보딩 선호 카테고리를 저장/조회하는 컨트롤러·DTO는 추가하지 않았다 — 화면 흐름(온보딩 설문 UI가 언제 몇 개를 보내는지 등)이 아직 스펙에 없어 엔티티만 SQL에 맞춰 두고 API 설계는 보류.

---

### 2026-09-03 (3차) — 기획서 PDF 갱신본(v1.1) 확인, 코드 변경 없음
`Click_HUB_서비스_기획서 (1) - Google 文档.pdf`를 이전 docx 버전과 비교. **엔티티/DTO/컨트롤러 변경 없음** — 바뀐 내용이 서비스/알고리즘 레이어이거나, SQL이 아직 없어 코드로 임의 반영하지 않은 항목이기 때문.

| 변경 내용 | 판단 |
|---|---|
| §2 핵심 사용자 과업에 "최초 로그인 후 관심 카테고리 온보딩 설문(건너뛰기 가능)" 추가 | **SQL 공백.** `users`/신규 테이블 어디에도 온보딩 선호 카테고리를 저장할 곳이 없다. 코드에서 임의로 필드를 추가하지 않았다 — `schema.sql`에 예: `user_category_preferences(user_id, category_id)` 같은 테이블이 먼저 추가되어야 함 |
| §3 포함 목록에 "소셜 로그인(Google, GitHub)" 추가 (이전엔 GitHub 로그인만 언급) | **SQL 공백.** `users` 테이블은 `github_user_id/github_login/github_connected_at`만 있고 Google 관련 컬럼(예: `google_sub`, `email`)이 없다. 프로젝트 **등록**은 여전히 GitHub 연결 필수(정책 문장 동일)라 트리거 로직엔 영향 없지만, 로그인 자체를 Google로 하는 사용자를 어떻게 식별할지는 SQL에 반영되어야 코드화 가능 |
| §3 포함 목록에 "커뮤니티 게시판(공지·자유·정보공유·질문답변)" 명시 | **2차 변경 이력에서 이미 반영됨.** 이전에 flag했던 "기획서 제외 vs SQL 포함" 상충이 이 개정으로 해소됨(위 2차 항목 취소선 참고). 다만 게시판 이름이 doc은 "공지·자유·정보공유·질문답변" 4종인데 `schema.sql` seed data는 `free/qna/showcase` 3종(프로젝트 홍보 포함, 공지·정보공유 없음)이라 **내용은 여전히 다름** — `community_boards`는 고정 enum이 아니라 row 기반 카탈로그라 코드 구조는 그대로 두면 되고, seed INSERT 문구만 SQL 담당자가 doc과 맞출지 확인 필요 |
| §5 검색설계 전면 재작성 (`search_score` 가중치·항목 변경, LLM을 "선택적 보강"으로 재정의) | 서비스/랭킹 로직 설명 변경 — `search_requests.parsed_filters`(jsonb)로 이미 opaque 저장 중이라 엔티티 변경 불필요. 추후 검색 서비스 구현 시 새 가중치 참고 |
| §6 추천설계 전면 재작성 (Matrix Factorization + SGD 명시, `feed_score` 가중치 변경) | 서비스 로직 변경, 저장 스키마 영향 없음. 코드 변경 불필요 |
| §13 AI 계약 JSON 재구성 (`concepts/expanded_terms`로 변경, 13.3 "현재 관심 문맥 분석" 신설) | `/internal/ai/*` 엔드포인트는 애초 스코프에서 제외했고, 결과는 `dashboard_ai_analyses`/`weekly_insights`의 jsonb 컬럼에 그대로 들어가므로 엔티티 변경 불필요 |
| §14 분석 계약 JSON 재구성 (`observations` 배열, 댓글 분석에 `evidence_comment_ids` 추가) | 동일하게 jsonb로 저장되므로 엔티티 변경 불필요 |
| §12 API 명세, §15 데이터 모델 표, 표준 이벤트 목록 | **문구까지 이전 버전과 동일, 변경 없음.** 기존에 flag했던 이벤트 enum 불일치(`search_submitted`/`notification_created`가 doc에는 있으나 `schema.sql`의 `interaction_event_type`에는 없고, 대신 `project_published`가 SQL에만 있음)는 이번에도 그대로 남아있음 — 미해결 상태 유지 |

**결론**: 이번 개정으로 실제 저장 구조에 영향을 주는 확정 요구사항은 없다. 온보딩 선호도 저장과 Google 로그인 식별자, 이 두 가지는 `schema.sql`이 갱신되어야 엔티티에 반영할 수 있으므로 지금 임의로 컬럼을 추가하지 않았다 — SQL 담당자 확인 후 다음 동기화에서 처리.

---

### 2026-09-03 (2차) — community_boards 도메인 추가
`schema.sql`/`erd.drawio`가 19개 → 22개 테이블로 변경됨. 추가된 3개 테이블은 모두 새로운 **커뮤니티 게시판** 도메인:

| 테이블 | 내용 |
|---|---|
| `community_boards` | 게시판 목록 (slug, name, display_order, is_active) |
| `community_posts` | 게시글. `author_id`는 `ON DELETE SET NULL` — 작성자가 탈퇴해도 글은 남고, 화면에는 "알 수 없는 사용자"로 표시해야 한다(SQL 주석 명시) |
| `community_post_comments` | 대댓글 트리. `FOREIGN KEY (post_id, parent_id) REFERENCES community_post_comments(post_id, id)`로 "부모 댓글은 반드시 같은 게시글 소속"을 DB가 강제 |

**SQL ↔ ERD 교차검증**: 두 파일 모두 19→22 동일하게 갱신되어 있고, 신규 FK 5개(`community_boards→community_posts`, `community_posts→community_post_comments`, `users→community_posts`, `users→community_post_comments`, `community_post_comments→community_post_comments` 자기참조)도 양쪽에 동일하게 존재. **불일치 없음.**

**~~⚠️ 기획서와의 상충~~ → 2026-09-03 (3차)에서 해소됨**: 당시 `Click_HUB_서비스_기획서.docx`(구버전) 3장 "제외" 열에 "커뮤니티 게시판"이 있어 SQL과 상충한다고 플래그했었다. 아래 3차 항목 참고 — PDF로 갱신된 v1.1 최신본은 "포함"으로 명시해 해소되었다.

**코드 반영**:
- `entity`: `CommunityBoard`, `CommunityPost`, `CommunityPostComment` + `CommunityPostStatus`(PUBLISHED/HIDDEN/DELETED, varchar+CHECK라 네이티브 enum 아님) 추가
- `CommunityPostComment.parent`는 단순 자기참조 `@ManyToOne`으로만 매핑 — SQL의 복합 FK(같은 게시글 소속 강제)는 JPA로 자연스럽게 표현 불가해 매핑에서 빠짐(DB 제약이 대신 보증). TODO로 명시.
- `dto/community/CommunityDtos.java`, `controller/CommunityController.java` 신규 추가 (게시판 목록/글 목록·상세·작성/댓글 목록·작성) — §12에는 없던 도메인이라 엔드포인트는 기존 REST 컨벤션에 맞춰 새로 추정함(`/v1/community/**`), 실제 프론트 화면 설계가 나오면 재확인 필요.

---

## 1. 검증 결과: SQL vs ERD (최초 동기화 시점)

| 항목 | 결과 |
|---|---|
| 테이블 개수/이름 | 최초 동기화 시점 `schema.sql` 19개 테이블 = `erd.drawio` 19개 테이블. 완전 일치 (그 후 로컬에서만 19→22→19로 오갔으나, 5차에서 실제 배포된 Flyway V1이 22테이블 커뮤니티-포함 버전임을 확인하고 그쪽으로 확정. 현재 코드 기준 V1 22개 + V2 1개 = 23개. ERD 갱신 여부는 미확인 상태로 남음) |
| 뷰 | `project_top100_7d`, `developer_top100_7d` 2개 모두 양쪽에 존재 |
| FK 관계(52개 edge) | `owner_id`, `primary_category_id`, `project_id`, `user_id` 등 실제 `REFERENCES` 제약과 전부 일치 |
| 파생/비동기 관계 | `anonymous_sessions → interaction_events/search_requests`(actor_key 폴리모픽 참조), `interaction_events/project_daily_metrics → weekly_insights/dashboard_ai_analyses`(배치 집계)는 점선으로 표시되어 있고, SQL에도 실제 FK 제약이 아닌 논리적 참조로 존재 — 표시 방식과 실제 구조가 일치 |

**결론: 불일치 없음.** 두 산출물이 서로 다른 시점에 따로 작성된 게 아니라 같은 설계를 그대로 반영하고 있어, 코드는 `schema.sql`을 그대로 따르면 된다.

## 2. 패키지별 역할과 이번 변경 사항

### `entity`
현재 23개 테이블(V1 22개 + V2 1개, 변경 이력 5차 참고)을 1:1로 매핑. 이전 스켈레톤(MySQL·Long PK 가정)은 전면 폐기하고 다시 작성했다.

- **PK 전략**: UUID 테이블은 `@GeneratedValue(strategy = GenerationType.UUID)`, bigint identity 테이블(`notifications`, `notification_outbox`, `interaction_events`, `search_requests`)은 `Long` + `GenerationType.IDENTITY`.
- **복합키 5종** (`project_technologies`, `project_reactions`, `creator_subscriptions`, `project_daily_metrics`, `creator_daily_metrics`): `entity/id/` 패키지에 `@Embeddable` ID 클래스를 두고 엔티티에서 `@EmbeddedId` + `@ManyToOne @MapsId`로 연결.
- **PostgreSQL 전용 타입**: `text[]`/`uuid[]` → `String[]`/`UUID[]` + `@JdbcTypeCode(SqlTypes.ARRAY)`, `jsonb` → `JsonNode` + `@JdbcTypeCode(SqlTypes.JSON)`. 둘 다 Hibernate 6 내장 지원이라 추가 의존성 없이 매핑했다.
- **삭제된 엔티티**: `Tag`, `ProjectTag`, `ProjectMedia`, `MediaType` — SQL은 태그를 `projects.tags`(text[]), 스크린샷을 `projects.screenshots`(jsonb)로 프로젝트 테이블에 직접 저장하고, 별도 조인 테이블을 두지 않는다.
- **교체된 엔티티**: `ProjectTechStack`(자유 텍스트) → `ProjectTechnology`(technologies 카탈로그 참조, 복합키). `Reaction` + 별도 즐겨찾기 개념 → `ProjectReaction` 하나로 통합 (`reaction_type`: `LIKE`/`FAVORITE`).
- **신규 엔티티** (이전에 없던 것): `Technology`, `AnonymousSession`, `SearchRequest`, `NotificationOutbox`, `CreatorDailyMetric`, `ProjectSearchDocument`, `DashboardAiAnalysis`, `UserOnboardingInterestCategory`, `CommunityBoard`/`CommunityPost`/`CommunityPostComment` (2차 추가 → 4차 삭제 → 5차에서 V1에 실제로 존재함을 확인하고 복원, 변경 이력 참고).
- **User 확장**: `authProvider`(`SocialLoginProvider`: GOOGLE/GITHUB), `googleSubject` 추가 (4차).
- **알려진 한계(의도적으로 미완성 처리, TODO)**:
  - `ProjectSearchDocument.embedding` (`vector(1536)`) — Hibernate 기본 매핑 대상이 아니라 필드 자체를 뺐다. `com.pgvector:pgvector` 의존성 + 커스텀 UserType 추가 후 복원 필요.
  - `DashboardAiAnalysis.sourcePeriod` (`daterange`) — 임시로 `String`에 매핑. 실제 range 연산이 필요해지면 커스텀 UserType 필요.
  - `project_top100_7d`/`developer_top100_7d` 뷰 — `@Entity`로 만들지 않았다. 랭킹 조회는 값 객체(`RankingDtos`)로 결과만 반환하는 네이티브 쿼리 기반 조회를 권장.

### `dto`
§12 API 도메인당 1파일 구조는 유지하고, 필드를 엔티티 변경에 맞춰 동기화했다.

- ID 타입: `Long` → `UUID` (거의 전부). `NotificationDtos`만 `Long` 유지 (`notifications.id`가 bigint).
- `ProjectDtos`: `url` → `siteUrl`, `pricing` 필드 추가, `tags`를 `List<String>`으로 직접 노출(조인 없음), `screenshots`를 `List<ScreenshotItem>(url, alt)`로 구조화, 기술 스택을 카탈로그 참조 기반(`TechStackSelection`/`TechStackItem`)으로 분리.
- `DashboardDtos`/`InsightDtos`/`TutorialDtos`: 필드명을 실제 컬럼명(`uniqueVisitors`, `weekStart`, `estimatedMinutes`, `categorySlugs`, `technologySlugs`)에 맞춤.

### `controller`
로직은 여전히 미구현(스켈레톤) 상태 유지. `@PathVariable` 타입만 엔티티 PK와 맞춰 수정했다.

- `UUID`로 변경: `ProjectController`, `ReactionController`, `FavoriteController`, `SubscribeController`, `DashboardController`
- `Long` 유지: `NotificationController` (`notifications.id`가 bigint이기 때문)
- `CommunityController`도 동일하게 2차 추가 → 4차 삭제 → 5차 복원 (변경 이력 참고)

### `common`
`ApiResponse<T>`, `CursorPageResponse<T>` — 이번 SQL 동기화로 인한 변경 없음. 응답 포맷은 엔티티 구조와 독립적이라 영향받지 않는다.

### `exception`
`ErrorCode`/`BusinessException`/`GlobalExceptionHandler` — 로직 변경 없음. 다만 SQL 트리거(`validate_project_write`, `record_project_url_validation` 등)가 상태 전이 규칙을 DB 레벨에서 강제하고 있으므로, 서비스 레이어 구현 시 트리거가 던지는 `RAISE EXCEPTION`을 `SQLException` → `ErrorCode`로 변환하는 매핑을 추가해야 한다 (아직 미구현, TODO).

## 3. SQL ↔ Java 타입 매핑

| SQL 타입 | Java 타입 | 비고 |
|---|---|---|
| `uuid` | `UUID` | `GenerationType.UUID` |
| `bigint GENERATED ... AS IDENTITY` | `Long` | `GenerationType.IDENTITY` |
| `timestamptz` | `OffsetDateTime` | `BaseTimeEntity` 포함 전체 통일 |
| `text[]` | `String[]` | `@JdbcTypeCode(SqlTypes.ARRAY)` |
| `uuid[]` | `UUID[]` | 동일 |
| `jsonb` | `JsonNode` | `@JdbcTypeCode(SqlTypes.JSON)`, Jackson 기반 |
| `vector(1536)` | (미매핑) | pgvector 의존성 필요 — 후속 작업 |
| `daterange` | `String` (임시) | 커스텀 UserType 필요 — 후속 작업 |
| `tsvector` | (미매핑) | DB `STORED GENERATED`, 앱에서 쓰지 않음 |

## 4. 빌드 설정 변경

- `build.gradle`: MySQL 드라이버 제거, `org.postgresql:postgresql` 추가. `com.fasterxml.jackson.core:jackson-databind` 명시적 추가 (엔티티의 `JsonNode` 컴파일에 필요).
- `application.properties`: `datasource.url`을 `jdbc:postgresql://localhost:5432/clickhub`로 변경, `hibernate.dialect=PostgreSQLDialect` 명시.

## 5. 후속 작업 (TODO)

1. `com.pgvector:pgvector` 의존성 추가 후 `ProjectSearchDocument.embedding` 필드 복원.
2. `daterange` 커스텀 Hibernate `UserType` 작성 후 `DashboardAiAnalysis.sourcePeriod` 타입 교체.
3. `project_top100_7d` / `developer_top100_7d` 뷰 조회용 네이티브 쿼리 Repository 작성.
4. `repository`/`service` 레이어 구현 — 이번 단계에서도 의도적으로 비워둠(비즈니스 로직 제외 범위).
5. SQL 트리거 예외 → `ErrorCode` 매핑 규칙 정의.
