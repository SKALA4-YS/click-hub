import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import SignupView from '@/views/SignupView.vue'

async function mountSignup() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div>Home</div>' } },
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

async function fillValidFields(wrapper) {
  await wrapper.get('[name="name"]').setValue('클릭허브')
  await wrapper.get('[name="email"]').setValue('hello@clickhub.dev')
  await wrapper.get('[name="password"]').setValue('clickhub123!')
  await wrapper.get('[name="passwordConfirmation"]').setValue('clickhub123!')
}

async function acceptRequiredAgreements(wrapper) {
  await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
  await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
}

describe('SignupView', () => {
  it('keeps the visible submit button disabled until both required agreements are accepted', async () => {
    const { wrapper } = await mountSignup()
    const submit = wrapper.get('[data-testid="signup-submit"]')

    expect(submit.attributes('disabled')).toBeDefined()
    await submit.trigger('click')
    expect(wrapper.find('[data-error="agreement"]').exists()).toBe(false)

    await wrapper.get('[data-testid="terms-agreement"]').setValue(true)
    expect(submit.attributes('disabled')).toBeDefined()
    await wrapper.get('[data-testid="privacy-agreement"]').setValue(true)
    expect(submit.attributes('disabled')).toBeUndefined()
  })

  it('shows field errors after the user clicks an enabled invalid signup button without navigating', async () => {
    const { router, wrapper } = await mountSignup()
    await acceptRequiredAgreements(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')

    expect(wrapper.get('[data-error="name"]').text()).toBe('이름을 입력해주세요.')
    expect(wrapper.get('[data-error="email"]').text()).toBe('이메일을 입력해주세요.')
    expect(wrapper.get('[data-error="password"]').text()).toBe('비밀번호를 입력해주세요.')
    expect(router.currentRoute.value.path).toBe('/signup')
  })

  it('rejects an invalid email address without navigating', async () => {
    const { router, wrapper } = await mountSignup()
    await fillValidFields(wrapper)
    await wrapper.get('[name="email"]').setValue('not-an-email')
    await acceptRequiredAgreements(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')

    expect(wrapper.get('[data-error="email"]').text()).toBe('올바른 이메일 주소를 입력해주세요.')
    expect(router.currentRoute.value.path).toBe('/signup')
  })

  it('rejects a short password without navigating', async () => {
    const { router, wrapper } = await mountSignup()
    await fillValidFields(wrapper)
    await wrapper.get('[name="password"]').setValue('short')
    await wrapper.get('[name="passwordConfirmation"]').setValue('short')
    await acceptRequiredAgreements(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')

    expect(wrapper.get('[data-error="password"]').text()).toBe('비밀번호는 8자 이상 입력해주세요.')
    expect(router.currentRoute.value.path).toBe('/signup')
  })

  it('rejects a mismatched password confirmation without navigating', async () => {
    const { router, wrapper } = await mountSignup()
    await fillValidFields(wrapper)
    await wrapper.get('[name="passwordConfirmation"]').setValue('different-password')
    await acceptRequiredAgreements(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')

    expect(wrapper.get('#signup-password-confirmation-error').text()).toBe(
      '비밀번호가 일치하지 않습니다.',
    )
    expect(router.currentRoute.value.path).toBe('/signup')
  })

  it('navigates to onboarding after the user clicks a valid signup button', async () => {
    const { router, wrapper } = await mountSignup()
    await fillValidFields(wrapper)
    await acceptRequiredAgreements(wrapper)

    await wrapper.get('[data-testid="signup-submit"]').trigger('click')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/onboarding')
    expect(wrapper.text()).toContain('Onboarding page')
  })

  it('takes the visible login link to the login route', async () => {
    const { router, wrapper } = await mountSignup()

    await wrapper.get('a[href="/login"]').trigger('click')
    await new Promise((resolve) => setTimeout(resolve))

    expect(router.currentRoute.value.path).toBe('/login')
    expect(wrapper.text()).toContain('Login page')
  })
})
