<script setup>
import { RouterLink } from 'vue-router'
import { getApiBaseUrl } from '@/services/api'
import AuthLogo from '@/components/auth/AuthLogo.vue'
import SocialProofBar from '@/components/auth/SocialProofBar.vue'

// 로그인/가입이 같은 Google OAuth "있으면 로그인, 없으면 가입" upsert 플로우라 시작점은
// 동일하다 — 백엔드 콜백이 신규 가입 여부를 알려주지 않아 온보딩으로의 분기는 아직 못 한다.
function signupWithGoogle() {
  window.location.href = `${getApiBaseUrl()}/v1/auth/google`
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
        <h1 class="font-headline text-xl font-bold">Click-Hub에 오신 것을 환영합니다</h1>
        <p class="mt-2 text-sm text-neutral-500">
          배포한 사이드 프로젝트를 세상에 알리고 전 세계 메이커들과 함께 피드백을 나누며 성장하세요.
        </p>
      </div>

      <button
        type="button"
        class="flex w-full items-center justify-center gap-2 rounded-full border border-neutral-300 bg-white px-4 py-3 text-sm font-semibold text-neutral-700 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-200 dark:hover:bg-neutral-800"
        @click="signupWithGoogle"
      >
        <span aria-hidden="true">G</span>
        Google로 3초 만에 시작하기
        <span
          class="ml-1 rounded-full bg-primary-100 px-2 py-0.5 text-[11px] font-semibold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
        >
          추천
        </span>
      </button>

      <p class="text-center text-xs text-neutral-400">
        Google 계정으로 가입하면 별도 절차 없이 바로 관심사 설정으로 이동해요.
      </p>

      <p class="text-center text-sm text-neutral-500">
        이미 계정이 있으신가요?
        <RouterLink to="/login" class="font-medium text-primary-600 hover:underline"
          >로그인하기</RouterLink
        >
      </p>
    </div>

    <SocialProofBar />
  </div>
</template>
