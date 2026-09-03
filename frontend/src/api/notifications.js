import { apiClient } from '@/api/client'

export function getNotifications() {
  return apiClient.get('/v1/notifications', { auth: 'required' })
}

export function markNotificationRead(id) {
  return apiClient.patch(`/v1/notifications/${id}/read`, { auth: 'required' })
}
