import { apiClient } from '@/api/client'

export function getWeeklyInsight() {
  return apiClient.get('/v1/insights/weekly')
}
