<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { goals, interestCategories, popularTechStacks } from '@/data/onboardingOptions'

const router = useRouter()
const auth = useAuthStore()

const selectedGoals = ref([])
const selectedCategories = ref([])
const selectedTechStacks = ref([])

function toggle(list, value) {
  const index = list.value.indexOf(value)
  if (index === -1) {
    list.value.push(value)
  } else {
    list.value.splice(index, 1)
  }
}

function complete() {
  auth.completeOnboarding({
    goals: selectedGoals.value,
    categories: selectedCategories.value,
    techStacks: selectedTechStacks.value,
  })
  router.push('/')
}

function skip() {
  auth.skipOnboarding()
  router.push('/')
}
</script>

<template>
  <div class="mx-auto flex max-w-2xl flex-col gap-8">
    <div>
      <p class="text-sm font-semibold text-primary-600">맞춤 설정 · 1/1단계</p>
      <h1 class="mt-1 text-xl font-bold">어떤 사이드 프로젝트를 찾고 계신가요?</h1>
      <p class="mt-2 text-sm text-neutral-500">
        관심사와 기술 스택을 선택하면 홈 피드를 맞춤으로 구성해 드려요. (건너뛰어도 됩니다)
      </p>
    </div>

    <section class="rounded-xl border border-neutral-200 p-5 dark:border-neutral-800">
      <h2 class="mb-3 font-semibold">1. 주 활동 목표 (복수 선택 가능)</h2>
      <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
        <button
          v-for="goal in goals"
          :key="goal.id"
          type="button"
          class="rounded-lg border p-3 text-left text-sm transition"
          :class="
            selectedGoals.includes(goal.id)
              ? 'border-primary-500 bg-primary-50 dark:bg-primary-950'
              : 'border-neutral-200 hover:bg-neutral-50 dark:border-neutral-800 dark:hover:bg-neutral-900'
          "
          @click="toggle(selectedGoals, goal.id)"
        >
          <p class="font-medium">{{ goal.label }}</p>
          <p class="mt-1 text-xs text-neutral-500">{{ goal.description }}</p>
        </button>
      </div>
    </section>

    <section class="rounded-xl border border-neutral-200 p-5 dark:border-neutral-800">
      <h2 class="mb-3 font-semibold">2. 관심 프로젝트 카테고리</h2>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="category in interestCategories"
          :key="category.slug"
          type="button"
          class="rounded-full border px-3 py-1.5 text-sm transition"
          :class="
            selectedCategories.includes(category.slug)
              ? 'border-primary-500 bg-primary-600 text-white'
              : 'border-neutral-200 text-neutral-600 hover:bg-neutral-50 dark:border-neutral-800 dark:text-neutral-300 dark:hover:bg-neutral-900'
          "
          @click="toggle(selectedCategories, category.slug)"
        >
          {{ category.name }}
        </button>
      </div>
    </section>

    <section class="rounded-xl border border-neutral-200 p-5 dark:border-neutral-800">
      <h2 class="mb-3 font-semibold">3. 관심 기술 스택</h2>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="tech in popularTechStacks"
          :key="tech"
          type="button"
          class="rounded-full border px-3 py-1.5 text-sm transition"
          :class="
            selectedTechStacks.includes(tech)
              ? 'border-primary-500 bg-primary-600 text-white'
              : 'border-neutral-200 text-neutral-600 hover:bg-neutral-50 dark:border-neutral-800 dark:text-neutral-300 dark:hover:bg-neutral-900'
          "
          @click="toggle(selectedTechStacks, tech)"
        >
          {{ tech }}
        </button>
      </div>
    </section>

    <div class="flex items-center gap-4">
      <button
        type="button"
        class="rounded-full bg-primary-600 px-6 py-3 text-sm font-semibold text-white hover:bg-primary-700"
        @click="complete"
      >
        설정 완료하고 시작하기
      </button>
      <button
        type="button"
        class="text-sm font-medium text-neutral-500 hover:text-neutral-700 dark:hover:text-neutral-300"
        @click="skip"
      >
        건너뛰기
      </button>
    </div>
  </div>
</template>
