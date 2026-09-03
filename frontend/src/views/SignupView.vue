<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({
  profile: '',
  email: '',
  password: '',
  terms: false,
  privacy: false,
  newsletter: false,
})
const errors = reactive({})
const passwordVisible = ref(false)
const selectedChips = ref([])
const chips = [
  'Next.js',
  'Vue.js',
  'Spring Boot',
  'AI / LLM',
  'Flutter',
  '1인 빌더',
  '디자이너',
  '풀스택',
]

const allAgreements = computed({
  get: () => form.terms && form.privacy && form.newsletter,
  set: (checked) => {
    form.terms = checked
    form.privacy = checked
    form.newsletter = checked
  },
})
const requiredAgreementsAccepted = computed(() => form.terms && form.privacy)
const passwordLevel = computed(() => {
  if (!form.password) return 0
  let level = 0
  if (form.password.length >= 8) level += 1
  if (/[a-zA-Z]/.test(form.password) && /\d/.test(form.password)) level += 1
  if (/[^a-zA-Z\d]/.test(form.password)) level += 1
  if (form.password.length >= 10) level += 1
  return level
})

function toggleChip(chip) {
  selectedChips.value = selectedChips.value.includes(chip)
    ? selectedChips.value.filter((value) => value !== chip)
    : [...selectedChips.value, chip]
}

function validate() {
  Object.keys(errors).forEach((key) => delete errors[key])
  if (!form.profile.trim()) errors.profile = '이름 또는 닉네임을 입력해주세요.'
  if (!form.email.trim()) errors.email = '이메일 주소를 입력해주세요.'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
    errors.email = '올바른 이메일 주소를 입력해주세요.'
  if (passwordLevel.value < 2) errors.password = '영문, 숫자를 포함해 8자 이상 입력해주세요.'
  return Object.keys(errors).length === 0
}

async function completeSignup() {
  if (!requiredAgreementsAccepted.value || !validate()) return
  await router.push('/onboarding')
}

async function startWithGoogle() {
  await router.push('/onboarding')
}
</script>

<template>
  <section class="mx-auto w-full max-w-[520px] py-5 sm:py-9" aria-labelledby="signup-heading">
    <div
      class="rounded-xl border border-[#e7e7ec] bg-white p-5 shadow-[0_8px_22px_rgba(15,14,71,0.08)] sm:p-7"
    >
      <header class="text-center">
        <p
          class="inline-flex items-center rounded-full bg-primary-50 px-2.5 py-1 text-[11px] font-bold text-primary-600"
        >
          인디 메이커를 위한 플랫폼
        </p>
        <div
          class="mt-3 flex items-center justify-center gap-2 font-headline text-xl font-extrabold text-secondary"
        >
          <span
            class="h-5 w-5 rounded-md bg-primary-600"
            aria-label="Click-Hub 로고 placeholder"
          ></span>
          <span>Click-Hub</span>
        </div>
        <h1
          id="signup-heading"
          class="mt-4 text-[25px] font-extrabold tracking-tight text-secondary sm:text-[28px]"
        >
          Click-Hub에 오신 것을 환영합니다
        </h1>
        <p class="mt-2 text-[12px] leading-5 text-body-light">
          배포한 사이드 프로젝트를 세상에 알리고<br />함께 고민하고 성장하세요.
        </p>
      </header>

      <button
        data-testid="google-signup"
        type="button"
        class="mt-6 flex w-full items-center justify-between rounded-xl border border-[#e5e5ea] bg-white px-4 py-3 text-sm font-semibold text-[#34343f] shadow-sm hover:bg-neutral-50"
        @click="startWithGoogle"
      >
        <span class="flex items-center gap-2"
          ><span
            class="grid h-5 w-5 place-items-center rounded-full border border-neutral-200 text-xs font-bold text-[#4285f4]"
            >G</span
          >Google로 3초 만에 시작하기</span
        >
        <span class="rounded-full bg-primary-50 px-2 py-0.5 text-[10px] text-primary-600"
          >추천</span
        >
      </button>

      <div class="my-5 flex items-center gap-3 text-[11px] text-neutral-400">
        <span class="h-px flex-1 bg-neutral-200"></span>또는 이메일로 직접 가입<span
          class="h-px flex-1 bg-neutral-200"
        ></span>
      </div>

      <form class="space-y-4" novalidate @submit.prevent="completeSignup">
        <div>
          <label for="signup-profile" class="mb-1.5 block text-xs font-bold text-[#34343f]"
            >이름 / 닉네임 <span class="text-primary-600">*</span></label
          >
          <div class="relative">
            <input
              id="signup-profile"
              v-model="form.profile"
              name="profile"
              aria-label="이름 또는 닉네임"
              autocomplete="nickname"
              placeholder="예: 김메이커 (maker_kim)"
              :aria-invalid="Boolean(errors.profile)"
              class="w-full rounded-lg border bg-[#f4f4f6] px-3 py-2.5 pr-16 text-sm outline-none placeholder:text-neutral-400 focus:border-primary-600 focus:bg-white focus:ring-2 focus:ring-primary-100"
              :class="errors.profile ? 'border-danger' : 'border-transparent'"
            />
            <span
              class="pointer-events-none absolute inset-y-0 right-3 grid place-items-center text-[11px] text-neutral-400"
              >@handle</span
            >
          </div>
          <p v-if="errors.profile" class="mt-1 text-[11px] text-danger">{{ errors.profile }}</p>
        </div>
        <div>
          <label for="signup-email" class="mb-1.5 block text-xs font-bold text-[#34343f]"
            >이메일 주소 <span class="text-primary-600">*</span></label
          >
          <input
            id="signup-email"
            v-model="form.email"
            name="email"
            type="email"
            autocomplete="email"
            placeholder="maker@domain.com"
            aria-describedby="email-helper"
            :aria-invalid="Boolean(errors.email)"
            class="w-full rounded-lg border bg-[#f4f4f6] px-3 py-2.5 text-sm outline-none placeholder:text-neutral-400 focus:border-primary-600 focus:bg-white focus:ring-2 focus:ring-primary-100"
            :class="errors.email ? 'border-danger' : 'border-transparent'"
          />
          <p id="email-helper" class="mt-1 text-[10px] text-neutral-400">
            계정 생성 및 프로젝트 소식을 수신할 이메일을 입력하세요.
          </p>
          <p v-if="errors.email" class="mt-1 text-[11px] text-danger">{{ errors.email }}</p>
        </div>
        <div>
          <div class="mb-1.5 flex items-center justify-between">
            <label for="signup-password" class="text-xs font-bold text-[#34343f]"
              >비밀번호 <span class="text-primary-600">*</span></label
            ><span class="text-[10px] text-neutral-400">8자 이상 영문/숫자/특수문자</span>
          </div>
          <div class="relative">
            <input
              id="signup-password"
              v-model="form.password"
              name="password"
              :type="passwordVisible ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="••••••••"
              :aria-invalid="Boolean(errors.password)"
              class="w-full rounded-lg border bg-[#f4f4f6] px-3 py-2.5 pr-12 text-sm outline-none placeholder:text-neutral-400 focus:border-primary-600 focus:bg-white focus:ring-2 focus:ring-primary-100"
              :class="errors.password ? 'border-danger' : 'border-transparent'"
            /><button
              type="button"
              class="absolute inset-y-0 right-2 px-2 text-[11px] text-neutral-500"
              :aria-label="passwordVisible ? '비밀번호 숨기기' : '비밀번호 보기'"
              @click="passwordVisible = !passwordVisible"
            >
              보기
            </button>
          </div>
          <div
            data-testid="password-strength"
            :data-level="passwordLevel"
            class="mt-1.5 grid grid-cols-4 gap-1"
          >
            <span
              v-for="level in 4"
              :key="level"
              class="h-1 rounded-full bg-neutral-200"
              :class="{ 'is-active bg-primary-600': level <= passwordLevel }"
            ></span>
          </div>
          <p v-if="errors.password" class="mt-1 text-[11px] text-danger">{{ errors.password }}</p>
        </div>

        <fieldset>
          <legend class="text-xs font-bold text-[#34343f]">
            관심 기술 스택 및 포지션 <span class="ml-1 font-normal text-neutral-400">(선택)</span>
          </legend>
          <p class="mt-1 text-[10px] text-neutral-400">피드 맞춤 추천에만 활용됩니다.</p>
          <div class="mt-2 flex flex-wrap gap-1.5">
            <button
              v-for="chip in chips"
              :key="chip"
              type="button"
              :data-chip="chip"
              :aria-pressed="selectedChips.includes(chip)"
              class="rounded-full border px-2.5 py-1 text-[10px] font-medium transition"
              :class="
                selectedChips.includes(chip)
                  ? 'border-primary-600 bg-primary-50 text-primary-600'
                  : 'border-transparent bg-[#f1f1f3] text-[#555565]'
              "
              @click="toggleChip(chip)"
            >
              {{ chip }}
            </button>
          </div>
        </fieldset>

        <fieldset class="rounded-lg border border-[#e5e5ea] bg-[#fafafd] px-3 py-2.5">
          <legend class="sr-only">약관 동의</legend>
          <label
            class="flex items-center gap-2 border-b border-neutral-200 pb-2 text-xs font-bold text-[#34343f]"
            ><input
              v-model="allAgreements"
              data-testid="all-agreements"
              type="checkbox"
              class="h-4 w-4 accent-primary-600"
            />모든 약관에 전체 동의합니다</label
          >
          <div class="space-y-1.5 pt-2 text-[10px] text-[#555565]">
            <label class="flex items-center gap-2"
              ><input
                v-model="form.terms"
                data-testid="terms-agreement"
                type="checkbox"
                class="h-3.5 w-3.5 accent-primary-600"
              /><span>[필수] 서비스 이용약관 동의</span
              ><a href="/terms" class="ml-auto text-neutral-400 underline">보기</a></label
            >
            <label class="flex items-center gap-2"
              ><input
                v-model="form.privacy"
                data-testid="privacy-agreement"
                type="checkbox"
                class="h-3.5 w-3.5 accent-primary-600"
              /><span>[필수] 개인정보 수집 및 이용 동의</span
              ><a href="/privacy" class="ml-auto text-neutral-400 underline">보기</a></label
            >
            <label class="flex items-center gap-2"
              ><input
                v-model="form.newsletter"
                data-testid="newsletter-agreement"
                type="checkbox"
                class="h-3.5 w-3.5 accent-primary-600"
              /><span>[선택] 신규 프로젝트, 인사이트 뉴스레터 받기</span
              ><span class="ml-auto text-neutral-400">주 1회</span></label
            >
          </div>
        </fieldset>

        <button
          data-testid="signup-submit"
          type="submit"
          :disabled="!requiredAgreementsAccepted"
          class="w-full rounded-full bg-primary-600 py-3 text-sm font-bold text-white shadow-[0_4px_8px_rgba(34,32,162,0.22)] transition hover:bg-primary-700 disabled:cursor-not-allowed disabled:bg-primary-300"
          @click.prevent="completeSignup"
        >
          회원가입 완료하기 <span aria-hidden="true">→</span>
        </button>
      </form>
      <div class="mt-4 flex items-center justify-between text-[10px] text-neutral-500">
        <p>
          이미 계정이 있으신가요?
          <RouterLink to="/login" class="font-bold text-primary-600 underline"
            >로그인하기</RouterLink
          >
        </p>
        <p>256-bit SSL</p>
      </div>
    </div>
    <aside
      class="mt-3 flex items-center gap-2 rounded-lg border border-[#e7e7ec] bg-white px-3 py-2 text-[10px] text-[#555565] shadow-sm"
      aria-label="Click-Hub 활동 지표"
    >
      <div class="flex -space-x-1">
        <span
          v-for="avatar in 3"
          :key="avatar"
          class="h-5 w-5 rounded-full border-2 border-white bg-neutral-300"
        ></span>
      </div>
      <p>
        <strong class="text-secondary">이미 1,400+ 명의 인디 메이커 활동 중</strong><br />지난
        30일간 2,204개 프로젝트 런칭
      </p>
      <span class="ml-auto rounded-full bg-primary-50 px-2 py-1 text-primary-600"
        >주간 추천 프로젝트 #1 · 좋아요 984</span
      >
    </aside>
  </section>
</template>
