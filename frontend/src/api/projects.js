import { mockProjects, mockComments } from '@/data/mockProjects'
import { getApiBaseUrl } from '@/services/api'

// 실제 API 명세(기획서 12장, 확정 시)로 교체될 자리.
// 기획서의 "Interface First" 원칙에 따라 호출부(views/components)는 이 모듈의 함수 시그니처만 알면 되도록 유지한다.
const MOCK_DELAY_MS = 200

function delay(value) {
  return new Promise((resolve) => setTimeout(() => resolve(value), MOCK_DELAY_MS))
}

export function fetchFeed({ category = 'all' } = {}) {
  const items =
    category === 'all'
      ? mockProjects
      : mockProjects.filter((project) => project.category === category)
  return delay(items)
}

// GET /v1/rankings/projects — 실제 백엔드 연동(2026-09-03). 응답은 { rank, projectId, title, score }
// 뿐이라 description/category/stats 같은 목업 전용 필드는 없다 — projectId를 id로 별칭해
// ProjectCard의 라우터 링크/키가 그대로 동작하게만 맞춘다(나머지는 ProjectCard에서 방어 렌더링).
export async function fetchTop100() {
  const response = await fetch(`${getApiBaseUrl()}/v1/rankings/projects`)
  if (!response.ok) {
    throw new Error(`GET /v1/rankings/projects failed with HTTP ${response.status}`)
  }
  const { data } = await response.json()
  return data.map((item) => ({
    id: item.projectId,
    projectId: item.projectId,
    title: item.title,
    score: item.score,
  }))
}

export function fetchProjectById(id) {
  const project = mockProjects.find((item) => item.id === id) ?? null
  return delay(project)
}

export function fetchCommentsByProjectId(id) {
  return delay(mockComments.filter((comment) => comment.project_id === id))
}

export function recordOutboundClick(id) {
  // POST /v1/projects/{id}/outbound-clicks 자리 — 목업 단계에서는 로그만 남긴다.
  console.info(`[mock] outbound click recorded for ${id}`)
  return delay({ ok: true })
}
