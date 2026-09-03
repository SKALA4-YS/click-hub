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

  it('renders the dedicated sign-in page structure from the design', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('a[href="/"]').text()).toContain('홈으로 돌아가기')
    expect(wrapper.text()).toContain('인디 서비스 실시간 탐색 중')
    expect(wrapper.get('h1').text()).toBe('다시 오신 것을 환영해요')
    expect(wrapper.text()).toContain('배포된 사이드 프로젝트의 성과를 확인하고')
    expect(wrapper.get('button[name="google-login"]').text()).toContain('Google로 3초 만에 로그인')
    expect(wrapper.text()).toContain('추천')
    expect(wrapper.text()).toContain('또는 이메일로 로그인')
    expect(wrapper.text()).toContain('1,400+ 명의 인디 메이커 활동 중')
  })

  it('provides labelled email, password, keep-login, and password visibility controls', async () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('input[type="email"]').attributes('placeholder')).toBe('maker@domain.com')
    expect(wrapper.get('input[type="password"]').attributes('placeholder')).toBe(
      '비밀번호를 입력하세요',
    )
    expect(wrapper.get('input[type="checkbox"]').attributes('name')).toBe('keep-logged-in')

    await wrapper.get('button[name="toggle-password"]').trigger('click')

    expect(wrapper.get('input[name="password"]').attributes('type')).toBe('text')
  })

  it('keeps invalid email sign-in on the form and describes the correction', async () => {
    const wrapper = mount(LoginView)

    await wrapper.get('form').trigger('submit')

    expect(push).not.toHaveBeenCalled()
    expect(wrapper.get('[role="alert"]').text()).toContain('이메일 주소를 입력해주세요')
  })

  it('moves to onboarding for Google and valid local static sign-in actions', async () => {
    const wrapper = mount(LoginView)

    await wrapper.get('button[name="google-login"]').trigger('click')
    expect(useAuthStore().isLoggedIn).toBe(true)
    expect(push).toHaveBeenLastCalledWith('/onboarding')

    useAuthStore().logout()
    await wrapper.get('input[name="email"]').setValue('maker@domain.com')
    await wrapper.get('input[name="password"]').setValue('secret123')
    await wrapper.get('form').trigger('submit')

    expect(useAuthStore().isLoggedIn).toBe(true)
    expect(push).toHaveBeenLastCalledWith('/onboarding')
  })

  it('offers the static signup route without a dead forgot-password link', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('a[href="/signup"]').text()).toContain('회원가입')
    expect(wrapper.find('a[href="#"]').exists()).toBe(false)
  })
})
