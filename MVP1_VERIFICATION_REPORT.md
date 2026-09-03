# Click HUB MVP1 검증 보고서

- 검증일: 2026-09-04
- 대상 브랜치: `feature/mvp1-integration`
- 로컬 환경: macOS, Docker Engine 29.6.1
- 판정: 로컬 MVP 통합 통과, 외부 배포와 실제 Google OAuth는 미검증

## 1. 검증 범위

Vue 화면의 API 전환, Spring Boot API와 PostgreSQL 저장, Flyway demo 데이터, Compose 기동, CORS, 인증 쓰기와 Backend 재시작 후 지속성을 확인했다. 실제 Vercel·Render 리소스 생성과 Google 계정 로그인은 해당 플랫폼 credential과 공개 URL이 없어 이번 로컬 검증에서 제외했다.

## 2. 자동 검사 결과

| 영역 | 실행 내용 | 결과 |
| --- | --- | --- |
| Frontend 단위·통합 | Vitest 전체 | 27개 파일, 94개 테스트 통과 |
| Frontend 정적 검사 | oxlint, ESLint, Prettier | 통과 |
| Frontend build | Vite production build | 통과 |
| Backend 전체 테스트 | Gradle test, PostgreSQL Testcontainers 포함 | 통과 |
| Backend build | Gradle bootJar | 통과 |
| Container build | FE Nginx image, BE Java image | 통과 |
| Compose health | FE, BE, PostgreSQL | 세 컨테이너 healthy |
| Runtime mock 검사 | MVP 화면의 `@/data/mock*`, `*Fixture` import | 0건 |

Backend 테스트는 실제 pgvector PostgreSQL에서 프로젝트 반응, 댓글, 프로필, 온보딩, 카탈로그와 구독을 저장한 뒤 다시 조회한다.

## 3. 로컬 통합 결과

검증 시 호스트의 기존 PostgreSQL이 5432를 사용하고 있어 `DATABASE_PORT=15432`로만 변경했다. 컨테이너 내부 Backend–DB 연결은 `db:5432`를 유지했다.

| 확인 항목 | 근거 | 결과 |
| --- | --- | --- |
| Health | `/actuator/health` | `UP` |
| FE–BE 기본 연결 | `/api/v1/ping` | `status=ok` |
| 공개 피드 | `/v1/feed` | demo 프로젝트 2건 |
| 프로젝트 랭킹 | `/v1/rankings/projects` | 2건, 점수 계산 확인 |
| 튜토리얼 | `/v1/tutorials` | demo 자료 2건 |
| 주간 인사이트 | `/v1/insights/weekly` | 최신 게시 데이터 1건 |
| SPA rewrite | FE `/projects/<uuid>` HEAD | HTTP 200 |
| 허용 CORS | Origin `http://localhost:5173` | 정확한 allow-origin 반환 |
| 차단 CORS | 임의 외부 origin | HTTP 403 |

Flyway는 V1, V2, V3와 repeatable demo migration을 순서대로 적용했다. DB에서 게시 프로젝트 2건, 게시 튜토리얼 2건, 게시 인사이트 1건을 직접 확인했다.

## 4. 인증 쓰기와 지속성

로컬 demo user를 subject로 갖는 단기 HS256 token을 검증 전용으로 생성해 실제 Security Filter와 API를 통과시켰다. 운영 로그인 우회 기능이나 token 발급 endpoint를 코드에 추가하지는 않았다.

다음 작업이 HTTP API를 거쳐 성공했다.

- `/v1/users/me` 사용자 조회
- 온보딩 목표·카테고리·기술 저장
- 프로젝트 좋아요와 즐겨찾기
- 프로젝트 댓글 작성
- 메이커 구독
- 커뮤니티 게시글 작성

Backend 컨테이너를 재시작한 뒤 즐겨찾기와 구독 API가 같은 결과를 반환했다. DB 직접 조회에서도 smoke 댓글 1건, 커뮤니티 글 1건, 온보딩 profile 1건이 유지됐다. 이를 통해 Backend 프로세스 메모리가 아니라 `db/data`에 mount된 PostgreSQL에 저장됨을 확인했다.

재현 가능한 검사는 다음 명령으로 실행한다.

```bash
DATABASE_PORT=15432 docker compose -p click-hub-mvp1 up --build -d
./scripts/mvp1-smoke.sh
```

## 5. 구현 결과

- 공통 API client가 base URL, query, timeout, Bearer token, `ApiResponse`, 401 정리를 처리한다.
- OAuth 성공 응답은 Access Token만 URL fragment로 전달하며 Frontend가 즉시 `sessionStorage`로 이동하고 URL을 정리한다.
- 공개 탐색과 사용자 보관함, 프로젝트·커뮤니티 쓰기, 알림 읽음 처리가 실제 API에 연결됐다.
- 프로젝트 생성은 `DRAFT` 저장 후 `PENDING_REVIEW` 제출로 처리하며 즉시 공개됐다고 표시하지 않는다.
- 이미지 업로드와 AI 자동 분석 등 제외 기능은 MVP1 이후 기능이라고 명시한다.
- demo 콘텐츠는 운영 schema migration과 분리돼 `demo` profile에서만 적용된다.

## 6. 미검증 항목과 다음 조치

| 항목 | 미검증 이유 | 완료 방법 |
| --- | --- | --- |
| 실제 Google 로그인 | Google client credential 없음 | Console URI 등록 후 브라우저 수동 시나리오 수행 |
| Vercel 배포 | CLI·프로젝트 연결 없음 | GitHub main 연결, Root `frontend`, FE env 설정 |
| Render 배포 | CLI·workspace/secret 연결 없음 | Blueprint 생성, `sync:false` secret 입력 |
| 공개 CORS·callback | 공개 URL 미확정 | 양쪽 URL 확정 후 체크리스트 실행 |
| 시각 브라우저 QA | 현재 세션에 제어 가능한 브라우저 없음 | 로컬 또는 배포 URL에서 주요 route 수동 확인 |

따라서 현재 결과는 “로컬에서 실제 동작하는 MVP1”에는 해당하지만 “공개 배포까지 완료된 MVP1”로는 판정하지 않는다. 외부 계정 설정 후 [MVP1_SETUP_AND_DEPLOYMENT.md](./MVP1_SETUP_AND_DEPLOYMENT.md)의 배포 후 점검표를 통과하고 실제 URL을 기록해야 최종 완료다.

