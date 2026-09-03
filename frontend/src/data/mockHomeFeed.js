// 홈 피드 목업 — GET /api/v1/feed, GET /api/v1/rankings/projects 자리
// 실제 연동 전까지 화면 작업을 위해 콘텐츠 도메인(projects) 최소 필드만 흉내낸다.
// stats는 project_daily_metrics 집계값(좋아요·댓글·조회수)을 단순화한 것이다.

function project({ id, title, description, category, categorySlug, likes, comments, views }) {
  return {
    id,
    title,
    description,
    category,
    category_slug: categorySlug,
    thumbnail_url: null,
    stats: { likes, comments, views },
  }
}

// Top 100 — project_top100_7d 뷰 (최근 7일 활동 기준) 상위 항목
export const mockTopRankedProjects = [
  project({
    id: 'prj_301',
    title: 'DevFlow Analytics',
    description: 'Real-time performance tracking for indie makers. Monitor your server load, user engagement, and error...',
    category: '개발자 도구',
    categorySlug: 'developer-tools',
    likes: 1204,
    comments: 20,
    views: 20,
  }),
  project({
    id: 'prj_302',
    title: 'PromptCraft Studio',
    description: 'AI 프롬프트를 팀 단위로 관리하고 버전을 비교할 수 있는 워크스페이스.',
    category: '개발자 도구',
    categorySlug: 'developer-tools',
    likes: 980,
    comments: 14,
    views: 15,
  }),
  project({
    id: 'prj_303',
    title: 'IconGenie Studio',
    description: '텍스트 설명만으로 앱 아이콘 세트를 생성하는 디자인 도구.',
    category: '디자인',
    categorySlug: 'design-creative',
    likes: 860,
    comments: 9,
    views: 12,
  }),
]

// 맞춤 추천 — feed_score 기반 개인화 후보 (콘텐츠 기반 + 협업 필터링)
export const mockRecommendedProjects = [
  project({
    id: 'prj_304',
    title: 'GitPulse Activity',
    description: 'GitHub 커밋 히스토리를 시각화해 꾸준함을 보여주는 잔디밭 대체 위젯.',
    category: '개발자 도구',
    categorySlug: 'developer-tools',
    likes: 540,
    comments: 8,
    views: 11,
  }),
  project({
    id: 'prj_305',
    title: 'FlowBoard Kanban',
    description: '팀 회고와 칸반 보드를 한 화면에서 관리하는 협업 툴.',
    category: '생산성',
    categorySlug: 'productivity-work',
    likes: 421,
    comments: 6,
    views: 9,
  }),
  project({
    id: 'prj_306',
    title: 'CodeSnap Pro',
    description: '코드 스니펫을 예쁜 이미지로 내보내는 macOS 유틸리티.',
    category: '개발자 도구',
    categorySlug: 'developer-tools',
    likes: 389,
    comments: 5,
    views: 7,
  }),
]

// 내가 팔로잉한 개발자 — creator_subscriptions 기반, 구독한 제작자의 최신 프로젝트
export const mockFollowingProjects = [
  project({
    id: 'prj_307',
    title: 'StudyMate Planner',
    description: '취준생을 위한 스터디 일정과 회고 관리 서비스.',
    category: '교육/취업',
    categorySlug: 'education-career',
    likes: 312,
    comments: 4,
    views: 6,
  }),
  project({
    id: 'prj_308',
    title: 'MoodPalette',
    description: '오늘의 기분을 색으로 기록하는 미니멀 다이어리 앱.',
    category: '디자인',
    categorySlug: 'design-creative',
    likes: 245,
    comments: 3,
    views: 5,
  }),
  project({
    id: 'prj_309',
    title: 'ReceiptRadar',
    description: '영수증 사진 한 장으로 지출을 자동 분류하는 가계부.',
    category: '생활/건강',
    categorySlug: 'life-health',
    likes: 198,
    comments: 2,
    views: 4,
  }),
]
