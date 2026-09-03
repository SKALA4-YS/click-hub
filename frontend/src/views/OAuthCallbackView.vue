<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const errorMessage = ref('')

onMounted(async () => {
  const { accessToken, refreshToken, error } = route.query

  if (error) {
    errorMessage.value = `로그인에 실패했어요. (${error})`
    setTimeout(() => router.replace('/login'), 1500)
    return
  }

  try {
    await auth.loginWithTokens({ accessToken, refreshToken })
    router.replace('/')
  } catch {
    errorMessage.value = '로그인 처리 중 문제가 발생했어요.'
    setTimeout(() => router.replace('/login'), 1500)
  }
})
</script>

<template>
  <div class="mx-auto flex max-w-md flex-col items-center gap-3 py-24 text-center">
    <p v-if="errorMessage" class="text-sm text-red-600">{{ errorMessage }}</p>
    <p v-else class="text-sm text-neutral-500">로그인 처리 중이에요...</p>
  </div>
</template>
