<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const credentials = reactive({ username: '', password: '' })
const errorMessage = ref('')

function safeDestination(value) {
  return typeof value === 'string' && value.startsWith('/admin/') && !value.startsWith('//')
    ? value
    : '/admin/projects'
}

async function submit() {
  if (auth.loading) return
  errorMessage.value = ''
  try {
    await auth.loginAsAdmin(credentials)
    await router.replace(safeDestination(route.query.redirect))
  } catch (error) {
    errorMessage.value = error.message
  }
}

watch(
  () => auth.isAdmin,
  (isAdmin) => {
    if (isAdmin) router.replace('/admin/projects')
  },
  { immediate: true },
)
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-slate-950 px-4 py-10">
    <section
      class="w-full max-w-md rounded-2xl border border-slate-700 bg-slate-900 p-7 text-white shadow-2xl"
      aria-labelledby="admin-login-heading"
    >
      <header class="mb-7">
        <p class="text-xs font-bold tracking-[0.2em] text-primary-400">CLICK HUB ADMIN</p>
        <h1 id="admin-login-heading" class="mt-3 font-headline text-2xl font-extrabold">
          관리자 로그인
        </h1>
        <p class="mt-2 text-sm leading-6 text-slate-400">
          승인 업무 권한이 있는 관리자 계정만 사용할 수 있습니다.
        </p>
      </header>

      <form class="space-y-5" @submit.prevent="submit">
        <label class="block">
          <span class="mb-2 block text-sm font-semibold text-slate-200">관리자 ID</span>
          <input
            v-model="credentials.username"
            name="username"
            type="text"
            autocomplete="username"
            required
            maxlength="100"
            class="w-full rounded-lg border border-slate-600 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-500/30"
          />
        </label>

        <label class="block">
          <span class="mb-2 block text-sm font-semibold text-slate-200">비밀번호</span>
          <input
            v-model="credentials.password"
            name="password"
            type="password"
            autocomplete="current-password"
            required
            maxlength="200"
            class="w-full rounded-lg border border-slate-600 bg-slate-950 px-4 py-3 text-white outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-500/30"
          />
        </label>

        <p
          v-if="errorMessage"
          role="alert"
          class="rounded-lg bg-red-950/60 px-4 py-3 text-sm text-red-200"
        >
          {{ errorMessage }}
        </p>

        <button
          type="submit"
          :disabled="auth.loading"
          class="w-full rounded-lg bg-primary-600 px-5 py-3 font-bold text-white transition hover:bg-primary-500 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {{ auth.loading ? '확인 중...' : '관리자 로그인' }}
        </button>
      </form>

      <a href="/" class="mt-6 block text-center text-sm text-slate-400 hover:text-white">
        서비스 홈으로 돌아가기
      </a>
    </section>
  </main>
</template>
