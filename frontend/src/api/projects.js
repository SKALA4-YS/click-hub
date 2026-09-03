import { apiClient } from '@/api/client'

export function getProject(id) {
  return apiClient.get(`/v1/projects/${id}`)
}

export function createProject(project) {
  return apiClient.post('/v1/projects', { auth: 'required', body: project })
}

export function updateProject(id, project) {
  return apiClient.patch(`/v1/projects/${id}`, { auth: 'required', body: project })
}

export function submitProject(id) {
  return apiClient.post(`/v1/projects/${id}/submit`, { auth: 'required' })
}

export function deleteProject(id) {
  return apiClient.delete(`/v1/projects/${id}`, { auth: 'required' })
}

export function recordOutboundClick(id) {
  return apiClient.post(`/v1/projects/${id}/outbound-clicks`)
}

export function toggleProjectLike(id) {
  return apiClient.put(`/v1/projects/${id}/like`, { auth: 'required' })
}

export function toggleProjectFavorite(id) {
  return apiClient.put(`/v1/projects/${id}/favorite`, { auth: 'required' })
}

export function getProjectComments(id) {
  return apiClient.get(`/v1/projects/${id}/comments`)
}

export function createProjectComment(id, body) {
  return apiClient.post(`/v1/projects/${id}/comments`, {
    auth: 'required',
    body: { body },
  })
}
