import { defineStore } from 'pinia'

// Google OAuth 연동 확정 (2026-09-03) — 기획서 v1.1의 GitHub 로그인 필수 조항은 갱신 예정.
// 실제 OAuth 플로우는 백엔드 확정 후 연동, 현재는 화면 흐름 확인용 목업 상태만 관리한다.
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
  }),
  getters: {
    isLoggedIn: (state) => state.user !== null,
  },
  actions: {
    mockLoginWithGoogle() {
      this.user = { display_name: '김민준', avatar_initial: '김' }
    },
    logout() {
      this.user = null
    },
  },
})
