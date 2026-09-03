import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import ProjectDetailView from '@/views/ProjectDetailView.vue'
import { useAuthStore } from '@/stores/auth'

const api = vi.hoisted(() => ({
  getProject: vi.fn(),
  getProjectComments: vi.fn(),
  recordOutboundClick: vi.fn(),
  toggleProjectFavorite: vi.fn(),
  toggleProjectLike: vi.fn(),
  createProjectComment: vi.fn(),
  searchProjects: vi.fn(),
  getCreator: vi.fn(),
  toggleCreatorSubscription: vi.fn(),
}))

vi.mock('@/api/projects', () => ({
  getProject: api.getProject,
  getProjectComments: api.getProjectComments,
  recordOutboundClick: api.recordOutboundClick,
  toggleProjectFavorite: api.toggleProjectFavorite,
  toggleProjectLike: api.toggleProjectLike,
  createProjectComment: api.createProjectComment,
}))
vi.mock('@/api/search', () => ({ searchProjects: api.searchProjects }))
vi.mock('@/api/users', () => ({
  getCreator: api.getCreator,
  toggleCreatorSubscription: api.toggleCreatorSubscription,
  updateOnboarding: vi.fn(),
  updateProfile: vi.fn(),
}))

const detail = {
  id: 'project-id',
  title: 'Click HUB',
  description: '실제 Backend 상세 API 응답',
  siteUrl: 'https://clickhub.example',
  categorySlug: 'developer-tools',
  categoryName: '개발자 도구',
  tags: ['vue', 'spring'],
  thumbnailUrl: null,
  techStacks: [{ technologyName: 'Vue.js', group: 'FRONTEND', version: '3' }],
  ownerName: '김민준',
  ownerId: 'creator-id',
  publishedAt: '2026-09-01T00:00:00Z',
  likeCount: 4,
  favoriteCount: 2,
  likedByMe: false,
  favoritedByMe: false,
}

async function mountView({ loggedIn = false } = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/projects/:id', component: ProjectDetailView },
      { path: '/login', name: 'login', component: { template: '<div>로그인</div>' } },
      { path: '/rankings', component: { template: '<div>프로젝트</div>' } },
      { path: '/', component: { template: '<div>홈</div>' } },
    ],
  })
  await router.push('/projects/project-id')
  await router.isReady()
  if (loggedIn) useAuthStore().$patch({ user: { display_name: '김민준' } })

  const wrapper = mount(ProjectDetailView, { global: { plugins: [router] } })
  await flushPromises()
  return { wrapper, router }
}

describe('ProjectDetailView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getProject.mockReset().mockResolvedValue(detail)
    api.getProjectComments.mockReset().mockResolvedValue([
      {
        id: 'comment-1',
        authorId: 'viewer-id',
        authorName: '방문자',
        body: '실제 저장된 댓글',
        createdAt: '2026-09-03T00:00:00Z',
      },
    ])
    api.getCreator.mockReset().mockResolvedValue({
      subscriberCount: 3,
      subscribedByMe: false,
      projects: [{ id: 'project-id' }],
    })
    api.searchProjects
      .mockReset()
      .mockResolvedValue({ items: [], nextCursor: null, hasNext: false })
    api.toggleProjectLike.mockReset().mockResolvedValue({ liked: true, likeCount: 5 })
    api.toggleProjectFavorite.mockReset().mockResolvedValue({ favorited: true })
    api.toggleCreatorSubscription.mockReset().mockResolvedValue({ subscribed: true })
    api.createProjectComment.mockReset().mockResolvedValue({
      id: 'comment-2',
      authorId: 'viewer-id',
      authorName: '김민준',
      body: '새 피드백',
      createdAt: '2026-09-04T00:00:00Z',
    })
    api.recordOutboundClick.mockReset().mockResolvedValue({ recorded: true })
    vi.stubGlobal('open', vi.fn())
  })

  it('renders project, creator and comments from backend APIs', async () => {
    const { wrapper } = await mountView()

    expect(wrapper.get('h1').text()).toContain('Click HUB')
    expect(wrapper.text()).toContain('실제 Backend 상세 API 응답')
    expect(wrapper.text()).toContain('실제 저장된 댓글')
    expect(wrapper.text()).toContain('Vue.js 3')
    expect(api.getProject).toHaveBeenCalledWith('project-id')
    expect(api.getProjectComments).toHaveBeenCalledWith('project-id')
  })

  it('persists authenticated reactions and comments through APIs', async () => {
    const { wrapper } = await mountView({ loggedIn: true })

    await wrapper.get('button[aria-label="프로젝트 좋아요"]').trigger('click')
    await flushPromises()
    expect(api.toggleProjectLike).toHaveBeenCalledWith('project-id')
    expect(wrapper.get('button[aria-label="프로젝트 좋아요"]').text()).toContain('5')

    await wrapper.get('textarea[name="feedback"]').setValue('새 피드백')
    await wrapper.get('button[name="submit-feedback"]').trigger('click')
    await flushPromises()
    expect(api.createProjectComment).toHaveBeenCalledWith('project-id', '새 피드백')
    expect(wrapper.text()).toContain('새 피드백')
  })

  it('records outbound clicks without blocking navigation', async () => {
    const { wrapper } = await mountView()

    await wrapper
      .findAll('button')
      .find((button) => button.text().includes('사이트 방문'))
      .trigger('click')

    expect(window.open).toHaveBeenCalledWith('https://clickhub.example', '_blank', 'noopener')
    expect(api.recordOutboundClick).toHaveBeenCalledWith('project-id')
  })
})
