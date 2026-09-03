# Click HUB

배포된 사이드 프로젝트를 발견하고 실제 서비스 방문으로 연결하는 프로젝트 허브입니다.

이 저장소는 Vue.js Frontend, Spring Boot Backend와 PostgreSQL Database를 함께 관리하는 모노레포입니다.

## 저장소 구조

```text
click-hub/
├── frontend/   # Vue 3 + Vite
├── backend/    # Spring Boot + Gradle
├── db/         # PostgreSQL migration, ERD, local data
└── .github/    # Issue 및 Pull Request 템플릿
```

## 빠른 시작

Frontend:

```bash
cd frontend
npm ci
npm run dev
```

Backend:

```bash
cd backend
./gradlew bootRun
```

Backend 상태 확인:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/ping
```

## Docker로 실행

FE, BE와 PostgreSQL을 함께 실행합니다.

```bash
docker compose up --build -d
```

- Frontend: <http://localhost:5173>
- Backend health: <http://localhost:8080/actuator/health>
- PostgreSQL: `localhost:5432`

기본 포트가 이미 사용 중이면 호스트 포트를 변경할 수 있습니다.

```bash
FRONTEND_PORT=15173 \
BACKEND_PORT=18080 \
DATABASE_PORT=15432 \
VITE_API_BASE_URL=http://localhost:18080 \
CORS_ALLOWED_ORIGINS=http://localhost:15173 \
docker compose up --build -d
```

컨테이너 상태와 로그를 확인합니다.

```bash
docker compose ps
docker compose logs -f
```

실행을 종료합니다.

```bash
docker compose down
```

Vercel과 Render 운영 배포 절차는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고하세요.

기여 전에는 [CONTRIBUTING.md](CONTRIBUTING.md)를 읽어 주세요.

팀원 개발 전 준비와 기능 병합 이후의 운영 절차는 [TEAM_WORKFLOW.md](TEAM_WORKFLOW.md)를 참고하세요.

PostgreSQL 구성과 마이그레이션 규칙은 [db/README.md](db/README.md), Backend 담당 변경사항은 [BE_POSTGRES_CHANGE_REQUEST.md](BE_POSTGRES_CHANGE_REQUEST.md)를 참고하세요.
