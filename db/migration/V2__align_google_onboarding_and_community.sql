-- Align the deployed V1 schema with the current Click HUB domain contract.
BEGIN;

DO $$ BEGIN
  CREATE TYPE social_login_provider AS ENUM ('GOOGLE', 'GITHUB');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE community_post_status AS ENUM ('PUBLISHED', 'DELETED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

ALTER TABLE users
  ADD COLUMN IF NOT EXISTS auth_provider social_login_provider,
  ADD COLUMN IF NOT EXISTS google_subject varchar(255);

-- Existing GitHub users can be migrated safely. Earlier rows with no provider
-- remain NULL because their original identity provider cannot be reconstructed.
UPDATE users
SET auth_provider = 'GITHUB'
WHERE auth_provider IS NULL
  AND github_user_id IS NOT NULL;

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS users_auth_provider_identity_ck;

ALTER TABLE users
  ADD CONSTRAINT users_auth_provider_identity_ck CHECK (
    auth_provider IS NULL
    OR (auth_provider = 'GOOGLE' AND nullif(btrim(google_subject), '') IS NOT NULL)
    OR auth_provider = 'GITHUB'
  );

ALTER TABLE users
  DROP CONSTRAINT IF EXISTS users_google_subject_nonempty_ck;

ALTER TABLE users
  ADD CONSTRAINT users_google_subject_nonempty_ck
  CHECK (google_subject IS NULL OR btrim(google_subject) <> '');

CREATE UNIQUE INDEX IF NOT EXISTS users_google_subject_uq
ON users (google_subject)
WHERE google_subject IS NOT NULL;

CREATE TABLE IF NOT EXISTS user_onboarding_interest_categories (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  category_id uuid NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, category_id)
);

CREATE INDEX IF NOT EXISTS user_onboarding_interest_categories_category_idx
ON user_onboarding_interest_categories (category_id, user_id);

CREATE TABLE IF NOT EXISTS user_onboarding_profiles (
  user_id uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
  goals text[] NOT NULL DEFAULT '{}'::text[] CHECK (is_nonempty_text_array(goals)),
  completed_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_onboarding_interest_technologies (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  technology_id uuid NOT NULL REFERENCES technologies(id) ON DELETE RESTRICT,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, technology_id)
);

CREATE INDEX IF NOT EXISTS user_onboarding_interest_technologies_technology_idx
ON user_onboarding_interest_technologies (technology_id, user_id);

DROP TRIGGER IF EXISTS user_onboarding_profiles_updated_at ON user_onboarding_profiles;
CREATE TRIGGER user_onboarding_profiles_updated_at
BEFORE UPDATE ON user_onboarding_profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- GitHub identity fields remain for backwards compatibility but are no longer
-- required before a project can be created.
CREATE OR REPLACE FUNCTION validate_project_write()
RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  expected_hash text;
BEGIN
  IF TG_OP = 'INSERT' THEN
    IF NEW.status <> 'DRAFT' THEN
      RAISE EXCEPTION '새 프로젝트는 DRAFT 상태로 등록해야 합니다.';
    END IF;
  ELSIF NEW.status IS DISTINCT FROM OLD.status THEN
    IF NOT (
      (OLD.status = 'DRAFT' AND NEW.status = 'PENDING_REVIEW')
      OR (OLD.status = 'PENDING_REVIEW' AND NEW.status IN ('PUBLISHED', 'REJECTED'))
      OR (OLD.status = 'REJECTED' AND NEW.status = 'DRAFT')
      OR (OLD.status = 'PUBLISHED' AND NEW.status = 'ARCHIVED')
      OR (OLD.status = 'ARCHIVED' AND NEW.status = 'DRAFT')
    ) THEN
      RAISE EXCEPTION '허용되지 않은 프로젝트 상태 변경입니다: % -> %', OLD.status, NEW.status;
    END IF;
  END IF;

  IF NEW.status = 'PUBLISHED' THEN
    expected_hash := encode(digest(NEW.site_url, 'sha256'), 'hex');
    IF NEW.primary_category_id IS NULL THEN
      RAISE EXCEPTION '게시 전 주 카테고리가 필요합니다.';
    END IF;
    IF NEW.url_checked_at IS NULL
       OR NEW.url_checked_at < clock_timestamp() - interval '7 days'
       OR NOT NEW.url_is_reachable
       OR NEW.url_http_status NOT BETWEEN 200 AND 399
       OR NEW.url_final_url IS NULL
       OR NEW.url_validation_hash IS DISTINCT FROM expected_hash
    THEN
      RAISE EXCEPTION '현재 서비스 URL에 대한 최근의 성공 검증이 필요합니다.';
    END IF;
    NEW.published_at := coalesce(NEW.published_at, clock_timestamp());
    NEW.archived_at := NULL;
    NEW.rejection_reason := NULL;
  ELSIF NEW.status = 'REJECTED' THEN
    NEW.published_at := NULL;
    NEW.archived_at := NULL;
  ELSIF NEW.status = 'ARCHIVED' THEN
    NEW.archived_at := coalesce(NEW.archived_at, clock_timestamp());
  ELSIF NEW.status = 'DRAFT' THEN
    NEW.published_at := NULL;
    NEW.archived_at := NULL;
    NEW.rejection_reason := NULL;
  END IF;

  RETURN NEW;
END;
$$;

-- V1 supported HIDDEN posts. The current contract has only PUBLISHED/DELETED,
-- so hidden posts are preserved as deleted posts rather than being removed.
UPDATE community_posts
SET status = 'DELETED',
    deleted_at = coalesce(deleted_at, clock_timestamp())
WHERE status = 'HIDDEN';

DO $$
DECLARE
  constraint_name text;
BEGIN
  FOR constraint_name IN
    SELECT conname
    FROM pg_constraint
    WHERE conrelid = 'community_posts'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) ~ '(status|deleted_at)'
  LOOP
    EXECUTE format('ALTER TABLE community_posts DROP CONSTRAINT %I', constraint_name);
  END LOOP;
END;
$$;

-- V1's partial index uses a varchar status predicate. Recreate it after the
-- column becomes an enum so PostgreSQL does not need a non-immutable cast.
DROP INDEX IF EXISTS community_posts_board_list_idx;

ALTER TABLE community_posts
  ALTER COLUMN status DROP DEFAULT,
  ALTER COLUMN status TYPE community_post_status USING status::community_post_status,
  ALTER COLUMN status SET DEFAULT 'PUBLISHED'::community_post_status;

ALTER TABLE community_posts
  ADD CONSTRAINT community_posts_status_deleted_ck CHECK (
    (status = 'PUBLISHED'::community_post_status AND deleted_at IS NULL)
    OR (status = 'DELETED'::community_post_status AND deleted_at IS NOT NULL)
  );

CREATE INDEX IF NOT EXISTS community_boards_active_order_idx
ON community_boards (display_order, id) WHERE is_active;

CREATE INDEX IF NOT EXISTS community_posts_board_created_idx
ON community_posts (board_id, created_at DESC, id)
WHERE status = 'PUBLISHED'::community_post_status;

CREATE INDEX IF NOT EXISTS community_post_comments_post_created_idx
ON community_post_comments (post_id, created_at, id) WHERE deleted_at IS NULL;

CREATE OR REPLACE FUNCTION validate_community_comment_parent()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.parent_id IS NOT NULL AND EXISTS (
    SELECT 1
    FROM community_post_comments parent
    WHERE parent.id = NEW.parent_id
      AND parent.post_id = NEW.post_id
      AND parent.parent_id IS NOT NULL
  ) THEN
    RAISE EXCEPTION '커뮤니티 댓글은 1단계 대댓글까지만 허용됩니다.';
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS community_post_comments_validate_parent ON community_post_comments;
CREATE TRIGGER community_post_comments_validate_parent
BEFORE INSERT OR UPDATE OF post_id, parent_id ON community_post_comments
FOR EACH ROW EXECUTE FUNCTION validate_community_comment_parent();

INSERT INTO community_boards (slug, name, description, display_order) VALUES
  ('notice', '공지', 'Click HUB 운영 공지', 1),
  ('free', '자유', '자유롭게 이야기하는 공간', 2),
  ('share', '정보공유', '프로젝트와 개발 정보를 공유하는 공간', 3),
  ('qna', '질문답변', '개발과 프로젝트 관련 질문답변', 4)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  display_order = EXCLUDED.display_order;

COMMIT;
