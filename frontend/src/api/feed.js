import { apiClient } from '@/api/client'

export function getFeed({ cursor } = {}) {
  return apiClient.get('/v1/feed', { query: { cursor } })
}
