<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { getDeveloperRankings } from '@/api/rankings'

const developers = ref([])
const query = ref('')
const isLoading = ref(true)
const errorMessage = ref('')

const visibleDevelopers = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return needle
    ? developers.value.filter((developer) => developer.displayName.toLowerCase().includes(needle))
    : developers.value
})

async function loadRankings() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    developers.value = await getDeveloperRankings()
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

onMounted(loadRankings)
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-10" aria-labelledby="maker-ranking-heading">
    <nav class="mb-4 text-xs text-body-light dark:text-body-dark" aria-label="현재 위치">
      홈 <span class="px-1">›</span> 메이커 랭킹
    </nav>
    <header class="border-b border-divider/20 pb-7 dark:border-divider/30">
      <p
        class="mb-2 inline-flex rounded-full bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
      >
        최근 7일 유효 활동 기준
      </p>
      <h1
        id="maker-ranking-heading"
        class="font-headline text-3xl font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
      >
        메이커 랭킹 <span class="text-blue-500">Top Makers</span>
      </h1>
      <p class="mt-2 text-sm text-body-light dark:text-body-dark">
        프로젝트를 공급하는 사용자의 실제 활동 데이터로 집계한 순위입니다.
      </p>
    </header>

    <label
      class="mt-6 flex max-w-sm rounded-lg border border-divider/20 bg-white px-3 py-2 text-sm dark:border-divider/30 dark:bg-surface-dark-1 dark:text-heading-dark"
    >
      <span class="sr-only">메이커 검색</span>
      <input
        v-model="query"
        class="w-full bg-transparent outline-none"
        placeholder="메이커 검색..."
      />
    </label>

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      랭킹을 불러오는 중입니다.
    </p>
    <section v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadRankings">
        다시 시도
      </button>
    </section>
    <p
      v-else-if="visibleDevelopers.length === 0"
      class="py-16 text-center text-sm text-body-light dark:text-body-dark"
    >
      집계된 메이커가 없습니다.
    </p>
    <ol v-else class="mt-8 space-y-3" aria-label="메이커 순위">
      <li
        v-for="developer in visibleDevelopers"
        :key="developer.creatorId"
        class="grid grid-cols-[48px_1fr_auto] items-center gap-4 rounded-xl border border-divider/20 bg-white p-4 dark:border-divider/30 dark:bg-surface-dark-1"
      >
        <strong class="text-center text-lg text-primary-700 dark:text-primary-200">{{
          developer.rank
        }}</strong>
        <div>
          <RouterLink
            :to="`/developers/${developer.creatorId}`"
            class="font-bold text-heading-light hover:text-primary-600 dark:text-heading-dark"
          >
            {{ developer.displayName }}
          </RouterLink>
          <p class="mt-1 text-xs text-body-light dark:text-body-dark">Click HUB 메이커</p>
        </div>
        <span class="text-right text-sm font-bold text-primary-700 dark:text-primary-200">
          <span class="block text-[10px] font-medium text-body-light dark:text-body-dark"
            >활동 점수</span
          >
          {{ developer.score.toFixed(2) }}
        </span>
      </li>
    </ol>
  </section>
</template>
