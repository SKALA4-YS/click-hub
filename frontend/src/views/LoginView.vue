<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const keepLoggedIn = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')

function completeStaticLogin() {
  auth.mockLoginWithGoogle()
  router.push('/onboarding')
}

function loginWithGoogle() {
  errorMessage.value = ''
  completeStaticLogin()
}

function loginWithEmail() {
  if (!email.value.trim()) {
    errorMessage.value = '이메일 주소를 입력해주세요.'
    return
  }

  if (!password.value) {
    errorMessage.value = '비밀번호를 입력해주세요.'
    return
  }

  errorMessage.value = ''
  completeStaticLogin()
}
</script>

<template>
  <section
    class="mx-auto w-full max-w-[1280px] px-4 py-7 sm:px-8 sm:py-10"
    aria-labelledby="login-heading"
  >
    <div
      class="flex items-center justify-between text-xs font-medium text-body-light dark:text-body-dark"
    >
      <a href="/" class="inline-flex items-center gap-2 transition-colors hover:text-primary-600">
        <span aria-hidden="true">←</span>
        홈으로 돌아가기
      </a>
      <p class="hidden items-center gap-2 sm:inline-flex">
        <span class="h-2 w-2 rounded-full bg-success" aria-hidden="true" />
        인디 서비스 실시간 탐색 중
      </p>
    </div>

    <div class="mx-auto mt-6 grid max-w-[680px] gap-4">
      <article
        class="rounded-2xl border border-divider/20 bg-surface-light-1 px-5 py-8 shadow-[0_12px_30px_rgba(15,14,71,0.06)] sm:px-8 sm:py-10 dark:border-blue-500/20 dark:bg-surface-dark-1"
      >
        <div class="mx-auto max-w-[440px]">
          <div class="text-center">
            <div
              class="inline-flex items-center gap-2.5 font-headline text-[25px] font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
            >
              <span
                class="grid h-8 w-8 grid-cols-2 gap-1 rounded-lg bg-primary-600 p-1.5"
                aria-hidden="true"
              >
                <span class="rounded-sm bg-white" />
                <span class="rounded-sm bg-white/80" />
                <span class="rounded-sm bg-white/80" />
                <span class="rounded-sm bg-white" />
              </span>
              Click-Hub
            </div>
            <h1
              id="login-heading"
              class="mt-6 font-headline text-2xl font-bold tracking-tight text-heading-light dark:text-heading-dark"
            >
              다시 오신 것을 환영해요
            </h1>
            <p
              class="mx-auto mt-2 max-w-[360px] text-sm leading-6 text-body-light dark:text-body-dark"
            >
              배포된 사이드 프로젝트의 성과를 확인하고 전 세계 메이커들과 커뮤니티 피드백을
              나눠보세요.
            </p>
          </div>

          <button
            name="google-login"
            type="button"
            class="mt-7 flex h-11 w-full items-center justify-center gap-3 rounded-lg border border-divider/30 bg-white px-4 text-sm font-semibold text-heading-light transition-colors hover:bg-primary-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 dark:border-blue-500/25 dark:bg-surface-dark-2 dark:text-heading-dark dark:hover:bg-primary-700"
            @click="loginWithGoogle"
          >
            <span class="font-headline text-base font-extrabold text-primary-600" aria-hidden="true"
              >G</span
            >
            Google로 3초 만에 로그인
            <span
              class="rounded bg-primary-50 px-1.5 py-0.5 text-[10px] font-bold text-primary-700 dark:bg-primary-950 dark:text-primary-200"
              >추천</span
            >
          </button>

          <div
            class="my-6 flex items-center gap-3 text-xs text-body-light dark:text-body-dark"
            aria-hidden="true"
          >
            <span class="h-px flex-1 bg-divider/20 dark:bg-blue-500/20" />
            또는 이메일로 로그인
            <span class="h-px flex-1 bg-divider/20 dark:bg-blue-500/20" />
          </div>

          <form novalidate @submit.prevent="loginWithEmail">
            <p
              v-if="errorMessage"
              role="alert"
              class="mb-4 rounded-md bg-danger/10 px-3 py-2 text-xs font-medium text-danger"
            >
              {{ errorMessage }}
            </p>

            <label
              for="login-email"
              class="block text-sm font-semibold text-heading-light dark:text-heading-dark"
              >이메일 주소 <span class="text-primary-600">*</span></label
            >
            <input
              id="login-email"
              v-model="email"
              name="email"
              type="email"
              autocomplete="email"
              placeholder="maker@domain.com"
              class="mt-2 h-11 w-full rounded-lg border border-divider/30 bg-base-light px-3 text-sm text-heading-light outline-none placeholder:text-body-light/70 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:border-blue-500/25 dark:bg-base-dark dark:text-heading-dark"
            />

            <div class="mt-4 flex items-center justify-between gap-3">
              <label
                for="login-password"
                class="text-sm font-semibold text-heading-light dark:text-heading-dark"
                >비밀번호 <span class="text-primary-600">*</span></label
              >
              <button
                type="button"
                disabled
                class="text-xs font-medium text-body-light disabled:cursor-not-allowed dark:text-body-dark"
              >
                비밀번호 찾기
              </button>
            </div>
            <div class="relative mt-2">
              <input
                id="login-password"
                v-model="password"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="비밀번호를 입력하세요"
                class="h-11 w-full rounded-lg border border-divider/30 bg-base-light px-3 pr-12 text-sm text-heading-light outline-none placeholder:text-body-light/70 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:border-blue-500/25 dark:bg-base-dark dark:text-heading-dark"
              />
              <button
                name="toggle-password"
                type="button"
                class="absolute inset-y-0 right-0 px-3 text-xs font-semibold text-body-light hover:text-primary-600 dark:text-body-dark"
                :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 표시'"
                @click="showPassword = !showPassword"
              >
                {{ showPassword ? '숨기기' : '보기' }}
              </button>
            </div>

            <label
              class="mt-4 flex cursor-pointer items-start gap-2 text-sm text-heading-light dark:text-heading-dark"
            >
              <input
                v-model="keepLoggedIn"
                name="keep-logged-in"
                type="checkbox"
                class="mt-0.5 h-4 w-4 accent-primary-600"
              />
              <span>
                로그인 상태 유지
                <small class="mt-1 block text-xs leading-5 text-body-light dark:text-body-dark"
                  >개인 기기가 아닌 경우 선택하지 마세요.</small
                >
              </span>
            </label>

            <button
              type="submit"
              class="mt-6 flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary-600 px-4 text-sm font-bold text-white transition-colors hover:bg-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            >
              로그인하기 <span aria-hidden="true">→</span>
            </button>
          </form>

          <p class="mt-6 text-center text-sm text-body-light dark:text-body-dark">
            아직 Click-Hub 계정이 없으신가요?
            <a href="/signup" class="font-semibold text-primary-600 hover:underline">회원가입</a>
          </p>
          <p class="mt-4 text-center text-[11px] leading-5 text-body-light dark:text-body-dark">
            256-bit SSL 암호화로 로그인 정보를 안전하게 보호합니다.
          </p>
        </div>
      </article>

      <aside
        class="flex items-center justify-center gap-4 rounded-xl border border-divider/20 bg-surface-light-1 px-5 py-4 text-sm text-heading-light shadow-[0_8px_20px_rgba(15,14,71,0.04)] dark:border-blue-500/20 dark:bg-surface-dark-1 dark:text-heading-dark"
        aria-label="Click-Hub 메이커 활동 현황"
      >
        <div class="flex -space-x-2" aria-hidden="true">
          <span
            class="h-7 w-7 rounded-full border-2 border-white bg-primary-100 dark:border-surface-dark-1"
          />
          <span
            class="h-7 w-7 rounded-full border-2 border-white bg-blue-200 dark:border-surface-dark-1"
          />
          <span
            class="h-7 w-7 rounded-full border-2 border-white bg-primary-200 dark:border-surface-dark-1"
          />
        </div>
        <p><strong class="font-bold">1,400+ 명의 인디 메이커</strong> 활동 중</p>
      </aside>
    </div>
  </section>
</template>
