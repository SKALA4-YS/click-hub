import { defineStore } from 'pinia'

import { getMe } from '@/api/auth'
import { updateOnboarding, updateProfile } from '@/api/users'
import {
  clearAccessToken,
  getAccessToken,
  saveOAuthReturnPath,
  setAccessToken,
} from '@/auth/tokenStorage'

function toUserViewModel(user) {
  if (!user) return null
  const displayName = user.displayName ?? ''
  return {
    ...user,
    display_name: displayName,
    avatar_initial: displayName.slice(0, 1) || '?',
    new_project_notifications: user.newProjectNotifications,
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    onboarding: null,
    initialized: false,
    loading: false,
    error: null,
  }),
  getters: {
    isLoggedIn: (state) => state.user !== null,
    isAdmin: (state) => state.user?.role === 'ADMIN',
  },
  actions: {
    async restoreSession() {
      if (!getAccessToken()) {
        this.clearSession()
        this.initialized = true
        return null
      }

      this.loading = true
      this.error = null
      try {
        const user = toUserViewModel(await getMe())
        this.user = user
        this.onboarding = user.onboardingCompleted ? { completed: true } : null
        return user
      } catch (error) {
        this.clearSession()
        this.error = error.message
        throw error
      } finally {
        this.loading = false
        this.initialized = true
      }
    },
    async completeOAuthCallback(fragment) {
      const params = new URLSearchParams(fragment.replace(/^#/, ''))
      const oauthError = params.get('error')
      const accessToken = params.get('accessToken')

      if (oauthError) {
        this.clearSession()
        throw new Error(`Google 로그인에 실패했습니다. (${oauthError})`)
      }
      if (!accessToken) {
        this.clearSession()
        throw new Error('로그인 응답에 Access Token이 없습니다.')
      }

      setAccessToken(accessToken)
      return this.restoreSession()
    },
    prepareOAuthLogin(returnPath) {
      saveOAuthReturnPath(returnPath || '/')
    },
    async completeOnboarding({ goals, categories, techStacks }) {
      const onboarding = await updateOnboarding({
        goals,
        categorySlugs: categories,
        technologySlugs: techStacks,
      })
      this.onboarding = onboarding
      if (this.user) this.user.onboardingCompleted = true
      return onboarding
    },
    async skipOnboarding() {
      const onboarding = await updateOnboarding({
        goals: [],
        categorySlugs: [],
        technologySlugs: [],
      })
      this.onboarding = { ...onboarding, skipped: true }
      if (this.user) this.user.onboardingCompleted = true
      return this.onboarding
    },
    async saveProfile({ display_name, theme, new_project_notifications }) {
      const user = await updateProfile({
        displayName: display_name,
        theme,
        newProjectNotifications: new_project_notifications,
      })
      this.user = toUserViewModel(user)
      return this.user
    },
    clearSession() {
      clearAccessToken()
      this.user = null
      this.onboarding = null
    },
    logout() {
      this.clearSession()
      this.error = null
    },
  },
})
