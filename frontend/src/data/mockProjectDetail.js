// 프로젝트 상세 목업 — GET /api/v1/projects/{id} 자리.
// 홈 피드(mockHomeFeed.js)에 나온 것과 같은 id를 써서, 홈 카드 클릭 -> 상세로 자연스럽게 이어지게 한다.
// 필드는 schema.sql의 projects / project_technologies / project_comments / users를 단순화한 형태.

export const mockProjectDetails = {
  prj_301: {
    id: 'prj_301',
    title: 'DevFlow Analytics',
    description:
      '인디 개발자와 소규모 팀을 위한 실시간 초경량 웹 프로덕트 애널리틱스입니다. 쿠키 없는 경량 스크립트와 심플한 퍼널 분석을 통해 사이트 이탈률과 전환율을 파악할 수 있습니다.\n\n서버 로그 없이도 실시간 방문자 수, 외부 클릭, CTR을 대시보드 한 화면에서 확인할 수 있고, AI가 증감 원인을 자연어로 요약해줍니다.',
    site_url: 'https://devflow-analytics.io',
    category: '개발자 도구',
    tags: ['SaaS', '분석', 'Next.js'],
    thumbnail_url: null,
    created_at: '2026-08-20T00:00:00Z',
    owner: {
      id: 'usr_10',
      display_name: '김민준',
      avatar_initial: '김',
      followers: 2400,
      project_count: 3,
    },
    tech_stack: {
      FRONTEND: ['Next.js', 'TypeScript', 'Tailwind CSS'],
      BACKEND: ['Spring Boot'],
      DATABASE: ['PostgreSQL'],
      INFRA_DEPLOY: ['Vercel'],
    },
    stats: { likes: 1204, favorites: 1840, views: 24190, comments: 20 },
    rank: 1,
    comments: [
      {
        id: 'c_1',
        author: '정다은',
        body: '대시보드 로딩 속도가 진짜 빠르네요. 번들 사이즈도 작아서 좋아요!',
        created_at: '2026-08-30T02:00:00Z',
        likes: 42,
      },
      {
        id: 'c_2',
        author: '최우진',
        body: 'GA4 대신 붙여봤는데 설정이 훨씬 간단했습니다.',
        created_at: '2026-08-31T09:00:00Z',
        likes: 19,
      },
    ],
  },
  prj_302: {
    id: 'prj_302',
    title: 'PromptCraft Studio',
    description:
      'AI 프롬프트를 팀 단위로 관리하고 버전을 비교할 수 있는 워크스페이스입니다. OpenAI·Claude API 토큰 비용을 캐싱으로 절감하고, A/B 테스트 결과를 한눈에 비교할 수 있습니다.',
    site_url: 'https://promptcraft.io',
    category: '개발자 도구',
    tags: ['AI', '생산성'],
    thumbnail_url: null,
    created_at: '2026-08-15T00:00:00Z',
    owner: {
      id: 'usr_11',
      display_name: 'Sarah Park',
      avatar_initial: 'S',
      followers: 980,
      project_count: 2,
    },
    tech_stack: {
      FRONTEND: ['Vue.js', 'TypeScript'],
      BACKEND: ['FastAPI'],
      DATABASE: ['PostgreSQL'],
      AI_DATA: ['OpenAI API'],
    },
    stats: { likes: 980, favorites: 1420, views: 15230, comments: 14 },
    rank: 2,
    comments: [
      {
        id: 'c_3',
        author: 'Chloe Lee',
        body: '토큰 캐싱 시뮬레이션 기능 혹시 추가 계획 있으신가요?',
        created_at: '2026-09-01T01:00:00Z',
        likes: 6,
      },
    ],
  },
  prj_303: {
    id: 'prj_303',
    title: 'IconGenie Studio',
    description:
      '텍스트 설명만으로 앱 아이콘 세트를 생성하는 디자인 도구. Figma 플러그인과 반응형 SVG 엔진을 함께 제공합니다.',
    site_url: 'https://icongenie.dev',
    category: '디자인',
    tags: ['디자인', 'SVG'],
    thumbnail_url: null,
    created_at: '2026-08-10T00:00:00Z',
    owner: {
      id: 'usr_12',
      display_name: 'Yuna C.',
      avatar_initial: 'Y',
      followers: 1420,
      project_count: 1,
    },
    tech_stack: { FRONTEND: ['React', 'TypeScript'] },
    stats: { likes: 860, favorites: 990, views: 8900, comments: 9 },
    rank: 3,
    comments: [],
  },
}

// 상세 페이지 하단 "이런 사이트는 어때요?" — 실제로는 콘텐츠 기반 추천 API 자리
export function getRelatedProjects(currentId) {
  return Object.values(mockProjectDetails)
    .filter((project) => project.id !== currentId)
    .map((project) => ({
      id: project.id,
      title: project.title,
      description: project.description.split('\n')[0],
      category: project.category,
      thumbnail_url: null,
      stats: {
        likes: project.stats.likes,
        comments: project.stats.comments,
        views: project.stats.views,
      },
    }))
}
