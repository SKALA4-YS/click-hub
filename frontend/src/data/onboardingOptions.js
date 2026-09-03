// 최초 Google 로그인 이후 온보딩(회원가입 대체 플로우)에서 사용하는 선택지.
// 기획서 API 명세 확정 전까지는 로컬 상태로만 관리하고, 실제로는 프로필 생성 API 요청에 포함될 값이다.

export const goals = [
  {
    id: 'indie-maker',
    label: '1인 사이드 프로젝트 빌더',
    description: '직접 만든 서비스를 알리고 초기 피드백을 모으고 싶어요.',
  },
  {
    id: 'tech-explorer',
    label: '새로운 서비스 탐색자',
    description: '출시된 재미있는 사이드 프로젝트를 써보고 피드백을 남겨요.',
  },
  {
    id: 'career-benchmark',
    label: '취준생 / 포트폴리오 벤치마커',
    description: '실제 배포된 서비스의 아키텍처와 코드를 참고하고 싶어요.',
  },
  {
    id: 'co-founder',
    label: '동료 빌더 구인 / 협업자',
    description: '함께 프로젝트를 만들 파트너를 찾아요.',
  },
]

export const interestCategories = [
  { slug: 'productivity-work', name: '생산성/업무' },
  { slug: 'education-career', name: '교육/취업' },
  { slug: 'developer-tools', name: '개발자 도구' },
  { slug: 'ai-service', name: 'AI 서비스' },
  { slug: 'design-creative', name: '디자인/크리에이티브' },
  { slug: 'other', name: '기타' },
]

export const popularTechStacks = [
  'Vue.js',
  'React',
  'Spring Boot',
  'FastAPI',
  'TypeScript',
  'Docker',
]
