# Click HUB

배포된 사이드 프로젝트를 발견하고 실제 서비스 방문으로 연결하는 프로젝트 허브입니다.

이 저장소는 Vue.js Frontend와 Spring Boot Backend를 함께 관리하는 모노레포입니다.

## 저장소 구조

```text
click-hub/
├── frontend/   # Vue 3 + Vite
├── backend/    # Spring Boot + Gradle
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
```

기여 전에는 [CONTRIBUTING.md](CONTRIBUTING.md)를 읽어 주세요.
