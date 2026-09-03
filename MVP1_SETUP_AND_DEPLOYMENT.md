# Click HUB MVP1 설정·배포 가이드

- 대상: 로컬 개발자와 Vercel·Render 배포 담당자
- 기준일: 2026-09-04
- 구성: Vue 3 + Vite, Spring Boot 4 + Java 21, PostgreSQL 16 + pgvector

## 1. 환경변수 계약

| 위치 | 변수 | 예시·용도 | 비밀정보 |
| --- | --- | --- | --- |
| FE build | `VITE_API_BASE_URL` | `http://localhost:8080` 또는 Render HTTPS origin | 아니요 |
| BE | `CORS_ALLOWED_ORIGINS` | Vercel/로컬 FE origin, 복수 값은 쉼표로 구분 | 아니요 |
| BE | `CLICKHUB_OAUTH2_REDIRECT_URI` | 로그인 성공 후 이동할 FE `/oauth/callback` | 아니요 |
| BE | `SPRING_PROFILES_ACTIVE` | 로컬 `demo`, OAuth 포함 `demo,oauth2` | 아니요 |
| BE | `GOOGLE_CLIENT_ID` | Google Web application client ID | 예 |
| BE | `GOOGLE_CLIENT_SECRET` | Google client secret | 예 |
| BE | `CLICKHUB_JWT_SECRET` | 최소 256-bit 수준의 임의 secret | 예 |
| DB | `DB_HOST`, `DB_PORT`, `DB_NAME` | PostgreSQL 접속 대상 | 일부 |
| DB | `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL 인증 | 예 |

`.env`는 커밋하지 않는다. Vite 변수는 브라우저 번들에 포함되므로 secret을 넣지 않는다.

## 2. 로컬 Compose 실행

### 2.1 기본 실행

```bash
cp .env.example .env
docker compose up --build -d
docker compose ps
./scripts/mvp1-smoke.sh
```

기본 주소는 FE `http://localhost:5173`, BE `http://localhost:8080`, DB host port `5432`다. DB 파일은 `db/data/`에 bind mount되며 Git에서 제외된다. 기본 `demo` profile은 별도 repeatable migration인 `db/demo/R__demo_content.sql`을 적재한다.

호스트 5432가 이미 사용 중이면 컨테이너 내부 주소를 바꾸지 말고 host port만 변경한다.

```bash
DATABASE_PORT=15432 docker compose -p click-hub-mvp1 up --build -d
API_BASE_URL=http://localhost:8080 FRONTEND_URL=http://localhost:5173 \
  ./scripts/mvp1-smoke.sh
```

### 2.2 로컬 Google 로그인

Google Cloud Console에서 OAuth client 유형을 Web application으로 만든다. 로컬 Authorized redirect URI에는 아래 값을 정확히 등록한다.

```text
http://localhost:8080/login/oauth2/code/google
```

`.env`를 다음처럼 변경한 뒤 Backend를 다시 만든다.

```dotenv
SPRING_PROFILES_ACTIVE=demo,oauth2
GOOGLE_CLIENT_ID=발급받은-client-id
GOOGLE_CLIENT_SECRET=발급받은-client-secret
CLICKHUB_OAUTH2_REDIRECT_URI=http://localhost:5173/oauth/callback
```

Google은 redirect URI의 scheme, host, path와 trailing slash가 등록값과 정확히 일치해야 한다. localhost는 개발용 HTTP 예외가 허용되지만 운영 URI는 HTTPS를 사용한다. 자세한 규칙은 [Google OAuth 2.0 Web Server 문서](https://developers.google.com/identity/protocols/oauth2/web-server)를 따른다.

### 2.3 중지와 DB 초기화

```bash
docker compose down
```

DB를 처음부터 다시 만들려면 먼저 컨테이너를 내리고 `db/data`를 삭제하는 대신 백업 이름으로 이동한 후 재기동한다.

```bash
mv db/data "db/data.backup.$(date +%Y%m%d-%H%M%S)"
docker compose up --build -d
```

Flyway는 V1~V3 이후 demo migration을 적용한다. `demo` profile을 빼면 카탈로그만 존재하고 데모 사용자·프로젝트·튜토리얼·인사이트는 생성되지 않는다.

## 3. 직접 실행과 품질 검사

DB를 Compose로 실행하고 애플리케이션만 직접 실행할 수도 있다.

```bash
DATABASE_PORT=15432 docker compose up -d db

cd backend
DB_PORT=15432 SPRING_PROFILES_ACTIVE=demo ./gradlew bootRun

cd ../frontend
npm ci
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

커밋 전 전체 검사는 다음과 같다.

```bash
cd frontend
npm run test:unit -- --run
npm run lint
npx prettier --check src
npm run build

cd ../backend
./gradlew clean test bootJar
```

## 4. Render Backend와 PostgreSQL

`render.yaml`은 저장소 루트의 Docker build context를 사용해 Backend와 PostgreSQL을 함께 만든다. Render Blueprint는 기본적으로 저장소 루트의 `render.yaml`을 사용하며, `sync: false` 변수는 최초 생성 화면에서 값을 입력받는다. 기존 Blueprint에 새 `sync: false` 변수를 추가하면 자동으로 값이 생기지 않으므로 Render Dashboard에서 직접 채워야 한다. 이 동작과 `fromDatabase`, `generateValue`, `checksPass` 설정은 [Render Blueprint 명세](https://render.com/docs/blueprint-spec)에서 확인할 수 있다.

### 4.1 생성 순서

1. GitHub의 `main`에 검증된 변경을 병합한다.
2. Render Dashboard에서 New Blueprint를 선택하고 `SKALA4-YS/click-hub`를 연결한다.
3. `click-hub-backend`, `click-hub-db`가 생성되는지 확인한다.
4. 다음 수동 값을 입력한다.

```text
CORS_ALLOWED_ORIGINS=https://<vercel-project>.vercel.app
CLICKHUB_OAUTH2_REDIRECT_URI=https://<vercel-project>.vercel.app/oauth/callback
GOOGLE_CLIENT_ID=<secret>
GOOGLE_CLIENT_SECRET=<secret>
```

`CLICKHUB_JWT_SECRET`은 Blueprint의 `generateValue`, DB 변수는 `fromDatabase`로 자동 연결된다. `SPRING_PROFILES_ACTIVE=oauth2,demo`는 첫 MVP 공개 데이터와 Google 로그인을 함께 활성화한다.

### 4.2 Render 확인

```bash
curl -fsS https://<render-service>.onrender.com/actuator/health
curl -fsS https://<render-service>.onrender.com/api/v1/ping
curl -fsS https://<render-service>.onrender.com/v1/feed
```

Docker 서비스의 secret을 build-time `ARG`로 사용하지 않는다. Render는 Docker 서비스 환경변수를 build argument로도 제공할 수 있으므로, 이미지 레이어에 secret이 들어가지 않게 해야 한다. 관련 주의사항은 [Render Docker 문서](https://render.com/docs/docker)에 설명돼 있다.

## 5. Vercel Frontend

Vercel에서 같은 GitHub 저장소를 Import한 뒤 Root Directory를 `frontend`로 지정한다. 모노레포의 앱별 Root Directory 설정은 [Vercel Monorepo 문서](https://vercel.com/docs/monorepos)를 따른다.

Project Settings의 Production Environment Variables에 아래 값을 등록한다.

```text
VITE_API_BASE_URL=https://<render-service>.onrender.com
```

Vite는 `VITE_` 접두사 변수를 build 시점에 읽는다. 값을 바꾼 뒤 반드시 재배포한다. [Vercel Vite 문서](https://vercel.com/docs/frameworks/frontend/vite)도 Vite에서 노출할 변수가 `VITE_` 접두사를 사용한다고 안내한다. `frontend/vercel.json`은 `/projects/...`, `/oauth/callback` 같은 SPA 경로를 `index.html`로 rewrite한다.

## 6. 운영 Google OAuth 연결 순서

1. Render Backend URL과 Vercel Frontend URL을 확정한다.
2. Google OAuth client의 Authorized redirect URI에 다음을 추가한다.

```text
https://<render-service>.onrender.com/login/oauth2/code/google
```

3. Render의 `CLICKHUB_OAUTH2_REDIRECT_URI`를 다음으로 설정한다.

```text
https://<vercel-project>.vercel.app/oauth/callback
```

4. Vercel의 `VITE_API_BASE_URL`과 Render의 `CORS_ALLOWED_ORIGINS`를 서로의 HTTPS origin으로 맞춘다.
5. 두 서비스를 재배포한다.
6. Vercel의 로그인 버튼부터 시작해 Google 동의, FE callback, `/v1/users/me`, 온보딩까지 확인한다.

Backend callback과 Frontend callback은 서로 다르다. Google은 Backend의 `/login/oauth2/code/google`로 돌아오고, Backend가 JWT를 발급한 뒤 Frontend의 `/oauth/callback#accessToken=...`으로 이동시킨다. Frontend는 token을 `sessionStorage`로 옮긴 후 주소 fragment를 지운다.

## 7. 배포 후 점검표

- [ ] Render health와 ping이 200이다.
- [ ] Vercel `/projects/<uuid>` 직접 접근이 404가 아니다.
- [ ] Vercel origin의 CORS preflight에 `Access-Control-Allow-Origin`이 정확히 반환된다.
- [ ] 임의의 다른 origin은 CORS에서 거부된다.
- [ ] Google 로그인 후 URL에 token fragment가 남지 않는다.
- [ ] 새 사용자의 `/v1/users/me`와 온보딩 저장이 정상이다.
- [ ] 프로젝트 등록 후 마이페이지에 `PENDING_REVIEW` 상태가 보인다.
- [ ] 좋아요, 즐겨찾기, 댓글, 구독이 새로고침 후 유지된다.
- [ ] 커뮤니티 글·댓글과 알림 읽음 상태가 유지된다.

## 8. 장애 대응

| 증상 | 우선 확인 |
| --- | --- |
| FE에서 `Failed to fetch` | `VITE_API_BASE_URL`, Render 기동 상태, HTTPS 여부 |
| CORS 오류 | `CORS_ALLOWED_ORIGINS`의 정확한 origin과 trailing slash 제거 |
| `redirect_uri_mismatch` | Google 등록 URI가 Render callback과 문자 단위로 일치하는지 확인 |
| OAuth 시작 경로가 401/404 | Render profile이 `oauth2,demo`인지, Google secret이 설정됐는지 확인 |
| 화면 데이터가 비어 있음 | `demo` profile과 Flyway `demo content` 적용 로그 확인 |
| DB 연결 실패 | Render DB `fromDatabase` 연결과 같은 region 확인 |
| 첫 요청이 느림 | Free Web Service cold start 여부 확인 |

Render Free Web Service는 15분 동안 inbound traffic이 없으면 중지되고 다음 요청에서 약 1분의 재기동 시간이 발생할 수 있다. Free PostgreSQL은 1GB, 생성 후 30일 만료, 백업 미지원 조건이므로 운영 데이터 저장소로 사용하지 않는다. 최신 제한은 [Render Free 서비스 문서](https://render.com/docs/free)에서 확인한다.

## 9. 현재 배포 상태

- 로컬 Compose: 검증 완료
- GitHub 기능 브랜치: PR #31 생성 완료
- Vercel: PR Preview build 성공, Deployment Protection으로 비로그인 공개 검증은 대기
- Render: 실제 Blueprint 생성과 secret 입력 필요
- Google OAuth: 실제 client credential과 운영 redirect URI 등록 필요

Preview URL이 Vercel SSO로 이동하면 빌드 실패가 아니라 Deployment Protection 정책이다. 팀원이 공개 URL로 함께 검증하려면 Vercel Project Settings에서 보호 범위를 확인하거나, 허용된 계정으로 접속하거나, production 배포 후 공개 URL에서 점검한다. 보호를 변경하기 전에는 프로젝트의 접근 정책을 먼저 확인한다.

운영 배포가 끝나면 실제 FE·BE URL과 검증 일시를 [MVP1_VERIFICATION_REPORT.md](./MVP1_VERIFICATION_REPORT.md)에 추가한다.
