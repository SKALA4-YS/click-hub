import { apiClient } from '@/api/client'

export function updateOnboarding(onboarding) {
  return apiClient.put('/v1/users/me/onboarding', {
    auth: 'required',
    body: onboarding,
  })
}

export function updateProfile(profile) {
  return apiClient.patch('/v1/users/me', { auth: 'required', body: profile })
}

export function getMyProjects() {
  return apiClient.get('/v1/users/me/projects', { auth: 'required' })
}

export function getMyFavorites() {
  return apiClient.get('/v1/users/me/favorites', { auth: 'required' })
}

export function getMySubscriptions() {
  return apiClient.get('/v1/users/me/subscriptions', { auth: 'required' })
}

export function getCreator(creatorId) {
  return apiClient.get(`/v1/creators/${creatorId}`)
}

export function toggleCreatorSubscription(creatorId) {
  return apiClient.put(`/v1/creators/${creatorId}/subscription`, { auth: 'required' })
}
