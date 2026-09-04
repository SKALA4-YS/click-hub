<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { consumeOAuthReturnPath } from '@/auth/tokenStorage'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const errorMessage = ref('')

onMounted(async () => {
  const fragment = window.location.hash
  window.history.replaceState(
    {},
    document.title,
    `${window.location.pathname}${window.location.search}`,
  )

  try {
    const user = await auth.completeOAuthCallback(fragment)
    const destination = user.onboardingCompleted ? consumeOAuthReturnPath('/') : '/onboarding'
    await router.replace(destination)
  } catch (error) {
    errorMessage.value = error.message
  }
})
</script>

<template>
  <main class="grid min-h-screen place-items-center px-4" aria-live="polite">
    <section
      class="w-full max-w-md rounded-2xl border border-divider/20 bg-white p-8 text-center dark:border-divider/30 dark:bg-surface-dark-1"
    >
      <template v-if="errorMessage">
        <h1 class="text-xl font-bold text-heading-light dark:text-heading-dark">
          로그인을 완료하지 못했습니다
        </h1>
        <p role="alert" class="mt-3 text-sm text-danger">{{ errorMessage }}</p>
        <RouterLink class="mt-6 inline-block font-semibold text-primary-600" to="/login">
          로그인 화면으로 돌아가기
        </RouterLink>
      </template>
      <template v-else>
        <h1 class="text-xl font-bold text-heading-light dark:text-heading-dark">
          Google 로그인을 확인하고 있습니다
        </h1>
        <p class="mt-3 text-sm text-body-light dark:text-body-dark">잠시만 기다려주세요.</p>
      </template>
    </section>
  </main>
</template>
