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
      :class="
        project.thumbnail_url
          ? 'bg-surface-light-1 dark:bg-surface-dark-2'
          : 'bg-[#f5f5f5] dark:bg-surface-dark-2'
      "
    >
      <img
        v-if="project.thumbnail_url"
        :src="project.thumbnail_url"
        :alt="project.title"
        class="h-full w-full object-cover"
      />
      <span
        v-if="rank"
        class="absolute left-2 top-2 rounded-md px-2 py-1 text-xs font-bold"
        :class="rankBadgeClass"
      >
        {{ rank }}위
      </span>
    </div>

    <div class="flex flex-1 flex-col gap-2 p-4">
      <h3
        class="font-headline font-bold text-heading-light group-hover:text-primary-600 dark:text-heading-dark"
      >
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
          <span title="저장">저장 {{ formatCount(project.stats?.likes) }}</span>
          <span title="댓글">댓글 {{ formatCount(project.stats?.comments) }}</span>
          <span title="조회">
            조회 {{ project.stats?.viewsDisplay ?? formatCount(project.stats?.views) }}
          </span>
        </div>
      </div>
    </div>
  </RouterLink>
</template>
