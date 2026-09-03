import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import OAuthCallbackView from '@/views/OAuthCallbackView.vue'
import { useAuthStore } from '@/stores/auth'

const replace = vi.hoisted(() => vi.fn())

vi.mock('vue-router', () => ({
  useRouter: () => ({ replace }),
}))

describe('OAuthCallbackView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    window.sessionStorage.clear()
    replace.mockReset()
    window.history.replaceState({}, '', '/oauth/callback')
  })

  it('removes the fragment before navigating after a successful callback', async () => {
    window.history.replaceState({}, '', '/oauth/callback#accessToken=token')
    window.sessionStorage.setItem('clickhub.oauthReturnPath', '/favorites')
    const auth = useAuthStore()
    vi.spyOn(auth, 'completeOAuthCallback').mockResolvedValue({ onboardingCompleted: true })

    mount(OAuthCallbackView, { global: { stubs: { RouterLink: true } } })
    await flushPromises()

    expect(window.location.hash).toBe('')
    expect(auth.completeOAuthCallback).toHaveBeenCalledWith('#accessToken=token')
    expect(replace).toHaveBeenCalledWith('/favorites')
  })

  it('shows a recoverable error when the callback cannot be completed', async () => {
    window.history.replaceState({}, '', '/oauth/callback#error=oauth2_login_failed')
    const auth = useAuthStore()
    vi.spyOn(auth, 'completeOAuthCallback').mockRejectedValue(new Error('Google 로그인 실패'))

    const wrapper = mount(OAuthCallbackView, { global: { stubs: { RouterLink: true } } })
    await flushPromises()

    expect(wrapper.get('[role="alert"]').text()).toBe('Google 로그인 실패')
    expect(replace).not.toHaveBeenCalled()
  })
})
