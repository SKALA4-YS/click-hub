import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { beforeEach, describe, expect, it } from 'vitest'

import OnboardingView from '@/views/OnboardingView.vue'
import { useAuthStore } from '@/stores/auth'

function mountOnboarding() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>홈</div>' } },
      { path: '/onboarding', component: OnboardingView },
    ],
  })

  return { router, wrapper: mount(OnboardingView, { global: { plugins: [router] } }) }
}

describe('OnboardingView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('moves through the goal, category, and tech stack steps while retaining selections', async () => {
    const { wrapper } = mountOnboarding()

    expect(wrapper.get('[data-testid="onboarding-progress"]').text()).toContain('1/3')
    await wrapper.get('[data-testid="option-indie-maker"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-next"]').trigger('click')

    expect(wrapper.get('[data-testid="onboarding-progress"]').text()).toContain('2/3')
    await wrapper.get('[data-testid="option-ai-service"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-next"]').trigger('click')

    expect(wrapper.get('[data-testid="onboarding-progress"]').text()).toContain('3/3')
    await wrapper.get('[data-testid="option-Vue.js"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-back"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-back"]').trigger('click')

    expect(wrapper.get('[data-testid="option-indie-maker"]').attributes('aria-pressed')).toBe(
      'true',
    )
  })

  it('skips onboarding and returns home', async () => {
    const { router, wrapper } = mountOnboarding()
    await router.push('/onboarding')
    await router.isReady()

    await wrapper.get('[data-testid="onboarding-skip"]').trigger('click')
    await flushPromises()

    expect(useAuthStore().onboarding).toMatchObject({ skipped: true })
    expect(router.currentRoute.value.path).toBe('/')
  })

  it('completes onboarding with all selected preferences and returns home', async () => {
    const { router, wrapper } = mountOnboarding()
    await router.push('/onboarding')
    await router.isReady()

    await wrapper.get('[data-testid="option-indie-maker"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-next"]').trigger('click')
    await wrapper.get('[data-testid="option-ai-service"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-next"]').trigger('click')
    await wrapper.get('[data-testid="option-Vue.js"]').trigger('click')
    await wrapper.get('[data-testid="onboarding-complete"]').trigger('click')
    await flushPromises()

    expect(useAuthStore().onboarding).toMatchObject({
      goals: ['indie-maker'],
      categories: ['ai-service'],
      techStacks: ['Vue.js'],
    })
    expect(router.currentRoute.value.path).toBe('/')
  })
})
