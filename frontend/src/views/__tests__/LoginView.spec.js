import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import LoginView from '@/views/LoginView.vue'
import { useAuthStore } from '@/stores/auth'

const push = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    push.mockReset()
  })

  it('shows the Click HUB welcome copy and Google sign-in action', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('section').attributes('aria-labelledby')).toBe('login-heading')
    expect(wrapper.get('h1').attributes('id')).toBe('login-heading')
    expect(wrapper.get('h1').text()).toBe('Click HUB에 오신 것을 환영합니다')
    expect(wrapper.text()).toContain(
      '배포한 사이드 프로젝트를 알리고, 새로운 서비스를 발견해보세요.',
    )
    expect(wrapper.get('button').text()).toContain('Google로 시작하기')
    expect(wrapper.get('button').attributes('aria-label')).toBeUndefined()
  })

  it('sets the static logged-in state and moves to onboarding after the Google action', async () => {
    const wrapper = mount(LoginView)

    await wrapper.get('button').trigger('click')

    expect(useAuthStore().isLoggedIn).toBe(true)
    expect(push).toHaveBeenCalledWith('/onboarding')
  })
})
