import { apiClient } from '@/api/client'

export function getCategories() {
  return apiClient.get('/v1/catalog/categories')
}

export function getTechnologies() {
  return apiClient.get('/v1/catalog/technologies')
}
