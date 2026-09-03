<script setup>
import { computed, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({
  name: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  terms: false,
  privacy: false,
  marketing: false,
})
const errors = reactive({})
const requiredAgreementsAccepted = computed(() => form.terms && form.privacy)

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.name.trim()) errors.name = '이름을 입력해주세요.'
  if (!form.email.trim()) errors.email = '이메일을 입력해주세요.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
    errors.email = '올바른 이메일 주소를 입력해주세요.'
  if (!form.password) errors.password = '비밀번호를 입력해주세요.'
  else if (form.password.length < 8) errors.password = '비밀번호는 8자 이상 입력해주세요.'
  if (form.password && form.passwordConfirmation !== form.password)
    errors.passwordConfirmation = '비밀번호가 일치하지 않습니다.'
  return Object.keys(errors).length === 0
}

async function submit() {
  if (!validate()) return
  await router.push('/onboarding')
}
</script>

<template>
  <section class="mx-auto w-full max-w-[440px] py-8 sm:py-12" aria-labelledby="signup-heading">
    <div
      class="rounded-2xl border border-divider/20 bg-surface-light-1 px-6 py-8 shadow-sm sm:px-9 dark:border-blue-500/15 dark:bg-surface-dark-1"
    >
      <div class="mb-8 text-center">
        <RouterLink
          to="/"
          class="font-headline text-2xl font-extrabold tracking-tight text-primary-600 dark:text-heading-dark"
          aria-label="Click HUB 홈으로 이동"
          >Click HUB</RouterLink
        >
        <h1 id="signup-heading" class="mt-6 font-headline text-2xl font-bold">회원가입</h1>
        <p class="mt-2 text-sm text-body-light dark:text-body-dark">
          Click HUB에서 당신의 프로젝트를 소개해 보세요.
        </p>
      </div>

      <form class="space-y-5" novalidate @submit.prevent="submit">
        <div>
          <label for="signup-name" class="mb-2 block text-sm font-semibold">이름</label>
          <input
            id="signup-name"
            v-model="form.name"
            name="name"
            type="text"
            autocomplete="name"
            placeholder="이름을 입력해주세요"
            :aria-invalid="Boolean(errors.name)"
            :aria-describedby="errors.name ? 'signup-name-error' : undefined"
            class="w-full rounded-lg border bg-transparent px-4 py-3 text-sm outline-none transition placeholder:text-body-light/60 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:focus:ring-primary-950"
            :class="errors.name ? 'border-danger' : 'border-divider/35 dark:border-divider/50'"
          />
          <p
            v-if="errors.name"
            id="signup-name-error"
            data-error="name"
            class="mt-1.5 text-xs text-danger"
          >
            {{ errors.name }}
          </p>
        </div>
        <div>
          <label for="signup-email" class="mb-2 block text-sm font-semibold">이메일</label>
          <input
            id="signup-email"
            v-model="form.email"
            name="email"
            type="email"
            autocomplete="email"
            placeholder="example@clickhub.dev"
            :aria-invalid="Boolean(errors.email)"
            :aria-describedby="errors.email ? 'signup-email-error' : undefined"
            class="w-full rounded-lg border bg-transparent px-4 py-3 text-sm outline-none transition placeholder:text-body-light/60 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:focus:ring-primary-950"
            :class="errors.email ? 'border-danger' : 'border-divider/35 dark:border-divider/50'"
          />
          <p
            v-if="errors.email"
            id="signup-email-error"
            data-error="email"
            class="mt-1.5 text-xs text-danger"
          >
            {{ errors.email }}
          </p>
        </div>
        <div>
          <label for="signup-password" class="mb-2 block text-sm font-semibold">비밀번호</label>
          <input
            id="signup-password"
            v-model="form.password"
            name="password"
            type="password"
            autocomplete="new-password"
            placeholder="8자 이상 입력해주세요"
            :aria-invalid="Boolean(errors.password)"
            :aria-describedby="errors.password ? 'signup-password-error' : undefined"
            class="w-full rounded-lg border bg-transparent px-4 py-3 text-sm outline-none transition placeholder:text-body-light/60 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:focus:ring-primary-950"
            :class="errors.password ? 'border-danger' : 'border-divider/35 dark:border-divider/50'"
          />
          <p
            v-if="errors.password"
            id="signup-password-error"
            data-error="password"
            class="mt-1.5 text-xs text-danger"
          >
            {{ errors.password }}
          </p>
        </div>
        <div>
          <label for="signup-password-confirmation" class="mb-2 block text-sm font-semibold"
            >비밀번호 확인</label
          >
          <input
            id="signup-password-confirmation"
            v-model="form.passwordConfirmation"
            name="passwordConfirmation"
            type="password"
            autocomplete="new-password"
            placeholder="비밀번호를 다시 입력해주세요"
            :aria-invalid="Boolean(errors.passwordConfirmation)"
            :aria-describedby="
              errors.passwordConfirmation ? 'signup-password-confirmation-error' : undefined
            "
            class="w-full rounded-lg border bg-transparent px-4 py-3 text-sm outline-none transition placeholder:text-body-light/60 focus:border-primary-600 focus:ring-2 focus:ring-primary-100 dark:focus:ring-primary-950"
            :class="
              errors.passwordConfirmation
                ? 'border-danger'
                : 'border-divider/35 dark:border-divider/50'
            "
          />
          <p
            v-if="errors.passwordConfirmation"
            id="signup-password-confirmation-error"
            class="mt-1.5 text-xs text-danger"
          >
            {{ errors.passwordConfirmation }}
          </p>
        </div>
        <fieldset class="space-y-3 border-t border-divider/20 pt-5 dark:border-divider/35">
          <legend class="sr-only">약관 동의</legend>
          <label class="flex cursor-pointer items-center gap-3 text-sm font-medium"
            ><input
              v-model="form.terms"
              data-testid="terms-agreement"
              type="checkbox"
              class="h-4 w-4 accent-primary-600"
            /><span
              ><strong class="text-primary-600">[필수]</strong> 이용약관에 동의합니다.</span
            ></label
          >
          <label class="flex cursor-pointer items-center gap-3 text-sm font-medium"
            ><input
              v-model="form.privacy"
              data-testid="privacy-agreement"
              type="checkbox"
              class="h-4 w-4 accent-primary-600"
            /><span
              ><strong class="text-primary-600">[필수]</strong> 개인정보 수집 및 이용에
              동의합니다.</span
            ></label
          >
          <label
            class="flex cursor-pointer items-center gap-3 text-sm text-body-light dark:text-body-dark"
            ><input
              v-model="form.marketing"
              type="checkbox"
              class="h-4 w-4 accent-primary-600"
            /><span>[선택] 새로운 소식과 혜택을 받아볼게요.</span></label
          >
        </fieldset>
        <button
          data-testid="signup-submit"
          type="submit"
          @click.prevent="submit"
          :disabled="!requiredAgreementsAccepted"
          class="w-full rounded-lg bg-primary-600 py-3.5 text-sm font-bold text-white transition hover:bg-primary-700 disabled:cursor-not-allowed disabled:bg-primary-300"
        >
          가입하기
        </button>
      </form>
      <p class="mt-6 text-center text-sm text-body-light dark:text-body-dark">
        이미 계정이 있으신가요?
        <RouterLink to="/login" class="font-semibold text-primary-600 hover:underline"
          >로그인</RouterLink
        >
      </p>
    </div>
  </section>
</template>
