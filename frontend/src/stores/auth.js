import { defineStore } from 'pinia'

// Google OAuth 연동 확정 (2026-09-03) — 기획서 v1.1의 GitHub 로그인 필수 조항은 갱신 예정.
// 실제 OAuth 플로우는 백엔드 확정 후 연동, 현재는 화면 흐름 확인용 목업 상태만 관리한다.
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    onboarding: null,
  }),
  getters: {
    isLoggedIn: (state) => state.user !== null,
  },
  actions: {
    mockLoginWithGoogle() {
      this.user = {
        display_name: '김민준',
        avatar_initial: '김',
        new_project_notifications: true,
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
    },
  },
})
