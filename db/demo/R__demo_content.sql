-- Optional, idempotent MVP demo content. Loaded only with the Spring `demo` profile.
-- IDs are fixed so screenshots, smoke tests, and repeated environments are predictable.

INSERT INTO users (
  id, display_name, role, theme, new_project_notifications, auth_provider, google_subject
) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Click HUB Demo Maker', 'USER', 'SYSTEM', true, 'GOOGLE', 'demo-maker-1'),
  ('10000000-0000-0000-0000-000000000002', 'MVP Explorer', 'USER', 'LIGHT', true, 'GOOGLE', 'demo-maker-2')
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (
  id, owner_id, primary_category_id, title, description, site_url, repository_url,
  pricing, tags, thumbnail_url, screenshots, status
) VALUES
  (
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    (SELECT id FROM categories WHERE slug = 'developer-tools'),
    'Click HUB',
    '배포된 사이드 프로젝트를 발견하고 메이커와 피드백을 나누는 플랫폼입니다.',
    'https://github.com/SKALA4-YS/click-hub',
    'https://github.com/SKALA4-YS/click-hub',
    'FREE',
    ARRAY['MVP', 'DevOps', 'Community'],
    NULL,
    '[]'::jsonb,
    'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    (SELECT id FROM categories WHERE slug = 'ai-service'),
    'Release Radar',
    '팀의 배포 변화를 한눈에 모아보는 데모 프로젝트입니다.',
    'https://example.com/release-radar',
    NULL,
    'FREEMIUM',
    ARRAY['Release', 'Analytics'],
    NULL,
    '[]'::jsonb,
    'DRAFT'
  )
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_technologies (project_id, technology_id, technology_group)
SELECT '20000000-0000-0000-0000-000000000001', id, default_group
FROM technologies WHERE slug IN ('vue-js', 'spring-boot', 'postgresql', 'docker')
ON CONFLICT DO NOTHING;

INSERT INTO project_technologies (project_id, technology_id, technology_group)
SELECT '20000000-0000-0000-0000-000000000002', id, default_group
FROM technologies WHERE slug IN ('react', 'postgresql', 'vercel')
ON CONFLICT DO NOTHING;

SELECT record_project_url_validation(
  '20000000-0000-0000-0000-000000000001',
  'https://github.com/SKALA4-YS/click-hub', true, 200,
  'https://github.com/SKALA4-YS/click-hub', NULL
);
SELECT record_project_url_validation(
  '20000000-0000-0000-0000-000000000002',
  'https://example.com/release-radar', true, 200,
  'https://example.com/release-radar', NULL
);

UPDATE projects SET status = 'PENDING_REVIEW'
WHERE id IN ('20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002')
  AND status = 'DRAFT';
UPDATE projects SET status = 'PUBLISHED'
WHERE id IN ('20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002')
  AND status = 'PENDING_REVIEW';

INSERT INTO project_daily_metrics (
  project_id, metric_date, unique_visitors, impressions, detail_views,
  valid_outbound_clicks, unique_likes, unique_commenters
) VALUES
  ('20000000-0000-0000-0000-000000000001', current_date, 42, 120, 58, 30, 18, 7),
  ('20000000-0000-0000-0000-000000000002', current_date, 24, 80, 31, 14, 9, 4)
ON CONFLICT (project_id, metric_date) DO NOTHING;

INSERT INTO creator_daily_metrics (creator_id, metric_date, subscriber_growth, active_projects)
VALUES
  ('10000000-0000-0000-0000-000000000001', current_date, 8, 1),
  ('10000000-0000-0000-0000-000000000002', current_date, 3, 1)
ON CONFLICT (creator_id, metric_date) DO NOTHING;

INSERT INTO tutorials (
  id, title, description, type, difficulty, estimated_minutes, source_url,
  category_slugs, technology_slugs, is_published, published_at
) VALUES
  (
    '30000000-0000-0000-0000-000000000001',
    'Vue와 Spring Boot API 연결하기',
    '환경변수와 CORS를 포함해 프런트엔드와 백엔드를 연결하는 기본 흐름입니다.',
    'DEVELOPMENT', 'BEGINNER', 20,
    'https://spring.io/guides/tutorials/rest',
    ARRAY['developer-tools'], ARRAY['vue-js', 'spring-boot'], true, now()
  ),
  (
    '30000000-0000-0000-0000-000000000002',
    'Docker Compose로 PostgreSQL 함께 실행하기',
    '애플리케이션과 데이터베이스를 한 번에 실행하는 로컬 배포 실습입니다.',
    'DEVELOPMENT', 'INTERMEDIATE', 25,
    'https://docs.docker.com/compose/',
    ARRAY['developer-tools'], ARRAY['docker', 'postgresql'], true, now()
  )
ON CONFLICT (id) DO NOTHING;

INSERT INTO weekly_insights (
  id, week_start, raw_metrics, ai_summary, model_name, generated_at, published_at
) VALUES (
  '40000000-0000-0000-0000-000000000001',
  date_trunc('week', current_date)::date,
  '{"source":"mvp-demo"}'::jsonb,
  '{"headline":"배포 자동화와 운영 가시성이 주목받고 있습니다.","trends":[{"topic":"Docker Compose","direction":"UP","change_rate":18.4},{"topic":"Spring Boot","direction":"UP","change_rate":11.2}],"watchlist":["pgvector","Vercel","Render"]}'::jsonb,
  'MVP demo seed', now(), now()
)
ON CONFLICT (week_start) DO NOTHING;

INSERT INTO community_posts (id, board_id, author_id, title, body, status)
VALUES
  (
    '50000000-0000-0000-0000-000000000001',
    (SELECT id FROM community_boards WHERE slug = 'free'),
    '10000000-0000-0000-0000-000000000001',
    'Click HUB MVP1에 오신 것을 환영합니다',
    '이 글은 demo 프로필에서만 생성되는 샘플 콘텐츠입니다. 로그인 후 새 글과 댓글을 직접 저장해보세요.',
    'PUBLISHED'
  ),
  (
    '50000000-0000-0000-0000-000000000002',
    (SELECT id FROM community_boards WHERE slug = 'qna'),
    '10000000-0000-0000-0000-000000000002',
    'Render와 Vercel의 환경변수는 어떻게 연결하나요?',
    'Frontend에는 API 공개 주소를, Backend에는 CORS와 OAuth callback 주소를 설정합니다.',
    'PUBLISHED'
  )
ON CONFLICT (id) DO NOTHING;
