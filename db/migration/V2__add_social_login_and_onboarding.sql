-- V2: Google 소셜 로그인 지원 + 온보딩 관심 카테고리 저장
-- 기획서 v1.1 §2/§3: "최초 로그인 후 관심 카테고리 온보딩 설문(건너뛰기 가능)" +
-- "소셜 로그인(Google, GitHub)" 요구사항 반영.
-- V1(users)은 GitHub 전용 로그인만 가정했으므로, 이미 있을 수 있는 행은 전부 GITHUB로
-- 백필한 뒤 NOT NULL로 확정한다. V1 파일 자체는 수정하지 않는다.

DO $$ BEGIN
  CREATE TYPE social_login_provider AS ENUM ('GOOGLE', 'GITHUB');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS auth_provider social_login_provider,
  ADD COLUMN IF NOT EXISTS google_subject varchar(255);

UPDATE users SET auth_provider = 'GITHUB' WHERE auth_provider IS NULL;

ALTER TABLE users
  ALTER COLUMN auth_provider SET NOT NULL;

ALTER TABLE users
  ADD CONSTRAINT users_auth_provider_google_subject_check CHECK (
    (auth_provider = 'GOOGLE' AND nullif(btrim(google_subject), '') IS NOT NULL)
    OR auth_provider = 'GITHUB'
  ),
  ADD CONSTRAINT users_google_subject_not_blank_check CHECK (
    google_subject IS NULL OR btrim(google_subject) <> ''
  );

CREATE UNIQUE INDEX IF NOT EXISTS users_google_subject_uq
ON users (google_subject)
WHERE google_subject IS NOT NULL;

-- Onboarding survey: a user may select multiple catalog categories, or skip entirely.
CREATE TABLE IF NOT EXISTS user_onboarding_interest_categories (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  category_id uuid NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, category_id)
);

CREATE INDEX IF NOT EXISTS user_onboarding_interest_categories_category_idx
ON user_onboarding_interest_categories (category_id, user_id);
