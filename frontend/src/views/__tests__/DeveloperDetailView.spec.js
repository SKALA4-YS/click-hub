import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import DeveloperDetailView from '@/views/DeveloperDetailView.vue'
import { useAuthStore } from '@/stores/auth'

const api = vi.hoisted(() => ({
  getCreator: vi.fn(),
  getMyProjects: vi.fn(),
  toggleCreatorSubscription: vi.fn(),
  getProjectRankings: vi.fn(),
}))

vi.mock('@/api/users', () => ({
  getCreator: api.getCreator,
  getMyProjects: api.getMyProjects,
  toggleCreatorSubscription: api.toggleCreatorSubscription,
  updateOnboarding: vi.fn(),
  updateProfile: vi.fn(),
}))

vi.mock('@/api/rankings', () => ({
  getProjectRankings: api.getProjectRankings,
}))

async function mountView({ path = '/developers/creator-id', loggedIn = false } = {}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/developers/:id', component: DeveloperDetailView },
      { path: '/mypage', component: DeveloperDetailView },
      { path: '/projects/:id', component: { template: '<div>프로젝트</div>' } },
      { path: '/projects/new', component: { template: '<div>등록</div>' } },
      { path: '/login', name: 'login', component: { template: '<div>로그인</div>' } },
    ],
  })
  if (loggedIn) useAuthStore().$patch({ user: { id: 'viewer-id', display_name: '방문자' } })
  await router.push(path)
  await router.isReady()
  const wrapper = mount(DeveloperDetailView, { global: { plugins: [router] } })
  await flushPromises()
  return { wrapper, router }
}

describe('DeveloperDetailView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getCreator.mockReset().mockResolvedValue({
      id: 'creator-id',
      displayName: '김민준',
      avatarUrl: null,
      subscriberCount: 12,
      subscribedByMe: false,
      projects: [
        {
          id: 'project-id',
          title: 'Click HUB',
          description: '프로젝트 설명',
          categoryName: '개발자 도구',
        },
      ],
    })
    api.getMyProjects.mockReset().mockResolvedValue([])
    api.toggleCreatorSubscription.mockReset().mockResolvedValue({ subscribed: true })
    api.getProjectRankings.mockReset().mockResolvedValue([])
  })

  it('shows a creator and published projects from the backend', async () => {
    const { wrapper } = await mountView()

    expect(wrapper.get('h1').text()).toBe('김민준')
    expect(wrapper.text()).toContain('구독자 12명')
    expect(wrapper.text()).toContain('Click HUB')
    expect(api.getCreator).toHaveBeenCalledWith('creator-id')
  })

  it('persists subscriptions for an authenticated viewer', async () => {
    const { wrapper } = await mountView({ loggedIn: true })

    await wrapper
      .findAll('button')
      .find((button) => button.text() === '구독하기')
      .trigger('click')
    await flushPromises()

    expect(api.toggleCreatorSubscription).toHaveBeenCalledWith('creator-id')
    expect(wrapper.text()).toContain('구독 중')
  })

  it('uses the private project API on my page', async () => {
    api.getCreator.mockResolvedValue({
      id: 'viewer-id',
      displayName: '방문자',
      avatarUrl: null,
      subscriberCount: 0,
      subscribedByMe: false,
      projects: [],
    })
    api.getMyProjects.mockResolvedValue([
      { id: 'draft-id', title: '작성 중 프로젝트', description: '초안', status: 'DRAFT' },
    ])

    const { wrapper } = await mountView({ path: '/mypage', loggedIn: true })

    expect(api.getMyProjects).toHaveBeenCalledOnce()
    expect(wrapper.text()).toContain('작성 중 프로젝트')
  })
})
