-- 비밀번호는 DB에 저장하지 않고 ADMIN_PASSWORD 환경변수로만 검증한다.
-- users에는 JWT subject와 ROLE_ADMIN 권한을 연결할 로컬 로그인 ID만 보관한다.
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS local_login_id varchar(100);

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS users_auth_provider_google_subject_check;

ALTER TABLE users
  ADD CONSTRAINT users_auth_provider_identity_check CHECK (
    (
      auth_provider = 'GOOGLE'
      AND nullif(btrim(google_subject), '') IS NOT NULL
      AND local_login_id IS NULL
    )
    OR
    (
      auth_provider = 'GITHUB'
      AND google_subject IS NULL
      AND local_login_id IS NULL
    )
    OR
    (
      auth_provider = 'LOCAL'
      AND google_subject IS NULL
      AND github_user_id IS NULL
      AND github_login IS NULL
      AND github_connected_at IS NULL
      AND nullif(btrim(local_login_id), '') IS NOT NULL
    )
  );

ALTER TABLE users
  ADD CONSTRAINT users_local_login_id_not_blank_check CHECK (
    local_login_id IS NULL OR btrim(local_login_id) <> ''
  );

CREATE UNIQUE INDEX IF NOT EXISTS users_local_login_id_uq
ON users (lower(local_login_id))
WHERE local_login_id IS NOT NULL;
