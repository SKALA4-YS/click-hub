// 최초 Google 로그인 이후 온보딩(회원가입 대체 플로우)에서 사용하는 선택지.
// 기획서 API 명세 확정 전까지는 로컬 상태로만 관리하고, 실제로는 프로필 생성 API 요청에 포함될 값이다.

export const goals = [
  {
    id: 'indie-maker',
    label: '1인 사이드 프로젝트 빌더',
    description: '직접 만든 서비스를 알리고 초기 유저들의 피드백을 모으고 싶어요.',
  },
  {
    id: 'tech-explorer',
    label: '새로운 서비스 탐색자',
    description: '출시된 재미있는 사이드 프로젝트를 써보고 솔직한 피드백을 남겨요.',
  },
  {
    id: 'career-benchmark',
    label: '취준생 / 포트폴리오 벤치마커',
    description: '실제 배포된 서비스의 실전 아키텍처와 깃허브 코드를 참고하고 싶어요.',
  },
  {
    id: 'co-founder',
    label: '동료 빌더 구인 / 협업자',
    description: '함께 사이드 프로젝트를 런칭할 기획자, 디자이너, 개발자 파트너를 찾아요.',
  },
]

export const interestCategories = [
  { slug: 'productivity', name: '생산성 / 워크플로우' },
  { slug: 'developer-tools', name: '개발자 도구' },
  { slug: 'ai-service', name: 'AI 서비스 & LLM' },
  { slug: 'education', name: '교육 / 스터디 / 취업' },
  { slug: 'fintech', name: '핀테크 / 자산관리' },
  { slug: 'design', name: '디자인 & 크리에이티브' },
  { slug: 'social', name: '소셜 / 커뮤니티' },
  { slug: 'entertainment', name: '엔터테인먼트 & 미디어' },
  { slug: 'opensource', name: '오픈소스 & 유틸리티' },
]

export const popularTechStacks = [
  'Next.js 14',
  'Vue.js',
  'Spring Boot',
  'PostgreSQL',
  'Tailwind CSS',
  'OpenAI API',
  'React',
  'FastAPI',
  'Flutter',
  'Supabase',
  'Redis',
  'Docker',
  'TypeScript',
]
