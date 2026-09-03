# Click HUB 기여 가이드

## 기본 원칙

- 업무 단위마다 GitHub Issue를 만들고 Project에 연결합니다.
- 구현은 가능한 한 `feature/*` 브랜치에서 진행합니다.
- 커밋과 Pull Request는 한 가지 목적에 집중합니다.
- 비밀키, 토큰, 개인 환경 설정 파일을 커밋하지 않습니다.

## Git Flow

| 브랜치 | 시작 기준 | 병합 대상 | 용도 |
| --- | --- | --- | --- |
| `main` | - | - | 배포 가능한 안정 버전 |
| `develop` | `main` | - | 다음 버전 통합 |
| `feature/*` | `develop` | `develop` | 기능 및 일반 작업 |
| `release/*` | `develop` | `main`, 이후 `develop` 동기화 | 릴리스 준비 |
| `hotfix/*` | `main` | `main`, 이후 `develop` 동기화 | 긴급 수정 |

브랜치 이름 예시:

```text
feature/12-project-card
release/0.1.0
hotfix/login-redirect
```

## 커밋 메시지

Conventional Commits 형식을 사용합니다.

```text
feat: add project card
fix: handle empty search result
docs: update development guide
test: add project service tests
chore: update build configuration
```

## Pull Request

- 기본 대상은 `develop`입니다.
- `main`에는 `release/*` 또는 `hotfix/*`만 병합합니다.
- 관련 Issue, 변경 이유, 검증 결과를 PR 본문에 기록합니다.
- `main` 병합은 최소 1명의 승인이 필요합니다.
- 리뷰 대화를 모두 해결한 뒤 병합합니다.
