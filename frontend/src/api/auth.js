import { apiClient, getApiUrl } from '@/api/client'

export function getGoogleLoginUrl() {
  return getApiUrl('/v1/auth/google')
}

export function getMe() {
  return apiClient.get('/v1/users/me', { auth: 'required' })
}
