import { apiClient } from '@/api/client'

export function getPendingProjects() {
  return apiClient.get('/v1/admin/projects', { auth: 'required' })
}

export function getAdminProjectDetail(id) {
  return apiClient.get(`/v1/admin/projects/${id}`, { auth: 'required' })
}

export function approveProject(id) {
  return apiClient.post(`/v1/admin/projects/${id}/approve`, { auth: 'required' })
}

export function rejectProject(id, reason) {
  return apiClient.post(`/v1/admin/projects/${id}/reject`, {
    auth: 'required',
    body: { reason },
  })
}
