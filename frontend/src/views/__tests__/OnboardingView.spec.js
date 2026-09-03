import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({
  updateOnboarding: vi.fn(),
  getCategories: vi.fn(),
  getTechnologies: vi.fn(),
}))
vi.mock('@/api/users', () => ({ updateOnboarding: api.updateOnboarding, updateProfile: vi.fn() }))
vi.mock('@/api/catalog', () => ({
  getCategories: api.getCategories,
  getTechnologies: api.getTechnologies,
}))

import { useAuthStore } from '@/stores/auth'
import OnboardingView from '@/views/OnboardingView.vue'

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>홈</div>' } },
      { path: '/login', component: { template: '<div>로그인</div>' } },
      { path: '/onboarding', component: OnboardingView },
    ],
  })
}

async function mountOnboarding({ loggedIn = true, onboarding = null } = {}) {
  const pinia = createPinia()
  setActivePinia(pinia)
  const auth = useAuthStore()
  auth.user = loggedIn ? { id: 'viewer', displayName: '김민준' } : null
  auth.onboarding = onboarding
  const router = createTestRouter()
  await router.push('/onboarding')
  await router.isReady()
  const wrapper = mount(OnboardingView, { global: { plugins: [pinia, router] } })
  await flushPromises()
  return { auth, router, wrapper }
}

describe('OnboardingView', () => {
  beforeEach(() => {
    api.getCategories.mockReset().mockResolvedValue([
      { id: 'c1', slug: 'developer-tools', name: '개발자 도구' },
      { id: 'c2', slug: 'ai-service', name: 'AI 서비스' },
      { id: 'c3', slug: 'fintech', name: '핀테크' },
    ])
    api.getTechnologies.mockReset().mockResolvedValue([
      { id: 't1', slug: 'vue-js', name: 'Vue.js', defaultGroup: 'FRONTEND' },
      { id: 't2', slug: 'spring-boot', name: 'Spring Boot', defaultGroup: 'BACKEND' },
    ])
    api.updateOnboarding
      .mockReset()
      .mockImplementation(async (request) => ({ ...request, completedAt: '2026-09-04T00:00:00Z' }))
  })

  it('loads category and technology choices from the backend', async () => {
    const { wrapper } = await mountOnboarding()
    expect(wrapper.findAll('button[data-testid^="category-"]')).toHaveLength(3)
    expect(wrapper.text()).toContain('Vue.js')
    expect(api.getCategories).toHaveBeenCalledOnce()
    expect(api.getTechnologies).toHaveBeenCalledOnce()
  })

  it('persists catalog slugs for the selected preferences', async () => {
    const { wrapper, auth } = await mountOnboarding()
    await wrapper.get('[data-testid="category-developer-tools"]').trigger('click')
    await wrapper.get('[data-testid="recommended-vue-js"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-complete"]').trigger('click')
    await flushPromises()

    expect(api.updateOnboarding).toHaveBeenCalledWith(
      expect.objectContaining({
        categorySlugs: ['developer-tools'],
        technologySlugs: ['vue-js'],
      }),
    )
    expect(auth.onboarding.completedAt).toBeTruthy()
  })

  it('persists an explicit empty onboarding when skipped', async () => {
    const { wrapper } = await mountOnboarding()
    await wrapper.get('[data-testid="onboarding-skip"]').trigger('click')
    await flushPromises()
    expect(api.updateOnboarding).toHaveBeenCalledWith({
      goals: [],
      categorySlugs: [],
      technologySlugs: [],
    })
  })

  it('redirects visitors without a session', async () => {
    const { router } = await mountOnboarding({ loggedIn: false })
    expect(router.currentRoute.value.path).toBe('/login')
  })
})
