# Click HUB Backend — 코드 레벨 상세 아키텍처 설명서

이 문서는 `common`, `config`, `security`, `exception`, `entity`, `dto`, `controller` 패키지에 있는
**모든 자바 파일을 하나씩** 훑으면서, "이 파일이 왜 존재하는지 / 무엇을 하는지 / 다른 레이어와 왜 분리했는지 /
누구에게(프론트엔드·DB 담당자·서비스 레이어 구현자) 어떤 도움을 주는지"를 설명한다.

전체 배경은 `backend-structure.md`(SQL 스키마 동기화 변경 이력)에 정리돼 있고, 이 문서는 그 결과로 만들어진
**개별 소스 파일 하나하나의 사용설명서**라고 보면 된다. 아직 어떤 파일도 실제 DB 연동·비즈니스 로직은 담고 있지
않다 — 전부 "이 모양대로 채우면 된다"를 보여주는 뼈대(스켈레톤)다.

읽는 순서는 실제 패키지 트리 순서를 그대로 따른다:

```
com.skala.clickhub
├── ClickHubApplication.java
├── common/response/
├── config/
├── security/jwt/
├── exception/
├── entity/            (+ entity/id/)
├── dto/
└── controller/
```

---

## 0. 애플리케이션 진입점

### `src/main/java/com/skala/clickhub/ClickHubApplication.java`

**역할 및 구현 기능**
스프링 부트 애플리케이션의 `main()` 메서드가 있는 부트스트랩 클래스다. `@SpringBootApplication`으로
컴포넌트 스캔·자동 설정·스프링 빈 등록을 한 번에 켜고, `@EnableJpaAuditing`으로 JPA 감사(Auditing) 기능을
활성화한다.

**설계 목적 및 아키텍처적 의의**
`@EnableJpaAuditing`이 없으면 `entity/BaseTimeEntity.java`의 `@CreatedDate`/`@LastModifiedDate`가
동작하지 않는다 — 즉 `createdAt`/`updatedAt`이 계속 `null`로 저장된다. 이 한 줄이 엔티티 계층 전체의
"자동 타임스탬프" 기능을 켜는 스위치이기 때문에, 프로젝트 최상단 진입점에 명시적으로 박아 두었다. SQL
쪽에서는 `set_updated_at()` 트리거가 `updated_at`을 갱신해 주지만, `created_at`/`updated_at`을
**애플리케이션에서 최초 INSERT 시점에 채워 넣는 역할**은 이 어노테이션이 담당한다(트리거는 UPDATE만 갱신).

**팀 협업 및 확장 관점의 의미**
서비스 레이어를 구현하는 사람은 이 클래스를 건드릴 일이 거의 없다. 다만 나중에 스케줄러(주간 인사이트 집계,
`refresh_project_daily_metrics()` 같은 배치 실행)를 붙이게 되면 `@EnableScheduling`을, 비동기 처리가
필요하면 `@EnableAsync`를 이 클래스에 추가하게 될 가능성이 높다 — "전역 스위치는 여기에 모인다"는 걸
팀원들이 알아두면 된다.

---

## 1. `common/response` — API 공통 응답 규격

프론트엔드(Vue.js)와 백엔드가 주고받는 **모든 JSON 응답의 겉모양을 통일**하는 패키지다. 여기 있는 두 클래스는
컨트롤러 11개 전체가 예외 없이 반환 타입으로 사용한다.

### `src/main/java/com/skala/clickhub/common/response/ApiResponse.java`

**역할 및 구현 기능**
제네릭 클래스 `ApiResponse<T>`로, `success`(성공 여부) · `status`(HTTP 상태 코드 정수) · `message`(사람이
읽는 메시지) · `data`(실제 응답 데이터, `T`)의 4개 필드를 갖는다. 생성자는 `private`로 막아두고
`ApiResponse.success(data)` / `ApiResponse.error(status, message)` 같은 정적 팩토리 메서드로만 만들 수
있게 했다. `@JsonInclude(NON_NULL)`이 붙어 있어서 `data`가 없는 에러 응답에서는 `data` 필드 자체가 JSON에
안 찍힌다(불필요한 `"data": null` 노이즈 제거).

**설계 목적 및 아키텍처적 의의**
컨트롤러가 `Project`를 반환하든 `List<Tutorial>`을 반환하든, 프론트엔드 입장에서는 **항상 같은 모양의
봉투(envelope)**를 받는다. 그래서 프론트는 "성공했는지, 상태 코드가 뭔지"를 매번 다른 방식으로 파싱할
필요 없이 `response.success`, `response.data`만 보면 된다. 이건 SQL 스키마와는 직접적인 관계가 없는,
순수하게 **HTTP 계약(contract) 레이어**의 설계 결정이다 — 그래서 엔티티가 통째로 바뀌어도(이번에 실제로
`schema.sql`이 세 번 바뀌었다) 이 클래스는 단 한 줄도 수정되지 않았다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자는 Axios/Fetch 응답 타입을 `ApiResponse<T>` 하나로 통일해서 정의할 수 있다(TypeScript
제네릭으로 그대로 옮기면 된다). e2e 테스트를 짜는 사람도 "성공 케이스는 `success:true`, 실패 케이스는
`success:false` + `status`"라는 단일 규칙만 검증하면 되므로 테스트 코드가 훨씬 단순해진다. 서비스 레이어를
구현할 사람은 절대 이 클래스에 필드를 추가하면 안 된다(모든 화면이 이 모양에 의존하고 있기 때문) — 확장이
필요하면 `data` 안의 DTO를 확장해야 한다.

### `src/main/java/com/skala/clickhub/common/response/CursorPageResponse.java`

**역할 및 구현 기능**
`items`(현재 페이지의 데이터 목록) · `nextCursor`(다음 페이지를 요청할 때 쓸 커서 문자열) ·
`hasNext`(다음 페이지 존재 여부)로 구성된 레코드다. `of(items, nextCursor)` 정적 메서드를 쓰면
`hasNext`는 `nextCursor != null` 여부로 자동 계산된다.

**설계 목적 및 아키텍처적 의의**
기획서 §12의 `GET /v1/feed?cursor=`, `GET /v1/search?...`처럼 **무한 스크롤 화면**은 전통적인
페이지 번호(`page=1,2,3...`) 방식이 아니라 커서 기반 페이지네이션을 쓴다. 이 값 객체를
`ApiResponse<CursorPageResponse<T>>`처럼 이중으로 감싸서 쓰면, "봉투 안에 또 페이지네이션 봉투가 있는"
일관된 구조가 된다. `ApiResponse<T>`와 마찬가지로 특정 엔티티에 종속되지 않는 **범용 값 객체**라서 Feed든
Search든 앞으로 생길 어떤 목록 API든 재사용 가능하다.

**팀 협업 및 확장 관점의 의미**
프론트엔드는 "스크롤이 바닥에 닿으면 `nextCursor`를 다음 요청 파라미터로 보낸다"는 로직을 컴포넌트 하나에
캡슐화해 모든 무한 스크롤 화면(홈 피드, 검색 결과, Top 100 등)에 재사용할 수 있다. 서비스 레이어 구현자는
"커서 값을 어떻게 인코딩할지"(예: 마지막 아이템의 `id`+`createdAt`을 Base64로 인코딩)만 정하면 되고,
API 계약 자체는 이미 확정돼 있어 프론트와 별도 협의가 필요 없다.

---

## 2. `config` — 스프링 시큐리티 필터 체인과 CORS

### `src/main/java/com/skala/clickhub/config/SecurityConfig.java`

**역할 및 구현 기능**
스프링 시큐리티 6.x의 람다 DSL 스타일로 `SecurityFilterChain` 빈을 정의한다. 내부적으로 CSRF를
비활성화하고(REST API + JWT 조합에서는 세션 쿠키 기반 CSRF 공격 자체가 성립하지 않으므로), 세션을
`STATELESS`로 설정하고(서버가 세션 상태를 들고 있지 않음 = JWT 방식의 전제조건), `/api/auth/**`와
`/api/public/**` 경로만 인증 없이 통과시키고 나머지는 전부 인증을 요구한다. 마지막으로
`JwtAuthenticationFilter`를 스프링 시큐리티 기본 필터인 `UsernamePasswordAuthenticationFilter` **앞에**
끼워 넣어서, 모든 요청이 시큐리티의 기본 인증 로직에 도달하기 전에 먼저 JWT 검증을 거치게 만든다. 그 외에
`PasswordEncoder`(BCrypt)와 `AuthenticationManager` 빈도 여기서 등록한다.

**설계 목적 및 아키텍처적 의의**
"어떤 URL이 인증 없이 열려 있고, 어떤 URL이 로그인을 요구하는지"는 애플리케이션 전체의 보안 정책을 결정하는
**단일 진실 공급원(single source of truth)**이어야 한다. 그래서 이 규칙을 컨트롤러 하나하나에 흩어 두지
않고 이 파일 한 곳에 모았다. `PUBLIC_ENDPOINTS` 배열은 사실 기획서 §12 API 표의 "인증" 열(없음/선택/로그인/
소유자/서비스)을 코드로 옮긴 것이다 — 문서와 코드가 어긋나지 않도록, 새 엔드포인트를 추가할 때마다 이
배열도 같이 검토해야 한다는 뜻이다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자 입장에서는 "이 API를 호출할 때 `Authorization` 헤더가 꼭 필요한가?"를 판단하는
근거가 이 파일이다. e2e 테스트를 짜는 사람은 "인증 없이 접근했을 때 401이 나와야 하는 엔드포인트 목록"을
이 배열의 여집합으로 뽑아낼 수 있다. 서비스 레이어 구현자에게는 아직 빠져 있는 것도 알려준다 — 현재
`§12`의 "소유자"(예: 대시보드는 프로젝트 소유자만) 같은 **행(row) 단위 권한**은 이 필터 체인만으로는
못 막는다. URL 단위 인증만 여기서 걸러지고, "이 프로젝트가 진짜 내 것인가"는 서비스 레이어에서
`ErrorCode.NOT_PROJECT_OWNER`를 던지는 방식으로 별도 구현해야 한다는 걸 이 파일의 한계로 이해하면 된다.

### `src/main/java/com/skala/clickhub/config/CorsConfig.java`

**역할 및 구현 기능**
`CorsConfigurationSource` 빈 하나를 만든다. 허용 출처(origin)는 하드코딩하지 않고
`application.properties`의 `clickhub.cors.allowed-origins` 값을 주입받아 쓰고, 기본값은
`http://localhost:5173`(Vite 개발 서버 기본 포트)이다. 허용 메서드는 GET/POST/PUT/PATCH/DELETE/OPTIONS,
자격 증명(쿠키/인증 헤더) 포함 요청도 허용(`allowCredentials(true)`), `Authorization` 헤더는 프론트가
읽을 수 있도록 노출시킨다.

**설계 목적 및 아키텍처적 의의**
`SecurityConfig`가 아니라 별도 파일로 뺀 이유는, **CORS는 "인증/인가" 문제가 아니라 "브라우저의 출처 정책"
문제**이기 때문이다. 개념적으로 다른 관심사를 한 클래스에 몰아넣으면 나중에 "왜 CORS가 안 되지"를 디버깅할
때 시큐리티 로직까지 같이 읽어야 하는 부담이 생긴다. `SecurityConfig`는 이 빈을 `.cors(cors ->
cors.configurationSource(corsConfigurationSource))` 한 줄로 가져다 쓰기만 한다 — 즉 두 파일은
"설정을 정의하는 쪽"과 "그 설정을 소비하는 쪽"으로 명확히 분리돼 있다.

**팀 협업 및 확장 관점의 의미**
Vue.js 프론트엔드 개발자가 로컬에서 개발 서버를 돌릴 때(`localhost:5173`) 백엔드 API를 호출하면서 브라우저
콘솔에 CORS 에러가 뜨는 상황을 원천적으로 막아주는 파일이다. 배포 환경에서 프론트 도메인이 정해지면
`CLICKHUB_CORS_ORIGINS` 환경변수 하나만 바꾸면 되고 코드 재배포가 필요 없다 — DevOps/배포 담당자에게는
"이 환경변수만 알면 CORS는 알아서 된다"는 명확한 가이드가 된다.

---

## 3. `security/jwt` — 토큰 발급·검증 골격

### `src/main/java/com/skala/clickhub/security/jwt/JwtUtils.java`

**역할 및 구현 기능**
JWT(JSON Web Token)를 생성하고 검증하는 유틸리티 컴포넌트다. `jjwt` 0.12.x API를 사용해서
`generateAccessToken(subject)` / `generateRefreshToken(subject)`로 토큰을 만들고,
`getSubject(token)` / `getExpiration(token)` / `extractClaim(token, resolver)`로 토큰 내부의 Claims를
꺼내고, `isTokenValid(token)` / `isTokenExpired(token)`으로 유효성·만료 여부를 검사한다. 서명 키(`secret`)와
액세스/리프레시 토큰 만료 시간은 전부 `application.properties`에서 주입받는다.

**설계 목적 및 아키텍처적 의의**
JWT 관련 로직(서명, 파싱, 만료 검사)을 컨트롤러나 필터에 직접 쓰지 않고 별도 컴포넌트로 뺀 이유는, 이
로직이 **"인증이 필요한 모든 곳에서 재사용되는 순수 유틸리티"**이기 때문이다.
`JwtAuthenticationFilter`가 요청을 가로챌 때도 이걸 쓰고, 나중에 `AuthController`가 실제 로그인 처리를
구현할 때 토큰을 "발급"하는 데도 같은 클래스를 쓰게 된다. SQL 관점에서는 이 클래스가 다루는 `subject`
문자열이 결국 `User.id`(UUID)가 될 텐데, JWT 자체는 DB와 무관하게 동작하도록 설계돼 있다 — 이게 바로
"무상태(stateless) 인증"의 핵심이다: 서버가 세션 저장소를 뒤지지 않고 토큰 자체의 서명만으로 신뢰한다.

**팀 협업 및 확장 관점의 의미**
서비스 레이어 구현자가 로그인 로직을 짤 때 "토큰을 어떻게 만드는지" 매번 새로 고민할 필요 없이
`jwtUtils.generateAccessToken(user.getId().toString())`처럼 가져다 쓰기만 하면 된다. 보안 담당자
입장에서는 `clickhub.jwt.secret`의 기본값이 로컬 개발용 placeholder라는 걸 코드 주석으로 명시해 뒀으므로,
운영 배포 전 체크리스트에 "이 값을 진짜 256비트 랜덤 값으로 바꿨는가"가 자연스럽게 포함된다.

### `src/main/java/com/skala/clickhub/security/jwt/JwtAuthenticationFilter.java`

**역할 및 구현 기능**
`OncePerRequestFilter`를 상속한 서블릿 필터다. 매 요청마다 `Authorization: Bearer {token}` 헤더에서
토큰을 꺼내고, `JwtUtils.isTokenValid()`로 검증에 성공하면 `UsernamePasswordAuthenticationToken`을 만들어
`SecurityContextHolder`에 등록한다 — 이렇게 등록된 인증 정보가 있어야 `SecurityConfig`의
`.anyRequest().authenticated()` 규칙을 통과할 수 있다.

**설계 목적 및 아키텍처적 의의**
`JwtUtils`(토큰을 다루는 방법)와 `JwtAuthenticationFilter`(토큰을 "언제, 어떻게 요청 파이프라인에
적용할지")를 분리한 것은 전형적인 **관심사 분리**다. 지금 이 필터는 토큰이 유효하면 `subject`(사용자
ID 문자열)만 인증 주체로 세팅하고 **권한(role) 목록은 빈 리스트로 남겨둔다** — 코드에 `TODO` 주석으로
명시했듯, "이 사용자가 실제로 DB에 존재하는지, role이 무엇인지"를 조회하는 `UserDetailsService` 연동은
아직 하지 않았다. User 엔티티와 리포지토리가 실제로 쓰이기 시작하는 다음 단계에서 반드시 채워야 하는
자리다.

**팀 협업 및 확장 관점의 의미**
이 필터는 "인증됐다"와 "권한이 있다"를 구분해서 이해하게 해주는 좋은 교육 자료이기도 하다 — 지금 상태로는
토큰만 유효하면 어떤 사용자든 인증된 것으로 취급되므로, 서비스 레이어 구현자는 나중에 반드시 실제 사용자
조회 로직을 이 필터(또는 별도 `AuthenticationProvider`)에 연결해야 한다는 걸 알고 다음 작업을 시작할 수
있다. QA/테스트 담당자에게는 "지금 스켈레톤 상태에서는 토큰 형식만 맞으면 어떤 API든 통과한다"는 현재
한계를 명확히 알려주는 파일이기도 하다.

---

## 4. `exception` — 전역 예외 처리 인프라

### `src/main/java/com/skala/clickhub/exception/ErrorCode.java`

**역할 및 구현 기능**
비즈니스 예외 하나하나를 `enum` 상수로 정의한다. 각 상수는 `HttpStatus`(응답 상태 코드) ·
`code`(예: `PROJECT_002`, 로그·프론트 분기용 문자열 코드) · `message`(사람이 읽는 한국어 메시지) 3개
필드를 갖는다. 지금 정의된 항목은 `GITHUB_LOGIN_REQUIRED`, `PROJECT_URL_UNREACHABLE`,
`NOT_PROJECT_OWNER`, `ALREADY_REACTED`, `CANNOT_SUBSCRIBE_SELF`, `NOTIFICATION_NOT_FOUND` 등이고, 각각
기획서 3장(등록·구독 정책)이나 12장(인증 요구사항)의 특정 문장에서 그대로 뽑아낸 것이다.

**설계 목적 및 아키텍처적 의의**
"에러 메시지를 어디에 하드코딩할 것인가"는 흔히 간과되는 설계 결정이다. 문자열을 서비스 코드 곳곳에
흩어 두면 나중에 메시지를 바꾸거나 다국어를 지원할 때 전부 찾아 고쳐야 한다. 이 enum은 **모든 비즈니스
예외의 카탈로그**로서, "어떤 실패 케이스들이 이미 식별되어 있는지"를 한눈에 보여주는 문서 역할도 한다.
SQL 쪽에서 `validate_project_write()` 같은 트리거가 `RAISE EXCEPTION`으로 던지는 것과 개념적으로
대응되는 지점들이 있다(예: DB의 "게시 전 주 카테고리가 필요합니다" 트리거 예외 ↔ 앱 레벨의
`PROJECT_URL_UNREACHABLE`류 사전 검증) — 즉 같은 규칙을 앱 레벨에서 먼저 걸러서 사용자에게 더 친절한
에러를 보여주고, DB 트리거는 최후의 방어선 역할을 하게 된다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자는 `code` 필드(`"PROJECT_002"` 같은 문자열)로 분기 처리를 할 수 있다 — 예를 들어
`PROJECT_URL_UNREACHABLE`이 오면 "URL을 다시 확인해 주세요" 같은 특정 UI를 띄우는 식이다. 새로운 비즈니스
규칙이 추가될 때마다(예: SQL 트리거 예외를 앱 레벨로 옮길 때) 이 enum에 항목만 추가하면 되므로, 팀
전체가 "예외는 여기 모아둔다"는 약속을 지키기만 하면 코드가 흩어지지 않는다.

### `src/main/java/com/skala/clickhub/exception/BusinessException.java`

**역할 및 구현 기능**
`RuntimeException`을 상속한 커스텀 예외 클래스. 생성자에서 `ErrorCode`를 하나 받아 보관하고, 예외
메시지는 그 `ErrorCode`의 메시지를 그대로 사용한다. 서비스 레이어에서
`throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);`처럼 던지는 용도다.

**설계 목적 및 아키텍처적 의의**
자바 표준 예외(`IllegalArgumentException` 등)를 그대로 쓰지 않고 감싸는 이유는, **"이건 우리가 의도적으로
설계한 비즈니스 실패다"**라는 것을 타입으로 명확히 구분하기 위해서다. `GlobalExceptionHandler`는 이
타입만 보고 "아, 이건 예상된 실패니까 `ErrorCode`에 정의된 상태 코드와 메시지를 그대로 응답에 실으면
되겠다"고 판단할 수 있다. 반대로 이 타입이 아닌 예외(NPE, DB 커넥션 오류 등)는 "예상 못 한 시스템 오류"로
분류되어 다르게 처리된다 — 이 구분이 다음 파일(`GlobalExceptionHandler`)의 핵심 로직이다.

**팀 협업 및 확장 관점의 의미**
서비스 레이어를 구현할 사람에게 "실패를 표현하는 방법은 이것 하나로 통일한다"는 규칙을 강제한다. 이
클래스가 없으면 개발자마다 `null` 반환, `Optional.empty()`, 각자 다른 커스텀 예외 등 제각각의 방식으로
실패를 표현하게 되는데, `BusinessException` 하나로 창구를 통일해 두면 `GlobalExceptionHandler`가 모든
실패를 예외 없이 `ApiResponse` 포맷으로 변환해 줄 수 있다.

### `src/main/java/com/skala/clickhub/exception/GlobalExceptionHandler.java`

**역할 및 구현 기능**
`@RestControllerAdvice`가 붙은 전역 예외 처리기다. 세 종류의 예외를 각각 다르게 처리한다.
① `BusinessException` → `ErrorCode`에 담긴 상태 코드·메시지 그대로 `ApiResponse.error(...)`로 변환.
② `MethodArgumentNotValidException`(`@Valid` 검증 실패) → 첫 번째 필드 에러 메시지를 뽑아 400
Bad Request로 응답. ③ 그 외 모든 `Exception`(예상 못 한 시스템 오류) → 500 Internal Server Error로
응답하되, 상세 내용은 로그로만 남기고 사용자에게는 일반화된 메시지만 노출한다(내부 구현 정보 유출 방지).

**설계 목적 및 아키텍처적 의의**
`@RestControllerAdvice`는 스프링이 **모든 컨트롤러에 걸쳐 예외 처리를 가로채는** 관점 지향적(AOP) 메커니즘을
제공한다. 이게 없으면 11개 컨트롤러 각각에 `try-catch`를 반복해서 써야 하고, 그러다 보면 어떤 컨트롤러는
`ApiResponse` 형식을 안 지키는 실수가 생기기 마련이다. 이 클래스 하나가 **"어떤 예외가 나든 응답 형식은
`common/response/ApiResponse`로 통일된다"**는 것을 프로젝트 전체에 걸쳐 보장한다. 즉 `common`,
`exception` 두 패키지는 서로 짝을 이루는 관계다 — 하나는 "성공/실패의 겉모양"을, 다른 하나는 "실패가
발생했을 때 그 겉모양으로 변환하는 규칙"을 담당한다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자는 어떤 API를 호출하든 실패 시 **항상 같은 JSON 구조**(`success:false`, `status`,
`message`)를 받는다는 걸 신뢰하고 에러 처리 컴포넌트 하나만 만들면 된다. e2e/통합 테스트를 짜는 사람은
"이 요청은 400이 나와야 한다, 메시지는 무엇이어야 한다"를 검증하는 공통 헬퍼를 만들 수 있다. 서비스
레이어 구현자는 이 파일을 건드릴 필요 없이 그냥 적절한 곳에서 `BusinessException`만 던지면, 나머지
변환은 이 클래스가 알아서 해준다는 걸 알고 개발 속도를 낼 수 있다.

---

## 5. `entity` — 데이터베이스 테이블 매핑 (JPA 엔티티)

`entity` 패키지의 모든 클래스는 `schema.sql`(현재 19개 테이블 + 2개 뷰) 기준으로 1:1로 매핑되어 있다.
`entity/id` 하위 패키지에는 복합키(Composite Key) 테이블을 위한 `@Embeddable` 클래스들이 따로 모여 있다.
아래에서는 SQL의 도메인 구분(스키마 파일의 주석 "1. Identity domain", "2. Catalog domain" ...)을 그대로
따라가며 설명한다 — 이렇게 묶어서 읽으면 "왜 이 테이블들이 서로 연관되어 있는지"가 훨씬 잘 보인다.

### 5-0. 공통 베이스

#### `src/main/java/com/skala/clickhub/entity/BaseTimeEntity.java`

**역할 및 구현 기능**
`createdAt`, `updatedAt` 두 개의 `OffsetDateTime` 필드만 가진 `@MappedSuperclass`(직접 테이블이 되지
않고, 상속한 자식 엔티티의 컬럼으로만 존재하는 추상 클래스)다. `@EntityListeners(AuditingEntityListener.class)`가
붙어 있어서, 엔티티가 처음 저장될 때 `createdAt`을, 수정될 때마다 `updatedAt`을 스프링 데이터 JPA가
자동으로 채워 넣는다.

**설계 목적 및 아키텍처적 의의**
`created_at`/`updated_at` 컬럼은 `users`, `projects`, `project_comments`, `tutorials` 등 여러 테이블에
반복해서 나타난다. 이걸 매번 각 엔티티에 똑같이 두 줄씩 복사해 넣으면(보일러플레이트) 실수로 타입을
다르게 쓰거나 어노테이션을 빠뜨릴 위험이 생긴다. 상속 구조로 한 번만 정의해 두면 "타임스탬프 규칙은
여기서 하나로 관리된다"가 보장된다. 중요한 기술적 포인트 하나: `schema.sql`의 모든 시각 컬럼은 PostgreSQL의
`timestamptz`(타임존 포함 시각) 타입이기 때문에, 자바 쪽 필드 타입을 (타임존 정보가 없는) `LocalDateTime`이
아니라 **`OffsetDateTime`으로 통일**했다 — 이 선택 하나가 프로젝트 전체 66개 엔티티의 시각 필드 타입을
결정한다.

**팀 협업 및 확장 관점의 의미**
`created_at`/`updated_at`이 있는 테이블을 새로 추가하는 팀원은 그냥 `extends BaseTimeEntity`만 붙이면
되고, 반대로 (`categories`, `weekly_insights`처럼) 컬럼 조합이 다른 테이블은 이 클래스를 상속하면 안 된다는
규칙도 알 수 있다 — 실제로 이 프로젝트에서는 `Category`, `Technology`, `NotificationOutbox`, `Notification`,
`ProjectReaction`, `CreatorSubscription`, `ProjectDailyMetric`, `CreatorDailyMetric`,
`ProjectSearchDocument`, `DashboardAiAnalysis`, `WeeklyInsight`처럼 SQL 컬럼 구성이 다른 테이블은 이
클래스를 상속하지 않고 필요한 시각 필드만 개별적으로 선언했다. DB 담당자가 마이그레이션에서 특정 테이블에
`updated_at`을 추가한다면, 해당 엔티티가 `BaseTimeEntity`를 상속하는지부터 확인하면 된다.

### 5-1. Identity 도메인 — 계정/설정 (`users`)

#### `src/main/java/com/skala/clickhub/entity/UserRole.java`

**역할 및 구현 기능** `USER`, `ADMIN` 두 값만 갖는 단순 enum. `users.role` 컬럼(PostgreSQL
`user_role` enum 타입)에 대응한다.

**설계 목적 및 아키텍처적 의의** PostgreSQL의 네이티브 enum 타입을 자바에서도 타입 안전하게 다루기 위해
문자열이 아니라 enum으로 선언했다. `User` 엔티티에서 `@Enumerated(EnumType.STRING)`으로 매핑되므로,
DB에는 `'USER'`/`'ADMIN'` 문자열 그대로 저장된다(숫자 인덱스 저장 방식인 `ORDINAL`은 컬럼 순서가 바뀌면
데이터가 깨지므로 사용하지 않는다).

**팀 협업 및 확장 관점의 의미** 권한 분기 로직을 짤 서비스 레이어 개발자는 문자열 `"ADMIN"`을 매직 스트링으로
쓰는 대신 `UserRole.ADMIN`을 쓰게 되어 오타로 인한 버그를 컴파일 시점에 방지할 수 있다.

#### `src/main/java/com/skala/clickhub/entity/Theme.java`

**역할 및 구현 기능** `LIGHT`, `DARK`, `SYSTEM` 3개 값. 기획서 10.2장 "다크모드" 설정 옵션과 정확히
일치한다.

**설계 목적 및 아키텍처적 의의** `users.theme` 컬럼과 매핑되며, 로그인 사용자의 테마 설정을 서버에
영속화하기 위한 값이다(비로그인 사용자는 브라우저 로컬 저장소를 쓴다 — 기획서에 명시된 정책).

**팀 협업 및 확장 관점의 의미** 프론트엔드 다크모드 토글 컴포넌트가 로그인 시에는 이 값을 서버에서
읽어와 초기 테마를 설정하고, 변경 시 PATCH 요청으로 갱신하면 된다는 걸 알 수 있다(현재 이 값을 바꾸는
API는 아직 스캐폴딩되지 않았다 — 후속 작업 대상).

#### `src/main/java/com/skala/clickhub/entity/SocialLoginProvider.java`

**역할 및 구현 기능** `GOOGLE`, `GITHUB` 2개 값. `schema (1).sql`에서 새로 추가된
`social_login_provider` enum 타입에 대응한다.

**설계 목적 및 아키텍처적 의의** 이 파일은 SQL 스키마가 **세 번째로 개정되면서** 새로 생겼다 — 최초
버전은 GitHub 로그인만 가정했지만, 기획서 v1.1이 "소셜 로그인(Google, GitHub)"으로 범위를 넓혔고, 그에
맞춰 SQL도 `users.auth_provider` 컬럼과 `google_subject` 컬럼을 추가했다. 즉 이 짧은 enum 파일 하나가
"인증 방식이 GitHub 단일 체계에서 멀티 프로바이더 체계로 바뀌었다"는 아키텍처 전환을 대표한다.

**팀 협업 및 확장 관점의 의미** 서비스 레이어에서 로그인 처리를 구현할 사람은 `authProvider` 값에 따라
Google OAuth 플로우와 GitHub OAuth 플로우를 분기해야 한다는 신호를 이 enum에서 얻는다. 다만 현재
`§12 API 명세`와 `AuthController`에는 아직 Google 로그인 엔드포인트가 없다 — 데이터 모델은 준비됐지만
API 계약은 아직 뒤따라오지 않은 상태라는 걸 팀이 인지하고 있어야 한다.

#### `src/main/java/com/skala/clickhub/entity/User.java`

**역할 및 구현 기능**
`users` 테이블 매핑. PK는 `UUID`(`GenerationType.UUID`로 애플리케이션이 직접 생성).
`authProvider`(필수), `googleSubject`(Google 로그인 시에만 값 존재), `githubUserId`/`githubLogin`/
`githubConnectedAt`(GitHub 연결 정보, 셋 다 null이거나 셋 다 값이 있어야 한다는 게 SQL CHECK 제약),
`displayName`(필수), `avatarUrl`(선택), `role`, `theme`, `newProjectNotifications`(구독 알림 켜짐/꺼짐),
`deletedAt`(소프트 삭제 시각)로 구성된다. `BaseTimeEntity`를 상속해 `createdAt`/`updatedAt`도 갖는다.

**설계 목적 및 아키텍처적 의의**
이 프로젝트에서 가장 많은 다른 엔티티들이 참조하는 **허브 엔티티**다(Project, Comment, Reaction, Subscription,
Notification, InteractionEvent 등 절반 이상의 테이블이 `users.id`를 외래키로 갖는다). `@Builder` +
`@NoArgsConstructor(PROTECTED)` 조합은 JPA가 요구하는 기본 생성자는 열어 두되(리플렉션으로 프록시를
만들 때 필요), 애플리케이션 코드에서는 오직 빌더를 통해서만 완전한 상태의 `User` 객체를 만들 수 있게
강제한다 — 필드를 하나씩 setter로 채우다가 필수값을 빠뜨리는 실수를 원천 차단하는 설계다. 소프트
삭제(`deletedAt`)를 쓰는 이유는, 실제로 DB에서 행을 지워버리면 `project_comments.author_id`,
`community_posts.author_id`(과거 버전) 같은 다른 테이블의 참조가 깨지거나 무의미해지기 때문이다 — 대신
"삭제된 사람"으로 표시만 하고 데이터는 남긴다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자는 로그인한 사용자 정보를 렌더링할 때 "GitHub로 가입했는가, Google로 가입했는가"를
`authProvider`로 분기해서 프로필 화면에 다르게 보여줄 수 있다(예: GitHub 연동 배지 표시 여부). 댓글/게시글
목록을 그리는 화면은 `author`가 `null`이거나 `author.deletedAt`이 채워져 있으면 "알 수 없는 사용자"로
표시해야 한다는 규칙을 지켜야 한다 — 이건 SQL 스키마 주석에 명시된 요구사항이라 프론트-백엔드 양쪽 모두가
알아야 하는 계약이다.

### 5-2. Catalog 도메인 — 카테고리·기술 스택·프로젝트

#### `src/main/java/com/skala/clickhub/entity/Category.java`

**역할 및 구현 기능** `categories` 테이블 매핑. `id`(UUID), `name`(고유), `slug`(고유, URL-safe
문자열), `createdAt`만 가진다. **`BaseTimeEntity`를 상속하지 않는다** — `updated_at` 컬럼이 SQL에
없기 때문이다.

**설계 목적 및 아키텍처적 의의** `createdAt` 필드에 `@CreationTimestamp`(Hibernate 전용 어노테이션)를
직접 붙여서, `BaseTimeEntity`의 스프링 데이터 감사 기능 없이도 최초 저장 시각이 자동으로 채워지게 했다.
이건 "테이블마다 정확히 SQL에 있는 컬럼만 존재해야 한다"는 원칙을 지키기 위한 선택이다 — 만약
`BaseTimeEntity`를 상속했다면 존재하지도 않는 `updated_at` 컬�럼에 값을 쓰려다 SQL 오류가 났을 것이다.

**팀 협업 및 확장 관점의 의미** AI가 프로젝트를 분류할 때(기획서 3장 "AI 기반 분류 정책") 참조하는 허용
카테고리 목록이 바로 이 테이블의 행들이다. `slug`는 검색 화면 URL이나 `project_search_documents.category_slug`
필터링에 쓰인다 — 프론트엔드가 카테고리 필터 UI를 만들 때 표시용 문자열은 `name`, API 쿼리 파라미터는
`slug`를 쓰면 된다는 관례를 여기서 세운 것이다.

#### `src/main/java/com/skala/clickhub/entity/Technology.java`

**역할 및 구현 기능** `technologies` 테이블 매핑. `id`, `name`, `slug`(둘 다 고유), `defaultGroup`
(`TechGroup` enum), `createdAt`. 이것도 `Category`와 같은 이유로 `BaseTimeEntity`를 상속하지 않는다.

**설계 목적 및 아키텍처적 의의** 이 테이블의 존재 자체가 이전 스켈레톤과의 가장 큰 구조적 차이다 —
처음에는 "Vue.js", "Spring Boot" 같은 기술명을 프로젝트마다 자유 텍스트로 저장했지만(`ProjectTechStack.name`
String 필드), 실제 SQL은 **기술 스택을 공용 카탈로그로 정규화**했다. 즉 오타("Vue.js" vs "vuejs" vs
"VueJS")로 인한 데이터 불일치를 막고, "Vue.js를 쓰는 프로젝트가 몇 개인지" 같은 집계 쿼리를 정확하게
만들 수 있게 됐다. `defaultGroup`은 제작자가 프로젝트에 기술을 추가할 때 그룹(Frontend/Backend/...)을
자동으로 미리 채워주는 기본값으로 쓰인다(실제 프로젝트별 그룹은 `ProjectTechnology.id.technologyGroup`에
따로 저장되어 있어, 같은 기술이라도 프로젝트에 따라 다른 그룹으로 쓰일 수 있다).

**팀 협업 및 확장 관점의 의미** 프론트엔드의 "기술 스택 선택" UI(프로젝트 등록 화면)는 자유 텍스트 입력이
아니라 이 테이블을 조회해서 만든 **자동완성/선택 목록**이어야 한다. 새로운 기술을 카탈로그에 추가하는
일은 사용자가 아니라 운영자(seed data 관리자)의 몫이라는 것도 이 구조에서 알 수 있다.

#### `src/main/java/com/skala/clickhub/entity/TechGroup.java`

**역할 및 구현 기능** `FRONTEND`, `BACKEND`, `DATABASE`, `INFRA_DEPLOY`, `AI_DATA` 5개 값. 기획서
10.3장의 기술 스택 그룹 표(Frontend/Backend/Database/Infra-Deploy/AI-Data)와 정확히 일치한다.

**설계 목적 및 아키텍처적 의의** `Technology.defaultGroup`과 `ProjectTechnology`의 복합키 일부
(`ProjectTechnologyId.technologyGroup`) 양쪽에서 재사용되는 공유 타입이다.

**팀 협업 및 확장 관점의 의미** 프로젝트 상세 화면에서 기술 스택을 그룹별로 묶어 보여줄 때(예: "Backend:
Spring Boot, PostgreSQL"), 이 enum 값을 그대로 화면 섹션 제목으로 매핑하면 된다.

#### `src/main/java/com/skala/clickhub/entity/PricingType.java`

**역할 및 구현 기능** `FREE`, `PAID`, `FREEMIUM`, `UNKNOWN` 4개 값. `projects.pricing` 컬럼(기본값
`UNKNOWN`)에 대응한다.

**설계 목적 및 아키텍처적 의의** 기획서에는 명시적으로 등장하지 않지만 SQL 스키마가 실무적으로 필요하다고
판단해 추가한 필드다 — 검색 필터(`§5 검색 설계`의 "메타데이터 필터링" 단계)에서 "무료 서비스만 보기" 같은
조건에 쓰인다. `project_search_documents.pricing`에도 그대로 복제되어(비정규화) 검색 성능을 높인다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 검색/필터 UI에 "가격" 필터 옵션(무료/유료/부분유료)을 넣을 때
이 4개 값을 그대로 옵션으로 쓰면 된다.

#### `src/main/java/com/skala/clickhub/entity/ProjectStatus.java`

**역할 및 구현 기능** `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, `ARCHIVED`, `REJECTED` 5개 값.
`projects.status` 컬럼에 대응하며, SQL 트리거 `validate_project_write()`가 이 값들 사이의 상태 전이
규칙(예: `DRAFT → PENDING_REVIEW → PUBLISHED`만 허용, `PUBLISHED`에서 바로 `DRAFT`로 못 감)을 DB
레벨에서 강제한다.

**설계 목적 및 아키텍처적 의의** 이 enum은 단순한 값 목록이 아니라 **"프로젝트 등록 승인 워크플로우"**
전체를 함축한다. 최초 스켈레톤에서는 `PENDING`, `PUBLISHED`, `SUSPENDED` 3단계뿐이었지만, 실제 SQL은
5단계 승인 프로세스(초안 → 검토 대기 → 게시 → 보관/반려)를 가진다는 걸 이 파일 하나의 변경 이력이 보여준다.

**팀 협업 및 확장 관점의 의미** 서비스 레이어에서 상태를 변경하는 로직을 짤 사람은 **애플리케이션
레벨에서 전이 규칙을 다시 구현할 필요가 없다** — DB 트리거가 이미 잘못된 전이를 거부하고 `RAISE EXCEPTION`을
던지므로, 서비스 코드는 그 예외를 잡아 `ErrorCode`로 변환하는 역할만 하면 된다(현재 `GlobalExceptionHandler`에는
아직 이 매핑이 없다 — `backend-structure.md`의 후속 작업 목록에 있는 TODO다). 프론트엔드는 프로젝트
카드/상세 화면에서 `PENDING_REVIEW`(심사중), `REJECTED`(반려, 사유는 `rejectionReason`) 같은 상태별
배지를 보여주는 UI를 준비해야 한다.

#### `src/main/java/com/skala/clickhub/entity/Project.java`

**역할 및 구현 기능**
`projects` 테이블 매핑 — 이 프로젝트에서 **가장 필드가 많고 가장 중요한 엔티티**다. `owner`(User FK,
필수), `primaryCategory`(Category FK, 선택), `title`/`description`(필수), `siteUrl`(필수, 실제 서비스
URL), `repositoryUrl`(선택, GitHub 저장소), `pricing`, `tags`(PostgreSQL `text[]` 배열을 자바
`String[]`로 직접 매핑), `thumbnailUrl`, `screenshots`(PostgreSQL `jsonb`를 Jackson의 `JsonNode`로
매핑, `[{"url":"...","alt":"..."}]` 형태), `status`, `rejectionReason`, `publishedAt`/`archivedAt`,
그리고 URL 검증 상태 6개 필드(`urlCheckedAt`, `urlIsReachable`, `urlHttpStatus`, `urlFinalUrl`,
`urlErrorCode`, `urlValidationHash`)로 구성된다.

**설계 목적 및 아키텍처적 의의**
이 엔티티는 **PostgreSQL 전용 타입을 자바로 다루는 방법을 보여주는 대표 사례**다. `tags`는
`@JdbcTypeCode(SqlTypes.ARRAY)`로, `screenshots`는 `@JdbcTypeCode(SqlTypes.JSON)`으로 매핑했는데, 둘
다 Hibernate 6이 기본 제공하는 기능이라 별도 서드파티 라이브러리(`hypersistence-utils` 등) 없이
동작한다. URL 검증 상태 필드들은 코드 주석에 명시했듯 **애플리케이션이 직접 쓰면 안 되는 필드**다 —
`record_project_url_validation()`이라는 SQL 함수가 URL 접속 가능 여부를 주기적으로 체크해서 채워 넣고,
`validate_project_write()` 트리거는 "최근 7일 이내에 성공적으로 검증된 URL"만 `PUBLISHED` 상태 전환을
허용한다. 즉 이 필드들은 자바 엔티티에 있지만 **소유권은 DB 트리거/배치 잡**에 있는, "읽기는 자유롭지만
쓰기는 특정 배치 프로세스만 해야 하는" 필드라는 걸 팀 전체가 알아야 한다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자에게는 `screenshots`가 단순 URL 배열이 아니라 `{url, alt}` 객체 배열이라는 걸 알려준다
(접근성을 위한 alt 텍스트가 구조적으로 강제되어 있다). DB 담당자에게는 "URL 재검증 배치를 언제, 얼마나
자주 돌릴지"를 설계해야 한다는 힌트를 준다(`urlCheckedAt`이 7일 지나면 게시 상태 유지가 안 됨). 서비스
레이어 구현자는 프로젝트 등록/수정 API를 구현할 때 URL 검증 필드에는 절대 값을 쓰지 말고, 대신
`record_project_url_validation()` SQL 함수를 호출하는 별도 배치/서비스를 통해서만 갱신되도록 설계해야
한다는 제약을 지켜야 한다.

#### `src/main/java/com/skala/clickhub/entity/ProjectTechnology.java` (+ `entity/id/ProjectTechnologyId.java`)

**역할 및 구현 기능**
`project_technologies` 조인 테이블 매핑. 대리키(surrogate id)가 없고 `(project_id, technology_id,
technology_group)` 3개 컬럼의 조합이 그대로 기본키다. 그래서 `ProjectTechnologyId`라는 `@Embeddable`
클래스를 따로 만들어 이 3개 값을 묶었고, `ProjectTechnology` 엔티티는 `@EmbeddedId`로 이 값 객체를 키로
쓰면서 동시에 `@ManyToOne @MapsId("projectId")`/`@MapsId("technologyId")`로 `Project`/`Technology`
엔티티도 함께 참조할 수 있게 했다. `version`(예: "3.4.0") 필드만 키가 아닌 일반 컬럼이다.

**설계 목적 및 아키텍처적 의의**
이건 SQL 스키마 동기화 관점에서 **가장 기술적으로 까다로운 부분**이다 — JPA의 표준 `@Id`는 단일 컬럼을
전제로 하는데, 이 테이블처럼 "의미 있는 여러 컬럼의 조합이 그 자체로 기본키"인 경우 `@EmbeddedId` +
`@MapsId` 조합을 써야 한다. `@MapsId`는 "이 연관관계(`@ManyToOne`)의 외래키 값이 동시에 복합키의 일부
값으로도 쓰인다"는 것을 JPA에게 알려주는 역할을 한다 — 이 덕분에 `project`와 `technology` 두 연관관계
필드를 매번 따로 세팅할 필요 없이, 생성자에서 `new ProjectTechnologyId(project.getId(),
technology.getId(), technologyGroup)`처럼 한 번만 조립하면 된다.

**팀 협업 및 확장 관점의 의미**
"이 프로젝트가 어떤 기술을 어떤 그룹으로 쓰고, 버전이 몇인지"를 조회하려는 프론트엔드/서비스 레이어
개발자는 `project.getProjectTechnologies()`(연관관계 편의 메서드가 추가된다면) 또는
`ProjectTechnologyRepository`(아직 없음, 후속 작업)를 통해 조회하게 될 것이다. 복합키 구조 자체를
몰라도 되고, `ProjectTechnology` 엔티티의 `getProject()`/`getTechnology()`/`getVersion()`만 쓰면 된다
— 복잡한 키 조립은 엔티티 내부에 캡슐화되어 있다.

### 5-3. Engagement 도메인 — 반응·댓글·구독·알림

#### `src/main/java/com/skala/clickhub/entity/ReactionType.java`

**역할 및 구현 기능** `LIKE`, `FAVORITE` 2개 값. `project_reactions.type` 컬럼에 대응한다.

**설계 목적 및 아키텍처적 의의** 최초 스켈레톤에서는 "좋아요"와 "즐겨찾기"를 각각 `Reaction`,
`Favorite`라는 별개 개념으로 다뤘지만, 실제 SQL은 **하나의 `project_reactions` 테이블에 `type` 컬럼으로
구분**하는 방식을 택했다. 이게 이 enum 파일이 존재하는 이유이자, 아래 `ProjectReaction` 엔티티 하나가
두 가지 기능(§7 Top 100에 반영되는 좋아요, 반영 안 되는 즐겨찾기)을 동시에 감당하는 이유다.

**팀 협업 및 확장 관점의 의미** `FavoriteController`(아래 컨트롤러 섹션 참고)는 별도 엔티티가 아니라 이
enum의 `FAVORITE` 값으로 `ProjectReaction`을 다루게 될 것이다 — 컨트롤러는 나뉘어 있지만(화면 기능이
다르므로) 데이터 저장은 통합되어 있다는 걸 서비스 레이어 구현자가 알아야 중복 테이블을 만드는 실수를
피할 수 있다.

#### `src/main/java/com/skala/clickhub/entity/ProjectReaction.java` (+ `entity/id/ProjectReactionId.java`)

**역할 및 구현 기능**
`project_reactions` 테이블 매핑. 기본키는 `(user_id, project_id, type)` 복합키(`ProjectTechnology`와
같은 `@EmbeddedId` + `@MapsId` 패턴). `createdAt`만 별도 컬럼으로 갖는다(수정 개념이 없는, "설정하거나
안 하거나"만 있는 데이터라 `updatedAt`이 없다).

**설계 목적 및 아키텍처적 의의**
복합키를 "한 사용자가 한 프로젝트에 같은 타입의 반응을 두 번 만들 수 없다"는 비즈니스 규칙 자체를 DB
제약(기본키 유일성)으로 강제하는 데 활용한 사례다. 기획서 7장의 "사용자당 프로젝트 1회" 정책이 애플리케이션
코드의 `if` 문이 아니라 **DB 스키마 설계 자체**로 보장된다 — 이게 왜 중요하냐면, 서비스 코드에 버그가
있어도(동시성 문제로 중복 INSERT 시도) DB가 유니크 키 위반으로 막아주기 때문이다.

**팀 협업 및 확장 관점의 의미**
"좋아요 토글" API(`PUT /v1/projects/{id}/like`)를 구현할 사람은 "이미 있으면 삭제, 없으면 생성"이라는
전형적인 토글 로직을 짜게 되는데, 이때 복합키(`user_id, project_id, LIKE`)로 존재 여부를 조회하는
`existsById(new ProjectReactionId(...))` 패턴을 쓰면 된다. `§7 Top 100` 집계 배치를 만들 사람에게는
"좋아요는 `type='LIKE'`인 행만 세어야 하고, 즐겨찾기(`FAVORITE`)는 Top 100에서 제외해야 한다"는 정책이
바로 이 테이블 하나의 `type` 컬럼 필터링으로 구현된다는 걸 알려준다.

#### `src/main/java/com/skala/clickhub/entity/ProjectComment.java`

**역할 및 구현 기능**
`project_comments` 테이블 매핑. `id`(UUID), `project`(FK), `author`(User FK), `body`(본문, `@Lob`),
`deletedAt`(소프트 삭제)로 구성. `BaseTimeEntity`를 상속해 `createdAt`/`updatedAt`도 갖는다.

**설계 목적 및 아키텍처적 의의**
댓글을 물리적으로 삭제(`DELETE`)하지 않고 `deletedAt`만 채우는 소프트 삭제 방식이다 — 대댓글 구조가 있는
`CommunityPostComment`(현재는 삭제됨, 이전 버전)와 같은 이유로, "삭제된 댓글이 있었다"는 흔적 자체가
스레드 구조 유지나 신고 이력 추적에 필요할 수 있기 때문이다. `author`가 `User` FK인데 `ON DELETE
RESTRICT`(SQL 제약)라는 점도 주목할 만하다 — 즉 댓글을 쓴 사용자는 계정을 완전히 지울 수 없고, 먼저
`deletedAt`으로 소프트 삭제해야 한다(User 자체도 소프트 삭제 방식이므로 이 정책과 일관성이 맞는다).

**팀 협업 및 확장 관점의 의미**
프론트엔드는 댓글 목록을 렌더링할 때 `deletedAt IS NOT NULL`인 댓글을 "삭제된 댓글입니다"로 표시할지,
아예 목록에서 뺄지 화면 정책을 정해야 한다. AI 분석 담당자(§14 댓글 의견 분석)는 삭제된 댓글을 분석
대상에서 제외해야 한다는 걸 이 필드로 판단할 수 있다.

#### `src/main/java/com/skala/clickhub/entity/CreatorSubscription.java` (+ `entity/id/CreatorSubscriptionId.java`)

**역할 및 구현 기능**
`creator_subscriptions` 테이블 매핑. 기본키는 `(subscriber_id, creator_id)` 복합키. `subscriber`와
`creator`가 둘 다 `User` 타입이라는 게 특이한 점이다 — **자기 참조에 가까운 구조**(같은 `users` 테이블을
두 번 다른 역할로 참조)다.

**설계 목적 및 아키텍처적 의의**
기획서 7장의 "사용자는 프로젝트가 아니라 제작자를 구독한다"는 정책을 그대로 반영한 구조다. 만약
"프로젝트 구독"으로 설계했다면 `project_id`를 참조했을 텐데, 그렇게 하지 않고 `creator_id`(즉
`users.id`)를 참조하게 만든 것이 이 도메인의 핵심 설계 결정이다 — 한 제작자가 새 프로젝트를 여러 개
게시해도 구독자는 자동으로 전부 알림을 받는다.

**팀 협업 및 확장 관점의 의미**
"구독 설정/해제" API(`PUT /v1/creators/{id}/subscription`)를 구현할 사람은 `id`가 프로젝트 ID가 아니라
**제작자(User)의 ID**라는 걸 명확히 인지해야 한다 — URL 경로만 보면 헷갈리기 쉬운 지점이라 컨트롤러
주석에도 명시해 뒀다.

#### `src/main/java/com/skala/clickhub/entity/NotificationType.java`

**역할 및 구현 기능** 현재 `CREATOR_PROJECT_PUBLISHED` 단 하나의 값만 존재한다. `notification_outbox.type`,
`notifications.type` 두 테이블에서 공유해서 쓴다.

**설계 목적 및 아키텍처적 의의** 값이 하나뿐인 enum이라 "왜 굳이 enum으로 만들었나" 싶을 수 있지만, 이는
**앞으로 알림 종류가 늘어날 것을 전제로 한 확장 지점**이다(예: 댓글 알림, 좋아요 알림 등이 추가될 수
있다). 처음부터 문자열이 아니라 enum + `payload`(jsonb)로 설계해 뒀기 때문에, 새 알림 타입이 추가돼도
테이블 구조를 바꿀 필요 없이 이 enum에 값만 추가하면 된다.

**팀 협업 및 확장 관점의 의미** 알림 종류가 늘어날 때 서비스 레이어 개발자는 "새 테이블을 만들지 말고
이 enum에 값을 추가하고 `payload`의 JSON 스키마만 새로 정의하면 된다"는 확장 패턴을 이 파일에서 배울 수
있다.

#### `src/main/java/com/skala/clickhub/entity/OutboxStatus.java`

**역할 및 구현 기능** `PENDING`, `PROCESSING`, `DONE`, `FAILED` 4개 값. `notification_outbox.status`
컬럼에 대응하며, 알림 발송 작업의 처리 상태를 나타낸다.

**설계 목적 및 아키텍처적 의의** 이 enum은 **트랜잭셔널 아웃박스 패턴**이라는, 분산 시스템에서 흔히 쓰는
설계 기법을 이해해야 그 의미가 보인다 — 아래 `NotificationOutbox` 설명에서 이어서 다룬다.

**팀 협업 및 확장 관점의 의미** 배치/큐 처리를 구현할 사람은 `PENDING`/`FAILED` 상태의 행만 골라
처리하고, 처리 중에는 `PROCESSING`으로 잠가서 중복 처리를 막고, 끝나면 `DONE`으로 표시하는 상태 기계를
구현해야 한다는 걸 알 수 있다.

#### `src/main/java/com/skala/clickhub/entity/NotificationOutbox.java`

**역할 및 구현 기능**
`notification_outbox` 테이블 매핑. `id`(bigint identity), `project`(FK), `actor`(User FK, 알림을
유발한 사람 = 프로젝트 게시자), `type`, `payload`(jsonb, 알림에 필요한 정보를 미리 조립해 저장),
`status`, `attempts`(재시도 횟수), `availableAt`(다음 처리 가능 시각), `lockedAt`, `processedAt`,
`lastError`, `createdAt`.

**설계 목적 및 아키텍처적 의의**
"프로젝트가 게시됐다"는 이벤트와 "구독자 각각에게 알림을 만든다"는 작업을 **같은 트랜잭션 안에서 즉시
처리하지 않고 분리**하기 위한 큐 테이블이다. SQL의 `enqueue_publish_notification()` 트리거가 프로젝트
상태가 `PUBLISHED`로 바뀌는 순간 이 테이블에 작업 하나를 넣어두면, 별도의 배치 프로세스
(`process_notification_outbox()` SQL 함수)가 이 큐를 읽어서 실제 구독자 수만큼 `notifications` 테이블에
행을 팬아웃(fan-out)한다. 이렇게 분리하는 이유는, 구독자가 수천 명이어도 "프로젝트 게시" 트랜잭션 자체는
빠르게 끝나야 하고, 알림 생성이 실패해도 재시도할 수 있어야 하기 때문이다(`attempts`, `lastError`,
`availableAt` 재시도 딜레이가 이걸 위한 필드들이다).

**팀 협업 및 확장 관점의 의미**
백엔드 인프라 담당자에게는 "이 큐를 주기적으로 폴링해서 처리하는 스케줄러(또는 메시지 큐 워커)를
반드시 하나 띄워야 한다"는 걸 알려준다 — 이게 없으면 프로젝트를 아무리 게시해도 구독자에게 알림이 절대
가지 않는다. 이건 이번 스캐폴딩 단계에서 아직 구현되지 않은, 명확히 남겨진 후속 작업이다.

#### `src/main/java/com/skala/clickhub/entity/Notification.java`

**역할 및 구현 기능**
`notifications` 테이블 매핑. `id`(bigint identity — `notifications`/`notification_outbox`만 UUID가
아니라 bigint를 쓴다는 점이 다른 테이블과 다르다), `recipient`(알림 받는 사람), `actor`(알림을 유발한
사람), `project`, `type`, `payload`(jsonb), `createdAt`, `readAt`(읽음 처리 시각, null이면 안 읽음).

**설계 목적 및 아키텍처적 의의**
기획서 10.1장이 요구하는 "알림에는 개발자명, 프로젝트명, 썸네일, 게시 시각, 상세 페이지 링크를
포함한다"는 정보를 이 엔티티가 어떻게 담아내는지가 흥미로운 지점이다 — 이 정보들을 `Notification`
엔티티에 각각 컬럼으로 중복 저장하지 않고, `recipient`/`actor`/`project` **연관관계 + payload(jsonb)
조합**으로 표현한다. 즉 화면에 보여줄 최종 데이터는 조회 시점에 이 연관관계들을 조합해서 만들어야 한다
— `NotificationDtos.NotificationResponse`가 바로 그 조합된 최종 모양이다.

**팀 협업 및 확장 관점의 의미**
프론트엔드는 알림 목록에서 "미읽음 개수"를 `readAt IS NULL`인 행의 개수로 계산해야 한다. 서비스 레이어
구현자는 `GET /v1/notifications` API를 구현할 때 `recipient`/`actor`/`project`를 조인해서
`NotificationResponse`로 변환하는 로직을 짜야 한다는 걸 이 구조에서 알 수 있다. `id`가 `Long`이라는 점은
컨트롤러의 `@PathVariable` 타입에도 그대로 반영되어 있다(다른 도메인은 대부분 `UUID`인데
`NotificationController`만 `Long`을 쓴다).

### 5-4. Activity 도메인 — 비로그인 사용자 추적과 원본 이벤트 로그

#### `src/main/java/com/skala/clickhub/entity/ActorKind.java`

**역할 및 구현 기능** `USER`, `ANONYMOUS` 2개 값. `interaction_events.actor_kind`,
`search_requests.actor_kind` 두 테이블에서 공유한다.

**설계 목적 및 아키텍처적 의의** "누가 이 행동을 했는가"를 표현할 때, 로그인 사용자는 `User.id`로, 비로그인
사용자는 아래 `AnonymousSession.id`로 식별하는 **폴리모픽(다형적) 액터 모델**의 타입 태그 역할을 한다.
`actor_key`(UUID) 컬럼 하나가 이 enum 값에 따라 두 가지 다른 테이블을 가리킬 수 있다는 뜻이라, SQL
레벨에서는 이걸 강제하는 외래키 제약을 걸 수 없다(하나의 컬럼이 두 개의 다른 테이블을 조건부로
참조하는 건 표준 FK로 표현 불가) — 그래서 이 참조는 "논리적 참조"로만 존재하고 애플리케이션이 정합성을
책임진다.

**팀 협업 및 확장 관점의 의미** 프론트엔드는 비로그인 사용자를 위한 익명 세션 ID를 쿠키/로컬스토리지에
저장하고 모든 이벤트 트래킹 요청에 실어 보내야 한다는 걸 이 구조에서 알 수 있다. 데이터 분석 담당자는
"로그인 사용자의 행동"과 "비로그인 사용자의 행동"을 같은 테이블에서 `actor_kind`로 구분해서 집계할 수
있다.

#### `src/main/java/com/skala/clickhub/entity/AnonymousSession.java`

**역할 및 구현 기능** `anonymous_sessions` 테이블 매핑. `id`(UUID), `createdAt`, `lastSeenAt`,
`expiresAt` 4개 필드뿐인 단순한 엔티티.

**설계 목적 및 아키텍처적 의의** 비로그인 사용자가 브라우저를 새로고침해도 "같은 사람"으로 인식하기 위한
**영속적 익명 식별자**다. 로그인하지 않아도 검색·조회 이벤트를 남길 수 있어야 개인화 추천의 콜드
스타트(§6) 품질을 조금이라도 높일 수 있기 때문에 존재한다. `expiresAt`이 있다는 건 이 세션이 무한정
유지되지 않고 만료된다는 뜻이고, `purge_expired_raw_activity()` SQL 함수가 만료된 세션과 오래된 원본
이벤트를 주기적으로 정리한다(개인정보 보호를 위한 데이터 보존 기간 정책, 기획서 8장).

**팀 협업 및 확장 관점의 의미** 프론트엔드는 최초 방문 시 이 세션을 생성하는 API(아직 스캐폴딩되지 않음)를
호출하고 받은 `id`를 쿠키에 저장해, 이후 모든 트래킹 이벤트에 `actor_key`로 실어 보내야 한다.

#### `src/main/java/com/skala/clickhub/entity/EventType.java`

**역할 및 구현 기능** 14개 값(`PROJECT_IMPRESSION`, `PROJECT_CARD_CLICK`, `PROJECT_DETAIL_VIEW`,
`OUTBOUND_CLICK`, `LIKE_SET`, `FAVORITE_SET`, `COMMENT_CREATED`, `CREATOR_SUBSCRIBED`,
`SEARCH_RESULT_CLICKED`, `PROJECT_REGISTERED`, `PROJECT_PUBLISHED`, `NOTIFICATION_CLICKED`,
`TUTORIAL_CLICKED`, `INSIGHT_VIEWED`)로, `interaction_events.event_type` 컬럼(PostgreSQL enum
`interaction_event_type`)과 정확히 일치한다.

**설계 목적 및 아키텍처적 의의** 기획서 15장의 "표준 이벤트" 목록과 이 SQL enum 사이에는 **아직 해소되지
않은 불일치**가 있다는 걸 알아둬야 한다 — 기획서는 `search_submitted`, `notification_created`를
포함하지만 SQL enum에는 없고, 대신 SQL에는 기획서에 없는 `project_published`가 있다. 코드는 SQL을
기준으로 삼았으므로 이 자바 enum도 SQL과 동일하게 맞춰져 있다.

**팀 협업 및 확장 관점의 의미** 프론트엔드에서 이벤트 트래킹 코드를 심을 사람(GA/Amplitude류 트래킹과
비슷한 개념)은 정확히 이 14개 값만 보낼 수 있다 — "검색을 실행했다"는 이벤트를 보내고 싶어도
`search_submitted`가 없으므로 대신 `search_requests` 테이블(별도 엔드포인트/로그)을 쓰거나, SQL
담당자에게 enum 추가를 요청해야 한다는 걸 이 파일이 알려준다.

#### `src/main/java/com/skala/clickhub/entity/InteractionEvent.java`

**역할 및 구현 기능**
`interaction_events` 테이블 매핑. `id`(bigint identity), `eventType`, `actorKind`, `actorKey`(UUID),
`project`(FK, 선택), `occurredAt`, `eventDate`(**읽기 전용** — `insertable=false, updatable=false`),
`context`(jsonb, 이벤트별 부가 정보).

**설계 목적 및 아키텍처적 의의**
이 테이블은 시스템 전체의 **원본 행동 로그(raw event log)**로, `project_daily_metrics`(방문자 수,
좋아요 수 등 집계)와 `project_top100_7d`/`developer_top100_7d`(랭킹 뷰)가 전부 이 테이블에서
파생된다(`refresh_project_daily_metrics()` SQL 함수가 이 테이블을 집계해서 `project_daily_metrics`를
채운다). `eventDate`를 읽기 전용으로 매핑한 이유는, SQL에서 이 컬럼이
`GENERATED ALWAYS AS ((occurred_at AT TIME ZONE 'UTC')::date) STORED`로 정의된 **DB가 자동 계산하는
컬럼**이기 때문이다 — 자바 쪽에서 이 값을 쓰려고 하면 SQL 오류가 나므로 애초에 쓰기를 막아 실수를
방지했다.

**팀 협업 및 확장 관점의 의미**
프론트엔드/서비스 레이어의 모든 사용자 행동(카드 노출, 클릭, 상세 조회, 외부 이동 등)은 이 테이블에
INSERT하는 것으로 시작된다고 보면 된다. 데이터 분석/AI 담당자에게는 "모든 집계와 인사이트의 근본 소스는
이 테이블"이라는 걸 알려준다 — 만약 특정 집계 숫자가 이상하면 여기서부터 데이터를 추적해야 한다.

#### `src/main/java/com/skala/clickhub/entity/SearchRequest.java`

**역할 및 구현 기능**
`search_requests` 테이블 매핑. `id`(bigint identity), `actorKind`/`actorKey`, `rawQuery`(사용자가 입력한
원문 검색어), `parsedFilters`(jsonb, AI가 추출한 키워드/조건), `usedFallback`(AI 장애 시 원문 검색으로
대체됐는지), `resultCount`, `searchedAt`.

**설계 목적 및 아키텍처적 의의**
기획서 §5의 검색 설계 원칙("AI 장애 시 원문 질의만으로 동일한 검색 흐름을 유지한다")을 **측정 가능하게**
만드는 테이블이다. `usedFallback` 컬럼 하나로 "AI 보강이 실패한 비율"을 집계할 수 있고,
`resultCount`(무결과 검색 여부)로 "검색 품질 지표"(§5 평가 지표: 무결과 검색률, fallback 비율)를 직접
계산할 수 있다.

**팀 협업 및 확장 관점의 의미**
검색 기능을 구현할 서비스 레이어 개발자는 매 검색 요청마다 이 테이블에 로그를 남기는 걸 잊으면 안
된다 — 그렇지 않으면 검색 품질 KPI(§16 핵심 KPI)를 나중에 전혀 측정할 수 없게 된다. 데이터 분석
담당자는 이 테이블만으로 "AI 보강 효과"(§5 평가 지표)를 검증하는 대시보드를 만들 수 있다.

### 5-5. Analytics 도메인 — 일별 집계

#### `src/main/java/com/skala/clickhub/entity/ProjectDailyMetric.java` (+ `entity/id/ProjectDailyMetricId.java`)

**역할 및 구현 기능**
`project_daily_metrics` 테이블 매핑. 기본키는 `(project_id, metric_date)` 복합키. `uniqueVisitors`,
`impressions`, `detailViews`, `validOutboundClicks`, `uniqueLikes`, `uniqueCommenters`(전부 그날의 집계
카운트), `abuseFactor`(어뷰징 감점 계수, `numeric(5,4)` → `BigDecimal`), `updatedAt`만 있고 **`created_at`은
없다**(이 테이블은 "생성"이 아니라 "매일 다시 계산해서 덮어쓰는" 성격이라 생성 시각이 의미 없다).

**설계 목적 및 아키텍처적 의의**
이 테이블은 애플리케이션이 실시간으로 INSERT하는 게 아니라, `refresh_project_daily_metrics(from, to)`라는
SQL 함수가 `interaction_events`를 집계해서 주기적으로(아마 매일 배치로) 다시 채워 넣는 **파생
테이블(derived table)**이다. 그래서 엔티티에 `@Builder`나 생성자를 통한 "새로 만들기" 기능을 넣지
않았다 — 이 테이블은 조회 전용으로 취급하는 게 맞다는 설계 의도를 코드 주석에 명시해 뒀다.
`project_top100_7d` 뷰도 바로 이 테이블을 원본으로 삼는다(`schema.sql`의 `CREATE VIEW` 정의 참고).

**팀 협업 및 확장 관점의 의미**
서비스 레이어에서 대시보드 API(`GET /v1/dashboard/projects/{id}`)를 구현할 사람은 이 테이블을 기간별로
`SUM()`해서 `DashboardDtos.RawMetrics`를 채우면 된다. 배치/인프라 담당자는 "이 집계 함수를 언제 얼마나
자주 실행할지"(예: 매일 자정 크론)를 결정해야 한다는 명확한 책임 소재를 이 테이블에서 확인할 수 있다.

#### `src/main/java/com/skala/clickhub/entity/CreatorDailyMetric.java` (+ `entity/id/CreatorDailyMetricId.java`)

**역할 및 구현 기능**
`creator_daily_metrics` 테이블 매핑. 기본키는 `(creator_id, metric_date)`. `subscriberGrowth`(그날
늘어난 구독자 수), `activeProjects`(그날 기준 게시 중인 프로젝트 수), `updatedAt`.

**설계 목적 및 아키텍처적 의의**
`developer_top100_7d` 뷰(§9.3 개발자 랭킹)가 `project_daily_metrics`와 이 테이블을 함께 조인해서 개발자
점수(`developer_score` 공식)를 계산한다. `ProjectDailyMetric`과 똑같은 "파생 테이블, 조회 전용" 설계
철학을 공유한다.

**팀 협업 및 확장 관점의 의미**
"내 구독자가 이번 주에 몇 명 늘었는지" 같은 제작자 대시보드 지표를 만들 서비스 레이어 개발자에게 바로
쓸 수 있는 소스 테이블이다.

### 5-6. Search 프로젝션 도메인 — 비정규화 검색 read model

#### `src/main/java/com/skala/clickhub/entity/ProjectSearchDocument.java`

**역할 및 구현 기능**
`project_search_documents` 테이블 매핑. 특이하게 **자체 UUID를 생성하지 않고 `projects.id`를 그대로
공유하는 PK**를 쓴다(`@OneToOne` + `@MapsId`). `title`/`description`/`siteUrl`/`status`/`pricing`을
`projects`에서 그대로 복제해 갖고, `categorySlug`/`tags`/`technologySlugs`(전부 검색 필터링용으로
비정규화된 값), `publishedAt`, `engagementQuality`(참여도 점수), `contentHash`/`modelName`/
`embeddingGeneratedAt`(임베딩 메타데이터)을 갖는다. **`embedding`(pgvector `vector(1536)` 컬럼)과
`search_document`(생성된 `tsvector` 컬럼)는 의도적으로 매핑에서 제외**했다.

**설계 목적 및 아키텍처적 의의**
이건 **CQRS(명령과 조회의 분리)** 패턴의 축소판이다 — `projects` 테이블이 "쓰기(등록/수정)"의 정답
소스라면, 이 테이블은 "검색 조회 전용으로 최적화된 사본"이다. SQL의 `sync_project_search_document()`
함수와 관련 트리거들이 `projects`/`project_technologies`가 바뀔 때마다 이 테이블을 자동으로 동기화한다
— 즉 애플리케이션 코드는 이 테이블에 직접 쓸 필요가 거의 없고, `projects`만 정상적으로 갱신하면 검색
인덱스는 DB가 알아서 맞춰준다. `embedding` 필드를 뺀 이유는 pgvector의 벡터 타입을 Hibernate가 기본
지원하지 않기 때문이다(`com.pgvector:pgvector` 같은 별도 라이브러리와 커스텀 UserType이 필요) — 지금은
"이 필드는 아직 자바에서 못 다룬다"는 걸 명시적으로 드러내는 편이, 어설프게 흉내 내는 매핑을 넣어두는
것보다 안전하다고 판단했다.

**팀 협업 및 확장 관점의 의미**
검색 기능을 실제로 구현할 사람(임베딩 생성, 벡터 유사도 검색)은 이 엔티티만으로는 부족하고, `pgvector`
Hibernate 확장을 추가하거나 네이티브 쿼리(`search_published_projects()` SQL 함수를 직접 호출)를 써야
한다는 걸 미리 알 수 있다. DB 담당자에게는 "검색 인덱스가 깨졌다면 `sync_project_search_document()`
트리거가 제대로 도는지부터 확인하라"는 디버깅 힌트를 준다.

### 5-7. AI/Content 도메인 — 대시보드 AI 분석, 주간 인사이트, 튜토리얼

#### `src/main/java/com/skala/clickhub/entity/AnalysisType.java`

**역할 및 구현 기능** `AUDIENCE_SUMMARY`, `COMMENT_SUMMARY`, `PROJECT_COMPARISON` 3개 값.
`dashboard_ai_analyses.analysis_type` 컬럼과 대응하며, 기획서 8장 대시보드의 "방문 문맥", "사용자
반응", "프로젝트 비교" 3개 AI 분석 영역과 정확히 일치한다.

**설계 목적 및 아키텍처적 의의** 하나의 `dashboard_ai_analyses` 테이블이 세 가지 종류의 AI 분석 결과를
전부 담을 수 있게, 결과 JSON(`result` 컬럼)의 "종류표"로 이 enum을 둔 것이다.

**팀 협업 및 확장 관점의 의미** AI 오케스트레이션을 구현할 사람은 분석 종류별로 다른 프롬프트/JSON
스키마를 쓰되, 저장은 이 하나의 테이블·enum 체계 안에서 하면 된다는 패턴을 알 수 있다.

#### `src/main/java/com/skala/clickhub/entity/DashboardAiAnalysis.java`

**역할 및 구현 기능**
`dashboard_ai_analyses` 테이블 매핑. `id`(UUID), `project`(FK), `analysisType`, `sourcePeriod`(SQL의
`daterange` 타입 — **임시로 `String`에 매핑**, 아래 설명 참고), `sourceMetricSnapshot`/`evidence`/
`result`(전부 jsonb → `JsonNode`), `modelName`, `generatedAt`, `createdAt`.

**설계 목적 및 아키텍처적 의의**
기획서 8장의 "표시 원칙: AI가 해석한 결과와 AI가 관여하지 않은 원본 데이터를 같은 화면에서 구분해
보여준다"는 원칙이 이 테이블 설계에 그대로 녹아 있다 — `sourceMetricSnapshot`(원본 지표를 분석 시점에
스냅샷으로 고정), `evidence`(근거), `result`(AI의 해석)를 **전부 별도 컬럼**으로 분리해서, 나중에 "이
AI 요약이 어떤 원본 데이터에 근거했는지" 추적 가능하게 만들었다. `sourcePeriod`를 `String`으로 임시
매핑한 이유는 PostgreSQL의 `daterange`(기간 범위) 타입이 Hibernate 기본 매핑 대상이 아니기 때문이다 —
지금은 원문 텍스트로만 다루고, 실제로 "이 기간과 겹치는 분석이 있는지" 같은 range 연산이 필요해지면
커스텀 `UserType`을 만들어야 한다는 걸 코드 주석과 `backend-structure.md`의 TODO에 명시해 뒀다.

**팀 협업 및 확장 관점의 의미**
AI 서비스 담당자가 대시보드 분석 파이프라인을 구현할 때, "원본 통계를 계산 → 스냅샷 저장 → LLM에 넘겨
해석 요청 → 결과 저장"이라는 4단계 흐름이 이 테이블 하나에 다 담긴다는 걸 알 수 있다. 프론트엔드는
대시보드 화면에서 `sourceMetricSnapshot`(원본)과 `result`(AI 해석)를 나란히 배치해서 보여줘야 한다는
기획 원칙을 이 구조에서 다시 확인할 수 있다.

#### `src/main/java/com/skala/clickhub/entity/WeeklyInsight.java`

**역할 및 구현 기능**
`weekly_insights` 테이블 매핑. `id`(UUID), `weekStart`(그 주의 월요일 날짜, 고유), `rawMetrics`/
`aiSummary`(jsonb), `modelName`, `generatedAt`, `publishedAt`. `created_at`/`updated_at` 컬럼이 SQL에
없어서 `BaseTimeEntity`를 상속하지 않는다.

**설계 목적 및 아키텍처적 의의**
기획서 9.1장 "인사이트 탭 — 주간 트렌드"의 저장소다. `weekStart`를 고유 제약으로 둔 이유는 "한 주에
하나의 인사이트만 존재해야 한다"는 규칙을 DB가 보장하게 하기 위해서다. `DashboardAiAnalysis`와 마찬가지로
`rawMetrics`(원본 집계)와 `aiSummary`(AI 해석)를 분리해 "AI 결과는 원본 지표를 덮어쓰지 않는다"는 원칙을
지킨다.

**팀 협업 및 확장 관점의 의미**
주간 배치(§11 기술 아키텍처의 "Async: Spring Scheduler/메시지 큐")를 구현할 사람이 매주 월요일마다 이
테이블에 새 행 하나를 만드는 잡을 짜야 한다는 걸 알 수 있다. 프론트엔드 인사이트 탭은 `publishedAt`이
채워진 행만 노출해야 한다(생성됐지만 아직 공개 안 된 초안 상태를 구분할 수 있게).

#### `src/main/java/com/skala/clickhub/entity/TutorialType.java`

**역할 및 구현 기능** `VIBE_CODING`, `DEVELOPMENT` 2개 값. 기획서 9.2장의 "바이브 코딩 강의"/"개발 강의"
구분과 정확히 일치.

**설계 목적 및 아키텍처적 의의** `tutorials.type` 컬럼에 대응하며, 튜토리얼 탭의 최상위 필터 기준이 된다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 튜토리얼 탭의 최상위 탭/필터 UI가 이 두 값을 그대로 옵션으로
쓰면 된다.

#### `src/main/java/com/skala/clickhub/entity/TutorialDifficulty.java`

**역할 및 구현 기능** `BEGINNER`, `INTERMEDIATE`, `ADVANCED` 3개 값.

**설계 목적 및 아키텍처적 의의** 기획서에는 난이도의 구체적인 등급명이 명시돼 있지 않아서, SQL/코드
양쪽에서 일반적인 3단계로 잠정 결정해 둔 값이다 — 즉 이 부분은 "합리적인 기본값"이지 확정 스펙에서 그대로
가져온 게 아니라는 걸 팀이 인지하고 있어야 한다.

**팀 협업 및 확장 관점의 의미** 기획팀과 난이도 등급 이름(예: "입문/중급/고급" 같은 한국어 라벨)을 다시
확인해서 필요하면 이 enum 값을 조정해야 할 수도 있다.

#### `src/main/java/com/skala/clickhub/entity/Tutorial.java`

**역할 및 구현 기능**
`tutorials` 테이블 매핑. `id`(UUID), `title`, `description`, `type`, `difficulty`,
`estimatedMinutes`(학습 예상 소요 시간), `sourceUrl`, `categorySlugs`/`technologySlugs`(전부
PostgreSQL `text[]` 배열), `relatedProjectIds`(`uuid[]` 배열, 연계된 프로젝트), `isPublished`,
`publishedAt`. `BaseTimeEntity` 상속.

**설계 목적 및 아키텍처적 의의**
`categorySlugs`/`technologySlugs`가 **복수형**(배열)이라는 점이 중요하다 — 프로젝트는 카테고리를 하나만
가지지만(`primaryCategory`), 튜토리얼은 여러 카테고리·여러 기술 스택에 동시에 걸쳐 있을 수 있다는 걸
스키마가 명시적으로 구분하고 있다. `relatedProjectIds`는 FK 제약이 없는 순수 UUID 배열이라는 점도
특이한데, "느슨한 연결"(참조 무결성을 DB가 강제하지 않는, soft reference)로 설계되어 있다 — 연계된
프로젝트가 삭제돼도 이 배열은 자동으로 정리되지 않으므로, 조회 시점에 존재 여부를 확인하는 방어 코드가
필요하다는 걸 서비스 레이어 구현자가 알아야 한다.

**팀 협업 및 확장 관점의 의미**
튜토리얼 등록은 사용자가 아니라 운영자가 하는 콘텐츠 관리 기능일 가능성이 높다(기획서에 "제작자가
등록한다"는 문구가 없다) — 즉 이 도메인은 별도의 관리자용 CMS 화면이 필요할 수 있다는 힌트를 준다.
프론트엔드는 "카테고리·난이도·기술 스택·학습 시간으로 필터링"(기획서 9.2장)하는 UI를 만들 때
`categorySlugs`/`technologySlugs` 배열에 대한 "포함 여부(contains)" 쿼리가 필요하다는 걸 알아야 한다.

### 5-8. Onboarding — 관심 카테고리 (가장 최근에 추가된 도메인)

#### `src/main/java/com/skala/clickhub/entity/UserOnboardingInterestCategory.java` (+ `entity/id/UserOnboardingInterestCategoryId.java`)

**역할 및 구현 기능**
`user_onboarding_interest_categories` 테이블 매핑. 기본키는 `(user_id, category_id)` 복합키(다른 복합키
엔티티들과 동일한 `@EmbeddedId` + `@MapsId` 패턴). `createdAt`만 별도로 갖는다.

**설계 목적 및 아키텍처적 의의**
이 엔티티는 `schema (1).sql`에서 가장 최근에 추가된 테이블이며, 기획서 v1.1의 "최초 로그인 후 관심
카테고리 등 온보딩 설문으로 선택할 수 있으며, 건너뛸 수 있다"는 §2 요구사항을 데이터로 뒷받침한다.
`User`가 카테고리를 "여러 개" 선택할 수 있다는 걸 다대다(N:M) 관계로 표현했고, `ON DELETE RESTRICT`(SQL
FK 옵션)이므로 사용자가 선택한 카테고리는 함부로 삭제될 수 없다.

**팀 협업 및 확장 관점의 의미**
이 엔티티는 만들어졌지만, **아직 이걸 다루는 DTO나 컨트롤러는 의도적으로 만들지 않았다**
(`backend-structure.md`의 "보류한 항목" 참고) — 온보딩 화면이 "몇 개까지 선택 가능한지", "건너뛰기를
어떻게 표현하는지"(빈 배열 저장 vs 아예 저장 안 함) 같은 화면 세부 사항이 아직 정해지지 않았기 때문이다.
프론트엔드 기획이 확정되면 `POST /v1/users/me/onboarding-interests` 같은 엔드포인트와
`UserDtos.OnboardingRequest(List<UUID> categoryIds)` 같은 DTO를 추가하는 게 다음 단계가 될 것이다 —
이 엔티티는 그 작업을 위한 "데이터가 어디에 저장될지"에 대한 준비를 미리 마쳐 둔 상태라고 이해하면 된다.

---

## 6. `entity/id` — 복합키 전용 `@Embeddable` 값 객체

앞서 각 엔티티 설명에서 다뤘지만, 이 하위 패키지 자체의 존재 이유를 한 번 더 정리한다. 아래 6개 클래스는
전부 같은 패턴을 공유한다: `@Embeddable` + `@NoArgsConstructor` + `@AllArgsConstructor` +
`@EqualsAndHashCode`(복합키는 JPA 영속성 컨텍스트가 각 필드 값으로 동등성을 판단해야 하므로 필수) +
`Serializable`(JPA 스펙이 복합키 클래스에 요구하는 조건).

- `ProjectTechnologyId.java` — `(projectId, technologyId, technologyGroup)`
- `ProjectReactionId.java` — `(userId, projectId, type)`
- `CreatorSubscriptionId.java` — `(subscriberId, creatorId)`
- `ProjectDailyMetricId.java` — `(projectId, metricDate)`
- `CreatorDailyMetricId.java` — `(creatorId, metricDate)`
- `UserOnboardingInterestCategoryId.java` — `(userId, categoryId)`

**설계 목적 및 아키텍처적 의의**: 이 패키지를 `entity`와 분리한 이유는, "이건 독립적인 테이블이 아니라
다른 엔티티의 기본키를 표현하기 위한 보조 타입일 뿐"이라는 걸 패키지 구조로 드러내기 위해서다. SQL
스키마 동기화 관점에서, **대리키(surrogate id) 없이 의미 있는 컬럼 조합 자체가 PK인 조인/집계
테이블**이 이 프로젝트에 6개나 있다는 것 자체가 하나의 설계 패턴이다 — 전부 "두 개 이상의 엔티티를
연결하는 다대다/집계 관계"라는 공통점이 있다.

**팀 협업 및 확장 관점의 의미**: 새로운 조인 테이블(예: SQL에 프로젝트-태그 다대다 조인 테이블이
생긴다면)을 추가할 팀원은 이 6개 클래스 중 아무거나 하나를 템플릿 삼아 그대로 복사해서 필드만 바꾸면
된다 — 이미 검증된 패턴이 여기 모여 있다.

---

## 7. `dto` — 화면별 요청/응답 계약

`dto` 패키지의 모든 파일은 클래스 하나가 아니라, **하나의 API 도메인에 속한 모든 요청/응답 타입을 모아
둔 "홀더(holder) 클래스"**다. 예를 들어 `ProjectDtos`는 `public final class`로 인스턴스화를 막아두고,
내부에 `record CreateRequest`, `record DetailResponse` 같은 중첩 레코드(record)들을 여러 개 담고
있다. 이렇게 한 이유는, 자바는 파일 하나에 public 최상위 타입을 하나만 허용하기 때문에 도메인마다 10개
가까운 파일을 만드는 대신, 관련된 요청/응답을 한 파일에서 한눈에 볼 수 있게 묶기 위해서다. 모든 응답은
`common/response/ApiResponse<T>`로 감싸져 나가고, 요청/응답 모두 **불변 객체인 `record`**로 선언해
실수로 값이 바뀌는 걸 원천 차단한다.

### `src/main/java/com/skala/clickhub/dto/auth/AuthDtos.java`

**역할 및 구현 기능** `OAuthStartResponse(String authorizationUrl)` 레코드 하나만 있다 — GitHub OAuth
로그인을 시작할 때 프론트엔드가 리다이렉트해야 할 URL을 담아 응답한다.

**설계 목적 및 아키텍처적 의의** OAuth 흐름은 보통 302 리다이렉트로 처리하는 경우도 많지만, 이 프로젝트는
"모든 응답은 `ApiResponse<T>`로 통일한다"는 원칙을 지키기 위해 리다이렉트 URL 자체를 JSON 데이터로
내려주는 방식을 택했다 — 즉 프론트가 이 URL을 받아서 `window.location.href`로 직접 이동시키는 SPA
친화적인 패턴이다.

**팀 협업 및 확장 관점의 의미** Google 로그인이 추가되면 이 파일에 provider 파라미터를 받는 요청 타입이나
`googleAuthorizationUrl` 같은 필드가 추가될 가능성이 높다는 걸 앞선 엔티티 변경(`SocialLoginProvider`)에서
미리 짐작할 수 있다.

### `src/main/java/com/skala/clickhub/dto/project/ProjectDtos.java`

**역할 및 구현 기능**
가장 복잡한 DTO 홀더다. `ScreenshotItem(url, alt)`, `TechStackSelection(technologySlug, group,
version)`(등록 요청용, 카탈로그에서 슬러그로 선택), `TechStackItem(technologyName, technologySlug,
group, version)`(응답용, 화면 표시 이름 포함), `CreateRequest`(등록 폼 전체), `CreateResponse(id)`,
`DetailResponse`(상세 화면 전체 — `likeCount`/`favoritedByMe` 같은 파생 필드 포함), `OutboundClickResponse`.

**설계 목적 및 아키텍처적 의의**
`TechStackSelection`과 `TechStackItem`을 굳이 나눈 이유가 이 파일의 핵심 설계 포인트다 — **요청과
응답의 비대칭성**을 보여주는 사례다. 등록할 때 사용자는 카탈로그에서 기술을 "선택"만 하면 되므로
`technologySlug`만 필요하지만, 조회할 때는 화면에 "Vue.js"라는 사람이 읽는 이름을 보여줘야 하므로
`technologyName`이 추가로 필요하다. `DetailResponse`의 `likeCount`, `favoritedByMe` 같은 필드는
`Project` 엔티티에는 존재하지 않는 **계산된(파생) 값**이다 — 서비스 레이어가 `ProjectReaction` 테이블을
집계하거나 현재 로그인 사용자 기준으로 조회해서 채워 넣어야 한다는 뜻이다.

**팀 협업 및 확장 관점의 의미**
프론트엔드 개발자는 등록 폼을 만들 때 `CreateRequest`의 필드 순서 그대로 폼 필드를 배치하면 되고,
TypeScript 타입도 이 record를 그대로 옮기면 된다. e2e 테스트 작성자는 `likedByMe`/`favoritedByMe`가
"로그인 사용자에 따라 달라지는 필드"라는 걸 인지하고, 서로 다른 사용자로 로그인해서 같은 프로젝트를
조회하는 테스트 케이스를 반드시 만들어야 한다.

### `src/main/java/com/skala/clickhub/dto/search/SearchDtos.java`

**역할 및 구현 기능** `SearchResultItem(id, title, thumbnailUrl, category, tags)` — 검색 결과 카드
하나를 표현하는 레코드.

**설계 목적 및 아키텍처적 의의** `ProjectDtos.DetailResponse`보다 훨씬 가벼운 구조다 — 검색 결과
목록에서는 상세 정보(기술 스택 전체, 스크린샷 등)가 필요 없고 카드에 보여줄 최소 정보만 필요하다는
"화면 목적에 맞는 최소 데이터"라는 DTO 설계 원칙을 보여준다. `controller/SearchController`에서
`CursorPageResponse<SearchResultItem>`로 감싸져 무한 스크롤 응답으로 나간다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 검색 결과 그리드/리스트 컴포넌트는 이 5개 필드만으로 카드
UI를 완성할 수 있어야 한다 — 만약 카드에 더 많은 정보가 필요하다는 게 나중에 밝혀지면, 이 record에
필드를 추가하는 게 아니라 "왜 상세 화면과 다른 최소 정보만 담았는지"부터 다시 검토해야 한다.

### `src/main/java/com/skala/clickhub/dto/feed/FeedDtos.java`

**역할 및 구현 기능** `FeedItem(id, title, thumbnailUrl, category, likeCount)` — 홈 피드 카드 하나.
`SearchResultItem`과 거의 비슷하지만 `tags` 대신 `likeCount`를 갖는다(피드는 인기 신호를 카드에 바로
보여주는 게 화면 목적에 더 맞기 때문).

**설계 목적 및 아키텍처적 의의** 검색 결과와 피드가 비슷해 보여도 **DTO를 공유하지 않고 별도로
분리**했다는 점이 중요한 설계 결정이다 — 지금은 필드가 비슷하지만, 두 화면은 애초에 다른 목적(하나는
"찾기", 하나는 "발견하기")을 가지므로 앞으로 서로 다르게 진화할 가능성이 높다. 미리 공유 타입으로
묶어뒀다면 한쪽 화면의 요구사항 때문에 다른 쪽 DTO에 불필요한 필드가 끼어드는 결합이 생겼을 것이다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 입장에서는 "검색 카드 컴포넌트"와 "피드 카드 컴포넌트"를
같은 컴포넌트로 재사용하고 싶은 유혹이 들 수 있지만, 백엔드 DTO가 이미 분리되어 있으므로 화면 컴포넌트도
분리해서 설계하는 게 백엔드 구조와 일관성이 맞다.

### `src/main/java/com/skala/clickhub/dto/ranking/RankingDtos.java`

**역할 및 구현 기능** `ProjectRankingItem(rank, projectId, title, score)`,
`DeveloperRankingItem(rank, creatorId, displayName, score)` — Top 100 화면의 프로젝트/개발자 랭킹 행
하나씩을 표현한다.

**설계 목적 및 아키텍처적 의의** `rank`(순위 번호)가 DB에는 없는 필드라는 점이 핵심이다 —
`project_top100_7d`/`developer_top100_7d` 뷰는 `score`만 계산해 줄 뿐 순위 번호를 매기지 않으므로,
서비스 레이어가 점수 내림차순으로 정렬한 뒤 인덱스를 매겨서 이 DTO를 조립해야 한다는 걸 알 수 있다.

**팀 협업 및 확장 관점의 의미** 프론트엔드는 순위 변동 애니메이션이나 "지난주 대비 순위 변화" 같은 기능을
추가하고 싶다면, 이 DTO에 `previousRank` 같은 필드를 추가해야 하고, 서비스 레이어는 지난주 스냅샷을
어딘가에 보관해야 한다는 후속 설계 과제를 짐작할 수 있다.

### `src/main/java/com/skala/clickhub/dto/reaction/ReactionDtos.java`

**역할 및 구현 기능** `LikeResponse(liked, likeCount)`(토글 후 현재 상태와 총 개수를 함께 반환),
`CommentCreateRequest(body)`, `CommentResponse(id, authorName, body, createdAt)`.

**설계 목적 및 아키텍처적 의의** `LikeResponse`가 `liked` 하나만 반환하지 않고 `likeCount`까지 함께
반환하는 이유는, 프론트엔드가 좋아요를 누른 직후 **추가 API 호출 없이 즉시 화면의 좋아요 개수를 갱신**할
수 있게 하기 위해서다 — 이건 좋은 API 설계의 대표적인 예시로, "클라이언트가 다음에 뭘 물어볼지"까지
예측해서 한 번의 응답에 담아주는 방식이다.

**팀 협업 및 확장 관점의 의미** 프론트엔드는 좋아요 버튼 클릭 핸들러에서 API 응답의 `likeCount`를 그대로
화면 상태에 반영하면 되고, 별도로 프로젝트 상세를 다시 불러올 필요가 없다 — 이게 사용자 체감 속도를
크게 높여준다는 걸 프론트 개발자가 알아두면 좋다.

### `src/main/java/com/skala/clickhub/dto/favorite/FavoriteDtos.java`

**역할 및 구현 기능** `FavoriteResponse(favorited)` 하나뿐.

**설계 목적 및 아키텍처적 의의** `LikeResponse`와 다르게 `favoriteCount`를 반환하지 않는다 — 기획서
7장에 따르면 즐겨찾기는 "비공개"(개인 저장/선호 신호) 정보라서 애초에 "전체 즐겨찾기 개수"라는 집계
자체를 화면에 노출할 일이 없기 때문이다. 엔티티 레벨에서는 `ProjectReaction`(type=`FAVORITE`)으로
`LikeResponse`와 같은 테이블을 쓰지만, DTO 레벨에서는 이렇게 노출 정책이 다르게 설계된다는 게 흥미로운
지점이다.

**팀 협업 및 확장 관점의 의미** 프론트엔드는 즐겨찾기 버튼에 "몇 명이 즐겨찾기했는지" 숫자를 표시하는
UI를 만들면 안 된다 — API가 애초에 그 정보를 주지 않는다는 게 기획 의도를 그대로 반영한 설계라는 걸
이해해야 한다.

### `src/main/java/com/skala/clickhub/dto/subscribe/SubscribeDtos.java`

**역할 및 구현 기능** `SubscriptionResponse(subscribed)` 하나뿐.

**설계 목적 및 아키텍처적 의의** `FavoriteResponse`와 똑같이 단순한 boolean 토글 결과만 반환한다 —
"구독자 수"를 이 응답에 넣지 않은 이유는, 구독은 프로젝트가 아니라 제작자 단위이고 제작자 프로필
화면에서 별도로 조회하는 게 더 적절하다고 판단했기 때문이다(토글 API 하나가 모든 정보를 다 짊어질
필요는 없다).

**팀 협업 및 확장 관점의 의미** 제작자 프로필 화면에 "구독자 수"를 보여주려면 별도의 조회 API(아직 없음,
후속 작업)가 필요하다는 걸 이 DTO의 "없음"에서 역으로 알 수 있다.

### `src/main/java/com/skala/clickhub/dto/dashboard/DashboardDtos.java`

**역할 및 구현 기능** `RawMetrics`(uniqueVisitors, impressions, detailViews, validOutboundClicks,
uniqueLikes, uniqueCommenters, ctr), `AiSummary`(changeSummary, generatedAt),
`DashboardResponse`(period, rawMetrics, aiSummary).

**설계 목적 및 아키텍처적 의의** 기획서 8장의 "원본 데이터와 AI 분석 결과를 같은 화면에서 구분해
보여준다"는 표시 원칙이 이 DTO 구조 자체에 그대로 코드화되어 있다 — `rawMetrics`와 `aiSummary`가
`DashboardResponse` 안에서 **형제 필드로 나란히** 존재하고, 서로 섞이지 않는다. `RawMetrics`의 필드명은
`ProjectDailyMetric` 엔티티의 컬럼명과 의도적으로 똑같이 맞췄다(`uniqueVisitors`, `validOutboundClicks`
등) — 엔티티 → DTO 변환이 "이름 그대로 옮기기"에 가까워서 변환 코드를 짤 때 실수가 줄어든다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 대시보드 화면은 이 두 섹션(원본/AI)을 시각적으로도 분리해서
배치해야 한다는 기획 의도를 이 DTO에서 재확인할 수 있다. `ctr`(클릭률)은 DB에 저장된 컬럼이 아니라
`validOutboundClicks / detailViews`로 서비스 레이어가 직접 계산해서 채워야 하는 파생값이라는 것도
알아둬야 한다.

### `src/main/java/com/skala/clickhub/dto/insight/InsightDtos.java`

**역할 및 구현 기능** `TrendItem(topic, direction, changeRate)`,
`WeeklyInsightResponse(weekStart, headline, trends, watchlist, modelName, generatedAt)`.

**설계 목적 및 아키텍처적 의의** `WeeklyInsight` 엔티티의 `aiSummary`(jsonb, 구조화되지 않은 원문
JSON)를 화면이 실제로 필요로 하는 **타입 안전한 구조**(`headline`, `trends` 리스트, `watchlist`
리스트)로 한 번 더 가공해서 내려주는 계층이라는 걸 보여준다 — 즉 엔티티의 jsonb는 "무엇이든 담을 수
있는 저장 형태"이고, DTO는 "이번 버전 화면이 실제로 쓰는 확정된 모양"이라는 역할 차이가 있다.

**팀 협업 및 확장 관점의 의미** AI 프롬프트를 설계하는 사람은 LLM의 출력 JSON 스키마를 이 DTO 구조와
맞춰서 설계하면 서비스 레이어의 변환 코드가 거의 필요 없어진다(입력 그대로 매핑) — 반대로 스키마가
어긋나면 매번 변환/검증 로직을 추가로 짜야 한다는 트레이드오프를 이해하고 있어야 한다.

### `src/main/java/com/skala/clickhub/dto/tutorial/TutorialDtos.java`

**역할 및 구현 기능** `TutorialResponse(id, title, description, type, difficulty, estimatedMinutes,
sourceUrl, categorySlugs, technologySlugs)` — `Tutorial` 엔티티의 필드를 거의 그대로 노출한다
(`relatedProjectIds`만 응답에서 빠져 있다).

**설계 목적 및 아키텍처적 의의** 다른 도메인과 달리 엔티티와 DTO가 거의 1:1이라는 점이 특징이다 —
튜토리얼은 "좋아요 개수"나 "로그인 사용자별 다른 값" 같은 파생/개인화 필드가 필요 없는, 비교적 단순한
콘텐츠 조회 화면이기 때문이다. `relatedProjectIds`를 뺀 건 의도적인 설계 선택으로 보이며, 연계 프로젝트
정보가 필요하면 각 ID를 실제 프로젝트 카드 정보로 부풀려서 별도 필드(`relatedProjects:
List<ProjectSummary>` 같은)로 추가하는 게 프론트엔드에 더 유용할 것이다.

**팀 협업 및 확장 관점의 의미** 이 DTO를 확장할 때는 "ID 배열을 그대로 내려줄지, 실제 데이터로 부풀려서
내려줄지"를 고민해야 한다는 일반적인 API 설계 교훈을 여기서 얻을 수 있다.

### `src/main/java/com/skala/clickhub/dto/notification/NotificationDtos.java`

**역할 및 구현 기능** `NotificationResponse(id, creatorName, projectTitle, thumbnailUrl, projectId,
publishedAt, readAt)`, `ReadResponse(id, readAt)`. `id`/`projectId` 타입이 다른 도메인과 다르게
`Long`/`UUID`로 섞여 있다.

**설계 목적 및 아키텍처적 의의** `Notification` 엔티티는 `recipient`/`actor`/`project` 연관관계 +
`payload`(jsonb)만 갖고 있는데, 이 DTO는 그걸 화면이 바로 쓸 수 있는 **평탄화된(flattened) 모양**으로
재조립한다 — `creatorName`은 `actor.getDisplayName()`에서, `projectTitle`/`thumbnailUrl`은
`project`에서(또는 `payload`에서, 프로젝트가 나중에 수정되어도 알림 발송 시점의 정보를 그대로 보여주고
싶다면 payload 쪽이 맞다), `id`는 알림 자체의 `Long` PK를 그대로 쓰지만 `projectId`는 상세 페이지
이동 링크를 만들기 위한 `UUID`라는 걸 명확히 구분해서 필드명을 지었다.

**팀 협업 및 확장 관점의 의미** 서비스 레이어 구현자는 "프로젝트 제목/썸네일을 조회 시점의 최신 값으로
보여줄지, 알림 발송 시점의 스냅샷(`payload`)으로 보여줄지"를 결정해야 한다 — 이건 기획팀과 협의가
필요한 지점이라는 걸 이 DTO 하나가 암시하고 있다. 프론트엔드는 `NotificationResponse.projectId`로
"상세 페이지 링크"를 만들고, `id`(알림 자체의 ID)는 오직 `PATCH /v1/notifications/{id}/read` 요청에만
쓴다는 걸 구분해야 한다.

---

## 8. `controller` — 화면별 REST 엔드포인트

11개 컨트롤러 전부 `@RestController`이고, 반환 타입은 예외 없이 `ApiResponse<T>`(또는
`ApiResponse<CursorPageResponse<T>>`, `ApiResponse<List<T>>`)다. 메서드 바디는 지금 전부
`throw new UnsupportedOperationException("not implemented")`로만 채워져 있다 — `return null;`을 쓰지
않은 이유는, 실수로 이 상태 그대로 배포되더라도 "조용히 null이 나가는" 대신 컴파일은 되지만 실행하면
확실히 터지는 방식으로 "여기 아직 구현 안 됐다"는 걸 강제로 드러내기 위해서다. 각 메서드 위 주석에는
기획서 §12 API 명세의 "인증" 열(없음/선택/로그인/소유자) 값을 그대로 옮겨 적어 뒀다 — 이게
`SecurityConfig`의 `PUBLIC_ENDPOINTS`와 서로 짝을 이루는 정보다.

### `src/main/java/com/skala/clickhub/controller/AuthController.java`

**역할 및 구현 기능** `GET /v1/auth/github` 하나. 인증: 없음.

**설계 목적 및 아키텍처적 의의** 로그인은 다른 모든 기능의 전제조건이라 별도 컨트롤러로 분리했다. 지금은
GitHub 엔드포인트 하나뿐이지만, `SocialLoginProvider` enum이 이미 `GOOGLE`을 포함하고 있으므로 이
컨트롤러는 조만간 `GET /v1/auth/google`이 추가될 자리라는 걸 예상할 수 있다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 로그인 버튼("GitHub로 로그인")이 호출할 엔드포인트가 바로
이것이다. e2e 테스트에서는 이 흐름이 외부 서비스(GitHub)에 의존하므로 목(mock) 서버나 스텁 처리가
필요하다는 걸 미리 계획해야 한다.

### `src/main/java/com/skala/clickhub/controller/ProjectController.java`

**역할 및 구현 기능** `POST /v1/projects`(등록, GitHub 로그인 필요), `GET /v1/projects/{id}`(상세,
인증 선택), `POST /v1/projects/{id}/outbound-clicks`(외부 이동 기록, 인증 선택). `{id}`는 `UUID` 타입.

**설계 목적 및 아키텍처적 의의** 이 컨트롤러가 다루는 세 엔드포인트는 기획서의 "북극성 행동"(§2 — 상세
페이지에서 실제 프로젝트로 이동한 유효 외부 클릭)으로 이어지는 핵심 사용자 여정 전체를 담당한다. 상세
조회 API의 인증이 "선택"인 이유는, 비로그인 사용자도 프로젝트를 볼 수 있어야 하지만 로그인한 사용자에게는
`likedByMe` 같은 개인화 필드를 추가로 채워줘야 하기 때문이다 — 이 미묘한 차이를 구현할 사람은 "인증
헤더가 있으면 파싱하고, 없어도 에러 내지 않는" 선택적 인증 처리를 `SecurityConfig`와 별개로 서비스
레이어에서 신경 써야 한다.

**팀 협업 및 확장 관점의 의미** 외부 이동 기록 API는 프론트엔드가 "실제 서비스 방문" 버튼을 새 탭으로
열기 **직전에** 호출해야 하는 API라는 걸 기획서 §2/§4 흐름에서 알 수 있다 — 이 호출이 빠지면 핵심
전환 지표(KPI) 자체가 집계되지 않는다.

### `src/main/java/com/skala/clickhub/controller/SearchController.java`

**역할 및 구현 기능** `GET /v1/search?q=&category=&tags=&tech=&cursor=` 하나. 인증: 선택.

**설계 목적 및 아키텍처적 의의** 쿼리 파라미터 구성이 기획서 §5의 3단계 검색 파이프라인(키워드 추출 →
메타데이터 필터링 → 벡터 유사도)을 그대로 반영한다 — `q`(원문 질의), `category`/`tags`/`tech`(메타데이터
필터), `cursor`(페이지네이션). 이 컨트롤러 자체는 파이프라인을 구현하지 않지만, **파라미터 시그니처가
이미 그 파이프라인을 전제로 설계**되어 있다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 검색 UI는 이 4개 파라미터(검색어, 카테고리, 태그, 기술 스택)를
입력받는 필터 폼을 만들면 된다. 검색 서비스를 구현할 사람은 `search_requests` 테이블에 로그를 남기는
책임도 이 엔드포인트 안에서 함께 져야 한다는 걸 앞선 `SearchRequest` 엔티티 설명과 연결해서 이해해야
한다.

### `src/main/java/com/skala/clickhub/controller/FeedController.java`

**역할 및 구현 기능** `GET /v1/feed?cursor=` 하나. 인증: 선택.

**설계 목적 및 아키텍처적 의의** `SearchController`와 파라미터가 거의 같아 보이지만 필터 파라미터
(`category`, `tags`, `tech`)가 없다 — 피드는 "사용자가 조건을 입력해서 찾는" 화면이 아니라 "알고리즘이
알아서 추천하는" 화면이기 때문에, API 계약 자체가 그 차이를 반영한다(기획서 §4 "홈은 인기·최신·개인화를
분리하지 않고 하나의 종합 피드로 보여준다").

**팀 협업 및 확장 관점의 의미** 추천 알고리즘(§6)을 구현할 사람은 이 API 안에서 로그인 여부에 따라
협업 필터링/콘텐츠 기반/인기 후보를 섞는 로직을 캡슐화해야 한다 — 프론트엔드는 로그인 여부를 신경 쓸
필요 없이 그냥 이 API 하나만 호출하면 된다(개인화 여부는 서버가 토큰 유무로 알아서 판단).

### `src/main/java/com/skala/clickhub/controller/RankingController.java`

**역할 및 구현 기능** `GET /v1/rankings/projects`, `GET /v1/rankings/developers` 2개. 둘 다 인증
없음.

**설계 목적 및 아키텍처적 의의** 두 엔드포인트가 내부적으로는 `project_top100_7d`,
`developer_top100_7d`라는 **DB 뷰**를 조회하게 될 것이다 — 뷰는 JPA `@Entity`로 만들지 않기로
했으므로(엔티티 섹션 5-6, 5-5 참고), 이 컨트롤러/서비스는 스프링 데이터 JPA 리포지토리가 아니라 네이티브
쿼리나 `JdbcTemplate`으로 구현하게 될 가능성이 높다는 걸 미리 알 수 있다.

**팀 협업 및 확장 관점의 의미** 프론트엔드 Top 100 화면과 개발자 랭킹 화면은 완전히 별개의 API를
호출한다는 걸 명확히 알 수 있다(하나의 API로 합쳐지지 않는다). 캐싱을 고려할 사람에게는 "이 두 값은
최근 7일 집계라 자주 안 바뀌므로 캐시하기 좋은 후보"라는 최적화 힌트도 준다.

### `src/main/java/com/skala/clickhub/controller/ReactionController.java`

**역할 및 구현 기능** `PUT /v1/projects/{id}/like`(좋아요 토글, 로그인 필요),
`POST /v1/projects/{id}/comments`(댓글 작성, 로그인 필요). `{id}`는 `UUID`.

**설계 목적 및 아키텍처적 의의** `PUT`을 좋아요에, `POST`를 댓글에 쓴 것은 HTTP 메서드 시맨틱을 의도적으로
구분한 것이다 — 좋아요는 "이 상태를 이렇게 만들어라"(멱등적 설정, `PUT`에 어울림)에 가깝고, 댓글 작성은
"새 리소스를 하나 만들어라"(비멱등적 생성, `POST`에 어울림)이기 때문이다. 좋아요/즐겨찾기가 같은
`ProjectReaction` 테이블을 쓰지만 좋아요만 이 컨트롤러에 있고 즐겨찾기는 별도 컨트롤러
(`FavoriteController`)에 있다는 것도 주목할 점이다 — **데이터 계층은 통합되어 있어도 API/화면 계층은
분리될 수 있다**는 걸 보여주는 사례다.

**팀 협업 및 확장 관점의 의미** 서비스 레이어 구현자는 "사용자당 프로젝트 1회"(기획서 7장) 규칙을
지키기 위해, `ProjectReaction`의 복합키 존재 여부를 확인 후 토글하는 로직을 짜야 한다 — 이 규칙은 DB
기본키 유일성으로도 이중 보장되어 있으므로, 동시 요청이 와도 데이터가 깨질 걱정은 없다.

### `src/main/java/com/skala/clickhub/controller/FavoriteController.java`

**역할 및 구현 기능** `PUT /v1/projects/{id}/favorite` 하나. 로그인 필요, `{id}`는 `UUID`.

**설계 목적 및 아키텍처적 의의** `ReactionController`와 물리적으로 분리된 이유를 앞서 설명했다 — 좋아요는
Top 100 집계에 반영되는 "공개 신호"고, 즐겨찾기는 반영되지 않는 "개인 저장 신호"라는 정책 차이(기획서
7장 표)가 컨트롤러 분리로도 이어진 것이다.

**팀 협업 및 확장 관점의 의미** "마이페이지 > 즐겨찾기 목록" 화면을 만들 사람은 이 컨트롤러가 토글만
제공하고 목록 조회는 제공하지 않는다는 걸 발견하게 될 것이다 — `GET /v1/favorites` 같은 목록 조회
API가 아직 스캐폴딩되지 않은 후속 작업 대상이라는 걸 알 수 있다.

### `src/main/java/com/skala/clickhub/controller/SubscribeController.java`

**역할 및 구현 기능** `PUT /v1/creators/{id}/subscription` 하나. 로그인 필요, `{id}`는 제작자(User)의
`UUID`.

**설계 목적 및 아키텍처적 의의** URL 경로가 `/v1/creators/{id}`로 시작해서 `/v1/projects/{id}`와 확연히
다르다는 게 중요하다 — 이건 실수로 프로젝트 ID를 넣지 않도록 URL 설계 자체가 "이건 제작자 ID를 받는
API"라는 걸 명확히 드러낸다(`CreatorSubscription` 엔티티가 `project_id`를 전혀 참조하지 않는다는 것과
정확히 대응된다).

**팀 협업 및 확장 관점의 의미** 프론트엔드가 프로젝트 상세 화면에서 "이 프로젝트의 제작자 구독하기"
버튼을 만들 때, 프로젝트가 아니라 `project.owner.id`를 이 API에 넘겨야 한다는 걸 헷갈리지 않아야 한다.

### `src/main/java/com/skala/clickhub/controller/DashboardController.java`

**역할 및 구현 기능** `GET /v1/dashboard/projects/{id}?period=` 하나. **인증: 소유자**, `{id}`는
`UUID`.

**설계 목적 및 아키텍처적 의의** "소유자"라는 인증 수준은 `SecurityConfig`의 URL 패턴 매칭만으로는
표현할 수 없는 **행(row) 단위 권한**이다 — 로그인은 했지만 남의 프로젝트 대시보드를 보려는 요청을
막으려면, 서비스 레이어에서 `project.getOwner().getId().equals(현재로그인한사용자ID)`를 확인하고
아니면 `ErrorCode.NOT_PROJECT_OWNER`로 `BusinessException`을 던지는 코드를 반드시 추가해야 한다 — 이건
`GlobalExceptionHandler`가 이미 처리 준비가 되어 있는 케이스이므로, 서비스 레이어는 예외를 던지기만
하면 된다.

**팀 협업 및 확장 관점의 의미** 보안 담당자/QA에게는 "이 API는 URL 레벨 필터만으로는 보호되지 않으니
반드시 별도 소유권 검사 테스트 케이스가 있어야 한다"는 걸 명확히 알려주는 지점이다.

### `src/main/java/com/skala/clickhub/controller/InsightController.java`

**역할 및 구현 기능** `GET /v1/insights/weekly` 하나. 인증: 선택.

**설계 목적 및 아키텍처적 의의** 파라미터가 없다 — 항상 "가장 최근에 발행된 주간 인사이트"를 반환한다는
뜻이다. 특정 주차를 지정해서 과거 인사이트를 조회하는 기능은 지금 스펙에 없다는 것도 이 시그니처에서
읽을 수 있다.

**팀 협업 및 확장 관점의 의미** "지난 주 인사이트 다시 보기" 같은 기능이 필요해지면 이 엔드포인트에
`?week=2026-W35` 같은 파라미터를 추가하는 확장이 자연스러울 것이다.

### `src/main/java/com/skala/clickhub/controller/TutorialController.java`

**역할 및 구현 기능** `GET /v1/tutorials?type=&difficulty=&tech=` 하나. 인증: 선택.

**설계 목적 및 아키텍처적 의의** 필터 파라미터가 `Tutorial` 엔티티의 배열 필드(`categorySlugs`,
`technologySlugs`)와 정확히 대응한다. `category` 파라미터가 빠져 있다는 점도 관찰할 만하다 — 기획서
9.2장은 "카테고리·난이도·기술 스택·학습 시간"으로 필터링한다고 했는데, 현재 시그니처에는 카테고리 필터
파라미터가 없다. 이건 실제 구현 시 놓치기 쉬운 지점이라 문서에 명시해 둔다.

**팀 협업 및 확장 관점의 의미** 서비스 레이어를 구현할 사람이나 API 설계를 리뷰하는 사람은 이 빠진
`category` 파라미터를 추가해야 하는지 먼저 확인해야 한다 — 스캐폴딩 단계에서 발견된 작은 스펙 누락이다.

### `src/main/java/com/skala/clickhub/controller/NotificationController.java`

**역할 및 구현 기능** `GET /v1/notifications`(목록), `PATCH /v1/notifications/{id}/read`(읽음 처리).
둘 다 로그인 필요. **`{id}`는 다른 컨트롤러와 달리 `Long`** — `notifications.id`가 UUID가 아니라
bigint identity이기 때문이다.

**설계 목적 및 아키텍처적 의의** 이 컨트롤러가 프로젝트 전체에서 유일하게 `Long` 타입 경로 변수를 쓴다는
사실 자체가, "이 프로젝트의 PK 전략이 테이블마다 다르다"(UUID가 기본이지만 고빈도로 쓰이고 순서가
중요한 로그성 테이블은 bigint identity)는 SQL 설계 원칙을 코드에서 드러내는 대표적인 지점이다. `PATCH`를
쓴 이유는 "읽음 여부"라는 리소스의 일부 속성만 부분적으로 바꾸는 작업이기 때문이다(전체 알림을 다시
생성하는 게 아니므로 `PUT`이 아니라 `PATCH`가 시맨틱상 더 정확하다).

**팀 협업 및 확장 관점의 의미** 프론트엔드/서비스 레이어 개발자는 알림 관련 API를 호출할 때
`UUID.fromString()` 같은 변환을 하면 안 되고 그냥 정수로 다뤄야 한다는 걸 타입 시그니처에서 바로 알 수
있다 — 이런 "타입이 다른 이유"까지 문서화해 두는 게 이 설명서의 목적이다.

---

## 마무리 — 이 문서를 처음 읽는 사람을 위한 요약 지도

1. **요청이 들어오면**: `config/SecurityConfig`(인증 여부 검사) → `security/jwt/JwtAuthenticationFilter`
   (토큰 검증) → `controller/*`(요청 접수, 아직 로직 없음) 순서로 통과한다.
2. **요청/응답의 모양은**: `dto/*`(화면별 계약)로 정의되고, 최종적으로 `common/response/ApiResponse`로
   감싸져 나간다.
3. **실패하면**: 어디서든 `exception/BusinessException`을 던지면 `exception/GlobalExceptionHandler`가
   가로채서 `ApiResponse.error(...)`로 통일해 응답한다.
4. **데이터가 저장되는 곳은**: `entity/*`이고, 여기가 이번 스캐폴딩에서 가장 많은 공을 들인 부분이다 —
   PostgreSQL 전용 타입(배열, jsonb), 복합키(`entity/id/*`), 도메인별 enum까지 `schema (1).sql`과
   1:1로 맞춰져 있다.
5. **아직 없는 것**: `service`/`repository` 레이어(비즈니스 로직, DB 조회)는 의도적으로 완전히
   비어 있다. 이 문서에 나온 모든 "역할"은 전부 **누군가 다음 단계에서 구현해야 할 자리**를 미리
   설명해 둔 것이다.
