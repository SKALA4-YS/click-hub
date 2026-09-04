import { apiClient, getApiUrl } from '@/api/client'

export function getGoogleLoginUrl() {
  return getApiUrl('/v1/auth/google')
}

export function loginAdmin({ username, password }) {
  return apiClient.post('/v1/admin/session', {
    auth: 'none',
    body: { username, password },
  })
}

export function getMe() {
  return apiClient.get('/v1/users/me', { auth: 'required' })
}
