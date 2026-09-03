import { apiClient } from '@/api/client'

export function getProjectRankings() {
  return apiClient.get('/v1/rankings/projects')
}

export function getDeveloperRankings() {
  return apiClient.get('/v1/rankings/developers')
}
