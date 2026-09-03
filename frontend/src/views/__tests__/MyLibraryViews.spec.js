import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.hoisted(() => {
  Object.defineProperty(globalThis, 'localStorage', {
    value: { getItem: () => null, setItem: () => {}, removeItem: () => {} },
    configurable: true,
  })
})

const api = vi.hoisted(() => ({
  getMyFavorites: vi.fn(),
  getMySubscriptions: vi.fn(),
  toggleCreatorSubscription: vi.fn(),
  toggleProjectFavorite: vi.fn(),
}))
vi.mock('@/api/users', () => ({
  getMyFavorites: api.getMyFavorites,
  getMySubscriptions: api.getMySubscriptions,
  toggleCreatorSubscription: api.toggleCreatorSubscription,
  updateOnboarding: vi.fn(),
  updateProfile: vi.fn(),
}))
vi.mock('@/api/projects', () => ({ toggleProjectFavorite: api.toggleProjectFavorite }))
vi.mock('@/views/DeveloperDetailView.vue', () => ({
  default: { template: '<section><h1>김민준</h1><p>내 프로젝트</p></section>' },
}))

import FavoritesView from '@/views/FavoritesView.vue'
import FollowingView from '@/views/FollowingView.vue'
import MyPageView from '@/views/MyPageView.vue'
import { useAuthStore } from '@/stores/auth'

const routerLinkStub = {
  props: ['to'],
  template: '<a :href="typeof to === \'string\' ? to : to?.path"><slot /></a>',
}

describe('member library pages', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getMyFavorites.mockReset().mockResolvedValue([
      {
        id: 'project-1',
        title: 'CodeSnap Pro',
        description: 'API favorite',
        categoryName: '개발자 도구',
        ownerName: 'Maker',
      },
    ])
    api.getMySubscriptions.mockReset().mockResolvedValue([
      {
        id: 'creator-1',
        displayName: '김민준',
        avatarUrl: null,
        subscriberCount: 4,
        projectCount: 2,
      },
    ])
    api.toggleProjectFavorite.mockReset().mockResolvedValue({ favorited: false })
    api.toggleCreatorSubscription.mockReset().mockResolvedValue({ subscribed: false })
  })

  it('uses the API-backed developer profile for my page', () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().$patch({ user: { id: 'viewer', displayName: '김민준' } })
    const wrapper = mount(MyPageView, {
      global: { plugins: [pinia], stubs: { RouterLink: routerLinkStub } },
    })
    expect(wrapper.text()).toContain('김민준')
    expect(wrapper.text()).toContain('내 프로젝트')
  })

  it('loads and removes server favorites', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().$patch({ user: { id: 'viewer' } })
    const wrapper = mount(FavoritesView, {
      global: { plugins: [pinia], stubs: { RouterLink: routerLinkStub } },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('1개 저장됨')
    expect(wrapper.text()).toContain('CodeSnap Pro')
    await wrapper.get('[aria-label="CodeSnap Pro 즐겨찾기 해제"]').trigger('click')
    await flushPromises()
    expect(api.toggleProjectFavorite).toHaveBeenCalledWith('project-1')
    expect(wrapper.findAll('[data-testid="favorite-card"]')).toHaveLength(0)
  })

  it('loads and removes server subscriptions', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().$patch({ user: { id: 'viewer' } })
    const wrapper = mount(FollowingView, {
      global: { plugins: [pinia], stubs: { RouterLink: routerLinkStub } },
    })
    await flushPromises()
    expect(wrapper.text()).toContain('팔로잉 메이커 1명')
    await wrapper.get('[aria-label="김민준 팔로우 해제"]').trigger('click')
    await flushPromises()
    expect(api.toggleCreatorSubscription).toHaveBeenCalledWith('creator-1')
    expect(wrapper.findAll('[data-testid="following-card"]')).toHaveLength(0)
  })
})
