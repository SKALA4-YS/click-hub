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

    <div class="mx-auto mt-4 grid max-w-[450px] gap-3">
      <article
        class="rounded-2xl border border-divider/20 bg-surface-light-1 px-5 py-7 shadow-[0_12px_30px_rgba(15,14,71,0.06)] sm:px-8 dark:border-blue-500/20 dark:bg-surface-dark-1"
      >
        <div class="mx-auto max-w-[440px]">
          <div class="text-center">
            <p
              class="font-headline text-[25px] font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
            >
              Click-Hub
            </p>
            <h1
              id="login-heading"
              class="mt-6 font-headline text-2xl font-bold tracking-tight text-heading-light dark:text-heading-dark"
            >
              다시 오신 것을 환영해요
            </h1>
            <p
              class="mx-auto mt-2 max-w-[360px] text-sm leading-6 text-body-light dark:text-body-dark"
            >
              배포된 사이트 프로젝트의 성과를 확인하고 전 세계 메이커들과 피드백을 나누세요.
            </p>
          </div>

          <button
            name="google-login"
            type="button"
            class="mt-7 flex h-11 w-full items-center justify-center gap-3 rounded-lg border border-divider/30 bg-white px-4 text-sm font-semibold text-heading-light transition-colors hover:bg-primary-50 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 dark:border-blue-500/25 dark:bg-surface-dark-2 dark:text-heading-dark dark:hover:bg-primary-700"
            @click="loginWithGoogle"
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
              <span
                aria-disabled="true"
                title="비밀번호 재설정 기능은 아직 준비 중입니다."
                class="text-xs font-medium text-body-light dark:text-body-dark"
                >비밀번호를 잊으셨나요?</span
              >
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
                :aria-label="showPassword ? '비밀번호 숨기기' : '비밀번호 보기'"
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
                <small class="ml-2 text-xs text-body-light dark:text-body-dark"
                  >안전한 PC에서만 권장</small
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
            <a href="/signup" class="font-semibold text-primary-600 hover:underline"
              >회원가입하기</a
            >
          </p>
          <p class="mt-4 text-center text-[11px] leading-5 text-body-light dark:text-body-dark">
            <span aria-hidden="true">[보안]</span>
            256-bit SSL 엔드투엔드 암호화 보안 적용
          </p>
        </div>
      </article>

      <aside
        class="flex items-center justify-center gap-4 rounded-xl border border-divider/20 bg-surface-light-1 px-5 py-4 text-sm text-heading-light shadow-[0_8px_20px_rgba(15,14,71,0.04)] dark:border-blue-500/20 dark:bg-surface-dark-1 dark:text-heading-dark"
        aria-label="Click-Hub 메이커 활동 현황"
      >
        <div class="flex -space-x-2" aria-label="메이커 프로필 자리표시자">
          <span
            class="grid h-7 w-7 place-items-center rounded-full border-2 border-white bg-primary-100 text-[10px] text-primary-700 dark:border-surface-dark-1"
            >·</span
          >
          <span
            class="grid h-7 w-7 place-items-center rounded-full border-2 border-white bg-blue-200 text-[10px] text-primary-700 dark:border-surface-dark-1"
            >·</span
          >
          <span
            class="grid h-7 w-7 place-items-center rounded-full border-2 border-white bg-primary-200 text-[10px] text-primary-700 dark:border-surface-dark-1"
            >·</span
          >
        </div>
        <p class="whitespace-nowrap">
          <strong class="font-bold">1,400+명의 인디 메이커</strong> 활동 중
        </p>
        <span
          class="ml-auto inline-flex items-center gap-1 rounded-md bg-base-light px-2 py-1 text-xs text-body-light dark:bg-base-dark dark:text-body-dark"
          ><span class="h-1.5 w-1.5 rounded-full bg-primary-600" aria-hidden="true" />주간 핫
          프로젝트 #1 <strong class="font-semibold">984</strong></span
        >
      </aside>
    </div>
  </section>
</template>
