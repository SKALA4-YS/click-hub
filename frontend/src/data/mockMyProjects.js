// 마이페이지 "내 프로젝트" 탭 목업 — 콘텐츠(홈/랭킹/상세) 도메인의 mockProjects.js와는 별개로,
// 유저 도메인 화면(마이페이지)에서만 쓰는 최소 데이터. 실제 연동 시 GET /api/v1/users/me + 소유 프로젝트 목록으로 교체.
export const mockMyProjects = [
  {
    id: 'prj_301',
    title: 'DevFlow Analytics',
    status: 'PUBLISHED',
    pricing: 'FREEMIUM',
    weekly_visitors: 12410,
    unique_likes: 1204,
  },
  {
    id: 'prj_302b',
    title: 'PromptCraft Studio',
    status: 'PENDING_REVIEW',
    pricing: 'FREEMIUM',
    weekly_visitors: 0,
    unique_likes: 0,
  },
]
