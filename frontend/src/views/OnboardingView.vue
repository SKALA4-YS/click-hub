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
  <main class="mx-auto max-w-[832px] px-4 py-10">
    <header class="border-b border-primary-100 pb-7">
      <h1 class="font-headline text-3xl font-bold">어떤 사이드 프로젝트를 찾고 계신가요?</h1>
      <p class="mt-3 text-sm text-body-light">
        관심사와 기술 스택은 서버에 저장되며 언제든 다시 설정할 수 있습니다.
      </p>
    </header>
    <p v-if="errorMessage" role="alert" class="mt-5 text-sm text-danger">{{ errorMessage }}</p>

    <section class="mt-6 rounded-xl border border-primary-100 bg-white p-6">
      <h2 class="font-headline text-lg font-bold">1. 주 활동 목표</h2>
      <div class="mt-4 grid gap-3 sm:grid-cols-2">
        <button
          v-for="goal in goals"
          :key="goal.id"
          :data-testid="`goal-${goal.id}`"
          type="button"
          :aria-pressed="selectedGoals.includes(goal.id)"
          class="rounded-lg border p-4 text-left"
          :class="
            selectedGoals.includes(goal.id)
              ? 'border-primary-600 bg-primary-50'
              : 'border-divider/20'
          "
          @click="toggle(selectedGoals, goal.id)"
        >
          <strong>{{ goal.label }}</strong
          ><span class="mt-1 block text-xs text-body-light">{{ goal.description }}</span>
        </button>
      </div>
    </section>

    <section class="mt-5 rounded-xl border border-primary-100 bg-white p-6">
      <h2 class="font-headline text-lg font-bold">2. 관심 프로젝트 카테고리</h2>
      <input
        v-model="categorySearch"
        data-testid="category-search"
        type="search"
        class="mt-4 w-full rounded-lg border border-divider/20 px-3 py-2"
        placeholder="카테고리 검색"
      />
      <div class="mt-4 flex flex-wrap gap-2">
        <button
          v-for="item in visibleCategories"
          :key="item.id"
          :data-testid="`category-${item.slug}`"
          type="button"
          :aria-pressed="selectedCategories.includes(item.slug)"
          class="rounded-full border border-divider/20 px-3 py-2 text-sm"
          :class="selectedCategories.includes(item.slug) && 'bg-primary-600 text-white'"
          @click="toggle(selectedCategories, item.slug)"
        >
          {{ item.name }}
        </button>
      </div>
    </section>

    <section class="mt-5 rounded-xl border border-primary-100 bg-white p-6">
      <h2 class="font-headline text-lg font-bold">3. 관심 기술 스택</h2>
      <input
        v-model="technologySearch"
        data-testid="stack-search"
        type="search"
        class="mt-4 w-full rounded-lg border border-divider/20 px-3 py-2"
        placeholder="기술 스택 검색"
      />
      <div class="mt-4 flex flex-wrap gap-2">
        <button
          v-for="item in visibleTechnologies"
          :key="item.id"
          :data-testid="`recommended-${item.slug}`"
          type="button"
          :aria-pressed="selectedTechnologies.includes(item.slug)"
          class="rounded-full border border-divider/20 px-3 py-2 text-sm"
          :class="selectedTechnologies.includes(item.slug) && 'bg-primary-600 text-white'"
          @click="toggle(selectedTechnologies, item.slug)"
        >
          {{ item.name }}
        </button>
      </div>
    </section>

    <footer class="mt-7 flex flex-wrap items-center justify-between gap-4">
      <button
        data-testid="onboarding-skip"
        type="button"
        :disabled="isSaving"
        class="text-sm text-body-light"
        @click="skip"
      >
        나중에 설정하기</button
      ><button
        data-testid="onboarding-complete"
        type="button"
        :disabled="isSaving"
        class="rounded-lg bg-primary-700 px-5 py-3 text-sm font-semibold text-white"
        @click="complete"
      >
        {{ isSaving ? '저장 중...' : '설정 저장' }}
      </button>
    </footer>
  </main>
</template>
