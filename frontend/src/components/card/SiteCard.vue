<script setup>
import { computed } from 'vue'

// 프로젝트 카드 — 홈 피드·랭킹·검색 결과 등에서 공통으로 쓰는 단위 카드.
// project 필드는 schema.sql의 projects 테이블 + project_daily_metrics 집계를 단순화한 형태를 기대한다.
const props = defineProps({
  project: {
    type: Object,
    required: true,
  },
  // Top 100 같은 랭킹 목록에서만 전달, 1부터 시작하는 순위
  rank: {
    type: Number,
    default: null,
  },
})

// 1~3위는 금/은/동 뱃지로 구분하고, 그 밖의 순위는 브랜드 컬러로 표시한다 (디자인시스템 4.3)
const RANK_BADGE_STYLE = {
  1: 'bg-rank-gold text-heading-light',
  2: 'bg-rank-silver text-heading-light',
  3: 'bg-rank-bronze text-heading-light',
}
const rankBadgeClass = computed(() => RANK_BADGE_STYLE[props.rank] ?? 'bg-primary-600 text-white')

function formatCount(value) {
  return new Intl.NumberFormat('ko-KR').format(value ?? 0)
}
</script>

<template>
  <RouterLink
    :to="`/projects/${project.id}`"
    class="group flex flex-col overflow-hidden rounded-xl border border-divider/15 bg-surface-light-1 transition-shadow hover:shadow-md dark:border-blue-500/20 dark:bg-surface-dark-1"
  >
    <div
      class="relative aspect-video"
      :class="project.thumbnail_url ? 'bg-surface-light-1 dark:bg-surface-dark-2' : 'bg-gradient-to-br from-primary-500 to-blue-500'"
    >
      <img
        v-if="project.thumbnail_url"
        :src="project.thumbnail_url"
        :alt="project.title"
        class="h-full w-full object-cover"
      />
      <span v-if="rank" class="absolute left-2 top-2 rounded-md px-2 py-1 text-xs font-bold" :class="rankBadgeClass">
        {{ rank }}위
      </span>
    </div>

    <div class="flex flex-1 flex-col gap-2 p-4">
      <h3 class="font-headline font-bold text-heading-light group-hover:text-primary-600 dark:text-heading-dark">
        {{ project.title }}
      </h3>
      <p class="line-clamp-2 text-sm text-body-light dark:text-body-dark">
        {{ project.description }}
      </p>

      <div class="mt-auto flex items-center justify-between gap-2 pt-2">
        <span
          class="shrink-0 truncate rounded-md bg-neutral-100 px-2 py-1 text-xs font-medium whitespace-nowrap text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
        >
          {{ project.category }}
        </span>

        <div class="flex shrink-0 items-center gap-3 text-xs text-body-light dark:text-body-dark">
          <span class="flex items-center gap-1" title="좋아요">
            <svg viewBox="0 0 20 20" fill="currentColor" class="h-3.5 w-3.5">
              <path d="M10 17.5s-6.5-4.1-8.4-8.1C.4 6.6 1.7 3.5 4.7 3c1.9-.3 3.6.6 4.3 2 .7-1.4 2.4-2.3 4.3-2 3 .5 4.3 3.6 3.1 6.4-1.9 4-8.4 8.1-8.4 8.1z" />
            </svg>
            {{ formatCount(project.stats?.likes) }}
          </span>
          <span class="flex items-center gap-1" title="댓글">
            <svg viewBox="0 0 20 20" fill="currentColor" class="h-3.5 w-3.5">
              <path fill-rule="evenodd" d="M2 5a2 2 0 012-2h12a2 2 0 012 2v7a2 2 0 01-2 2H8l-4 3v-3H4a2 2 0 01-2-2V5z" clip-rule="evenodd" />
            </svg>
            {{ formatCount(project.stats?.comments) }}
          </span>
          <span class="flex items-center gap-1" title="조회수">
            <svg viewBox="0 0 20 20" fill="currentColor" class="h-3.5 w-3.5">
              <path d="M10 3.5c-4.5 0-8 3.7-8 6.5s3.5 6.5 8 6.5 8-3.7 8-6.5-3.5-6.5-8-6.5zm0 10.5a4 4 0 110-8 4 4 0 010 8z" />
              <circle cx="10" cy="10" r="1.8" />
            </svg>
            {{ formatCount(project.stats?.views) }}
          </span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>
