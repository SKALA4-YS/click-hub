<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AuthLogo from '@/components/auth/AuthLogo.vue'
import SocialProofBar from '@/components/auth/SocialProofBar.vue'

const router = useRouter()
const auth = useAuthStore()

function loginWithGoogle() {
  // TODO: 실제 Google OAuth 플로우로 교체 (백엔드 엔드포인트 확정 후)
  auth.mockLoginWithGoogle()
  router.push('/')
}
</script>

<template>
  <div class="mx-auto flex max-w-md flex-col gap-4">
    <RouterLink
      to="/"
      class="text-sm text-neutral-500 hover:text-neutral-700 dark:hover:text-neutral-300"
    >
      ← 홈으로 돌아가기
    </RouterLink>

    <div
      class="flex flex-col gap-6 rounded-2xl border border-neutral-200 p-8 dark:border-neutral-800"
    >
      <AuthLogo />

      <div class="text-center">
        <h1 class="font-headline text-xl font-bold">다시 오신 것을 환영해요</h1>
        <p class="mt-2 text-sm text-neutral-500">
          배포된 사이드 프로젝트의 성과를 확인하고 전 세계 메이커들과 피드백을 나누세요.
        </p>
      </div>

      <button
        type="button"
        class="flex w-full items-center justify-center gap-2 rounded-full border border-neutral-300 bg-white px-4 py-3 text-sm font-semibold text-neutral-700 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-200 dark:hover:bg-neutral-800"
        @click="loginWithGoogle"
      >
        <span aria-hidden="true">G</span>
        Google로 3초 만에 로그인
        <span
          class="ml-1 rounded-full bg-primary-100 px-2 py-0.5 text-[11px] font-semibold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
        >
          추천
        </span>
      </button>

      <p class="text-center text-xs text-neutral-400">
        현재는 Google 계정으로만 로그인할 수 있어요.
      </p>

      <p class="text-center text-sm text-neutral-500">
        아직 Click-Hub 계정이 없으신가요?
        <RouterLink to="/signup" class="font-medium text-primary-600 hover:underline"
          >회원가입하기</RouterLink
        >
      </p>
    </div>

    <SocialProofBar />
  </div>
</template>
