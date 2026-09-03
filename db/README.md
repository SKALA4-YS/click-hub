# Click HUB Database

## 구성

- Engine: PostgreSQL 16
- Extension: `pgcrypto`, `pgvector`
- Migration: Flyway
- 최초 스키마: `migration/V1__initial_schema.sql`
- 로컬 데이터: `data/` 바인드 마운트, Git 제외

`V1__initial_schema.sql`은 기본 테이블 22개와 조회 함수·트리거·인덱스·Seed 사전을 생성합니다. Backend의 Flyway 연동이 완료되면 빈 DB에서 최초 1회 자동 실행됩니다.

## 로컬 실행

루트 `.env.example`을 참고해 추적되지 않는 `.env`를 만들거나 기본 로컬 값을 사용합니다.

```bash
docker compose up --build -d
docker compose ps
```

기본 접속 정보:

```text
Host: localhost
Port: 5432
Database: clickhub
Username: clickhub
Password: clickhub-local
```

컨테이너를 내려도 `db/data/`는 남으므로 다음 실행에서 데이터를 재사용합니다.

```bash
docker compose down
```

## 마이그레이션 규칙

- 배포된 `V1__initial_schema.sql`은 수정하지 않습니다.
- 변경은 `V2__short_description.sql`처럼 다음 버전 파일로 추가합니다.
- Schema와 Seed 변경은 같은 PR에서 Backend Entity·테스트 영향도를 확인합니다.
- Hibernate는 `validate`만 사용하고 스키마 생성과 변경은 Flyway가 담당합니다.

## 초기화와 복구

로컬 DB를 처음부터 다시 만들려면 먼저 컨테이너를 내리고 `db/data/`를 별도 이름으로 이동해 백업한 뒤 재기동합니다. 데이터 디렉터리를 자동 삭제하는 스크립트는 제공하지 않습니다.

Render에서는 저장소의 `db/data/`를 사용하지 않습니다. Managed PostgreSQL 저장소를 사용하며 Free DB는 생성 30일 후 만료되고 백업을 제공하지 않으므로 검증 환경으로만 취급합니다.
