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

function figmaHomeProject(id) {
  return project({
    id,
    title: 'DevFlow Analytics',
    description:
      'Real-time performance tracking for indie makers. Monitor your server load, user engagement, and error...',
    category: '개발도구',
    categorySlug: 'developer-tools',
    likes: 1204,
    comments: 20,
    views: 20,
  })
}

// Top 100 — project_top100_7d 뷰 (최근 7일 활동 기준) 상위 항목
export const mockTopRankedProjects = ['prj_301', 'prj_302', 'prj_303'].map(figmaHomeProject)

// 맞춤 추천 — feed_score 기반 개인화 후보 (콘텐츠 기반 + 협업 필터링)
export const mockRecommendedProjects = ['prj_304', 'prj_305', 'prj_306'].map(figmaHomeProject)

// 내가 팔로잉한 개발자 — creator_subscriptions 기반, 구독한 제작자의 최신 프로젝트
export const mockFollowingProjects = ['prj_307', 'prj_308', 'prj_309'].map(figmaHomeProject)
