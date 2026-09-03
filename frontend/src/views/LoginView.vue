<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

function loginWithGoogle() {
  // TODO: 실제 Google OAuth 플로우로 교체 (백엔드 엔드포인트 확정 후)
  auth.mockLoginWithGoogle()
  router.push('/')
}
</script>

<template>
  <div class="mx-auto flex max-w-sm flex-col items-center gap-6 rounded-2xl border border-neutral-200 p-8 text-center dark:border-neutral-800">
    <div>
      <p class="text-xl font-extrabold text-primary-600">Click HUB</p>
      <h1 class="mt-3 text-xl font-bold">Click HUB에 오신 것을 환영합니다</h1>
      <p class="mt-2 text-sm text-neutral-500">
        배포한 사이드 프로젝트를 알리고, 새로운 서비스를 발견해보세요.
      </p>
    </div>

    <button
      type="button"
      class="flex w-full items-center justify-center gap-2 rounded-full border border-neutral-300 bg-white px-4 py-2.5 text-sm font-semibold text-neutral-700 hover:bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-200 dark:hover:bg-neutral-800"
      @click="loginWithGoogle"
    >
      Google로 시작하기
    </button>
  </div>
</template>
