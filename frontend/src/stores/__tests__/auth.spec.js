import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'

const getMe = vi.hoisted(() => vi.fn())

vi.mock('@/api/auth', () => ({ getMe }))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    window.sessionStorage.clear()
    getMe.mockReset()
  })

  it('stays signed out without a stored access token', async () => {
    const auth = useAuthStore()

    await expect(auth.restoreSession()).resolves.toBeNull()

    expect(auth.initialized).toBe(true)
    expect(auth.isLoggedIn).toBe(false)
    expect(getMe).not.toHaveBeenCalled()
  })

  it('collects the fragment token and restores the real backend user', async () => {
    getMe.mockResolvedValue({
      id: 'user-id',
      displayName: '김민준',
      newProjectNotifications: true,
      onboardingCompleted: true,
    })
    const auth = useAuthStore()

    await expect(
      auth.completeOAuthCallback('#accessToken=signed.jwt.token'),
    ).resolves.toMatchObject({
      id: 'user-id',
      display_name: '김민준',
      avatar_initial: '김',
    })

    expect(window.sessionStorage.getItem('clickhub.accessToken')).toBe('signed.jwt.token')
    expect(auth.isLoggedIn).toBe(true)
    expect(auth.onboarding).toEqual({ completed: true })
  })

  it('rejects OAuth errors without retaining a token', async () => {
    window.sessionStorage.setItem('clickhub.accessToken', 'old-token')
    const auth = useAuthStore()

    await expect(auth.completeOAuthCallback('#error=oauth2_login_failed')).rejects.toThrow(
      'Google 로그인에 실패했습니다.',
    )

    expect(window.sessionStorage.getItem('clickhub.accessToken')).toBeNull()
    expect(auth.isLoggedIn).toBe(false)
  })

  it('clears both memory and session storage on logout', () => {
    window.sessionStorage.setItem('clickhub.accessToken', 'token')
    const auth = useAuthStore()
    auth.$patch({ user: { id: 'user-id' }, onboarding: { completed: true } })

    auth.logout()

    expect(auth.user).toBeNull()
    expect(auth.onboarding).toBeNull()
    expect(window.sessionStorage.getItem('clickhub.accessToken')).toBeNull()
  })
})
