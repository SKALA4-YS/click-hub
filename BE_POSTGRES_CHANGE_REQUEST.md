# Backend PostgreSQL 전환 변경 요청

## 목적

Frontend–Backend–PostgreSQL 구성을 로컬 Docker Compose와 Render 운영 환경에서 동일하게 사용합니다. DB 스키마의 기준은 `db/migration/V1__initial_schema.sql`이며, Backend 시작 시 Flyway가 최초 1회 적용합니다.

이 문서는 BE 담당자가 수정할 범위를 명확히 분리하기 위한 요청서입니다. DevOps 작업에서는 아래 `backend/` 파일을 수정하지 않습니다.

## 환경변수 계약

| 변수 | 로컬 기본값 | 설명 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | PostgreSQL 호스트, Compose에서는 `db` |
| `DB_PORT` | `5432` | PostgreSQL 내부 포트 |
| `DB_NAME` | `clickhub` | Database 이름 |
| `DB_USERNAME` | `clickhub` | 접속 사용자 |
| `DB_PASSWORD` | `clickhub-local` | 로컬 전용 기본값, Render에서는 자동 생성 값 |
| `DB_POOL_MAX_SIZE` | `5` | Free DB용 Hikari 최대 연결 수 |

## 필수 변경사항

### 1. `backend/build.gradle`

- `com.mysql:mysql-connector-j`를 제거합니다.
- PostgreSQL JDBC Driver를 추가합니다.
- Flyway Core와 PostgreSQL 모듈을 추가합니다.
- 저장소 루트의 `db/migration`을 JAR의 `db/migration`에 포함합니다.
- DB 통합 테스트를 위해 Spring Boot Testcontainers, JUnit Jupiter, PostgreSQL 모듈을 추가합니다.

예상 구성:

```groovy
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
runtimeOnly 'org.postgresql:postgresql'

testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:junit-jupiter'
testImplementation 'org.testcontainers:postgresql'

tasks.named('processResources') {
    from('../db/migration') {
        into 'db/migration'
    }
}
```

Spring Boot dependency management가 제공하는 호환 버전을 사용하고 개별 버전은 임의로 고정하지 않습니다.

### 2. `backend/src/main/resources/application.properties`

- `spring.profiles.default=nodb`를 제거합니다.
- MySQL JDBC URL을 아래 PostgreSQL 설정으로 교체합니다.
- Hibernate는 스키마를 생성하지 않고 `validate`만 수행합니다.
- Flyway 연결 재시도와 소형 연결 풀을 설정합니다.

```properties
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:clickhub}
spring.datasource.username=${DB_USERNAME:clickhub}
spring.datasource.password=${DB_PASSWORD:clickhub-local}
spring.datasource.hikari.maximum-pool-size=${DB_POOL_MAX_SIZE:5}
spring.datasource.hikari.minimum-idle=1

spring.flyway.enabled=true
spring.flyway.connect-retries=10

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
```

JWT, CORS, Token 만료시간과 서버 포트 설정은 유지합니다.

### 3. `backend/src/main/resources/application-nodb.properties`

DB 없는 테스트 또는 비상 기동에서만 명시적으로 사용합니다. 기존 JDBC/JPA 자동 구성 제외는 유지하고 Flyway를 끕니다.

```properties
spring.flyway.enabled=false
```

### 4. `backend/Dockerfile`

Flyway 리소스가 Backend JAR에 포함되도록 Docker build context를 저장소 루트 기준으로 변경합니다.

- 작업 경로: `/workspace/backend`
- Gradle 관련 COPY 원본: `backend/gradlew`, `backend/settings.gradle`, `backend/build.gradle`, `backend/gradle`
- 소스 COPY 원본: `backend/src`
- 마이그레이션 COPY 원본: `db/migration` → `/workspace/db/migration`
- Runtime stage와 non-root `spring` 사용자는 유지합니다.

이 변경이 준비되면 DevOps 담당자가 다음 두 설정을 동시에 바꿉니다.

- Compose Backend build: `context: .`, `dockerfile: backend/Dockerfile`
- Render Backend build: Root Directory 제거, `dockerfilePath: ./backend/Dockerfile`, `dockerContext: .`

### 5. 기존 테스트

DB가 필요하지 않은 Spring Context 테스트에는 `nodb` 프로필을 명시합니다.

- `ClickHubApplicationTests`
- `PingControllerTests`

```java
@ActiveProfiles("nodb")
```

### 6. 신규 DB 통합 테스트

`pgvector/pgvector:pg16` Testcontainer로 실제 애플리케이션 Context를 실행합니다.

검증 항목:

- Flyway V1 적용 성공
- `flyway_schema_history`에 V1 성공 기록 존재
- `pgcrypto`, `vector` 확장 존재
- public 스키마의 기본 테이블 22개 존재
- 카테고리 14개, 기술 15개 Seed 존재
- Spring Context와 JPA `validate` 성공

## 엔티티 작업 시 주의사항

- SQL이 스키마의 기준이며 `ddl-auto=create` 또는 `update`를 사용하지 않습니다.
- 현재 SQL은 PostgreSQL enum, `uuid`, `jsonb`, 배열, `vector` 타입을 사용합니다.
- 진행 중인 Entity가 있다면 테이블·컬럼명, nullable, enum, 시간 타입과 위 PostgreSQL 전용 타입을 V1 SQL에 맞춥니다.
- pgvector Entity 매핑이 실제로 필요한 시점에만 Hibernate Vector 의존성을 추가합니다.
- 이미 적용된 V1을 수정하지 않고 변경사항은 `V2__*.sql`로 추가합니다.

## 완료 조건

- [ ] `./gradlew test build --no-daemon` 통과
- [ ] DB 없는 기존 Ping/CORS 테스트 통과
- [ ] PostgreSQL Testcontainers 통합 테스트 통과
- [ ] 변경된 Backend Dockerfile 이미지 빌드 성공
- [ ] MySQL 의존성·JDBC URL 잔존 없음
- [ ] Secret 또는 실제 운영 DB 접속정보 커밋 없음
- [ ] BE PR 대상을 `feature/postgresql-integration`으로 지정

## 담당 범위 경계

BE 담당자는 이 문서에 적힌 `backend/` 변경만 수행합니다. `compose.yaml`, `render.yaml`, `.env.example`, `db/`와 배포 문서는 DevOps 담당자가 관리하며, 최종 통합 검증 후 함께 `develop`으로 승격합니다.
