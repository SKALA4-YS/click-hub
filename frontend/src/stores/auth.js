import { defineStore } from 'pinia'
import { getApiBaseUrl } from '@/services/api'

// Google OAuth 연동 확정 (2026-09-03) — 기획서 v1.1의 GitHub 로그인 필수 조항은 갱신 예정.
const ACCESS_TOKEN_KEY = 'clickhub.accessToken'
const REFRESH_TOKEN_KEY = 'clickhub.refreshToken'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    onboarding: null,
    accessToken: localStorage.getItem(ACCESS_TOKEN_KEY),
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY),
  }),
  getters: {
    isLoggedIn: (state) => state.user !== null,
  },
  actions: {
    // GET /v1/users/me를 호출해 실제 프로필을 받아오되, 기존 화면(UserMenu 등)이 이미 기대하는
    // 목업 필드 이름(display_name/avatar_initial)을 그대로 맞춰서 다른 컴포넌트는 손대지 않는다.
    async fetchMe(accessToken) {
      const response = await fetch(`${getApiBaseUrl()}/v1/users/me`, {
        headers: { Authorization: `Bearer ${accessToken}` },
      })
      if (!response.ok) {
        throw new Error(`GET /v1/users/me failed with HTTP ${response.status}`)
      }
      const { data } = await response.json()

      this.user = {
        display_name: data.displayName,
        avatar_initial: data.displayName?.charAt(0) ?? '?',
        new_project_notifications: true,
        avatarUrl: data.avatarUrl,
        role: data.role,
        authProvider: data.authProvider,
      }
    },
    // /oauth/callback에서 백엔드가 발급한 토큰을 받아 로그인 상태를 완성한다.
    async loginWithTokens({ accessToken, refreshToken }) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
      localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
      await this.fetchMe(accessToken)
    },
    // 새로고침 등으로 앱이 다시 뜰 때, localStorage에 남아있는 토큰으로 로그인 상태를 복원한다.
    // 토큰이 만료/무효면 조용히 로그아웃 상태로 둔다(에러를 화면에 노출하지 않음).
    async restoreSession() {
      if (!this.accessToken || this.user) return
      try {
        await this.fetchMe(this.accessToken)
      } catch {
        this.logout()
      }
    },
    completeOnboarding({ goals, categories, techStacks }) {
      this.onboarding = { goals, categories, techStacks, completed_at: new Date().toISOString() }
    },
    skipOnboarding() {
      this.onboarding = { skipped: true, completed_at: new Date().toISOString() }
    },
    updateProfile({ display_name, new_project_notifications }) {
      if (!this.user) return
      if (display_name !== undefined) this.user.display_name = display_name
      if (new_project_notifications !== undefined) {
        this.user.new_project_notifications = new_project_notifications
      }
    },
    logout() {
      this.user = null
      this.onboarding = null
      this.accessToken = null
      this.refreshToken = null
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)
    },
  },
})
