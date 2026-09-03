# Backend PostgreSQL 전환 변경 내역

## 목적

Frontend–Backend–PostgreSQL 구성을 로컬 Docker Compose와 Render 운영 환경에서 동일하게 사용합니다. DB 스키마의 기준은 `db/migration/V1__initial_schema.sql`이며, Backend 시작 시 Flyway가 최초 1회 적용합니다.

이 문서는 PostgreSQL 통합을 위해 실제로 적용한 Backend 변경과 BE 담당자의 확인사항을 공유합니다.

## 환경변수 계약

| 변수 | 로컬 기본값 | 설명 |
| --- | --- | --- |
| `DB_HOST` | `localhost` | PostgreSQL 호스트, Compose에서는 `db` |
| `DB_PORT` | `5432` | PostgreSQL 내부 포트 |
| `DB_NAME` | `clickhub` | Database 이름 |
| `DB_USERNAME` | `clickhub` | 접속 사용자 |
| `DB_PASSWORD` | `clickhub-local` | 로컬 전용 기본값, Render에서는 자동 생성 값 |
| `DB_POOL_MAX_SIZE` | `5` | Free DB용 Hikari 최대 연결 수 |

## 적용 변경사항

### 1. `backend/build.gradle`

- `com.mysql:mysql-connector-j`를 제거했습니다.
- PostgreSQL JDBC Driver를 추가했습니다.
- Spring Boot Flyway Starter와 PostgreSQL 모듈을 추가했습니다.
- 저장소 루트의 `db/migration`을 JAR의 `db/migration`에 포함했습니다.
- DB 통합 테스트를 위해 Spring Boot Testcontainers, JUnit Jupiter, PostgreSQL 모듈을 추가했습니다.

예상 구성:

```groovy
implementation 'org.springframework.boot:spring-boot-starter-flyway'
implementation 'org.flywaydb:flyway-database-postgresql'
runtimeOnly 'org.postgresql:postgresql'

testImplementation 'org.springframework.boot:spring-boot-testcontainers'
testImplementation 'org.testcontainers:testcontainers-junit-jupiter'
testImplementation 'org.testcontainers:testcontainers-postgresql'

tasks.named('processResources') {
    from('../db/migration') {
        into 'db/migration'
    }
}
```

Spring Boot dependency management가 제공하는 호환 버전을 사용하고 개별 버전은 임의로 고정하지 않습니다.

### 2. `backend/src/main/resources/application.properties`

- `spring.profiles.default=nodb`를 제거했습니다.
- MySQL JDBC URL을 아래 PostgreSQL 설정으로 교체했습니다.
- Hibernate는 스키마를 생성하지 않고 `validate`만 수행합니다.
- Flyway 연결 재시도와 소형 연결 풀을 설정했습니다.

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

DB 없는 테스트 또는 비상 기동에서만 명시적으로 사용합니다. 기존 JDBC/JPA 자동 구성 제외를 유지하고 Flyway를 껐습니다.

```properties
spring.flyway.enabled=false
```

### 4. `backend/Dockerfile`

Flyway 리소스가 Backend JAR에 포함되도록 Docker build context를 저장소 루트 기준으로 변경했습니다.

- 작업 경로: `/workspace/backend`
- Gradle 관련 COPY 원본: `backend/gradlew`, `backend/settings.gradle`, `backend/build.gradle`, `backend/gradle`
- 소스 COPY 원본: `backend/src`
- 마이그레이션 COPY 원본: `db/migration` → `/workspace/db/migration`
- Runtime stage와 non-root `spring` 사용자는 유지합니다.
- 루트 `.dockerignore`에서 `.env`, `db/data`, 로컬 산출물과 Frontend를 Backend build context에서 제외했습니다.

DevOps 설정도 다음과 같이 함께 변경했습니다.

- Compose Backend build: `context: .`, `dockerfile: backend/Dockerfile`
- Render Backend build: Root Directory 제거, `dockerfilePath: ./backend/Dockerfile`, `dockerContext: .`

### 5. 기존 테스트

DB가 필요하지 않은 Spring Context 테스트에는 `nodb` 프로필을 명시했습니다.

- `ClickHubApplicationTests`
- `PingControllerTests`

```java
@ActiveProfiles("nodb")
```

### 6. 신규 DB 통합 테스트

`pgvector/pgvector:pg16` Testcontainer로 실제 애플리케이션 Context를 실행하는 `DatabaseMigrationTests`를 추가했습니다.

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

## 검증 결과

- [x] `./gradlew clean test build --no-daemon` 통과
- [x] DB 없는 기존 Ping/CORS 테스트 통과
- [x] PostgreSQL Testcontainers 통합 테스트 통과
- [x] MySQL 의존성·JDBC URL 제거
- [x] Secret 또는 실제 운영 DB 접속정보 미포함
- [x] 변경된 Backend Docker 이미지 빌드 성공
- [x] 전체 Docker Compose의 DB→BE→FE healthy 확인
- [x] 로컬 health, ping, CORS와 Flyway/Seed 검증

## BE 담당자 확인사항

- 진행 중인 Entity 변경을 `db/migration/V1__initial_schema.sql`의 테이블·컬럼·제약조건과 대조해 주세요.
- 충돌 가능 파일은 `backend/build.gradle`, `backend/Dockerfile`, 두 application properties와 기존 Context 테스트 2개입니다.
- 이미 적용된 V1 SQL을 수정해야 한다면 직접 고치지 말고 변경 내용을 공유해 V2 migration으로 분리합니다.
- PostgreSQL 통합 브랜치의 원격 CI가 통과한 뒤 기존 BE 브랜치를 rebase 또는 merge합니다.
