<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { getCategories, getTechnologies } from '@/api/catalog'
import { useAuthStore } from '@/stores/auth'

const goals = [
  {
    id: 'indie-maker',
    label: '1인 사이드 프로젝트 빌더',
    description: '직접 만든 서비스를 알리고 초기 유저들의 피드백을 모으고 싶어요.',
  },
  {
    id: 'tech-explorer',
    label: '새로운 서비스 탐색자',
    description: '출시된 프로젝트를 써보고 솔직한 피드백을 남기고 싶어요.',
  },
  {
    id: 'career-benchmark',
    label: '포트폴리오 벤치마커',
    description: '실제 배포된 서비스의 아키텍처와 코드를 참고하고 싶어요.',
  },
  {
    id: 'co-founder',
    label: '협업자 찾기',
    description: '함께 프로젝트를 출시할 동료를 찾고 싶어요.',
  },
]

const router = useRouter()
const auth = useAuthStore()
const selectedGoals = ref([])
const selectedCategories = ref([])
const selectedTechnologies = ref([])
const categories = ref([])
const technologies = ref([])
const categorySearch = ref('')
const technologySearch = ref('')
const isSaving = ref(false)
const errorMessage = ref('')

const visibleCategories = computed(() =>
  categories.value.filter((item) =>
    item.name.toLowerCase().includes(categorySearch.value.trim().toLowerCase()),
  ),
)
const visibleTechnologies = computed(() =>
  technologies.value.filter((item) =>
    item.name.toLowerCase().includes(technologySearch.value.trim().toLowerCase()),
  ),
)

function toggle(list, value) {
  const index = list.indexOf(value)
  if (index === -1) list.push(value)
  else list.splice(index, 1)
}

async function persist(payload) {
  if (isSaving.value) return
  isSaving.value = true
  errorMessage.value = ''
  try {
    await auth.completeOnboarding(payload)
    await router.replace('/')
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

function complete() {
  return persist({
    goals: selectedGoals.value,
    categories: selectedCategories.value,
    techStacks: selectedTechnologies.value,
  })
}

async function skip() {
  if (isSaving.value) return
  isSaving.value = true
  try {
    await auth.skipOnboarding()
    await router.replace('/')
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

onMounted(async () => {
  if (!auth.isLoggedIn) return router.replace('/login')
  if (auth.onboarding) return router.replace('/')
  try {
    ;[categories.value, technologies.value] = await Promise.all([
      getCategories(),
      getTechnologies(),
    ])
  } catch (error) {
    errorMessage.value = error.message
  }
})
</script>

<template>
  <main class="mx-auto w-full max-w-[832px] px-4 py-10 sm:px-0 lg:py-12">
    <header class="border-b border-primary-100 pb-7 dark:border-primary-900">
      <div
        class="flex items-center justify-between text-xs font-medium text-body-light dark:text-body-dark"
      >
        <span>맞춤 설정 1/3단계</span>
        <span>33% 완료됨</span>
      </div>
      <div
        class="mt-4 h-1.5 overflow-hidden rounded-full bg-primary-100 dark:bg-primary-950"
        role="progressbar"
        aria-label="맞춤 설정 진행률"
        aria-valuemin="0"
        aria-valuemax="100"
        aria-valuenow="33"
        aria-valuetext="맞춤 설정 1/3단계, 33% 완료됨"
      >
        <div class="h-full w-1/3 rounded-full bg-primary-600" />
      </div>
      <p class="mt-8 text-xs font-bold tracking-[0.14em] text-primary-700 dark:text-primary-200">
        CURATION ENGINE V2.4 <span class="font-medium text-divider">· AI 피드 최적화 중</span>
      </p>
      <h1
        class="mt-2 font-headline text-3xl font-bold tracking-tight text-heading-light sm:text-4xl dark:text-heading-dark"
      >
        어떤 사이드 프로젝트를 찾고 계신가요?
      </h1>
      <p class="mt-3 max-w-3xl text-sm leading-6 text-body-light dark:text-body-dark">
        관심사와 기술 스택을 선택하시면 AI와 추천 알고리즘이 매일 아침 딱 맞는 인디 프로젝트를 홈
        피드에 배달해드려요.
      </p>
    </header>
    <p
      v-if="errorMessage"
      role="alert"
      class="mt-5 rounded-md bg-danger/10 px-3 py-2 text-xs font-medium text-danger"
    >
      {{ errorMessage }}
    </p>

    <section
      class="mt-6 rounded-xl border border-primary-100 bg-white p-6 shadow-sm dark:border-primary-900 dark:bg-surface-dark-1"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-headline text-lg font-bold">1. 주 활동 포지션 / 목표</h2>
        <span
          class="bg-primary-50 px-2 py-1 text-xs font-semibold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >복수 선택 가능</span
        >
      </div>
      <div class="mt-5 grid gap-3 md:grid-cols-2">
        <button
          v-for="goal in goals"
          :key="goal.id"
          :data-testid="`goal-${goal.id}`"
          :aria-pressed="selectedGoals.includes(goal.id)"
          type="button"
          class="rounded-md border p-4 text-left transition focus-visible:outline-2 focus-visible:outline-primary-600"
          :class="
            selectedGoals.includes(goal.id)
              ? 'border-primary-600 bg-primary-50 dark:bg-primary-900/50'
              : 'border-transparent bg-primary-50/40 hover:border-primary-200 dark:bg-primary-950/30'
          "
          @click="toggle(selectedGoals, goal.id)"
        >
          <span class="flex gap-3"
            ><span
              class="mt-0.5 h-5 w-5 shrink-0 border"
              :class="
                selectedGoals.includes(goal.id)
                  ? 'border-primary-700 bg-primary-700 shadow-[inset_0_0_0_4px_white]'
                  : 'border-divider bg-white dark:bg-surface-dark-2'
              "
            /><span
              ><span class="font-semibold">{{ goal.label }}</span>
              <span class="ml-2 text-xs text-body-light dark:text-body-dark">{{
                goal.id === 'indie-maker'
                  ? 'Indie Maker'
                  : goal.id === 'tech-explorer'
                    ? 'Tech Explorer'
                    : goal.id === 'co-founder'
                      ? 'Co-founder'
                      : 'Career Benchmark'
              }}</span
              ><span class="mt-1.5 block text-xs leading-5 text-body-light dark:text-body-dark">{{
                goal.description
              }}</span></span
            ></span
          >
        </button>
      </div>
    </section>

    <section
      class="mt-5 rounded-xl border border-primary-100 bg-white p-6 shadow-sm dark:border-primary-900 dark:bg-surface-dark-1"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-headline text-lg font-bold">2. 관심 프로젝트 카테고리</h2>
        <span
          class="bg-primary-50 px-2 py-1 text-xs font-semibold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >{{ selectedCategories.length }}개 선택됨 (최소 3개 권장)</span
        >
      </div>
      <label class="mt-5 block"
        ><span class="sr-only">관심 프로젝트 카테고리 검색</span
        ><input
          v-model="categorySearch"
          data-testid="category-search"
          type="search"
          placeholder="관심 프로젝트 카테고리"
          class="w-full border-0 bg-primary-50 px-4 py-3 text-sm outline-none ring-1 ring-primary-100 placeholder:text-divider focus:ring-2 focus:ring-primary-600 dark:bg-primary-950 dark:ring-primary-900"
      /></label>
      <div class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <button
          v-for="item in visibleCategories"
          :key="item.id"
          :data-testid="`category-${item.slug}`"
          :aria-pressed="selectedCategories.includes(item.slug)"
          type="button"
          class="flex items-center justify-between rounded-md px-4 py-3 text-left text-sm font-medium transition focus-visible:outline-2 focus-visible:outline-primary-600"
          :class="
            selectedCategories.includes(item.slug)
              ? 'bg-primary-700 text-white'
              : 'bg-primary-50 text-heading-light hover:bg-primary-100 dark:bg-primary-950 dark:text-heading-dark'
          "
          @click="toggle(selectedCategories, item.slug)"
        >
          <span
            ><span class="mr-2 inline-block h-2 w-2 rounded-sm bg-current opacity-70" />{{
              item.name
            }}</span
          ><span
            class="h-4 w-4 border"
            :class="
              selectedCategories.includes(item.slug) ? 'border-white bg-white/25' : 'border-divider'
            "
          />
        </button>
      </div>
    </section>

    <section
      class="mt-5 rounded-xl border border-primary-100 bg-white p-6 shadow-sm dark:border-primary-900 dark:bg-surface-dark-1"
    >
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-headline text-lg font-bold">3. 관심 기술 스택 (Tech Stack)</h2>
        <span class="text-xs font-semibold text-body-light dark:text-body-dark"
          >{{ selectedTechnologies.length }}개 선택됨</span
        >
      </div>
      <label class="mt-5 block"
        ><span class="sr-only">관심 기술 스택 검색</span
        ><input
          v-model="technologySearch"
          data-testid="stack-search"
          type="search"
          placeholder="관심 기술 스택 검색 (예: Vue, Next, Spring, Docker)"
          class="w-full border-0 bg-primary-50 px-4 py-3 text-sm outline-none ring-1 ring-primary-100 placeholder:text-divider focus:ring-2 focus:ring-primary-600 dark:bg-primary-950 dark:ring-primary-900"
      /></label>
      <div class="mt-5 flex flex-wrap gap-2">
        <button
          v-for="item in visibleTechnologies"
          :key="item.id"
          :data-testid="`recommended-${item.slug}`"
          type="button"
          :aria-pressed="selectedTechnologies.includes(item.slug)"
          class="rounded bg-primary-50 px-3 py-1.5 text-xs font-medium text-body-light transition hover:bg-primary-100 dark:bg-primary-950 dark:text-body-dark"
          :class="
            selectedTechnologies.includes(item.slug) &&
            'bg-primary-700 text-white hover:bg-primary-700 dark:bg-primary-700 dark:text-white'
          "
          @click="toggle(selectedTechnologies, item.slug)"
        >
          {{ item.name }}
        </button>
      </div>
    </section>

    <section class="mt-6 rounded-xl bg-secondary p-6 text-white shadow-lg">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-headline text-lg font-bold">
          회원님의 취향을 기반으로 생성 중인 홈 피드 미리보기
        </h2>
        <span class="bg-primary-700 px-3 py-1.5 text-xs text-primary-100"
          ><span class="mr-1 inline-block h-2 w-2 rounded-full bg-emerald-300" />실시간 42개 매칭
          완료</span
        >
      </div>
      <div class="mt-5 grid gap-4 md:grid-cols-2">
        <article class="bg-white/10 p-4">
          <div class="flex gap-3">
            <div
              class="h-11 w-11 shrink-0 bg-gradient-to-br from-primary-100 via-primary-300 to-sky-300"
              role="img"
              aria-label="DevFlow Analytics 카테고리 시각 placeholder"
            />
            <div>
              <h3 class="font-semibold">
                DevFlow Analytics
                <span class="ml-1 text-xs font-normal text-primary-200">주간 1위</span>
              </h3>
              <p class="mt-1 text-xs text-primary-100">
                GitHub 커밋과 배포 빈도 기반 자동 생산성 분석기
              </p>
            </div>
          </div>
          <div class="mt-4 flex items-center justify-between bg-black/15 px-3 py-2 text-xs">
            <span
              class="flex items-end gap-1 text-emerald-300"
              role="img"
              aria-label="DevFlow Analytics 상승 추이"
              ><span class="h-1 w-4 -rotate-6 bg-emerald-300" /><span
                class="h-1 w-4 rotate-6 bg-emerald-300" /><span
                class="h-1 w-4 -rotate-12 bg-emerald-300"
            /></span>
            <span class="text-emerald-300">+340% 급상승</span><span>추천도 98%</span>
          </div>
          <p class="mt-3 text-xs text-primary-100">#개발자도구　#Next.js　#PostgreSQL</p>
        </article>
        <article class="bg-white/10 p-4">
          <div class="flex gap-3">
            <div
              class="h-11 w-11 shrink-0 bg-gradient-to-br from-fuchsia-200 via-primary-300 to-amber-200"
              role="img"
              aria-label="PromptCraft Studio 카테고리 시각 placeholder"
            />
            <div>
              <h3 class="font-semibold">
                PromptCraft Studio
                <span class="ml-1 text-xs font-normal text-primary-200">신규 주목</span>
              </h3>
              <p class="mt-1 text-xs text-primary-100">
                LLM 에이전트 체이닝 & 원클릭 프롬프트 디버거
              </p>
            </div>
          </div>
          <div class="mt-4 flex items-center justify-between bg-black/15 px-3 py-2 text-xs">
            <span
              class="inline-block h-4 w-4 rounded-full border-2 border-primary-200 border-r-emerald-300"
              role="img"
              aria-label="PromptCraft Studio 스택 매칭 94%"
            />
            <span class="text-primary-100">스택 매칭 94%</span><span>베타 피드백 진행중</span>
          </div>
          <p class="mt-3 text-xs text-primary-100">#AI　#생산성　#FastAPI</p>
        </article>
      </div>
      <div class="mt-5 flex flex-wrap justify-between gap-3 text-xs text-primary-100">
        <span
          >안내: 설정하신 관심사와 스택은 마이페이지에서 언제든지 자유롭게 수정하실 수
          있습니다.</span
        ><span>알고리즘 주기: 매일 오전 08:00 KST</span>
      </div>
    </section>

    <footer
      class="mt-8 flex flex-col gap-4 border-b border-primary-100 pb-10 sm:flex-row sm:items-center sm:justify-between dark:border-primary-900"
    >
      <button
        data-testid="onboarding-skip"
        type="button"
        :disabled="isSaving"
        class="text-sm font-medium text-body-light hover:text-primary-700 dark:text-body-dark"
        @click="skip"
      >
        나중에 설정하기 (기본 피드로 시작)
      </button>
      <p class="text-center text-xs text-primary-700 dark:text-primary-200">
        선택한 카테고리의 핫 프로젝트 42개를 추천해 드립니다.
      </p>
      <button
        data-testid="onboarding-complete"
        type="button"
        :disabled="isSaving"
        class="bg-primary-700 px-5 py-3 text-sm font-semibold text-white shadow transition hover:bg-primary-800"
        @click="complete"
      >
        {{ isSaving ? '저장 중...' : '맞춤 피드로 시작하기 →' }}
      </button>
    </footer>
  </main>
</template>
