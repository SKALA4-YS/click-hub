import { apiClient } from '@/api/client'

export function searchProjects({ q = '', category, tags, tech, cursor } = {}) {
  return apiClient.get('/v1/search', {
    query: { q, category, tags, tech, cursor },
  })
}
