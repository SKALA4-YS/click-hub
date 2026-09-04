# Click HUB 관리자 로그인 설정 가이드

## 동작 방식

- 프론트엔드 관리자 로그인 주소는 `/admin`이다.
- 백엔드 `POST /v1/admin/session`이 ID와 비밀번호를 검증하고 관리자 JWT를 발급한다.
- 관리자 계정은 일반 Google 사용자와 구분되는 `LOCAL` 인증 사용자로 최초 로그인 시 생성된다.
- 비밀번호는 DB나 Git에 저장하지 않고 백엔드 환경변수에서만 읽는다.
- 기존 `/v1/admin/**` API는 JWT의 `ROLE_ADMIN` 권한을 계속 검사한다.

## 로컬 Docker Compose

Compose의 로컬 초기값은 다음과 같다.

```text
ADMIN_LOGIN_ENABLED=true
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

로컬 실행:

```bash
docker compose up -d --build
```

접속:

```text
http://localhost:5173/admin
```

로컬에서도 공유 네트워크에서 실행한다면 `.env`의 `ADMIN_PASSWORD`를 반드시 변경한다.

## Render 설정

`render.yaml`은 관리자 로그인을 기본적으로 비활성화한다. Backend 서비스의 Environment에서 다음 값을 설정한다.

```text
ADMIN_LOGIN_ENABLED=true
ADMIN_USERNAME=<운영 관리자 ID>
ADMIN_PASSWORD=<길고 고유한 운영 비밀번호>
```

`ADMIN_PASSWORD`에는 최소 20자 이상의 임의 문자열을 권장한다. `admin/admin`은 로컬 확인 전용이며 Render에 사용하지 않는다.

환경변수를 저장한 뒤 Backend를 재배포한다. 배포 과정에서 Flyway V4/V5가 `LOCAL` 인증 타입과 로그인 ID 컬럼을 자동 적용한다. 이후 다음 주소로 접속한다.

```text
https://click-hub-wheat.vercel.app/admin
```

관리자 로그인을 닫으려면 `ADMIN_LOGIN_ENABLED=false`로 바꾸고 Backend를 재배포한다.

## Vercel 설정

관리자 ID와 비밀번호는 Vercel에 등록하지 않는다. 프론트엔드는 기존과 동일하게 Render Backend 주소만 사용한다.

```text
VITE_API_BASE_URL=https://click-hub-backend.onrender.com
```

현재 `frontend/vercel.json`의 SPA rewrite가 `/admin` 직접 접근을 `index.html`로 연결한다. `VITE_API_BASE_URL`이 이미 올바르면 Vercel 환경변수 변경은 필요 없고, 이 기능이 포함된 프론트 커밋을 재배포하면 된다.

## 운영 점검

1. 잘못된 ID 또는 비밀번호로 로그인할 때 `401`인지 확인한다.
2. 올바른 계정으로 로그인한 뒤 `/admin/projects`가 열리는지 확인한다.
3. 일반 Google 사용자 JWT로 `/v1/admin/projects` 호출 시 `403`인지 확인한다.
4. 관리자 로그아웃 후 브라우저 `sessionStorage`의 JWT가 제거되는지 확인한다.

비밀번호를 변경해도 이미 발급된 JWT는 최대 1시간 동안 유효하다. 모든 기존 세션을 즉시 무효화해야 한다면 `CLICKHUB_JWT_SECRET`을 교체해야 하지만, 이 경우 일반 사용자까지 모두 로그아웃된다.
