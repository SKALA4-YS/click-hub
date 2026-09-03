import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import SignupView from '@/views/SignupView.vue'

function mountSignup() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>Home</div>' } },
      { path: '/login', component: { template: '<div>Login</div>' } },
      { path: '/onboarding', component: { template: '<div>Onboarding</div>' } },
    ],
  })

  return { router, wrapper: mount(SignupView, { global: { plugins: [router] } }) }
}

describe('SignupView', () => {
  it('shows field errors when an empty signup form is submitted', async () => {
    const { wrapper } = mountSignup()

    await wrapper.get('form').trigger('submit')

    expect(wrapper.get('[data-error="name"]').text()).toBe('이름을 입력해주세요.')
    expect(wrapper.get('[data-error="email"]').text()).toBe('이메일을 입력해주세요.')
    expect(wrapper.get('[data-error="password"]').text()).toBe('비밀번호를 입력해주세요.')
    expect(wrapper.get('[data-error="agreement"]').text()).toBe('필수 약관에 동의해주세요.')
  })

  it('keeps the submit action disabled until required agreements are accepted', async () => {
    const { wrapper } = mountSignup()

    expect(wrapper.get('[data-testid="signup-submit"]').attributes('disabled')).toBeDefined()

    await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
    await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)

    expect(wrapper.get('[data-testid="signup-submit"]').attributes('disabled')).toBeUndefined()
  })

  it('navigates locally to onboarding after a valid signup submission', async () => {
    const { router, wrapper } = mountSignup()
    await router.push('/')
    await router.isReady()

    await wrapper.get('[name="name"]').setValue('클릭허브')
    await wrapper.get('[name="email"]').setValue('hello@clickhub.dev')
    await wrapper.get('[name="password"]').setValue('clickhub123!')
    await wrapper.get('[name="passwordConfirmation"]').setValue('clickhub123!')
    await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
    await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
    await wrapper.get('form').trigger('submit')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/onboarding')
    expect(wrapper.get('[role="status"]').text()).toContain('가입 정보를 확인했습니다')
  })
})
