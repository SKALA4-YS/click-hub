# Click HUB 배포 가이드

Click HUB는 하나의 GitHub 모노레포에서 Frontend와 Backend를 독립 배포하고 Backend는 Render Managed PostgreSQL을 사용합니다.

| 애플리케이션 | 플랫폼 | 저장소 Root Directory | 운영 브랜치 |
| --- | --- | --- | --- |
| Frontend | Vercel | `frontend` | `main` |
| Backend | Render Docker Web Service | 저장소 루트 build context | `main` |
| Database | Render PostgreSQL 16 | `render.yaml` | `main` |

## 배포 순서

### 1. Vercel 프로젝트 생성

1. Vercel Dashboard에서 GitHub 저장소 `SKALA4-YS/click-hub`를 Import합니다.
2. Project Name은 `click-hub-frontend`로 지정합니다.
3. Root Directory는 `frontend`, Framework Preset은 Vite, Production Branch는 `main`으로 지정합니다.
4. 첫 배포를 실행하고 생성된 `https://<project>.vercel.app` 운영 URL을 기록합니다.

첫 배포에서는 Backend URL이 아직 없으므로 연결 상태 카드가 실패로 표시될 수 있습니다.

### 2. Render Backend와 PostgreSQL 생성

1. Render Dashboard에서 New Blueprint를 선택하고 같은 GitHub 저장소를 연결합니다.
2. 저장소 루트의 `render.yaml`을 사용합니다.
3. Blueprint가 `click-hub-backend`와 `click-hub-db` 두 리소스를 생성하는지 확인합니다.
4. 초기 Blueprint 입력에서 `CORS_ALLOWED_ORIGINS`를 Vercel 운영 Origin으로 설정합니다.
   - 예: `https://click-hub-frontend.vercel.app`
   - 끝에 `/`를 붙이지 않습니다.
5. DB 접속정보는 `fromDatabase`, JWT Secret은 `generateValue`로 Backend에 자동 등록됩니다.
6. Backend 시작 로그에서 Flyway V1 적용 성공을 확인합니다.
7. Singapore 리전의 Free Web Service와 PostgreSQL 배포를 완료합니다.
8. 아래 주소가 HTTP 200인지 확인합니다.

```bash
curl https://<service>.onrender.com/actuator/health
curl https://<service>.onrender.com/api/v1/ping
```

### 3. Vercel에 Backend URL 연결

1. Vercel Project Settings의 Environment Variables에서 `VITE_API_BASE_URL`을 추가합니다.
2. 값은 Render HTTPS Origin이며 끝에 `/`를 붙이지 않습니다.
   - 예: `https://click-hub-backend.onrender.com`
3. Production 환경에 적용한 뒤 `main`의 최신 커밋을 재배포합니다.
4. Vercel 홈 화면의 Backend connection 카드가 `click-hub-backend` 연결 성공을 표시하는지 확인합니다.

Vite 환경변수는 빌드 시 번들에 포함되므로 값을 바꾼 뒤 반드시 재배포해야 합니다. API 주소는 공개 설정값이며 비밀정보로 사용하면 안 됩니다.

## 로컬 실행

Node와 Java를 직접 실행할 수 있습니다.

```bash
cd backend
./gradlew bootRun
```

```bash
cd frontend
npm ci
npm run dev
```

Docker Compose 기본 포트는 Frontend 5173, Backend 8080, PostgreSQL 5432입니다. DB 데이터는 `db/data/`에 저장되고 Git에는 포함되지 않습니다.

```bash
docker compose up --build -d
```

포트가 충돌하면 FE 빌드 URL과 BE CORS Origin도 함께 변경합니다.

```bash
FRONTEND_PORT=15173 \
BACKEND_PORT=18080 \
DATABASE_PORT=15432 \
VITE_API_BASE_URL=http://localhost:18080 \
CORS_ALLOWED_ORIGINS=http://localhost:15173 \
docker compose up --build -d
```

## 운영 확인

```bash
docker compose ps
docker compose logs -f
docker compose down
```

- Render health: `/actuator/health`
- FE/BE 연결 확인: `/api/v1/ping`
- Render Free 인스턴스는 유휴 상태에서 내려가며 첫 연결에 약 1분이 걸릴 수 있습니다.
- Render Free PostgreSQL은 생성 30일 후 만료되고 백업을 제공하지 않으므로 검증 환경으로만 사용합니다.
- CORS 허용 Origin은 쉼표로 여러 개를 지정할 수 있지만 `*`는 사용하지 않습니다.
- DB 스키마는 Flyway가 `db/migration`의 버전 순서대로 적용하며 Hibernate는 `validate`만 수행합니다.

## 배포 URL

실제 배포가 완료되면 아래 값을 갱신합니다.

- Frontend: 미배포
- Backend: 미배포
- Database: `click-hub-db` 미배포
