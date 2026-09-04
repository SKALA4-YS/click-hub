<script setup>
import { onMounted, ref, watch } from 'vue'

import { getTechnologies } from '@/api/catalog'
import { getTutorials } from '@/api/tutorials'

const tutorials = ref([])
const technologies = ref([])
const type = ref('')
const difficulty = ref('')
const technology = ref('')
const isLoading = ref(true)
const errorMessage = ref('')

const difficultyLabels = {
  BEGINNER: '입문',
  INTERMEDIATE: '중급',
  ADVANCED: '고급',
}

async function loadTutorials() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    tutorials.value = await getTutorials({
      type: type.value || undefined,
      difficulty: difficulty.value || undefined,
      tech: technology.value || undefined,
    })
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

watch([type, difficulty, technology], loadTutorials)
onMounted(async () => {
  try {
    technologies.value = await getTechnologies()
  } catch {
    technologies.value = []
  }
  await loadTutorials()
})
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-12" aria-labelledby="tutorial-heading">
    <header class="border-b border-divider/20 pb-7 dark:border-divider/30">
      <h1
        id="tutorial-heading"
        class="font-headline text-3xl font-extrabold text-heading-light dark:text-heading-dark"
      >
        튜토리얼
      </h1>
      <p class="mt-2 text-sm text-body-light dark:text-body-dark">
        검증된 자료로 사이드 프로젝트 개발과 배포를 학습하세요.
      </p>
    </header>

    <div class="mt-6 grid gap-3 sm:grid-cols-3" aria-label="튜토리얼 필터">
      <select
        v-model="type"
        aria-label="튜토리얼 유형"
        class="rounded-lg border border-divider/20 bg-white px-3 py-2 text-sm text-heading-light dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
      >
        <option value="">모든 유형</option>
        <option value="VIBE_CODING">바이브 코딩</option>
        <option value="DEVELOPMENT">개발</option>
      </select>
      <select
        v-model="difficulty"
        aria-label="튜토리얼 난이도"
        class="rounded-lg border border-divider/20 bg-white px-3 py-2 text-sm text-heading-light dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
      >
        <option value="">모든 난이도</option>
        <option value="BEGINNER">입문</option>
        <option value="INTERMEDIATE">중급</option>
        <option value="ADVANCED">고급</option>
      </select>
      <select
        v-model="technology"
        aria-label="튜토리얼 기술"
        class="rounded-lg border border-divider/20 bg-white px-3 py-2 text-sm text-heading-light dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
      >
        <option value="">모든 기술</option>
        <option v-for="item in technologies" :key="item.id" :value="item.slug">
          {{ item.name }}
        </option>
      </select>
    </div>

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      튜토리얼을 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadTutorials">
        다시 시도
      </button>
    </div>
    <p v-else-if="tutorials.length === 0" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      조건에 맞는 튜토리얼이 없습니다.
    </p>
    <div v-else class="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      <article
        v-for="tutorial in tutorials"
        :key="tutorial.id"
        class="flex flex-col rounded-xl border border-divider/20 bg-white p-5 dark:border-divider/30 dark:bg-surface-dark-1"
      >
        <div class="flex items-center gap-2 text-xs text-primary-700 dark:text-primary-200">
          <span>{{ tutorial.type === 'VIBE_CODING' ? '바이브 코딩' : '개발' }}</span>
          <span>·</span>
          <span>{{ difficultyLabels[tutorial.difficulty] ?? tutorial.difficulty }}</span>
          <span>· {{ tutorial.estimatedMinutes }}분</span>
        </div>
        <h2 class="mt-3 font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
          {{ tutorial.title }}
        </h2>
        <p class="mt-2 flex-1 text-sm leading-6 text-body-light dark:text-body-dark">
          {{ tutorial.description }}
        </p>
        <div class="mt-4 flex flex-wrap gap-1">
          <span
            v-for="slug in tutorial.technologySlugs"
            :key="slug"
            class="rounded bg-primary-50 px-2 py-1 text-xs text-primary-700"
            >{{ slug }}</span
          >
        </div>
        <a
          :href="tutorial.sourceUrl"
          target="_blank"
          rel="noopener"
          class="mt-5 font-semibold text-primary-600"
          >자료 열기 ↗</a
        >
      </article>
    </div>
  </section>
</template>
