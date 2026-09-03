import { apiClient } from '@/api/client'

export function getTutorials({ type, difficulty, tech } = {}) {
  return apiClient.get('/v1/tutorials', { query: { type, difficulty, tech } })
}
