export const roadmapFilters = [
  { id: 'all', label: '전체 로드맵', count: 5 },
  { id: 'planning', label: '기획 & 프롬프팅' },
  { id: 'ai', label: 'AI 도구' },
  { id: 'ui', label: 'UI/UX & 프론트엔드' },
  { id: 'backend', label: 'DB & 백엔드' },
  { id: 'release', label: '원클릭 런칭 & 배포' },
]

export const roadmapSteps = [
  {
    id: 'planning',
    step: 'Step 01',
    category: '기획 / 프롬프팅',
    title: 'Step 1. 막연한 아이디어를 AI가 이해하는 기획서(PRD)로 바꾸기',
    description:
      'Claude와 대화하며 기능 명세, 와이어프레임 플로우를 추출하고 자연어 한 문장으로 시작해 개발 착수 직전 수준의 구조화된 PRD를 정립합니다.',
    duration: '25분',
    tools: 'Claude / ChatGPT',
    outcome: '실습 프롬프트 포함',
  },
  {
    id: 'ui',
    step: 'Step 02',
    category: 'UI 컴포넌트',
    title: 'Step 2. 디자인 툴 없이 자연어로 고화질 반응형 UI 뽑아내기',
    description:
      'v0와 Stitch를 활용해 버튼 클릭 한 번으로 모던한 웹 컴포넌트를 만들고, 브랜드 톤에 맞는 반응형 코드를 완성합니다.',
    duration: '35분',
    tools: 'v0 / Stitch / Tailwind CSS',
    outcome: 'UI 컴포넌트 킷',
  },
  {
    id: 'ai',
    step: 'Step 03',
    category: 'AI 도구',
    title: 'Step 3. Cursor IDE로 에러 없이 조립하는 실전 테크닉',
    description:
      'Composer 모드와 @Docs 활용법, AI가 헛소리할 때 코드 롤백 및 디버깅 팁을 익혀 복잡한 클라이언트와 서버 로직을 매끄럽게 연결합니다.',
    duration: '45분',
    tools: 'Cursor IDE / Next.js',
    outcome: '실전 디버깅 가이드',
    recommended: true,
  },
  {
    id: 'backend',
    step: 'Step 04',
    category: '데이터베이스',
    title: 'Step 4. 복잡한 백엔드 없이 10분 만에 Supabase DB & 로그인 붙이기',
    description:
      '자연어로 스키마를 생성하고 소셜 로그인과 데이터를 연동합니다. SQL을 직접 짜지 않아도 보안 규칙까지 안전하게 설정하는 완벽 가이드입니다.',
    duration: '40분',
    tools: 'Supabase / PostgreSQL',
    outcome: 'SQL 프롬프트 제공',
  },
  {
    id: 'release',
    step: 'Step 05',
    category: '배포 & 런칭',
    title: 'Step 5. Vercel 원클릭 무료 배포와 커스텀 도메인 연결',
    description:
      'GitHub를 연결해 브랜치 푸시만으로 배포하고 검색엔진에 노출합니다. 내 도메인을 직접 붙이고 오픈그래프 이미지까지 자동 생성합니다.',
    duration: '20분',
    tools: 'Vercel / GitHub',
    outcome: '수료증 발급',
  },
]

export const starterTools = [
  { name: 'Cursor IDE', detail: '스마트한 차세대 AI 에디터', tag: '필수 도구' },
  { name: 'Claude 3.5 Sonnet', detail: '최고의 기획 & 추론 파트너', tag: '기획 추천' },
  { name: 'v0 / Stitch', detail: '초고속 반응형 UI 컴포넌트 생성', tag: 'UI 디자인' },
  { name: 'Supabase', detail: '10분 완성 서버리스 DB & 인증', tag: '백엔드' },
  { name: 'Vercel', detail: '원클릭 글로벌 무료 배포', tag: '배포/도메인' },
]

export const faqItems = [
  [
    '정말 코딩을 몰라도 사이트 런칭이 가능한가요?',
    'AI가 코딩의 95% 이상을 작성하므로, 논리적인 요구사항 전달과 에러 해결 요령만 익히면 충분합니다.',
  ],
  [
    'AI가 에러를 낼 때 가장 빠르게 해결하는 방법은?',
    '터미널 에러 메시지를 복사해 Cursor나 Claude에 던진 후 원인과 해결책을 수정해달라고 요청하면 빠르게 해결됩니다.',
  ],
  [
    '호스팅과 도메인 유지 비용은 얼마나 드나요?',
    'Vercel과 Supabase 무료 티어로 시작하면 초기 서버 비용은 없으며, 도메인 구매 비용만 발생합니다.',
  ],
]

export const promptTemplates = [
  [
    '원페이지 SaaS 랜딩페이지 기획 & 레이아웃 프롬프트',
    '나는 실리콘밸리 최고의 스타트업 그로스 디자이너야. 내 서비스 설명을 기반으로 전환율 높은 원페이지 랜딩페이지를 제작해줘. 히어로 섹션, 핵심 문제점 3가지, 해결책, 실제 고객 후기, FAQ, 가격표를 포함한 Tailwind CSS 컴포넌트 코드를 작성해줘.',
  ],
  [
    '모바일 반응형 대시보드 UI 컴포넌트 생성 프롬프트',
    'Cursor Composer 모드에서 작성해줘. Next.js App Router 환경이며 상단 네비게이션, 통계 KPI 카드 4개, 최근 활동 내역 리스트, 모바일 햄버거 토글이 완전히 동작하는 반응형 대시보드 레이아웃을 생성해줘.',
  ],
  [
    'Supabase 연동 회원가입 및 결제 유도 모달 프롬프트',
    'Supabase Auth 구글 OAuth 로그인 핸들러와 미들웨어 보호 라우트를 구성해줘. 비로그인 유저가 기능을 사용하면 결제 플랜 업그레이드를 안내하는 블러 모달 팝업을 띄우는 클라이언트 컴포넌트를 만들어줘.',
  ],
]

export const showcases = [
  [
    '뉴스레터 요약 AI 툴',
    '비개발자 마케터 김다솔 님',
    '코딩 경험 제로였지만 Claude와 Cursor를 사용해 48시간 만에 첫 유료 결제 고객 12명을 확보했습니다.',
    'MRR 32만원',
  ],
  [
    '인디해커 북마크 매니저',
    'UI 디자이너 정민지 님',
    'v0로 와이어프레임을 찍고 Supabase DB를 붙여 프로토타입을 배포했습니다. 개발자 협업 없이 런칭했어요.',
    '북마크 1,400개',
  ],
]
