import { mockProjects, mockComments } from '@/data/mockProjects'

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

export function fetchTop100() {
  const ranked = [...mockProjects].sort((a, b) => b.stats.unique_likes - a.stats.unique_likes)
  return delay(ranked)
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
