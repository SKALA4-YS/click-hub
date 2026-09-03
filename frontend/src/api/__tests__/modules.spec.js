import { beforeEach, describe, expect, it, vi } from 'vitest'

const client = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
  delete: vi.fn(),
}))

vi.mock('@/api/client', () => ({
  apiClient: client,
  getApiUrl: (path) => `https://api.clickhub.test${path}`,
}))

import { getGoogleLoginUrl, getMe } from '@/api/auth'
import { getCategories, getTechnologies } from '@/api/catalog'
import {
  createCommunityComment,
  createCommunityPost,
  getCommunityBoards,
  getCommunityPosts,
} from '@/api/community'
import { getFeed } from '@/api/feed'
import { getWeeklyInsight } from '@/api/insights'
import { getNotifications, markNotificationRead } from '@/api/notifications'
import {
  createProject,
  createProjectComment,
  getProject,
  getProjectComments,
  recordOutboundClick,
  toggleProjectFavorite,
  toggleProjectLike,
} from '@/api/projects'
import { getDeveloperRankings, getProjectRankings } from '@/api/rankings'
import { searchProjects } from '@/api/search'
import { getTutorials } from '@/api/tutorials'
import {
  getCreator,
  getMyFavorites,
  getMyProjects,
  getMySubscriptions,
  toggleCreatorSubscription,
  updateOnboarding,
  updateProfile,
} from '@/api/users'

describe('domain API modules', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses the backend authentication contract', () => {
    expect(getGoogleLoginUrl()).toBe('https://api.clickhub.test/v1/auth/google')
    getMe()
    expect(client.get).toHaveBeenCalledWith('/v1/users/me', { auth: 'required' })
  })

  it('maps public discovery requests to their API paths and query parameters', () => {
    getFeed({ cursor: 'feed-cursor' })
    searchProjects({ q: 'vue', category: 'developer-tools', tags: ['web'], tech: ['vue'] })
    getProjectRankings()
    getDeveloperRankings()
    getTutorials({ type: 'ARTICLE', difficulty: 'BEGINNER', tech: 'vue' })
    getWeeklyInsight()
    getCategories()
    getTechnologies()

    expect(client.get.mock.calls).toEqual([
      ['/v1/feed', { query: { cursor: 'feed-cursor' } }],
      [
        '/v1/search',
        {
          query: {
            q: 'vue',
            category: 'developer-tools',
            tags: ['web'],
            tech: ['vue'],
            cursor: undefined,
          },
        },
      ],
      ['/v1/rankings/projects'],
      ['/v1/rankings/developers'],
      ['/v1/tutorials', { query: { type: 'ARTICLE', difficulty: 'BEGINNER', tech: 'vue' } }],
      ['/v1/insights/weekly'],
      ['/v1/catalog/categories'],
      ['/v1/catalog/technologies'],
    ])
  })

  it('marks project writes and personal resources as authenticated', () => {
    getProject('project-id')
    createProject({ title: 'Click HUB' })
    recordOutboundClick('project-id')
    toggleProjectLike('project-id')
    toggleProjectFavorite('project-id')
    getProjectComments('project-id')
    createProjectComment('project-id', '좋은 프로젝트입니다.')

    expect(client.get).toHaveBeenCalledWith('/v1/projects/project-id')
    expect(client.post).toHaveBeenCalledWith('/v1/projects', {
      auth: 'required',
      body: { title: 'Click HUB' },
    })
    expect(client.post).toHaveBeenCalledWith('/v1/projects/project-id/outbound-clicks')
    expect(client.put).toHaveBeenCalledWith('/v1/projects/project-id/like', {
      auth: 'required',
    })
    expect(client.put).toHaveBeenCalledWith('/v1/projects/project-id/favorite', {
      auth: 'required',
    })
    expect(client.get).toHaveBeenCalledWith('/v1/projects/project-id/comments')
    expect(client.post).toHaveBeenCalledWith('/v1/projects/project-id/comments', {
      auth: 'required',
      body: { body: '좋은 프로젝트입니다.' },
    })
  })

  it('uses authenticated community, notification and user endpoints', () => {
    getCommunityBoards()
    getCommunityPosts('free', { cursor: 'next' })
    createCommunityPost('free', { title: '제목', body: '본문' })
    createCommunityComment('post-id', { body: '댓글', parentId: null })
    getNotifications()
    markNotificationRead(12)
    updateOnboarding({ categorySlugs: ['ai-service'] })
    updateProfile({ displayName: '클릭허브' })
    getMyProjects()
    getMyFavorites()
    getMySubscriptions()
    getCreator('creator-id')
    toggleCreatorSubscription('creator-id')

    expect(client.get).toHaveBeenCalledWith('/v1/community/boards', { auth: 'required' })
    expect(client.get).toHaveBeenCalledWith('/v1/community/boards/free/posts', {
      auth: 'required',
      query: { cursor: 'next' },
    })
    expect(client.patch).toHaveBeenCalledWith('/v1/notifications/12/read', {
      auth: 'required',
    })
    expect(client.put).toHaveBeenCalledWith('/v1/users/me/onboarding', {
      auth: 'required',
      body: { categorySlugs: ['ai-service'] },
    })
    expect(client.get).toHaveBeenCalledWith('/v1/users/me/projects', { auth: 'required' })
    expect(client.get).toHaveBeenCalledWith('/v1/users/me/favorites', { auth: 'required' })
    expect(client.get).toHaveBeenCalledWith('/v1/users/me/subscriptions', { auth: 'required' })
    expect(client.get).toHaveBeenCalledWith('/v1/creators/creator-id')
    expect(client.put).toHaveBeenCalledWith('/v1/creators/creator-id/subscription', {
      auth: 'required',
    })
  })
})
