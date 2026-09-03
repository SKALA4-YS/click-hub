<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { goals, interestCategories, popularTechStacks } from '@/data/onboardingOptions'

const router = useRouter()
const auth = useAuthStore()

const step = ref(1)
const selectedGoals = ref([])
const selectedCategories = ref([])
const selectedTechStacks = ref([])

const steps = [
  {
    eyebrow: '맞춤 설정',
    title: '어떤 사이드 프로젝트를 찾고 계신가요?',
    description: '주 활동 목표를 선택하면 나에게 맞는 프로젝트와 메이커를 먼저 보여드릴게요.',
    options: goals,
    value: selectedGoals,
    valueKey: 'id',
    labelKey: 'label',
    descriptionKey: 'description',
    optionStyle: 'card',
  },
  {
    eyebrow: '맞춤 설정',
    title: '관심 있는 프로젝트 분야를 알려주세요.',
    description: '관심 카테고리를 바탕으로 탐색 피드를 정리해드려요. 여러 개를 골라도 좋아요.',
    options: interestCategories,
    value: selectedCategories,
    valueKey: 'slug',
    labelKey: 'name',
    optionStyle: 'chip',
  },
  {
    eyebrow: '맞춤 설정',
    title: '관심 기술 스택을 선택해 주세요.',
    description: '기술 스택을 선택하면 비슷한 기술을 사용하는 프로젝트를 추천해드릴게요.',
    options: popularTechStacks,
    value: selectedTechStacks,
    optionStyle: 'chip',
  },
]

const currentStep = computed(() => steps[step.value - 1])
const progress = computed(() => `${(step.value / steps.length) * 100}%`)

function optionValue(option) {
  return typeof option === 'string' ? option : option[currentStep.value.valueKey]
}

function optionLabel(option) {
  return typeof option === 'string' ? option : option[currentStep.value.labelKey]
}

function toggle(value) {
  const selections = currentStep.value.value.value
  const index = selections.indexOf(value)
  if (index === -1) selections.push(value)
  else selections.splice(index, 1)
}

function isSelected(value) {
  return currentStep.value.value.value.includes(value)
}

function next() {
  if (step.value < steps.length) step.value += 1
}

function back() {
  if (step.value > 1) step.value -= 1
}

async function complete() {
  auth.completeOnboarding({
    goals: selectedGoals.value,
    categories: selectedCategories.value,
    techStacks: selectedTechStacks.value,
  })
  await router.push('/')
}

async function skip() {
  auth.skipOnboarding()
  await router.push('/')
}
</script>

<template>
  <main
    class="mx-auto flex min-h-[calc(100vh-12rem)] w-full max-w-3xl items-center px-4 py-10 sm:px-6"
  >
    <section
      class="w-full overflow-hidden rounded-3xl border border-primary-100 bg-surface-light-1 shadow-[0_20px_55px_rgba(15,14,71,0.08)] dark:border-primary-900 dark:bg-surface-dark-1"
    >
      <div class="border-b border-primary-100 px-6 py-5 sm:px-10 dark:border-primary-900">
        <div class="flex items-center justify-between gap-4">
          <p class="text-sm font-semibold text-primary-600">{{ currentStep.eyebrow }}</p>
          <p
            data-testid="onboarding-progress"
            class="text-sm font-medium text-body-light dark:text-body-dark"
          >
            {{ step }}/{{ steps.length }}
          </p>
        </div>
        <div
          class="mt-3 h-1.5 overflow-hidden rounded-full bg-primary-100 dark:bg-primary-950"
          aria-hidden="true"
        >
          <div
            class="h-full rounded-full bg-primary-600 transition-[width] duration-200"
            :style="{ width: progress }"
          />
        </div>
      </div>
      <div class="px-6 py-8 sm:px-10 sm:py-10">
        <h1
          class="font-headline text-2xl font-bold tracking-tight text-heading-light sm:text-3xl dark:text-heading-dark"
        >
          {{ currentStep.title }}
        </h1>
        <p class="mt-3 max-w-xl text-sm leading-6 text-body-light sm:text-base dark:text-body-dark">
          {{ currentStep.description }}
        </p>
        <div
          class="mt-8"
          :class="
            currentStep.optionStyle === 'card'
              ? 'grid gap-3 sm:grid-cols-2'
              : 'flex flex-wrap gap-2.5'
          "
        >
          <button
            v-for="option in currentStep.options"
            :key="optionValue(option)"
            :data-testid="`option-${optionValue(option)}`"
            :aria-pressed="isSelected(optionValue(option))"
            type="button"
            class="transition focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            :class="[
              currentStep.optionStyle === 'card'
                ? 'rounded-2xl border p-4 text-left'
                : 'rounded-full border px-4 py-2.5 text-sm font-medium',
              isSelected(optionValue(option))
                ? 'border-primary-600 bg-primary-600 text-white shadow-sm'
                : 'border-primary-100 text-heading-light hover:border-primary-300 hover:bg-primary-50 dark:border-primary-900 dark:text-heading-dark dark:hover:border-primary-700 dark:hover:bg-primary-900/40',
            ]"
            @click="toggle(optionValue(option))"
          >
            <span class="block font-semibold">{{ optionLabel(option) }}</span>
            <span
              v-if="option.description"
              class="mt-1.5 block text-xs leading-5"
              :class="
                isSelected(optionValue(option))
                  ? 'text-primary-100'
                  : 'text-body-light dark:text-body-dark'
              "
              >{{ option.description }}</span
            >
          </button>
        </div>
      </div>
      <footer
        class="flex flex-col-reverse gap-3 border-t border-primary-100 px-6 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-10 dark:border-primary-900"
      >
        <button
          v-if="step > 1"
          data-testid="onboarding-back"
          type="button"
          class="rounded-full px-4 py-2.5 text-sm font-semibold text-body-light transition hover:bg-primary-50 hover:text-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 dark:text-body-dark dark:hover:bg-primary-900/40 dark:hover:text-primary-100"
          @click="back"
        >
          이전
        </button>
        <span v-else aria-hidden="true" />
        <div class="flex items-center gap-2 self-end sm:self-auto">
          <button
            data-testid="onboarding-skip"
            type="button"
            class="rounded-full px-4 py-2.5 text-sm font-semibold text-body-light transition hover:bg-primary-50 hover:text-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 dark:text-body-dark dark:hover:bg-primary-900/40 dark:hover:text-primary-100"
            @click="skip"
          >
            건너뛰기
          </button>
          <button
            v-if="step < steps.length"
            data-testid="onboarding-next"
            type="button"
            class="rounded-full bg-primary-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            @click="next"
          >
            다음
          </button>
          <button
            v-else
            data-testid="onboarding-complete"
            type="button"
            class="rounded-full bg-primary-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-primary-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            @click="complete"
          >
            설정 완료하고 시작하기
          </button>
        </div>
      </footer>
    </section>
  </main>
</template>
