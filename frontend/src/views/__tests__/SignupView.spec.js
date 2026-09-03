import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import SignupView from '@/views/SignupView.vue'

async function mountSignup() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<main>Home page</main>' } },
      { path: '/login', component: { template: '<main>Login page</main>' } },
      { path: '/onboarding', component: { template: '<main>Onboarding page</main>' } },
      { path: '/signup', component: SignupView },
    ],
  })
  await router.push('/signup')
  await router.isReady()
  return {
    router,
    wrapper: mount({ template: '<RouterView />' }, { global: { plugins: [router] } }),
  }
}

async function fillValidSignup(wrapper) {
  await wrapper.get('[name="profile"]').setValue('김메이커')
  await wrapper.get('[name="email"]').setValue('maker@clickhub.dev')
  await wrapper.get('[name="password"]').setValue('clickhub12!')
  await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
  await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
}

describe('SignupView', () => {
  it('renders the Figma signup copy and single password field accessibly', async () => {
    const { wrapper } = await mountSignup()

    expect(wrapper.text()).toContain('인디 메이커를 위한 플랫폼')
    expect(wrapper.get('h1').text()).toBe('Click-Hub에 오신 것을 환영합니다')
    expect(wrapper.text()).toContain('Google로 3초 만에 시작하기')
    expect(wrapper.text()).toContain('또는 이메일로 직접 가입')
    expect(wrapper.findAll('input[type="password"]')).toHaveLength(1)
    expect(wrapper.get('[name="profile"]').attributes('aria-label')).toBe('이름 또는 닉네임')
    expect(wrapper.get('[name="email"]').attributes('aria-describedby')).toContain('email-helper')
  })

  it('updates the four-stage password strength indicator from empty to strong', async () => {
    const { wrapper } = await mountSignup()

    expect(wrapper.get('[data-testid="password-strength"]').attributes('data-level')).toBe('0')
    await wrapper.get('[name="password"]').setValue('clickhub12!')
    expect(wrapper.get('[data-testid="password-strength"]').attributes('data-level')).toBe('4')
    expect(wrapper.findAll('[data-testid="password-strength"] span.is-active')).toHaveLength(4)
  })

  it('toggles selected technology and position chips independently', async () => {
    const { wrapper } = await mountSignup()
    const next = wrapper.get('[data-chip="Next.js"]')
    const designer = wrapper.get('[data-chip="디자이너"]')

    await next.trigger('click')
    await designer.trigger('click')

    expect(next.attributes('aria-pressed')).toBe('true')
    expect(designer.attributes('aria-pressed')).toBe('true')
    expect(wrapper.get('[data-chip="Vue.js"]').attributes('aria-pressed')).toBe('false')
  })

  it('selects and clears every agreement from the all-agreements control', async () => {
    const { wrapper } = await mountSignup()
    const all = wrapper.get('[data-testid="all-agreements"]')

    await all.setValue(true)
    expect(wrapper.get('[data-testid="terms-agreement"]').element.checked).toBe(true)
    expect(wrapper.get('[data-testid="privacy-agreement"]').element.checked).toBe(true)
    expect(wrapper.get('[data-testid="newsletter-agreement"]').element.checked).toBe(true)

    await all.setValue(false)
    expect(wrapper.get('[data-testid="terms-agreement"]').element.checked).toBe(false)
    expect(wrapper.get('[data-testid="privacy-agreement"]').element.checked).toBe(false)
    expect(wrapper.get('[data-testid="newsletter-agreement"]').element.checked).toBe(false)
  })

  it('reflects individual agreement changes in the all-agreements control', async () => {
    const { wrapper } = await mountSignup()

    await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
    await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
    await wrapper.get('[data-testid="newsletter-agreement"]').setValue(true)

    expect(wrapper.get('[data-testid="all-agreements"]').element.checked).toBe(true)
  })

  it('keeps signup unavailable and exposes its disabled state until required agreements are selected', async () => {
    const { wrapper } = await mountSignup()

    expect(wrapper.get('[data-testid="signup-submit"]').attributes('disabled')).toBeDefined()
    await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
    expect(wrapper.get('[data-testid="signup-submit"]').attributes('disabled')).toBeDefined()
    await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
    expect(wrapper.get('[data-testid="signup-submit"]').attributes('disabled')).toBeUndefined()
  })

  it('navigates locally to onboarding after a valid email signup', async () => {
    const { router, wrapper } = await mountSignup()
    await fillValidSignup(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/onboarding')
    expect(wrapper.text()).toContain('Onboarding page')
  })

  it('takes the Google start CTA through the same local onboarding route', async () => {
    const { router, wrapper } = await mountSignup()

    await wrapper.get('[data-testid="google-signup"]').trigger('click')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/onboarding')
  })

  it('takes the login link to the login route', async () => {
    const { router, wrapper } = await mountSignup()

    await wrapper.get('a[href="/login"]').trigger('click')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/login')
    expect(wrapper.text()).toContain('Login page')
  })
})
