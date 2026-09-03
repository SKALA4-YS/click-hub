import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it, vi } from 'vitest'

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

function mountOnboarding({ loggedIn = true, onboarding = null } = {}) {
  const auth = useAuthStore()
  auth.user = loggedIn ? { display_name: '김민준' } : null
  auth.onboarding = onboarding
  const router = createTestRouter()

  return {
    auth,
    router,
    wrapper: mount(OnboardingView, { global: { plugins: [router] } }),
  }
}

describe('OnboardingView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders the single-page 1/3 preference screen with all three preference sections', () => {
    const { wrapper } = mountOnboarding()

    expect(wrapper.find('main').exists()).toBe(false)
    expect(wrapper.get('[role="progressbar"]').attributes()).toMatchObject({
      'aria-valuenow': '33',
      'aria-valuetext': '맞춤 설정 1/3단계, 33% 완료됨',
    })
    expect(wrapper.text()).toContain('1. 주 활동 포지션 / 목표')
    expect(wrapper.text()).toContain('2. 관심 프로젝트 카테고리')
    expect(wrapper.text()).toContain('3. 관심 기술 스택 (Tech Stack)')
    expect(wrapper.findAll('button[data-testid^="category-"]')).toHaveLength(9)
    expect(wrapper.text()).toContain('3개 선택됨 (최소 3개 권장)')
    expect(wrapper.text()).toContain('Next.js 14')
    expect(wrapper.text()).toContain('회원님의 취향을 기반으로 생성 중인 홈 피드 미리보기')
    expect(wrapper.text()).toContain('실시간 42개 매칭 완료')
  })

  it('filters categories, keeps checkbox selections, and updates the selection count', async () => {
    const { wrapper } = mountOnboarding()

    await wrapper.get('[data-testid="category-search"]').setValue('핀테크')
    expect(wrapper.findAll('button[data-testid^="category-"]')).toHaveLength(1)
    await wrapper.get('[data-testid="category-fintech"]').trigger('click')

    expect(wrapper.get('[data-testid="category-fintech"]').attributes('aria-pressed')).toBe('true')
    expect(wrapper.text()).toContain('4개 선택됨 (최소 3개 권장)')
  })

  it('filters stacks, adds a recommended stack, and removes selected tags', async () => {
    const { wrapper } = mountOnboarding()

    await wrapper.get('[data-testid="stack-search"]').setValue('React')
    expect(wrapper.get('[data-testid="recommended-React"]')).toBeTruthy()
    await wrapper.get('[data-testid="recommended-React"]').trigger('click')

    expect(wrapper.get('[data-testid="selected-React"]')).toBeTruthy()
    await wrapper.get('[data-testid="remove-React"]').trigger('click')
    expect(wrapper.find('[data-testid="selected-React"]').exists()).toBe(false)
  })

  it('uses replace navigation for skip and stores the skipped state', async () => {
    const { auth, router, wrapper } = mountOnboarding()
    const replace = vi.spyOn(router, 'replace')

    await wrapper.get('[data-testid="onboarding-skip"]').trigger('click')
    await flushPromises()

    expect(auth.onboarding).toMatchObject({ skipped: true })
    expect(replace).toHaveBeenCalledWith('/')
  })

  it('uses replace navigation for completion and stores current selections', async () => {
    const { auth, router, wrapper } = mountOnboarding()
    const replace = vi.spyOn(router, 'replace')

    await wrapper.get('[data-testid="goal-co-founder"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-complete"]').trigger('click')
    await flushPromises()

    expect(auth.onboarding.goals).toContain('co-founder')
    expect(replace).toHaveBeenCalledWith('/')
  })

  it('redirects unauthenticated visitors to login and completed visitors home', async () => {
    const unauthenticated = mountOnboarding({ loggedIn: false })
    await flushPromises()
    expect(unauthenticated.router.currentRoute.value.path).toBe('/login')

    const completed = mountOnboarding({ onboarding: { skipped: true } })
    await flushPromises()
    expect(completed.router.currentRoute.value.path).toBe('/')
  })
})
