import { apiClient } from '@/api/client'

export function getCommunityBoards() {
  return apiClient.get('/v1/community/boards', { auth: 'required' })
}

export function getCommunityPosts(boardSlug, { cursor } = {}) {
  return apiClient.get(`/v1/community/boards/${boardSlug}/posts`, {
    auth: 'required',
    query: { cursor },
  })
}

export function createCommunityPost(boardSlug, post) {
  return apiClient.post(`/v1/community/boards/${boardSlug}/posts`, {
    auth: 'required',
    body: post,
  })
}

export function getCommunityPost(postId) {
  return apiClient.get(`/v1/community/posts/${postId}`, { auth: 'required' })
}

export function updateCommunityPost(postId, post) {
  return apiClient.patch(`/v1/community/posts/${postId}`, {
    auth: 'required',
    body: post,
  })
}

export function deleteCommunityPost(postId) {
  return apiClient.delete(`/v1/community/posts/${postId}`, { auth: 'required' })
}

export function getCommunityComments(postId) {
  return apiClient.get(`/v1/community/posts/${postId}/comments`, { auth: 'required' })
}

export function createCommunityComment(postId, comment) {
  return apiClient.post(`/v1/community/posts/${postId}/comments`, {
    auth: 'required',
    body: comment,
  })
}
