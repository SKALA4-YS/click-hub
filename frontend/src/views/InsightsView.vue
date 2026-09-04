<script setup>
import { onMounted, ref } from 'vue'

import { getWeeklyInsight } from '@/api/insights'

const insight = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')

async function loadInsight() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    insight.value = await getWeeklyInsight()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

function formatWeek(value) {
  return value
    ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long' }).format(new Date(value))
    : ''
}

onMounted(loadInsight)
</script>

<template>
  <section class="mx-auto max-w-[1000px] pb-12" aria-labelledby="insight-heading">
    <p v-if="isLoading" class="py-24 text-center text-sm text-body-light dark:text-body-dark">
      주간 인사이트를 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-24 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadInsight">
        다시 시도
      </button>
    </div>
    <template v-else>
      <header
        class="rounded-2xl border border-primary-100 bg-primary-50/60 p-7 dark:border-primary-800 dark:bg-primary-950/60"
      >
        <p class="text-xs font-bold tracking-wide text-primary-700 dark:text-primary-200">
          WEEKLY TREND REPORT · {{ formatWeek(insight.weekStart) }}
        </p>
        <h1
          id="insight-heading"
          class="mt-3 font-headline text-3xl font-extrabold text-heading-light dark:text-heading-dark"
        >
          {{ insight.headline }}
        </h1>
        <p class="mt-3 text-xs text-body-light dark:text-body-dark">
          {{ insight.modelName }} · {{ insight.generatedAt }}
        </p>
      </header>

      <section class="mt-8">
        <h2 class="font-headline text-xl font-bold text-heading-light dark:text-heading-dark">
          이번 주 변화
        </h2>
        <p
          v-if="insight.trends.length === 0"
          class="mt-5 text-sm text-body-light dark:text-body-dark"
        >
          집계된 트렌드가 없습니다.
        </p>
        <ol v-else class="mt-5 space-y-3">
          <li
            v-for="(trend, index) in insight.trends"
            :key="`${trend.topic}-${index}`"
            class="flex items-center justify-between rounded-xl border border-divider/20 bg-white p-4 dark:border-divider/30 dark:bg-surface-dark-1"
          >
            <div>
              <strong class="text-heading-light dark:text-heading-dark">{{ trend.topic }}</strong>
              <p class="mt-1 text-xs text-body-light dark:text-body-dark">{{ trend.direction }}</p>
            </div>
            <span class="font-bold text-primary-700 dark:text-primary-200"
              >{{ trend.changeRate > 0 ? '+' : '' }}{{ trend.changeRate }}%</span
            >
          </li>
        </ol>
      </section>

      <section
        class="mt-8 rounded-xl border border-divider/20 bg-white p-5 dark:border-divider/30 dark:bg-surface-dark-1"
      >
        <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
          다음 주 주목할 키워드
        </h2>
        <div class="mt-4 flex flex-wrap gap-2">
          <span
            v-for="keyword in insight.watchlist"
            :key="keyword"
            class="rounded-full bg-primary-50 px-3 py-1 text-sm text-primary-700"
            >{{ keyword }}</span
          >
        </div>
      </section>
    </template>
  </section>
</template>
