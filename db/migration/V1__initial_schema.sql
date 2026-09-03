-- Click HUB compact schema
-- PostgreSQL 16 + pgvector 0.8+
-- 22 base tables, 2 ranking views

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS vector;

-- -----------------------------------------------------------------------------
-- Types (safe to rerun)
-- -----------------------------------------------------------------------------

DO $$ BEGIN
  CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE theme_preference AS ENUM ('LIGHT', 'DARK', 'SYSTEM');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE project_status AS ENUM (
    'DRAFT', 'PENDING_REVIEW', 'PUBLISHED', 'ARCHIVED', 'REJECTED'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE pricing_type AS ENUM ('FREE', 'PAID', 'FREEMIUM', 'UNKNOWN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE tech_group AS ENUM (
    'FRONTEND', 'BACKEND', 'DATABASE', 'INFRA_DEPLOY', 'AI_DATA'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE reaction_type AS ENUM ('LIKE', 'FAVORITE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE notification_type AS ENUM ('CREATOR_PROJECT_PUBLISHED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE outbox_status AS ENUM ('PENDING', 'PROCESSING', 'DONE', 'FAILED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE actor_kind AS ENUM ('USER', 'ANONYMOUS');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE interaction_event_type AS ENUM (
    'project_impression',
    'project_card_click',
    'project_detail_view',
    'outbound_click',
    'like_set',
    'favorite_set',
    'comment_created',
    'creator_subscribed',
    'search_result_clicked',
    'project_registered',
    'project_published',
    'notification_clicked',
    'tutorial_clicked',
    'insight_viewed'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE analysis_type AS ENUM (
    'AUDIENCE_SUMMARY', 'COMMENT_SUMMARY', 'PROJECT_COMPARISON'
  );
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE tutorial_type AS ENUM ('VIBE_CODING', 'DEVELOPMENT');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE tutorial_difficulty AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- -----------------------------------------------------------------------------
-- Shared validation helpers
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = clock_timestamp();
  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION is_public_http_url(p_url text)
RETURNS boolean
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
RETURN
  p_url IS NOT NULL
  AND p_url ~* '^https?://[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?(?::[0-9]{1,5})?(?:[/?#][^[:space:]]*)?$'
  AND p_url !~* '^https?://(?:localhost|127\.|0\.|10\.|192\.168\.|169\.254\.|172\.(?:1[6-9]|2[0-9]|3[01])\.|\[?::1\]?)(?::|/|$)';

CREATE OR REPLACE FUNCTION is_nonempty_text_array(p_values text[])
RETURNS boolean
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
RETURN
  array_position(p_values, NULL) IS NULL
  AND NOT EXISTS (
    SELECT 1 FROM unnest(p_values) AS value WHERE btrim(value) = ''
  )
  AND cardinality(p_values) = (
    SELECT count(DISTINCT lower(btrim(value))) FROM unnest(p_values) AS value
  );

CREATE OR REPLACE FUNCTION text_array_document(p_values text[])
RETURNS text
LANGUAGE sql
IMMUTABLE
PARALLEL SAFE
RETURN array_to_string(p_values, ' ');

CREATE OR REPLACE FUNCTION is_screenshot_array(p_value jsonb)
RETURNS boolean
LANGUAGE plpgsql
IMMUTABLE
PARALLEL SAFE AS $$
DECLARE
  item jsonb;
BEGIN
  IF jsonb_typeof(p_value) <> 'array' OR jsonb_array_length(p_value) > 12 THEN
    RETURN false;
  END IF;

  FOR item IN SELECT value FROM jsonb_array_elements(p_value)
  LOOP
    IF jsonb_typeof(item) <> 'object'
       OR NOT is_public_http_url(item ->> 'url')
       OR (item ? 'alt' AND length(item ->> 'alt') > 255)
    THEN
      RETURN false;
    END IF;
  END LOOP;

  RETURN true;
END;
$$;

-- -----------------------------------------------------------------------------
-- 1. Identity domain: three 1:1 tables folded into users
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  github_user_id bigint,
  github_login varchar(100),
  github_connected_at timestamptz,
  display_name varchar(100) NOT NULL CHECK (btrim(display_name) <> ''),
  avatar_url text CHECK (avatar_url IS NULL OR is_public_http_url(avatar_url)),
  role user_role NOT NULL DEFAULT 'USER',
  theme theme_preference NOT NULL DEFAULT 'SYSTEM',
  new_project_notifications boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  CHECK (
    (github_user_id IS NULL AND github_login IS NULL AND github_connected_at IS NULL)
    OR
    (github_user_id IS NOT NULL AND github_login IS NOT NULL AND github_connected_at IS NOT NULL)
  ),
  CHECK (github_login IS NULL OR btrim(github_login) <> '')
);

CREATE UNIQUE INDEX IF NOT EXISTS users_github_user_id_uq
ON users (github_user_id)
WHERE github_user_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS users_github_login_uq
ON users (lower(github_login))
WHERE github_login IS NOT NULL;

DROP TRIGGER IF EXISTS users_updated_at ON users;
CREATE TRIGGER users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- -----------------------------------------------------------------------------
-- 2. Catalog domain
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS categories (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
  slug varchar(100) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS technologies (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name varchar(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
  slug varchar(100) NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  default_group tech_group NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS projects (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  primary_category_id uuid REFERENCES categories(id) ON DELETE RESTRICT,

  title varchar(160) NOT NULL CHECK (btrim(title) <> ''),
  description text NOT NULL CHECK (btrim(description) <> ''),
  site_url text NOT NULL CHECK (is_public_http_url(site_url)),
  repository_url text CHECK (repository_url IS NULL OR is_public_http_url(repository_url)),
  pricing pricing_type NOT NULL DEFAULT 'UNKNOWN',
  tags text[] NOT NULL DEFAULT '{}'::text[] CHECK (is_nonempty_text_array(tags)),
  thumbnail_url text CHECK (thumbnail_url IS NULL OR is_public_http_url(thumbnail_url)),
  screenshots jsonb NOT NULL DEFAULT '[]'::jsonb CHECK (is_screenshot_array(screenshots)),

  status project_status NOT NULL DEFAULT 'DRAFT',
  rejection_reason text,
  published_at timestamptz,
  archived_at timestamptz,

  url_checked_at timestamptz,
  url_is_reachable boolean NOT NULL DEFAULT false,
  url_http_status integer CHECK (url_http_status BETWEEN 100 AND 599),
  url_final_url text CHECK (url_final_url IS NULL OR is_public_http_url(url_final_url)),
  url_error_code varchar(100),
  url_validation_hash char(64),

  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),

  CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL),
  CHECK (status <> 'REJECTED' OR nullif(btrim(rejection_reason), '') IS NOT NULL),
  CHECK (status <> 'ARCHIVED' OR archived_at IS NOT NULL),
  CHECK (
    url_checked_at IS NOT NULL
    OR (
      url_is_reachable = false
      AND url_http_status IS NULL
      AND url_final_url IS NULL
      AND url_error_code IS NULL
      AND url_validation_hash IS NULL
    )
  )
);

CREATE TABLE IF NOT EXISTS project_technologies (
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  technology_id uuid NOT NULL REFERENCES technologies(id) ON DELETE RESTRICT,
  technology_group tech_group NOT NULL,
  version varchar(100) CHECK (version IS NULL OR btrim(version) <> ''),
  PRIMARY KEY (project_id, technology_id, technology_group)
);

CREATE INDEX IF NOT EXISTS projects_owner_idx
ON projects (owner_id, created_at DESC);

CREATE INDEX IF NOT EXISTS projects_published_idx
ON projects (published_at DESC, id)
WHERE status = 'PUBLISHED';

CREATE INDEX IF NOT EXISTS projects_category_published_idx
ON projects (primary_category_id, published_at DESC)
WHERE status = 'PUBLISHED';

CREATE INDEX IF NOT EXISTS projects_tags_gin_idx
ON projects USING gin (tags);

CREATE INDEX IF NOT EXISTS project_technologies_technology_idx
ON project_technologies (technology_id, project_id);

CREATE OR REPLACE FUNCTION clear_changed_url_validation()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.site_url IS DISTINCT FROM OLD.site_url THEN
    NEW.url_checked_at := NULL;
    NEW.url_is_reachable := false;
    NEW.url_http_status := NULL;
    NEW.url_final_url := NULL;
    NEW.url_error_code := NULL;
    NEW.url_validation_hash := NULL;
  END IF;
  RETURN NEW;
END;
$$;

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
    IF NOT EXISTS (
      SELECT 1 FROM users u
      WHERE u.id = NEW.owner_id
        AND u.deleted_at IS NULL
        AND u.github_user_id IS NOT NULL
    ) THEN
      RAISE EXCEPTION 'GitHub 연결 사용자만 프로젝트를 등록할 수 있습니다.';
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

DROP TRIGGER IF EXISTS projects_clear_url_validation ON projects;
CREATE TRIGGER projects_clear_url_validation
BEFORE UPDATE OF site_url ON projects
FOR EACH ROW EXECUTE FUNCTION clear_changed_url_validation();

DROP TRIGGER IF EXISTS projects_validate_write ON projects;
CREATE TRIGGER projects_validate_write
BEFORE INSERT OR UPDATE ON projects
FOR EACH ROW EXECUTE FUNCTION validate_project_write();

DROP TRIGGER IF EXISTS projects_updated_at ON projects;
CREATE TRIGGER projects_updated_at
BEFORE UPDATE ON projects
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP FUNCTION IF EXISTS record_project_url_validation(uuid, text, boolean, integer, text);

CREATE OR REPLACE FUNCTION record_project_url_validation(
  p_project_id uuid,
  p_checked_url text,
  p_is_reachable boolean,
  p_http_status integer,
  p_final_url text,
  p_error_code text DEFAULT NULL
)
RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
  IF NOT is_public_http_url(p_checked_url)
     OR (p_http_status IS NOT NULL AND p_http_status NOT BETWEEN 100 AND 599)
     OR (p_final_url IS NOT NULL AND NOT is_public_http_url(p_final_url))
     OR (p_is_reachable AND (p_http_status IS NULL OR p_final_url IS NULL))
     OR (NOT p_is_reachable AND p_http_status IS NULL AND nullif(btrim(p_error_code), '') IS NULL)
  THEN
    RAISE EXCEPTION 'URL 검증 결과의 성공/실패 필드 조합이 올바르지 않습니다.';
  END IF;

  UPDATE projects
  SET url_checked_at = clock_timestamp(),
      url_is_reachable = p_is_reachable,
      url_http_status = p_http_status,
      url_final_url = p_final_url,
      url_error_code = nullif(btrim(p_error_code), ''),
      url_validation_hash = encode(digest(p_checked_url, 'sha256'), 'hex')
  WHERE id = p_project_id
    AND site_url = p_checked_url;

  IF NOT FOUND THEN
    RAISE EXCEPTION '프로젝트가 없거나 검증 URL이 현재 URL과 다릅니다.';
  END IF;
END;
$$;

-- -----------------------------------------------------------------------------
-- 3. Engagement and notification domain
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS project_reactions (
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  type reaction_type NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, project_id, type)
);

CREATE TABLE IF NOT EXISTS project_comments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  author_id uuid NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  body text NOT NULL CHECK (length(btrim(body)) BETWEEN 1 AND 3000),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);

-- Community posts are retained when an author account is physically removed.
-- The application must render a NULL author_id, or users.deleted_at IS NOT NULL,
-- as "알 수 없는 사용자". Posts themselves are soft-deleted via status/deleted_at.
CREATE TABLE IF NOT EXISTS community_boards (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  slug varchar(50) NOT NULL UNIQUE
    CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
  name varchar(100) NOT NULL UNIQUE CHECK (btrim(name) <> ''),
  description varchar(300),
  display_order integer NOT NULL DEFAULT 0 CHECK (display_order >= 0),
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS community_posts (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  board_id uuid NOT NULL REFERENCES community_boards(id) ON DELETE RESTRICT,
  author_id uuid REFERENCES users(id) ON DELETE SET NULL,
  title varchar(200) NOT NULL
    CHECK (length(btrim(title)) BETWEEN 1 AND 200),
  body text NOT NULL CHECK (length(btrim(body)) BETWEEN 1 AND 10000),
  status varchar(20) NOT NULL DEFAULT 'PUBLISHED'
    CHECK (status IN ('PUBLISHED', 'HIDDEN', 'DELETED')),
  view_count integer NOT NULL DEFAULT 0 CHECK (view_count >= 0),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  CHECK (
    (status = 'DELETED' AND deleted_at IS NOT NULL)
    OR (status <> 'DELETED' AND deleted_at IS NULL)
  ),
  CHECK (deleted_at IS NULL OR deleted_at >= created_at)
);

CREATE TABLE IF NOT EXISTS community_post_comments (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  post_id uuid NOT NULL REFERENCES community_posts(id) ON DELETE CASCADE,
  author_id uuid REFERENCES users(id) ON DELETE SET NULL,
  parent_id uuid,
  body text NOT NULL CHECK (length(btrim(body)) BETWEEN 1 AND 3000),
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz,
  UNIQUE (post_id, id),
  FOREIGN KEY (post_id, parent_id)
    REFERENCES community_post_comments(post_id, id) ON DELETE CASCADE,
  CHECK (parent_id IS NULL OR parent_id <> id),
  CHECK (deleted_at IS NULL OR deleted_at >= created_at)
);

CREATE TABLE IF NOT EXISTS creator_subscriptions (
  subscriber_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  creator_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  created_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (subscriber_id, creator_id),
  CHECK (subscriber_id <> creator_id)
);

CREATE TABLE IF NOT EXISTS notification_outbox (
  id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  actor_id uuid REFERENCES users(id) ON DELETE SET NULL,
  type notification_type NOT NULL,
  payload jsonb NOT NULL CHECK (jsonb_typeof(payload) = 'object' AND payload <> '{}'::jsonb),
  status outbox_status NOT NULL DEFAULT 'PENDING',
  attempts integer NOT NULL DEFAULT 0 CHECK (attempts >= 0),
  available_at timestamptz NOT NULL DEFAULT now(),
  locked_at timestamptz,
  processed_at timestamptz,
  last_error text,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (project_id, type)
);

CREATE TABLE IF NOT EXISTS notifications (
  id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  recipient_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  actor_id uuid REFERENCES users(id) ON DELETE SET NULL,
  project_id uuid REFERENCES projects(id) ON DELETE SET NULL,
  type notification_type NOT NULL,
  payload jsonb NOT NULL CHECK (jsonb_typeof(payload) = 'object' AND payload <> '{}'::jsonb),
  created_at timestamptz NOT NULL DEFAULT now(),
  read_at timestamptz,
  UNIQUE (recipient_id, project_id, type)
);

CREATE INDEX IF NOT EXISTS project_reactions_project_type_idx
ON project_reactions (project_id, type, created_at DESC);

CREATE INDEX IF NOT EXISTS project_comments_project_idx
ON project_comments (project_id, created_at DESC)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS community_posts_board_list_idx
ON community_posts (board_id, created_at DESC)
WHERE deleted_at IS NULL AND status = 'PUBLISHED';

CREATE INDEX IF NOT EXISTS community_posts_author_idx
ON community_posts (author_id, created_at DESC)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS community_post_comments_post_idx
ON community_post_comments (post_id, created_at ASC)
WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS community_post_comments_parent_idx
ON community_post_comments (parent_id, created_at ASC)
WHERE parent_id IS NOT NULL AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS creator_subscriptions_creator_idx
ON creator_subscriptions (creator_id, subscriber_id);

CREATE INDEX IF NOT EXISTS notification_outbox_pending_idx
ON notification_outbox (available_at, id)
WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX IF NOT EXISTS notifications_unread_idx
ON notifications (recipient_id, created_at DESC)
WHERE read_at IS NULL;

DROP TRIGGER IF EXISTS project_comments_updated_at ON project_comments;
CREATE TRIGGER project_comments_updated_at
BEFORE UPDATE ON project_comments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS community_boards_updated_at ON community_boards;
CREATE TRIGGER community_boards_updated_at
BEFORE UPDATE ON community_boards
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS community_posts_updated_at ON community_posts;
CREATE TRIGGER community_posts_updated_at
BEFORE UPDATE ON community_posts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS community_post_comments_updated_at ON community_post_comments;
CREATE TRIGGER community_post_comments_updated_at
BEFORE UPDATE ON community_post_comments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE FUNCTION enqueue_publish_notification()
RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
  creator_name text;
BEGIN
  IF OLD.status <> 'PUBLISHED' AND NEW.status = 'PUBLISHED' THEN
    SELECT display_name INTO creator_name FROM users WHERE id = NEW.owner_id;

    INSERT INTO notification_outbox (project_id, actor_id, type, payload)
    VALUES (
      NEW.id,
      NEW.owner_id,
      'CREATOR_PROJECT_PUBLISHED',
      jsonb_build_object(
        'creator_name', creator_name,
        'project_title', NEW.title,
        'thumbnail_url', NEW.thumbnail_url,
        'published_at', NEW.published_at,
        'detail_path', '/projects/' || NEW.id::text
      )
    )
    ON CONFLICT (project_id, type) DO NOTHING;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS projects_enqueue_publish_notification ON projects;
CREATE TRIGGER projects_enqueue_publish_notification
AFTER UPDATE OF status ON projects
FOR EACH ROW EXECUTE FUNCTION enqueue_publish_notification();

CREATE OR REPLACE FUNCTION process_notification_outbox(p_outbox_id bigint)
RETURNS integer
LANGUAGE plpgsql AS $$
DECLARE
  job notification_outbox%ROWTYPE;
  inserted_count integer;
BEGIN
  SELECT * INTO job
  FROM notification_outbox
  WHERE id = p_outbox_id
    AND status IN ('PENDING', 'FAILED')
    AND available_at <= clock_timestamp()
  FOR UPDATE SKIP LOCKED;

  IF NOT FOUND THEN
    RETURN 0;
  END IF;

  UPDATE notification_outbox
  SET status = 'PROCESSING', locked_at = clock_timestamp(), attempts = attempts + 1
  WHERE id = job.id;

  INSERT INTO notifications (recipient_id, actor_id, project_id, type, payload)
  SELECT s.subscriber_id, job.actor_id, job.project_id, job.type, job.payload
  FROM creator_subscriptions s
  JOIN users u ON u.id = s.subscriber_id
  WHERE s.creator_id = job.actor_id
    AND u.deleted_at IS NULL
    AND u.new_project_notifications
  ON CONFLICT (recipient_id, project_id, type) DO NOTHING;

  GET DIAGNOSTICS inserted_count = ROW_COUNT;

  UPDATE notification_outbox
  SET status = 'DONE', processed_at = clock_timestamp(), locked_at = NULL, last_error = NULL
  WHERE id = job.id;

  RETURN inserted_count;
EXCEPTION WHEN OTHERS THEN
  UPDATE notification_outbox
  SET status = 'FAILED', locked_at = NULL, last_error = left(SQLERRM, 2000),
      available_at = clock_timestamp() + interval '5 minutes'
  WHERE id = p_outbox_id;
  RETURN -1;
END;
$$;

-- -----------------------------------------------------------------------------
-- 4. Activity domain: durable pseudonymous actor keys
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS anonymous_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  created_at timestamptz NOT NULL DEFAULT now(),
  last_seen_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL,
  CHECK (last_seen_at >= created_at),
  CHECK (expires_at > created_at)
);

CREATE TABLE IF NOT EXISTS interaction_events (
  id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  event_type interaction_event_type NOT NULL,
  actor_kind actor_kind NOT NULL,
  actor_key uuid NOT NULL,
  project_id uuid REFERENCES projects(id) ON DELETE SET NULL,
  occurred_at timestamptz NOT NULL DEFAULT now(),
  event_date date GENERATED ALWAYS AS ((occurred_at AT TIME ZONE 'UTC')::date) STORED,
  context jsonb NOT NULL DEFAULT '{}'::jsonb CHECK (jsonb_typeof(context) = 'object')
);

CREATE TABLE IF NOT EXISTS search_requests (
  id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  actor_kind actor_kind NOT NULL,
  actor_key uuid NOT NULL,
  raw_query text NOT NULL CHECK (length(btrim(raw_query)) BETWEEN 1 AND 1000),
  parsed_filters jsonb NOT NULL DEFAULT '{}'::jsonb CHECK (jsonb_typeof(parsed_filters) = 'object'),
  used_fallback boolean NOT NULL DEFAULT false,
  result_count integer NOT NULL DEFAULT 0 CHECK (result_count >= 0),
  searched_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS interaction_events_day_project_idx
ON interaction_events (event_date, project_id, event_type, actor_key)
WHERE project_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS interaction_events_project_time_idx
ON interaction_events (project_id, occurred_at DESC)
WHERE project_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS interaction_events_actor_time_idx
ON interaction_events (actor_kind, actor_key, occurred_at DESC);

CREATE INDEX IF NOT EXISTS interaction_events_time_brin_idx
ON interaction_events USING brin (occurred_at) WITH (pages_per_range = 64);

CREATE INDEX IF NOT EXISTS search_requests_time_idx
ON search_requests (searched_at DESC);

CREATE INDEX IF NOT EXISTS search_requests_actor_time_idx
ON search_requests (actor_kind, actor_key, searched_at DESC);

-- -----------------------------------------------------------------------------
-- 5. Analytics domain
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS project_daily_metrics (
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  metric_date date NOT NULL,
  unique_visitors integer NOT NULL DEFAULT 0 CHECK (unique_visitors >= 0),
  impressions integer NOT NULL DEFAULT 0 CHECK (impressions >= 0),
  detail_views integer NOT NULL DEFAULT 0 CHECK (detail_views >= 0),
  valid_outbound_clicks integer NOT NULL DEFAULT 0 CHECK (valid_outbound_clicks >= 0),
  unique_likes integer NOT NULL DEFAULT 0 CHECK (unique_likes >= 0),
  unique_commenters integer NOT NULL DEFAULT 0 CHECK (unique_commenters >= 0),
  abuse_factor numeric(5,4) NOT NULL DEFAULT 1 CHECK (abuse_factor BETWEEN 0 AND 1),
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (project_id, metric_date)
);

CREATE TABLE IF NOT EXISTS creator_daily_metrics (
  creator_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  metric_date date NOT NULL,
  subscriber_growth integer NOT NULL DEFAULT 0,
  active_projects integer NOT NULL DEFAULT 0 CHECK (active_projects >= 0),
  updated_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (creator_id, metric_date)
);

CREATE INDEX IF NOT EXISTS project_daily_metrics_date_idx
ON project_daily_metrics (metric_date, project_id);

CREATE INDEX IF NOT EXISTS creator_daily_metrics_date_idx
ON creator_daily_metrics (metric_date, creator_id);

CREATE OR REPLACE FUNCTION refresh_project_daily_metrics(p_from date, p_to date)
RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
  IF p_from IS NULL OR p_to IS NULL OR p_from > p_to THEN
    RAISE EXCEPTION '올바른 집계 기간이 필요합니다: % ~ %', p_from, p_to;
  END IF;

  PERFORM pg_advisory_xact_lock(hashtextextended('clickhub.project_daily_metrics', 0));

  DELETE FROM project_daily_metrics
  WHERE metric_date BETWEEN p_from AND p_to;

  INSERT INTO project_daily_metrics (
    project_id, metric_date, unique_visitors, impressions, detail_views,
    valid_outbound_clicks, unique_likes, unique_commenters
  )
  SELECT
    project_id,
    event_date,
    count(DISTINCT actor_key) FILTER (WHERE event_type = 'project_detail_view'),
    count(*) FILTER (WHERE event_type = 'project_impression'),
    count(*) FILTER (WHERE event_type = 'project_detail_view'),
    count(DISTINCT actor_key) FILTER (WHERE event_type = 'outbound_click'),
    count(DISTINCT actor_key) FILTER (
      WHERE event_type = 'like_set'
        AND (NOT context ? 'enabled' OR context @> '{"enabled": true}'::jsonb)
    ),
    count(DISTINCT actor_key) FILTER (WHERE event_type = 'comment_created')
  FROM interaction_events
  WHERE project_id IS NOT NULL
    AND event_date BETWEEN p_from AND p_to
  GROUP BY project_id, event_date;
END;
$$;

CREATE OR REPLACE VIEW project_top100_7d AS
SELECT
  p.id AS project_id,
  p.title,
  (
    0.50 * ln(1 + sum(m.unique_visitors))
    + 0.30 * ln(1 + sum(m.unique_likes))
    + 0.20 * ln(1 + sum(m.unique_commenters))
  )
  * exp(-0.02 * greatest(0, current_date - max(m.metric_date)))
  * min(m.abuse_factor) AS score
FROM projects p
JOIN project_daily_metrics m
  ON m.project_id = p.id
 AND m.metric_date >= current_date - 6
WHERE p.status = 'PUBLISHED'
GROUP BY p.id, p.title;

CREATE OR REPLACE VIEW developer_top100_7d AS
WITH project_scores AS (
  SELECT
    p.owner_id AS creator_id,
    sum(m.valid_outbound_clicks) AS outbound_clicks,
    sum(m.unique_likes) AS likes,
    sum(m.unique_commenters) AS commenters,
    min(m.abuse_factor) AS abuse_factor
  FROM projects p
  JOIN project_daily_metrics m
    ON m.project_id = p.id
   AND m.metric_date >= current_date - 6
  WHERE p.status = 'PUBLISHED'
  GROUP BY p.owner_id
),
creator_scores AS (
  SELECT
    creator_id,
    sum(subscriber_growth) AS subscriber_growth,
    max(active_projects) AS active_projects
  FROM creator_daily_metrics
  WHERE metric_date >= current_date - 6
  GROUP BY creator_id
)
SELECT
  u.id AS creator_id,
  u.display_name,
  (
    0.35 * ln(1 + greatest(ps.outbound_clicks, 0))
    + 0.25 * ln(1 + greatest(ps.likes, 0))
    + 0.15 * ln(1 + greatest(ps.commenters, 0))
    + 0.15 * ln(1 + greatest(coalesce(cs.subscriber_growth, 0), 0))
    + 0.10 * ln(1 + greatest(coalesce(cs.active_projects, 0), 0))
  ) * ps.abuse_factor AS score
FROM project_scores ps
JOIN users u ON u.id = ps.creator_id AND u.deleted_at IS NULL
LEFT JOIN creator_scores cs ON cs.creator_id = ps.creator_id;

-- -----------------------------------------------------------------------------
-- 6. Search projection: denormalized read model + HNSW
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS project_search_documents (
  project_id uuid PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
  title varchar(160) NOT NULL,
  description text NOT NULL,
  site_url text NOT NULL,
  status project_status NOT NULL,
  pricing pricing_type NOT NULL,
  category_slug varchar(100),
  tags text[] NOT NULL DEFAULT '{}'::text[],
  technology_slugs text[] NOT NULL DEFAULT '{}'::text[],
  published_at timestamptz,
  engagement_quality real NOT NULL DEFAULT 0 CHECK (engagement_quality BETWEEN 0 AND 1),
  search_document tsvector GENERATED ALWAYS AS (
    to_tsvector(
      'simple',
      coalesce(title, '') || ' ' || coalesce(description, '') || ' '
      || coalesce(category_slug, '') || ' ' || text_array_document(tags)
      || ' ' || text_array_document(technology_slugs)
    )
  ) STORED,
  embedding vector(1536),
  content_hash char(64),
  model_name varchar(100),
  embedding_generated_at timestamptz,
  updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (is_nonempty_text_array(tags)),
  CHECK (is_nonempty_text_array(technology_slugs)),
  CHECK (
    (embedding IS NULL AND content_hash IS NULL AND model_name IS NULL AND embedding_generated_at IS NULL)
    OR
    (embedding IS NOT NULL AND content_hash IS NOT NULL
      AND nullif(btrim(model_name), '') IS NOT NULL AND embedding_generated_at IS NOT NULL)
  )
);

CREATE INDEX IF NOT EXISTS project_search_hnsw_idx
ON project_search_documents USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS project_search_document_gin_idx
ON project_search_documents USING gin (search_document);

CREATE INDEX IF NOT EXISTS project_search_tags_gin_idx
ON project_search_documents USING gin (tags);

CREATE INDEX IF NOT EXISTS project_search_technologies_gin_idx
ON project_search_documents USING gin (technology_slugs);

CREATE INDEX IF NOT EXISTS project_search_category_pricing_idx
ON project_search_documents (category_slug, pricing, published_at DESC)
WHERE status = 'PUBLISHED';

CREATE OR REPLACE FUNCTION sync_project_search_document(p_project_id uuid)
RETURNS void
LANGUAGE sql AS $$
  INSERT INTO project_search_documents (
    project_id, title, description, site_url, status, pricing, category_slug,
    tags, technology_slugs, published_at, updated_at
  )
  SELECT
    p.id,
    p.title,
    p.description,
    p.site_url,
    p.status,
    p.pricing,
    c.slug,
    p.tags,
    coalesce(array_agg(DISTINCT t.slug ORDER BY t.slug)
      FILTER (WHERE t.slug IS NOT NULL), '{}'::text[]),
    p.published_at,
    clock_timestamp()
  FROM projects p
  LEFT JOIN categories c ON c.id = p.primary_category_id
  LEFT JOIN project_technologies pt ON pt.project_id = p.id
  LEFT JOIN technologies t ON t.id = pt.technology_id
  WHERE p.id = p_project_id
  GROUP BY p.id, c.slug
  ON CONFLICT (project_id) DO UPDATE SET
    title = EXCLUDED.title,
    description = EXCLUDED.description,
    site_url = EXCLUDED.site_url,
    status = EXCLUDED.status,
    pricing = EXCLUDED.pricing,
    category_slug = EXCLUDED.category_slug,
    tags = EXCLUDED.tags,
    technology_slugs = EXCLUDED.technology_slugs,
    published_at = EXCLUDED.published_at,
    updated_at = EXCLUDED.updated_at;
$$;

CREATE OR REPLACE FUNCTION sync_project_search_from_project()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  PERFORM sync_project_search_document(NEW.id);
  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION sync_project_search_from_technology()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  PERFORM sync_project_search_document(coalesce(NEW.project_id, OLD.project_id));
  RETURN coalesce(NEW, OLD);
END;
$$;

DROP TRIGGER IF EXISTS projects_sync_search_document ON projects;
CREATE TRIGGER projects_sync_search_document
AFTER INSERT OR UPDATE OF title, description, site_url, status, pricing,
  primary_category_id, tags, published_at
ON projects
FOR EACH ROW EXECUTE FUNCTION sync_project_search_from_project();

DROP TRIGGER IF EXISTS project_technologies_sync_search_document ON project_technologies;
CREATE TRIGGER project_technologies_sync_search_document
AFTER INSERT OR UPDATE OR DELETE ON project_technologies
FOR EACH ROW EXECUTE FUNCTION sync_project_search_from_technology();

CREATE OR REPLACE FUNCTION store_project_embedding(
  p_project_id uuid,
  p_embedding vector(1536),
  p_content_hash char(64),
  p_model_name text,
  p_generated_at timestamptz DEFAULT clock_timestamp()
)
RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
  IF p_embedding IS NULL
     OR p_content_hash !~ '^[0-9a-f]{64}$'
     OR nullif(btrim(p_model_name), '') IS NULL
  THEN
    RAISE EXCEPTION '유효한 1536차원 임베딩, 콘텐츠 해시, 모델명이 필요합니다.';
  END IF;

  PERFORM sync_project_search_document(p_project_id);

  UPDATE project_search_documents
  SET embedding = p_embedding,
      content_hash = p_content_hash,
      model_name = p_model_name,
      embedding_generated_at = p_generated_at,
      updated_at = clock_timestamp()
  WHERE project_id = p_project_id;

  IF NOT FOUND THEN
    RAISE EXCEPTION '프로젝트를 찾을 수 없습니다.';
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION search_published_projects(
  p_query_text text,
  p_query_embedding vector(1536),
  p_category_slug text DEFAULT NULL,
  p_tags text[] DEFAULT NULL,
  p_technologies text[] DEFAULT NULL,
  p_pricing pricing_type DEFAULT NULL,
  p_limit integer DEFAULT 20
)
RETURNS TABLE (
  project_id uuid,
  title varchar,
  site_url text,
  search_score double precision
)
LANGUAGE sql
STABLE
AS $$
  WITH nearest AS MATERIALIZED (
    SELECT
      d.project_id,
      d.title,
      d.site_url,
      d.search_document,
      d.engagement_quality,
      d.published_at,
      1 - (d.embedding <=> p_query_embedding) AS vector_similarity
    FROM project_search_documents d
    WHERE d.status = 'PUBLISHED'
      AND d.embedding IS NOT NULL
      AND (p_category_slug IS NULL OR d.category_slug = p_category_slug)
      AND (p_pricing IS NULL OR d.pricing = p_pricing)
      AND (p_tags IS NULL OR d.tags && p_tags)
      AND (p_technologies IS NULL OR d.technology_slugs && p_technologies)
    ORDER BY d.embedding <=> p_query_embedding
    -- Keep this literal so PostgreSQL can cost the HNSW early-stop correctly
    -- inside the cached SQL-function plan. The public result size is capped at
    -- 50 below.
    LIMIT 50
  )
  SELECT
    n.project_id,
    n.title,
    n.site_url,
    (
      0.50 * n.vector_similarity
      + 0.30 * ts_rank(n.search_document, websearch_to_tsquery('simple', p_query_text))
      + 0.10 * n.engagement_quality
      + 0.10 * exp(-greatest(0, extract(epoch FROM (CURRENT_TIMESTAMP - n.published_at)) / 86400) / 30)
    )::double precision AS search_score
  FROM nearest n
  ORDER BY search_score DESC, n.project_id
  LIMIT least(greatest(p_limit, 1), 50);
$$;

-- -----------------------------------------------------------------------------
-- 7. AI analysis and content domain
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS dashboard_ai_analyses (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
  analysis_type analysis_type NOT NULL,
  source_period daterange NOT NULL CHECK (NOT isempty(source_period)),
  source_metric_snapshot jsonb NOT NULL CHECK (
    jsonb_typeof(source_metric_snapshot) = 'object'
    AND source_metric_snapshot <> '{}'::jsonb
  ),
  evidence jsonb NOT NULL CHECK (
    jsonb_typeof(evidence) IN ('object', 'array')
    AND evidence NOT IN ('{}'::jsonb, '[]'::jsonb)
  ),
  result jsonb NOT NULL CHECK (
    jsonb_typeof(result) = 'object' AND result <> '{}'::jsonb
  ),
  model_name varchar(100) NOT NULL CHECK (btrim(model_name) <> ''),
  generated_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS weekly_insights (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  week_start date NOT NULL UNIQUE CHECK (extract(isodow FROM week_start) = 1),
  raw_metrics jsonb NOT NULL CHECK (jsonb_typeof(raw_metrics) = 'object' AND raw_metrics <> '{}'::jsonb),
  ai_summary jsonb NOT NULL CHECK (jsonb_typeof(ai_summary) = 'object' AND ai_summary <> '{}'::jsonb),
  model_name varchar(100) NOT NULL CHECK (btrim(model_name) <> ''),
  generated_at timestamptz NOT NULL,
  published_at timestamptz,
  CHECK (published_at IS NULL OR published_at >= generated_at)
);

CREATE TABLE IF NOT EXISTS tutorials (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  title varchar(200) NOT NULL CHECK (btrim(title) <> ''),
  description text NOT NULL CHECK (btrim(description) <> ''),
  type tutorial_type NOT NULL,
  difficulty tutorial_difficulty NOT NULL,
  estimated_minutes integer NOT NULL CHECK (estimated_minutes > 0),
  source_url text NOT NULL CHECK (is_public_http_url(source_url)),
  category_slugs text[] NOT NULL DEFAULT '{}'::text[] CHECK (is_nonempty_text_array(category_slugs)),
  technology_slugs text[] NOT NULL DEFAULT '{}'::text[] CHECK (is_nonempty_text_array(technology_slugs)),
  related_project_ids uuid[] NOT NULL DEFAULT '{}'::uuid[] CHECK (array_position(related_project_ids, NULL) IS NULL),
  is_published boolean NOT NULL DEFAULT false,
  published_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CHECK (
    (is_published AND published_at IS NOT NULL)
    OR (NOT is_published AND published_at IS NULL)
  )
);

CREATE INDEX IF NOT EXISTS dashboard_ai_analyses_project_period_idx
ON dashboard_ai_analyses (project_id, generated_at DESC);

CREATE INDEX IF NOT EXISTS tutorials_published_idx
ON tutorials (published_at DESC)
WHERE is_published;

CREATE INDEX IF NOT EXISTS tutorials_categories_gin_idx
ON tutorials USING gin (category_slugs);

CREATE INDEX IF NOT EXISTS tutorials_technologies_gin_idx
ON tutorials USING gin (technology_slugs);

DROP TRIGGER IF EXISTS tutorials_updated_at ON tutorials;
CREATE TRIGGER tutorials_updated_at
BEFORE UPDATE ON tutorials
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE OR REPLACE FUNCTION get_project_dashboard(
  p_project_id uuid,
  p_requester_id uuid,
  p_from date,
  p_to date
)
RETURNS jsonb
LANGUAGE plpgsql
STABLE AS $$
DECLARE
  result jsonb;
BEGIN
  IF p_from IS NULL OR p_to IS NULL OR p_from > p_to THEN
    RAISE EXCEPTION '올바른 조회 기간이 필요합니다.';
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM projects
    WHERE id = p_project_id AND owner_id = p_requester_id
  ) THEN
    RAISE EXCEPTION '프로젝트 소유자만 대시보드를 조회할 수 있습니다.'
      USING ERRCODE = '42501';
  END IF;

  SELECT jsonb_build_object(
    'project_id', p_project_id,
    'period', jsonb_build_object('from', p_from, 'to', p_to),
    'metrics', jsonb_build_object(
      'unique_visitors', coalesce(sum(unique_visitors), 0),
      'impressions', coalesce(sum(impressions), 0),
      'detail_views', coalesce(sum(detail_views), 0),
      'valid_outbound_clicks', coalesce(sum(valid_outbound_clicks), 0),
      'unique_likes', coalesce(sum(unique_likes), 0),
      'unique_commenters', coalesce(sum(unique_commenters), 0)
    )
  ) INTO result
  FROM project_daily_metrics
  WHERE project_id = p_project_id
    AND metric_date BETWEEN p_from AND p_to;

  RETURN result;
END;
$$;

CREATE OR REPLACE FUNCTION purge_expired_raw_activity(
  p_reference_time timestamptz DEFAULT clock_timestamp(),
  p_retention interval DEFAULT interval '13 months'
)
RETURNS TABLE (deleted_events bigint, deleted_searches bigint, deleted_sessions bigint)
LANGUAGE plpgsql AS $$
DECLARE
  cutoff timestamptz;
BEGIN
  IF p_retention < interval '1 month' THEN
    RAISE EXCEPTION '원본 데이터 보존 기간은 최소 1개월이어야 합니다.';
  END IF;

  cutoff := p_reference_time - p_retention;

  DELETE FROM interaction_events WHERE occurred_at < cutoff;
  GET DIAGNOSTICS deleted_events = ROW_COUNT;

  DELETE FROM search_requests WHERE searched_at < cutoff;
  GET DIAGNOSTICS deleted_searches = ROW_COUNT;

  DELETE FROM anonymous_sessions
  WHERE expires_at < p_reference_time;
  GET DIAGNOSTICS deleted_sessions = ROW_COUNT;

  RETURN NEXT;
END;
$$;

-- -----------------------------------------------------------------------------
-- Seed dictionaries
-- -----------------------------------------------------------------------------

INSERT INTO categories (name, slug) VALUES
  ('생산성/업무', 'productivity-work'),
  ('교육/취업', 'education-career'),
  ('개발자 도구', 'developer-tools'),
  ('금융', 'finance'),
  ('생활/건강', 'life-health'),
  ('콘텐츠/엔터테인먼트', 'content-entertainment'),
  ('소셜/커뮤니티', 'social-community'),
  ('쇼핑/커머스', 'shopping-commerce'),
  ('여행/지역', 'travel-local'),
  ('디자인/크리에이티브', 'design-creative'),
  ('AI 서비스', 'ai-service'),
  ('데이터/분석', 'data-analytics'),
  ('보안/인증', 'security-auth'),
  ('기타', 'other')
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name;

INSERT INTO community_boards (slug, name, description, display_order) VALUES
  ('free', '자유게시판', '자유롭게 이야기를 나누는 공간입니다.', 1),
  ('qna', '질문게시판', '개발과 프로젝트에 관한 질문을 남기는 공간입니다.', 2),
  ('showcase', '프로젝트 홍보', '직접 만든 프로젝트를 소개하는 공간입니다.', 3)
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  description = EXCLUDED.description,
  display_order = EXCLUDED.display_order;

INSERT INTO technologies (name, slug, default_group) VALUES
  ('Vue.js', 'vue-js', 'FRONTEND'),
  ('React', 'react', 'FRONTEND'),
  ('TypeScript', 'typescript', 'FRONTEND'),
  ('Spring Boot', 'spring-boot', 'BACKEND'),
  ('Node.js', 'node-js', 'BACKEND'),
  ('FastAPI', 'fastapi', 'BACKEND'),
  ('PostgreSQL', 'postgresql', 'DATABASE'),
  ('Redis', 'redis', 'DATABASE'),
  ('MongoDB', 'mongodb', 'DATABASE'),
  ('Docker', 'docker', 'INFRA_DEPLOY'),
  ('AWS', 'aws', 'INFRA_DEPLOY'),
  ('Vercel', 'vercel', 'INFRA_DEPLOY'),
  ('GitHub Actions', 'github-actions', 'INFRA_DEPLOY'),
  ('OpenAI API', 'openai-api', 'AI_DATA'),
  ('pgvector', 'pgvector', 'AI_DATA')
ON CONFLICT (slug) DO UPDATE SET
  name = EXCLUDED.name,
  default_group = EXCLUDED.default_group;
