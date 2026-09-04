-- Presentation-ready, deterministic demo content.
-- Loaded only when the Spring `demo` profile includes classpath:db/demo.
-- Every mutable row uses a reserved UUID prefix so repeat runs never delete or
-- overwrite users and activity created through the real application.

-- Refresh URL validation before touching an already-published presentation row.
-- validate_project_write() checks the validation age on every PUBLISHED update.
SELECT record_project_url_validation(p.id, p.site_url, true, 200, p.site_url, NULL)
FROM projects p
WHERE p.id IN (
  '20000000-0000-0000-0000-000000000001',
  '20000000-0000-0000-0000-000000000002',
  '20000000-0000-0000-0000-000000000003',
  '20000000-0000-0000-0000-000000000004',
  '20000000-0000-0000-0000-000000000005',
  '20000000-0000-0000-0000-000000000006',
  '20000000-0000-0000-0000-000000000007',
  '20000000-0000-0000-0000-000000000008',
  '20000000-0000-0000-0000-000000000009',
  '20000000-0000-0000-0000-000000000010',
  '20000000-0000-0000-0000-000000000011',
  '20000000-0000-0000-0000-000000000012'
);

INSERT INTO users (
  id, display_name, avatar_url, role, theme, new_project_notifications,
  auth_provider, google_subject
) VALUES
  ('10000000-0000-0000-0000-000000000001', 'Click HUB Team',
   'https://api.dicebear.com/9.x/initials/svg?seed=Click%20HUB%20Team',
   'USER', 'SYSTEM', true, 'GOOGLE', 'presentation-maker-01'),
  ('10000000-0000-0000-0000-000000000002', 'MVP Explorer',
   'https://api.dicebear.com/9.x/initials/svg?seed=MVP%20Explorer',
   'USER', 'LIGHT', true, 'GOOGLE', 'presentation-maker-02'),
  ('10000000-0000-0000-0000-000000000003', 'Flow Maker',
   'https://api.dicebear.com/9.x/initials/svg?seed=Flow%20Maker',
   'USER', 'LIGHT', true, 'GOOGLE', 'presentation-maker-03'),
  ('10000000-0000-0000-0000-000000000004', 'Prompt Gardener',
   'https://api.dicebear.com/9.x/initials/svg?seed=Prompt%20Gardener',
   'USER', 'DARK', true, 'GOOGLE', 'presentation-maker-04'),
  ('10000000-0000-0000-0000-000000000005', 'Data Voyager',
   'https://api.dicebear.com/9.x/initials/svg?seed=Data%20Voyager',
   'USER', 'SYSTEM', true, 'GOOGLE', 'presentation-maker-05'),
  ('10000000-0000-0000-0000-000000000006', 'Learning Lab',
   'https://api.dicebear.com/9.x/initials/svg?seed=Learning%20Lab',
   'USER', 'LIGHT', true, 'GOOGLE', 'presentation-maker-06'),
  ('10000000-0000-0000-0000-000000000007', 'Indie Studio',
   'https://api.dicebear.com/9.x/initials/svg?seed=Indie%20Studio',
   'USER', 'DARK', true, 'GOOGLE', 'presentation-maker-07'),
  ('10000000-0000-0000-0000-000000000008', 'Secure Park',
   'https://api.dicebear.com/9.x/initials/svg?seed=Secure%20Park',
   'USER', 'SYSTEM', true, 'GOOGLE', 'presentation-maker-08')
ON CONFLICT (id) DO UPDATE SET
  display_name = EXCLUDED.display_name,
  avatar_url = EXCLUDED.avatar_url,
  theme = EXCLUDED.theme,
  new_project_notifications = EXCLUDED.new_project_notifications,
  updated_at = now();

INSERT INTO projects (
  id, owner_id, primary_category_id, title, description, site_url, repository_url,
  pricing, tags, thumbnail_url, screenshots, status
) VALUES
  (
    '20000000-0000-0000-0000-000000000001',
    '10000000-0000-0000-0000-000000000001',
    (SELECT id FROM categories WHERE slug = 'developer-tools'),
    'Click HUB',
    '배포된 사이드 프로젝트를 발견하고 메이커와 피드백을 나누는 프로젝트 허브입니다.',
    'https://github.com/SKALA4-YS/click-hub',
    'https://github.com/SKALA4-YS/click-hub',
    'FREE', ARRAY['MVP', 'DevOps', 'Community'],
    'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000002',
    '10000000-0000-0000-0000-000000000002',
    (SELECT id FROM categories WHERE slug = 'ai-service'),
    'Release Radar',
    '팀의 배포 내역을 모으고 AI가 릴리스 핵심 변화를 요약해주는 서비스입니다.',
    'https://example.com/release-radar', NULL,
    'FREEMIUM', ARRAY['Release', 'Analytics', 'AI'],
    'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000003',
    '10000000-0000-0000-0000-000000000003',
    (SELECT id FROM categories WHERE slug = 'productivity-work'),
    'FlowNote',
    '회의에서 나온 결정과 할 일을 한 화면에 정리하는 가벼운 협업 노트입니다.',
    'https://example.com/flow-note', NULL,
    'FREEMIUM', ARRAY['Productivity', 'Team', 'Note'],
    'https://images.unsplash.com/photo-1497215728101-856f4ea42174?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000004',
    '10000000-0000-0000-0000-000000000004',
    (SELECT id FROM categories WHERE slug = 'ai-service'),
    'Prompt Palette',
    '목적별 프롬프트를 저장하고 팀과 함께 개선할 수 있는 AI 작업 공간입니다.',
    'https://example.com/prompt-palette', NULL,
    'FREE', ARRAY['Prompt', 'AI', 'Workspace'],
    'https://images.unsplash.com/photo-1677442136019-21780ecad995?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000005',
    '10000000-0000-0000-0000-000000000005',
    (SELECT id FROM categories WHERE slug = 'data-analytics'),
    'DevPulse',
    '개발팀의 배포 빈도와 장애 복구 흐름을 직관적인 지표로 보여주는 대시보드입니다.',
    'https://example.com/dev-pulse', NULL,
    'PAID', ARRAY['DORA', 'Dashboard', 'Metrics'],
    'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000006',
    '10000000-0000-0000-0000-000000000006',
    (SELECT id FROM categories WHERE slug = 'education-career'),
    'StudyMate',
    '학습 목표를 작은 루틴으로 나누고 동료와 진척도를 공유하는 스터디 도우미입니다.',
    'https://example.com/study-mate', NULL,
    'FREE', ARRAY['Study', 'Career', 'Routine'],
    'https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000007',
    '10000000-0000-0000-0000-000000000007',
    (SELECT id FROM categories WHERE slug = 'finance'),
    'Budget Beam',
    '구독과 생활비를 자동 분류해 이번 달 소비 흐름을 알려주는 개인 예산 서비스입니다.',
    'https://example.com/budget-beam', NULL,
    'FREEMIUM', ARRAY['Budget', 'Finance', 'Insight'],
    'https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000008',
    '10000000-0000-0000-0000-000000000003',
    (SELECT id FROM categories WHERE slug = 'life-health'),
    'Habit Grove',
    '매일의 작은 습관을 나무처럼 키우며 꾸준함을 시각적으로 기록하는 앱입니다.',
    'https://example.com/habit-grove', NULL,
    'FREE', ARRAY['Habit', 'Health', 'Challenge'],
    'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000009',
    '10000000-0000-0000-0000-000000000007',
    (SELECT id FROM categories WHERE slug = 'travel-local'),
    'Local Loop',
    '동네 주민이 추천한 장소를 취향별 산책 코스로 연결해주는 로컬 탐색 서비스입니다.',
    'https://example.com/local-loop', NULL,
    'FREE', ARRAY['Local', 'Travel', 'Map'],
    'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000010',
    '10000000-0000-0000-0000-000000000004',
    (SELECT id FROM categories WHERE slug = 'design-creative'),
    'Pixel Forge',
    '브랜드 컬러와 컴포넌트를 조합해 빠르게 UI 시안을 만드는 디자인 도구입니다.',
    'https://example.com/pixel-forge', NULL,
    'PAID', ARRAY['Design', 'UI', 'Prototype'],
    'https://images.unsplash.com/photo-1545235617-9465d2a55698?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000011',
    '10000000-0000-0000-0000-000000000006',
    (SELECT id FROM categories WHERE slug = 'social-community'),
    'Crew Circle',
    '사이드 프로젝트 동료를 역할과 관심 기술로 찾고 짧게 협업을 시작하는 커뮤니티입니다.',
    'https://example.com/crew-circle', NULL,
    'FREE', ARRAY['Team', 'Community', 'SideProject'],
    'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  ),
  (
    '20000000-0000-0000-0000-000000000012',
    '10000000-0000-0000-0000-000000000008',
    (SELECT id FROM categories WHERE slug = 'security-auth'),
    'Deploy Guard',
    '배포 전 환경변수와 공개 설정을 점검해 실수를 줄여주는 보안 체크 도구입니다.',
    'https://example.com/deploy-guard', NULL,
    'FREEMIUM', ARRAY['Security', 'Deploy', 'Checklist'],
    'https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=1200&q=80',
    '[]'::jsonb, 'DRAFT'
  )
ON CONFLICT (id) DO UPDATE SET
  owner_id = EXCLUDED.owner_id,
  primary_category_id = EXCLUDED.primary_category_id,
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  repository_url = EXCLUDED.repository_url,
  pricing = EXCLUDED.pricing,
  tags = EXCLUDED.tags,
  thumbnail_url = EXCLUDED.thumbnail_url,
  screenshots = EXCLUDED.screenshots;

-- The site URLs are intentionally immutable in this repeatable seed. Refresh
-- validation, then move only new/draft rows through the legal publish states.
SELECT record_project_url_validation(p.id, p.site_url, true, 200, p.site_url, NULL)
FROM projects p
WHERE p.id::text LIKE '20000000-0000-0000-0000-0000000000%';

UPDATE projects
SET status = 'PENDING_REVIEW'
WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'
  AND status = 'DRAFT';

UPDATE projects
SET status = 'PUBLISHED'
WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'
  AND status = 'PENDING_REVIEW';

UPDATE projects p
SET published_at = now() - (v.days_ago * interval '1 day')
FROM (VALUES
  ('20000000-0000-0000-0000-000000000001'::uuid, 0),
  ('20000000-0000-0000-0000-000000000002'::uuid, 1),
  ('20000000-0000-0000-0000-000000000003'::uuid, 2),
  ('20000000-0000-0000-0000-000000000004'::uuid, 3),
  ('20000000-0000-0000-0000-000000000005'::uuid, 4),
  ('20000000-0000-0000-0000-000000000006'::uuid, 5),
  ('20000000-0000-0000-0000-000000000007'::uuid, 6),
  ('20000000-0000-0000-0000-000000000008'::uuid, 7),
  ('20000000-0000-0000-0000-000000000009'::uuid, 8),
  ('20000000-0000-0000-0000-000000000010'::uuid, 9),
  ('20000000-0000-0000-0000-000000000011'::uuid, 10),
  ('20000000-0000-0000-0000-000000000012'::uuid, 11)
) AS v(project_id, days_ago)
WHERE p.id = v.project_id;

DELETE FROM project_technologies
WHERE project_id::text LIKE '20000000-0000-0000-0000-0000000000%';

INSERT INTO project_technologies (project_id, technology_id, technology_group, version)
SELECT v.project_id, t.id, t.default_group, v.version
FROM (VALUES
  ('20000000-0000-0000-0000-000000000001'::uuid, 'vue-js', '3'),
  ('20000000-0000-0000-0000-000000000001'::uuid, 'spring-boot', '4'),
  ('20000000-0000-0000-0000-000000000001'::uuid, 'postgresql', '16'),
  ('20000000-0000-0000-0000-000000000001'::uuid, 'docker', NULL),
  ('20000000-0000-0000-0000-000000000002'::uuid, 'react', NULL),
  ('20000000-0000-0000-0000-000000000002'::uuid, 'node-js', NULL),
  ('20000000-0000-0000-0000-000000000002'::uuid, 'openai-api', NULL),
  ('20000000-0000-0000-0000-000000000003'::uuid, 'vue-js', NULL),
  ('20000000-0000-0000-0000-000000000003'::uuid, 'spring-boot', NULL),
  ('20000000-0000-0000-0000-000000000003'::uuid, 'postgresql', NULL),
  ('20000000-0000-0000-0000-000000000004'::uuid, 'react', NULL),
  ('20000000-0000-0000-0000-000000000004'::uuid, 'fastapi', NULL),
  ('20000000-0000-0000-0000-000000000004'::uuid, 'openai-api', NULL),
  ('20000000-0000-0000-0000-000000000004'::uuid, 'pgvector', NULL),
  ('20000000-0000-0000-0000-000000000005'::uuid, 'typescript', NULL),
  ('20000000-0000-0000-0000-000000000005'::uuid, 'spring-boot', NULL),
  ('20000000-0000-0000-0000-000000000005'::uuid, 'postgresql', NULL),
  ('20000000-0000-0000-0000-000000000005'::uuid, 'aws', NULL),
  ('20000000-0000-0000-0000-000000000006'::uuid, 'vue-js', NULL),
  ('20000000-0000-0000-0000-000000000006'::uuid, 'node-js', NULL),
  ('20000000-0000-0000-0000-000000000006'::uuid, 'mongodb', NULL),
  ('20000000-0000-0000-0000-000000000007'::uuid, 'react', NULL),
  ('20000000-0000-0000-0000-000000000007'::uuid, 'spring-boot', NULL),
  ('20000000-0000-0000-0000-000000000007'::uuid, 'postgresql', NULL),
  ('20000000-0000-0000-0000-000000000008'::uuid, 'vue-js', NULL),
  ('20000000-0000-0000-0000-000000000008'::uuid, 'node-js', NULL),
  ('20000000-0000-0000-0000-000000000008'::uuid, 'mongodb', NULL),
  ('20000000-0000-0000-0000-000000000009'::uuid, 'react', NULL),
  ('20000000-0000-0000-0000-000000000009'::uuid, 'fastapi', NULL),
  ('20000000-0000-0000-0000-000000000009'::uuid, 'postgresql', NULL),
  ('20000000-0000-0000-0000-000000000010'::uuid, 'typescript', NULL),
  ('20000000-0000-0000-0000-000000000010'::uuid, 'node-js', NULL),
  ('20000000-0000-0000-0000-000000000010'::uuid, 'vercel', NULL),
  ('20000000-0000-0000-0000-000000000011'::uuid, 'vue-js', NULL),
  ('20000000-0000-0000-0000-000000000011'::uuid, 'spring-boot', NULL),
  ('20000000-0000-0000-0000-000000000011'::uuid, 'postgresql', NULL),
  ('20000000-0000-0000-0000-000000000012'::uuid, 'spring-boot', NULL),
  ('20000000-0000-0000-0000-000000000012'::uuid, 'redis', NULL),
  ('20000000-0000-0000-0000-000000000012'::uuid, 'docker', NULL),
  ('20000000-0000-0000-0000-000000000012'::uuid, 'github-actions', NULL)
) AS v(project_id, technology_slug, version)
JOIN technologies t ON t.slug = v.technology_slug
ON CONFLICT DO NOTHING;

-- Rebuild only relationships created by presentation personas. Real users are
-- outside this reserved UUID range and their reactions/subscriptions remain.
DELETE FROM project_reactions
WHERE user_id::text LIKE '10000000-0000-0000-0000-0000000000%'
  AND project_id::text LIKE '20000000-0000-0000-0000-0000000000%';

WITH demo_users AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS user_no
  FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'
), demo_projects AS (
  SELECT id, owner_id, row_number() OVER (ORDER BY id)::integer AS project_no
  FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'
)
INSERT INTO project_reactions (user_id, project_id, type, created_at)
SELECT u.id, p.id, 'LIKE'::reaction_type,
       now() - ((p.project_no + u.user_no) * interval '2 hours')
FROM demo_users u CROSS JOIN demo_projects p
WHERE u.id <> p.owner_id
  AND mod(u.user_no + p.project_no, 3) <> 0
ON CONFLICT DO NOTHING;

WITH demo_users AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS user_no
  FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'
), demo_projects AS (
  SELECT id, owner_id, row_number() OVER (ORDER BY id)::integer AS project_no
  FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'
)
INSERT INTO project_reactions (user_id, project_id, type, created_at)
SELECT u.id, p.id, 'FAVORITE'::reaction_type,
       now() - ((p.project_no + u.user_no) * interval '3 hours')
FROM demo_users u CROSS JOIN demo_projects p
WHERE u.id <> p.owner_id
  AND mod((2 * u.user_no) + p.project_no, 3) = 0
ON CONFLICT DO NOTHING;

INSERT INTO project_comments (id, project_id, author_id, body, created_at, updated_at)
VALUES
  ('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', '배포 구조와 프로젝트 탐색 흐름이 한눈에 들어오네요.', now() - interval '1 day', now() - interval '1 day'),
  ('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', '메이커 랭킹까지 연결된 점이 인상적입니다!', now() - interval '12 hours', now() - interval '12 hours'),
  ('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000004', '릴리스 요약을 슬랙으로도 받아볼 수 있으면 좋겠어요.', now() - interval '2 days', now() - interval '2 days'),
  ('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', '여러 저장소를 묶어서 보는 기능도 기대됩니다.', now() - interval '18 hours', now() - interval '18 hours'),
  ('21000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006', '회의 후 할 일이 사라지는 문제를 잘 해결해주네요.', now() - interval '3 days', now() - interval '3 days'),
  ('21000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', '팀 단위 템플릿도 추가되면 바로 써보고 싶습니다.', now() - interval '20 hours', now() - interval '20 hours'),
  ('21000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', '좋은 프롬프트를 팀 자산으로 관리하기 좋겠어요.', now() - interval '4 days', now() - interval '4 days'),
  ('21000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000007', '버전별 결과 비교 기능이 특히 궁금합니다.', now() - interval '1 day', now() - interval '1 day'),
  ('21000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000008', '배포 지표를 회고 때 바로 활용할 수 있겠네요.', now() - interval '3 days', now() - interval '3 days'),
  ('21000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003', '작은 팀을 위한 무료 플랜도 있으면 좋겠습니다.', now() - interval '8 hours', now() - interval '8 hours'),
  ('21000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000005', '함께 공부하는 친구들과 써보고 싶어요.', now() - interval '5 days', now() - interval '5 days'),
  ('21000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000007', '주간 회고 카드가 특히 유용해 보입니다.', now() - interval '2 days', now() - interval '2 days'),
  ('21000000-0000-0000-0000-000000000013', '20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000002', '구독 지출을 놓치지 않게 해주는 점이 좋아요.', now() - interval '4 days', now() - interval '4 days'),
  ('21000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000004', '습관이 자라는 시각화가 동기부여에 딱이네요.', now() - interval '2 days', now() - interval '2 days'),
  ('21000000-0000-0000-0000-000000000015', '20000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000006', '주말 산책 코스를 찾을 때 사용해보고 싶습니다.', now() - interval '1 day', now() - interval '1 day'),
  ('21000000-0000-0000-0000-000000000016', '20000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000003', '개발자도 쉽게 시안을 만들 수 있겠어요.', now() - interval '3 days', now() - interval '3 days'),
  ('21000000-0000-0000-0000-000000000017', '20000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000008', '사이드 프로젝트 팀원을 찾는 데 도움이 되겠네요.', now() - interval '2 days', now() - interval '2 days'),
  ('21000000-0000-0000-0000-000000000018', '20000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000001', '배포 전 체크리스트로 보안 사고를 줄일 수 있겠어요.', now() - interval '6 hours', now() - interval '6 hours')
ON CONFLICT (id) DO UPDATE SET
  body = EXCLUDED.body,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at,
  deleted_at = NULL;

DELETE FROM creator_subscriptions
WHERE subscriber_id::text LIKE '10000000-0000-0000-0000-0000000000%'
  AND creator_id::text LIKE '10000000-0000-0000-0000-0000000000%';

WITH demo_users AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS user_no
  FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'
), demo_creators AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS creator_no
  FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'
)
INSERT INTO creator_subscriptions (subscriber_id, creator_id, created_at)
SELECT u.id, c.id, now() - ((u.user_no + c.creator_no) * interval '5 hours')
FROM demo_users u CROSS JOIN demo_creators c
WHERE u.id <> c.id
  AND mod(u.user_no + c.creator_no, 3) = 0
ON CONFLICT DO NOTHING;

-- Seven complete days keep home and both ranking screens stable regardless of
-- when the repeatable migration is applied.
WITH demo_projects AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS project_no
  FROM projects WHERE id::text LIKE '20000000-0000-0000-0000-0000000000%'
), days AS (
  SELECT generate_series(0, 6)::integer AS day_no
)
INSERT INTO project_daily_metrics (
  project_id, metric_date, unique_visitors, impressions, detail_views,
  valid_outbound_clicks, unique_likes, unique_commenters, abuse_factor
)
SELECT
  p.id,
  current_date - d.day_no,
  155 - (p.project_no * 7) - (d.day_no * 2),
  430 - (p.project_no * 13) - (d.day_no * 4),
  190 - (p.project_no * 8) - (d.day_no * 2),
  76 - (p.project_no * 3) - d.day_no,
  43 - (p.project_no * 2) - (d.day_no / 2),
  20 - p.project_no - (d.day_no / 3),
  1
FROM demo_projects p CROSS JOIN days d
ON CONFLICT (project_id, metric_date) DO UPDATE SET
  unique_visitors = EXCLUDED.unique_visitors,
  impressions = EXCLUDED.impressions,
  detail_views = EXCLUDED.detail_views,
  valid_outbound_clicks = EXCLUDED.valid_outbound_clicks,
  unique_likes = EXCLUDED.unique_likes,
  unique_commenters = EXCLUDED.unique_commenters,
  abuse_factor = EXCLUDED.abuse_factor,
  updated_at = now();

WITH demo_creators AS (
  SELECT id, row_number() OVER (ORDER BY id)::integer AS creator_no
  FROM users WHERE id::text LIKE '10000000-0000-0000-0000-0000000000%'
), days AS (
  SELECT generate_series(0, 6)::integer AS day_no
)
INSERT INTO creator_daily_metrics (
  creator_id, metric_date, subscriber_growth, active_projects
)
SELECT
  c.id,
  current_date - d.day_no,
  greatest(1, 12 - c.creator_no - (d.day_no / 2)),
  (SELECT count(*) FROM projects p WHERE p.owner_id = c.id AND p.status = 'PUBLISHED')
FROM demo_creators c CROSS JOIN days d
ON CONFLICT (creator_id, metric_date) DO UPDATE SET
  subscriber_growth = EXCLUDED.subscriber_growth,
  active_projects = EXCLUDED.active_projects,
  updated_at = now();

INSERT INTO tutorials (
  id, title, description, type, difficulty, estimated_minutes, source_url,
  category_slugs, technology_slugs, related_project_ids, is_published, published_at
) VALUES
  ('30000000-0000-0000-0000-000000000001', 'Vue와 Spring Boot API 연결하기', '환경변수와 CORS를 포함해 프런트엔드와 백엔드를 연결하는 기본 흐름입니다.', 'DEVELOPMENT', 'BEGINNER', 20, 'https://spring.io/guides/tutorials/rest', ARRAY['developer-tools'], ARRAY['vue-js', 'spring-boot'], ARRAY['20000000-0000-0000-0000-000000000001'::uuid], true, now() - interval '1 day'),
  ('30000000-0000-0000-0000-000000000002', 'Docker Compose로 PostgreSQL 함께 실행하기', '애플리케이션과 데이터베이스를 한 번에 실행하는 로컬 배포 실습입니다.', 'DEVELOPMENT', 'INTERMEDIATE', 25, 'https://docs.docker.com/compose/', ARRAY['developer-tools'], ARRAY['docker', 'postgresql'], ARRAY['20000000-0000-0000-0000-000000000001'::uuid], true, now() - interval '2 days'),
  ('30000000-0000-0000-0000-000000000003', '프롬프트 실험을 작게 시작하는 법', '반복 가능한 프롬프트 실험과 결과 기록 방식을 익히는 입문 가이드입니다.', 'VIBE_CODING', 'BEGINNER', 15, 'https://platform.openai.com/docs/guides/prompt-engineering', ARRAY['ai-service'], ARRAY['openai-api'], ARRAY['20000000-0000-0000-0000-000000000004'::uuid], true, now() - interval '3 days'),
  ('30000000-0000-0000-0000-000000000004', 'GitHub Actions로 배포 파이프라인 만들기', '테스트부터 컨테이너 배포까지 이어지는 CI/CD 워크플로를 구성합니다.', 'DEVELOPMENT', 'INTERMEDIATE', 35, 'https://docs.github.com/en/actions', ARRAY['developer-tools'], ARRAY['github-actions', 'docker'], ARRAY['20000000-0000-0000-0000-000000000012'::uuid], true, now() - interval '4 days'),
  ('30000000-0000-0000-0000-000000000005', 'pgvector로 유사도 검색 구현하기', 'PostgreSQL에 임베딩을 저장하고 가까운 결과를 조회하는 흐름을 설명합니다.', 'DEVELOPMENT', 'ADVANCED', 45, 'https://github.com/pgvector/pgvector', ARRAY['data-analytics', 'ai-service'], ARRAY['postgresql', 'pgvector'], ARRAY['20000000-0000-0000-0000-000000000005'::uuid], true, now() - interval '5 days'),
  ('30000000-0000-0000-0000-000000000006', 'Vercel과 Render 환경변수 체크리스트', '프런트엔드와 백엔드 배포 주소, CORS, OAuth 설정을 안전하게 연결합니다.', 'VIBE_CODING', 'BEGINNER', 18, 'https://vercel.com/docs/environment-variables', ARRAY['developer-tools', 'security-auth'], ARRAY['vercel', 'docker'], ARRAY['20000000-0000-0000-0000-000000000001'::uuid], true, now() - interval '6 days')
ON CONFLICT (id) DO UPDATE SET
  title = EXCLUDED.title,
  description = EXCLUDED.description,
  type = EXCLUDED.type,
  difficulty = EXCLUDED.difficulty,
  estimated_minutes = EXCLUDED.estimated_minutes,
  source_url = EXCLUDED.source_url,
  category_slugs = EXCLUDED.category_slugs,
  technology_slugs = EXCLUDED.technology_slugs,
  related_project_ids = EXCLUDED.related_project_ids,
  is_published = EXCLUDED.is_published,
  published_at = EXCLUDED.published_at,
  updated_at = now();

INSERT INTO weekly_insights (
  id, week_start, raw_metrics, ai_summary, model_name, generated_at, published_at
) VALUES (
  '40000000-0000-0000-0000-000000000001',
  date_trunc('week', current_date)::date,
  '{"source":"presentation-demo","project_count":12,"creator_count":8}'::jsonb,
  '{"headline":"작게 배포하고 빠르게 피드백을 받는 프로젝트가 성장하고 있습니다.","trends":[{"topic":"AI 협업 도구","direction":"UP","change_rate":24.8},{"topic":"개발 생산성","direction":"UP","change_rate":18.2},{"topic":"로컬 커뮤니티","direction":"UP","change_rate":11.6}],"watchlist":["pgvector","GitHub Actions","Vercel","Render"]}'::jsonb,
  'Click HUB presentation seed', now() - interval '30 minutes', now()
)
ON CONFLICT (week_start) DO NOTHING;

UPDATE weekly_insights
SET raw_metrics = '{"source":"presentation-demo","project_count":12,"creator_count":8}'::jsonb,
    ai_summary = '{"headline":"작게 배포하고 빠르게 피드백을 받는 프로젝트가 성장하고 있습니다.","trends":[{"topic":"AI 협업 도구","direction":"UP","change_rate":24.8},{"topic":"개발 생산성","direction":"UP","change_rate":18.2},{"topic":"로컬 커뮤니티","direction":"UP","change_rate":11.6}],"watchlist":["pgvector","GitHub Actions","Vercel","Render"]}'::jsonb,
    model_name = 'Click HUB presentation seed',
    generated_at = now() - interval '30 minutes',
    published_at = now()
WHERE id = '40000000-0000-0000-0000-000000000001';

INSERT INTO community_posts (
  id, board_id, author_id, title, body, status, view_count, created_at, updated_at
) VALUES
  ('50000000-0000-0000-0000-000000000001', (SELECT id FROM community_boards WHERE slug = 'notice'), '10000000-0000-0000-0000-000000000001', 'Click HUB MVP 발표 데모에 오신 것을 환영합니다', '프로젝트를 발견하고 메이커에게 피드백을 남겨보세요. 발표 환경의 모든 공개 콘텐츠는 안전한 데모 데이터입니다.', 'PUBLISHED', 184, now() - interval '6 hours', now() - interval '6 hours'),
  ('50000000-0000-0000-0000-000000000002', (SELECT id FROM community_boards WHERE slug = 'qna'), '10000000-0000-0000-0000-000000000002', 'Render와 Vercel의 환경변수는 어떻게 연결하나요?', '프런트엔드에는 공개 API 주소를, 백엔드에는 CORS 허용 주소와 OAuth 콜백 주소를 설정하면 됩니다.', 'PUBLISHED', 132, now() - interval '12 hours', now() - interval '12 hours'),
  ('50000000-0000-0000-0000-000000000003', (SELECT id FROM community_boards WHERE slug = 'share'), '10000000-0000-0000-0000-000000000005', '사이드 프로젝트 지표를 고를 때 참고한 기준', '처음부터 많은 지표를 모으기보다 방문, 저장, 댓글처럼 다음 행동을 설명하는 지표부터 시작했습니다.', 'PUBLISHED', 96, now() - interval '1 day', now() - interval '1 day'),
  ('50000000-0000-0000-0000-000000000004', (SELECT id FROM community_boards WHERE slug = 'free'), '10000000-0000-0000-0000-000000000003', '이번 주에 만든 작은 기능을 자랑해봐요', '저는 회의 노트에서 담당자별 할 일을 자동으로 묶는 기능을 만들었습니다. 여러분의 한 주도 궁금해요!', 'PUBLISHED', 88, now() - interval '2 days', now() - interval '2 days'),
  ('50000000-0000-0000-0000-000000000005', (SELECT id FROM community_boards WHERE slug = 'qna'), '10000000-0000-0000-0000-000000000006', '첫 사용자 피드백은 어디서 받으셨나요?', '기능 개발은 끝났는데 실제 사용자에게 어떻게 소개해야 할지 고민입니다. 경험을 나눠주세요.', 'PUBLISHED', 74, now() - interval '3 days', now() - interval '3 days'),
  ('50000000-0000-0000-0000-000000000006', (SELECT id FROM community_boards WHERE slug = 'share'), '10000000-0000-0000-0000-000000000008', '배포 전에 꼭 확인하는 보안 체크 5가지', '환경변수 노출, CORS 범위, 관리자 경로, 의존성 취약점, 로그의 개인정보 포함 여부를 확인합니다.', 'PUBLISHED', 143, now() - interval '4 days', now() - interval '4 days'),
  ('50000000-0000-0000-0000-000000000007', (SELECT id FROM community_boards WHERE slug = 'free'), '10000000-0000-0000-0000-000000000007', '프로젝트 이름은 어떻게 정하시나요?', '짧고 기억하기 쉬우면서 기능이 떠오르는 이름을 찾는 저만의 방법을 공유해봅니다.', 'PUBLISHED', 61, now() - interval '5 days', now() - interval '5 days'),
  ('50000000-0000-0000-0000-000000000008', (SELECT id FROM community_boards WHERE slug = 'share'), '10000000-0000-0000-0000-000000000004', '프롬프트 실험 기록 템플릿을 공유합니다', '목표, 입력, 기대 결과, 실제 결과, 다음 수정 사항 다섯 칸만 있어도 반복 실험이 훨씬 편해집니다.', 'PUBLISHED', 117, now() - interval '6 days', now() - interval '6 days')
ON CONFLICT (id) DO UPDATE SET
  board_id = EXCLUDED.board_id,
  author_id = EXCLUDED.author_id,
  title = EXCLUDED.title,
  body = EXCLUDED.body,
  status = EXCLUDED.status,
  view_count = EXCLUDED.view_count,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at,
  deleted_at = NULL;

INSERT INTO community_post_comments (
  id, post_id, author_id, parent_id, body, created_at, updated_at
) VALUES
  ('51000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', NULL, '발표에서 직접 좋아요와 댓글도 남겨볼게요!', now() - interval '5 hours', now() - interval '5 hours'),
  ('51000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', NULL, '다양한 프로젝트를 한 번에 볼 수 있어 좋습니다.', now() - interval '4 hours', now() - interval '4 hours'),
  ('51000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000008', NULL, 'OAuth 승인 URI도 배포 주소 기준으로 맞춰야 합니다.', now() - interval '10 hours', now() - interval '10 hours'),
  ('51000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', '51000000-0000-0000-0000-000000000003', '맞아요. Google Console 설정도 함께 확인해야 합니다.', now() - interval '9 hours', now() - interval '9 hours'),
  ('51000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000006', NULL, '초기에는 저장 수가 가장 솔직한 신호였어요.', now() - interval '20 hours', now() - interval '20 hours'),
  ('51000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', NULL, '저는 프롬프트 즐겨찾기 기능을 완성했습니다.', now() - interval '40 hours', now() - interval '40 hours'),
  ('51000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000002', NULL, '작은 완성도 꼭 기록해두면 큰 힘이 되더라고요.', now() - interval '38 hours', now() - interval '38 hours'),
  ('51000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000007', NULL, 'Click HUB 같은 커뮤니티에 먼저 소개해보세요!', now() - interval '2 days', now() - interval '2 days'),
  ('51000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000005', NULL, '로그에 토큰이 찍히는지도 꼭 확인하고 있습니다.', now() - interval '3 days', now() - interval '3 days'),
  ('51000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000003', NULL, '기능을 나타내는 단어 두 개를 조합하는 편이에요.', now() - interval '4 days', now() - interval '4 days'),
  ('51000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000006', NULL, '팀 회고 때 바로 써보겠습니다. 공유 감사해요.', now() - interval '5 days', now() - interval '5 days'),
  ('51000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000004', '51000000-0000-0000-0000-000000000011', '사용해보고 개선 아이디어도 알려주세요!', now() - interval '4 days 20 hours', now() - interval '4 days 20 hours')
ON CONFLICT (id) DO UPDATE SET
  body = EXCLUDED.body,
  parent_id = EXCLUDED.parent_id,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at,
  deleted_at = NULL;
