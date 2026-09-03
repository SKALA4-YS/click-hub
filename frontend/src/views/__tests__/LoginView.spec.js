import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import LoginView from '@/views/LoginView.vue'

const route = vi.hoisted(() => ({ query: {} }))

vi.mock('vue-router', () => ({
  useRoute: () => route,
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    window.sessionStorage.clear()
    route.query = {}
  })

  it('renders the dedicated Google sign-in page structure', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('a[href="/"]').text()).toContain('홈으로 돌아가기')
    expect(wrapper.text()).toContain('인디 서비스 실시간 탐색 중')
    expect(wrapper.get('h1').text()).toBe('다시 오신 것을 환영해요')
    expect(wrapper.text()).toContain(
      '배포된 사이트 프로젝트의 성과를 확인하고 전 세계 메이커들과 피드백을 나누세요.',
    )
    expect(wrapper.get('[name="google-login"]').text()).toContain('Google로 3초 만에 로그인')
    expect(wrapper.text()).toContain('추천')
    expect(wrapper.text()).toContain('1,400+명의 인디 메이커 활동 중')
    expect(wrapper.text()).toContain('주간 핫 프로젝트 #1')
    expect(wrapper.text()).toContain('984')
    expect(wrapper.text()).toContain('256-bit SSL 엔드투엔드 암호화 보안 적용')
  })

  it('links directly to the backend Google authorization endpoint', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.get('[name="google-login"]').attributes('href')).toBe(
      'http://localhost:8080/v1/auth/google',
    )
  })

  it('remembers the protected destination before leaving for Google', async () => {
    route.query = { redirect: '/favorites?category=ai' }
    const wrapper = mount(LoginView)
    wrapper.get('[name="google-login"]').element.addEventListener('click', (event) => {
      event.preventDefault()
    })

    await wrapper.get('[name="google-login"]').trigger('click')

    expect(window.sessionStorage.getItem('clickhub.oauthReturnPath')).toBe('/favorites?category=ai')
  })

  it('does not expose a fake email or password login', () => {
    const wrapper = mount(LoginView)

    expect(wrapper.find('input[type="email"]').exists()).toBe(false)
    expect(wrapper.find('input[type="password"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('MVP1은 Google 로그인만 지원합니다')
  })
})
