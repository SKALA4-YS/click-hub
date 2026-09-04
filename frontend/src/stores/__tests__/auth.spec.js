import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/stores/auth'

const getMe = vi.hoisted(() => vi.fn())
const loginAdmin = vi.hoisted(() => vi.fn())

vi.mock('@/api/auth', () => ({ getMe, loginAdmin }))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    window.sessionStorage.clear()
    getMe.mockReset()
    loginAdmin.mockReset()
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
    expect(auth.isAdmin).toBe(false)
  })

  it('exposes isAdmin only for users with the ADMIN role', () => {
    const auth = useAuthStore()

    auth.$patch({ user: { id: 'user-id', role: 'ADMIN' } })
    expect(auth.isAdmin).toBe(true)

    auth.$patch({ user: { id: 'user-id', role: 'USER' } })
    expect(auth.isAdmin).toBe(false)
  })

  it('stores the admin token and restores an ADMIN session', async () => {
    loginAdmin.mockResolvedValue({ accessToken: 'admin.jwt.token' })
    getMe.mockResolvedValue({
      id: 'admin-id',
      displayName: '관리자',
      role: 'ADMIN',
      authProvider: 'LOCAL',
      onboardingCompleted: false,
    })
    const auth = useAuthStore()

    await expect(
      auth.loginAsAdmin({ username: 'admin', password: 'admin' }),
    ).resolves.toMatchObject({
      id: 'admin-id',
      role: 'ADMIN',
    })

    expect(loginAdmin).toHaveBeenCalledWith({ username: 'admin', password: 'admin' })
    expect(window.sessionStorage.getItem('clickhub.accessToken')).toBe('admin.jwt.token')
    expect(auth.isAdmin).toBe(true)
  })

  it('does not retain a token when the restored user is not an admin', async () => {
    loginAdmin.mockResolvedValue({ accessToken: 'user.jwt.token' })
    getMe.mockResolvedValue({
      id: 'user-id',
      displayName: '일반 사용자',
      role: 'USER',
      onboardingCompleted: false,
    })
    const auth = useAuthStore()

    await expect(auth.loginAsAdmin({ username: 'admin', password: 'admin' })).rejects.toThrow(
      '관리자 권한이 없는 계정입니다.',
    )

    expect(window.sessionStorage.getItem('clickhub.accessToken')).toBeNull()
    expect(auth.isLoggedIn).toBe(false)
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
