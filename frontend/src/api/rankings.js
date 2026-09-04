import { apiClient } from '@/api/client'

export function getDeveloperRankings() {
  return apiClient.get('/v1/rankings/developers')
}
