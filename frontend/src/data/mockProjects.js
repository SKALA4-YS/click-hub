// Mock 데이터: 기획서 12장 API 명세 확정 전까지 홈/상세/랭킹 화면을 채우는 임시 데이터.
// 실제 API 연동 시 src/api/projects.js 의 함수 내부만 교체하면 되도록 스키마(design/schema.sql)의
// projects / project_technologies / categories 필드 이름을 그대로 따른다.

export const categories = [
  { slug: 'all', name: '전체' },
  { slug: 'developer-tools', name: '개발도구' },
  { slug: 'design-creative', name: '디자인' },
  { slug: 'content-entertainment', name: '엔터테인먼트' },
  { slug: 'ai-service', name: 'AI' },
  { slug: 'productivity-work', name: '생산성' },
  { slug: 'marketing', name: '마케팅' },
  { slug: 'other', name: '기타' },
]

export const mockProjects = [
  {
    id: 'prj_301',
    title: 'DevFlow Analytics',
    description:
      '1인 개발자를 위한 실시간 성능 모니터링 & 사용자 행동 분석 SaaS. 서버 부하, 트래픽, 에러 로그를 한눈에 시각화합니다.',
    site_url: 'https://devflow.example.com',
    repository_url: 'https://github.com/alex-dev/devflow-analytics',
    category: 'developer-tools',
    pricing: 'FREEMIUM',
    tags: ['SaaS', 'Next.js', 'TypeScript'],
    tech_stack: {
      FRONTEND: ['Next.js', 'TypeScript'],
      BACKEND: ['Node.js'],
      DATABASE: ['ClickHouse'],
      INFRA_DEPLOY: ['Vercel'],
    },
    owner: { display_name: 'Alex Kim', github_login: 'alex-dev' },
    stats: {
      unique_visitors: 12410,
      valid_outbound_clicks: 3820,
      unique_likes: 1204,
      unique_commenters: 20,
      unique_favorites: 1806,
    },
    published_at: '2026-08-20T02:00:00Z',
  },
  {
    id: 'prj_302',
    title: 'PromptCraft Studio',
    description:
      'LLM 프롬프트 체이닝과 버전 관리를 위한 올인원 워크스페이스. 팀 간 프롬프트 A/B 테스트를 자동화합니다.',
    site_url: 'https://promptcraft.example.com',
    repository_url: null,
    category: 'ai-service',
    pricing: 'FREEMIUM',
    tags: ['AI', 'Python', 'FastAPI'],
    tech_stack: {
      FRONTEND: ['React'],
      BACKEND: ['FastAPI'],
      AI_DATA: ['OpenAI API'],
    },
    owner: { display_name: 'Sarah Park', github_login: 'sarah-maker' },
    stats: {
      unique_visitors: 8120,
      valid_outbound_clicks: 2210,
      unique_likes: 942,
      unique_commenters: 14,
      unique_favorites: 1413,
    },
    published_at: '2026-08-14T02:00:00Z',
  },
  {
    id: 'prj_303',
    title: 'IconGenie Studio',
    description:
      'SVG 벡터 아이콘을 수초 만에 생성하고 디자인 토큰으로 내보내는 웹 디자이너 필수 툴입니다.',
    site_url: 'https://icongenie.example.com',
    repository_url: 'https://github.com/yuna-c/icongenie',
    category: 'design-creative',
    pricing: 'PAID',
    tags: ['디자인', 'SVG'],
    tech_stack: {
      FRONTEND: ['Vue.js'],
      BACKEND: ['Node.js'],
    },
    owner: { display_name: 'Yuna C.', github_login: 'yuna-c' },
    stats: {
      unique_visitors: 4230,
      valid_outbound_clicks: 980,
      unique_likes: 830,
      unique_commenters: 8,
      unique_favorites: 1245,
    },
    published_at: '2026-08-25T02:00:00Z',
  },
  {
    id: 'prj_304',
    title: 'GitPulse Activity',
    description:
      '깃허브 기여 내역을 시각적인 3D 타임라인으로 렌더링해주는 개발자 프로필 포트폴리오 위젯입니다.',
    site_url: 'https://gitpulse.example.com',
    repository_url: 'https://github.com/minjun/gitpulse',
    category: 'developer-tools',
    pricing: 'FREE',
    tags: ['생산성', 'GitHub API'],
    tech_stack: {
      FRONTEND: ['React'],
      AI_DATA: [],
    },
    owner: { display_name: 'Minjun', github_login: 'minjun' },
    stats: {
      unique_visitors: 3010,
      valid_outbound_clicks: 640,
      unique_likes: 480,
      unique_commenters: 9,
      unique_favorites: 720,
    },
    published_at: '2026-08-10T02:00:00Z',
  },
  {
    id: 'prj_305',
    title: 'FlowBoard Kanban',
    description:
      '마크다운 기반의 미니멀리스트 1인 개발자용 로컬 칸반 보드 및 스프린트 트래커입니다.',
    site_url: 'https://flowboard.example.com',
    repository_url: 'https://github.com/kenji-t/flowboard',
    category: 'other',
    pricing: 'FREE',
    tags: ['생산성', '오픈소스'],
    tech_stack: {
      FRONTEND: ['Vue.js'],
      DATABASE: ['SQLite'],
    },
    owner: { display_name: 'Kenji T.', github_login: 'kenji-t' },
    stats: {
      unique_visitors: 2400,
      valid_outbound_clicks: 510,
      unique_likes: 420,
      unique_commenters: 16,
      unique_favorites: 630,
    },
    published_at: '2026-07-30T02:00:00Z',
  },
  {
    id: 'prj_306',
    title: 'CodeSnap Pro',
    description:
      '소스코드를 고해상도 그래픽 카드 이미지로 원클릭 변환하여 SNS와 릴리즈 노트에 활용합니다.',
    site_url: 'https://codesnap.example.com',
    repository_url: 'https://github.com/david-dev/codesnap',
    category: 'developer-tools',
    pricing: 'FREEMIUM',
    tags: ['개발도구', 'TypeScript'],
    tech_stack: {
      FRONTEND: ['TypeScript'],
    },
    owner: { display_name: 'David Dev', github_login: 'david-dev' },
    stats: {
      unique_visitors: 3960,
      valid_outbound_clicks: 890,
      unique_likes: 775,
      unique_commenters: 31,
      unique_favorites: 1162,
    },
    published_at: '2026-08-05T02:00:00Z',
  },
]

export const mockComments = [
  {
    id: 'c_1',
    project_id: 'prj_301',
    author: '정우성',
    body: 'DevFlow 연동해봤는데 번들 사이즈가 작아서 좋았어요!',
    created_at: '2026-09-01T05:00:00Z',
  },
  {
    id: 'c_2',
    project_id: 'prj_301',
    author: 'Chloe Lee',
    body: '대시보드 로딩 속도가 인상적이네요. 캐싱 전략이 궁금해요.',
    created_at: '2026-09-02T01:00:00Z',
  },
]
