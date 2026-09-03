// 전체 사이트 목록 목업 — GET /api/v1/projects (카테고리·태그·기술스택·가격 필터, 커서 페이지네이션) 자리.
// 홈 피드와 상세페이지에 이미 나온 항목(prj_301~303)을 포함해 화면 간 이동이 자연스럽게 이어지도록 한다.

function project({
  id,
  title,
  description,
  category,
  categorySlug,
  likes,
  comments,
  views,
  viewsDisplay,
}) {
  return {
    id,
    title,
    description,
    category,
    category_slug: categorySlug,
    thumbnail_url: null,
    stats: { likes, comments, views, viewsDisplay },
  }
}

export const mockProjectList = [
  project({ id: 'prj_301', title: 'DevFlow Analytics', description: '인디 개발자와 소규모 팀을 위한 실시간 초경량 웹 프로덕트 애널리틱스.', category: '개발자 도구', categorySlug: 'developer-tools', likes: 1204, comments: 20, views: 24190 }),
  project({ id: 'prj_302', title: 'PromptCraft Studio', description: 'AI 프롬프트 체이닝과 버전 관리를 위한 협업 워크스페이스.', category: 'AI', categorySlug: 'ai-service', likes: 942, comments: 14, views: 4100, viewsDisplay: '4.1k' }),
  project({ id: 'prj_303', title: 'IconGenie Studio', description: '텍스트 설명만으로 앱 아이콘 세트를 생성하는 디자인 도구.', category: '디자인', categorySlug: 'design-creative', likes: 830, comments: 9, views: 8900 }),
  project({ id: 'prj_310', title: 'LogStack Pro', description: '분산 서버 환경의 로그를 실시간으로 스트리밍하고 AI 기반 이상 징후를 탐지합니다.', category: '개발자 도구', categorySlug: 'developer-tools', likes: 694, comments: 19, views: 2800 }),
  project({ id: 'prj_311', title: 'GrowthPulse', description: '사이드 프로젝트의 전환율과 이탈률을 한눈에 보여주는 마케팅 분석 대시보드.', category: '마케팅', categorySlug: 'marketing', likes: 512, comments: 12, views: 1500 }),
  project({ id: 'prj_305', title: 'FlowBoard Kanban', description: '마크다운 기반의 미니멀리스트 1인 개발자용 칸반 보드 및 스프린트 트래커.', category: '생산성', categorySlug: 'productivity-work', likes: 420, comments: 16, views: 1100 }),
  project({ id: 'prj_304', title: 'GitPulse Activity', description: 'GitHub 커밋 히스토리를 시각화해 꾸준함을 보여주는 잔디밭 대체 위젯.', category: '개발자 도구', categorySlug: 'developer-tools', likes: 480, comments: 9, views: 890 }),
  project({ id: 'prj_306', title: 'CodeSnap Pro', description: 'Turn beautiful code snippets into shareable images for your next launch.', category: '개발자 도구', categorySlug: 'developer-tools', likes: 775, comments: 31, views: 3400, viewsDisplay: '3.4k' }),
  project({ id: 'prj_312', title: 'MoodPalette', description: '오늘의 기분을 색으로 기록하는 미니멀 다이어리 앱.', category: '디자인', categorySlug: 'design-creative', likes: 245, comments: 3, views: 500 }),
  project({ id: 'prj_313', title: 'StudyMate Planner', description: '취준생을 위한 스터디 일정과 회고 관리 서비스.', category: '교육/취업', categorySlug: 'education-career', likes: 312, comments: 4, views: 600 }),
  project({ id: 'prj_314', title: 'ReceiptRadar', description: '영수증 사진 한 장으로 지출을 자동 분류하는 가계부.', category: '생활/건강', categorySlug: 'life-health', likes: 198, comments: 2, views: 400 }),
  project({ id: 'prj_315', title: 'PixelMuse AI', description: '프롬프트 한 줄로 아이콘·일러스트를 생성하는 AI 이미지 툴.', category: 'AI', categorySlug: 'ai-service', likes: 380, comments: 11, views: 3400 }),
  project({ id: 'prj_316', title: 'ClipCast Studio', description: '유튜브 롱폼 영상을 숏폼 클립으로 자동 편집해주는 서비스.', category: '엔터테인먼트', categorySlug: 'content-entertainment', likes: 365, comments: 17, views: 5200 }),
  project({ id: 'prj_317', title: 'TinyCRM', description: '1인 프리랜서를 위한 초경량 고객 관리 스프레드시트 대체 툴.', category: '생산성', categorySlug: 'productivity-work', likes: 288, comments: 4, views: 900 }),
]
