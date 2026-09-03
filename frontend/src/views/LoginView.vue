<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function loginWithGoogle() {
  // TODO: 실제 Google OAuth 플로우로 교체 (백엔드 엔드포인트 확정 후)
  auth.mockLoginWithGoogle()
  router.push('/onboarding')
}
</script>

<template>
  <section
    aria-labelledby="login-heading"
    class="mx-auto flex min-h-[calc(100vh-13rem)] w-full max-w-[440px] items-center py-8 sm:py-12"
  >
    <div
      class="w-full rounded-3xl border border-divider/20 bg-surface-light-1 px-6 py-10 text-center shadow-[0_18px_50px_rgba(15,14,71,0.08)] sm:px-10 dark:border-blue-500/20 dark:bg-surface-dark-1 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]"
    >
      <p
        class="font-headline text-2xl font-extrabold tracking-tight text-primary-600 dark:text-heading-dark"
      >
        Click HUB
      </p>
      <h1
        id="login-heading"
        class="mt-7 font-headline text-2xl font-bold tracking-tight text-heading-light dark:text-heading-dark"
      >
        Click HUB에 오신 것을 환영합니다
      </h1>
      <p class="mx-auto mt-3 max-w-[300px] text-sm leading-6 text-body-light dark:text-body-dark">
        배포한 사이드 프로젝트를 알리고, 새로운 서비스를 발견해보세요.
      </p>

      <div class="my-8 h-px bg-divider/20 dark:bg-blue-500/20" aria-hidden="true" />

      <button
        type="button"
        aria-label="Google 계정으로 시작하기"
        class="flex min-h-12 w-full items-center justify-center rounded-xl border border-divider/30 bg-surface-light-1 px-4 text-sm font-semibold text-heading-light transition-colors hover:bg-primary-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 dark:border-blue-500/25 dark:bg-surface-dark-2 dark:text-heading-dark dark:hover:bg-primary-700"
        @click="loginWithGoogle"
      >
        Google로 시작하기
      </button>
    </div>
  </section>
</template>
