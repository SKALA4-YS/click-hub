<script setup>
defineProps({ trend: { type: Object, required: true } })
</script>

<template>
  <article
    data-testid="trend-ranking-card"
    class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 shadow-sm dark:border-divider/25 dark:bg-surface-dark-1"
  >
    <div class="flex items-center justify-between gap-3">
      <div class="flex flex-wrap items-center gap-2">
        <span
          class="flex h-6 w-6 items-center justify-center rounded-md bg-primary-50 text-xs font-bold text-primary-600 dark:bg-primary-900"
          >{{ trend.rank }}</span
        >
        <span
          v-for="tag in trend.tags"
          :key="tag"
          class="rounded-md bg-primary-50 px-2 py-1 text-[11px] font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >{{ tag }}</span
        >
      </div>
      <div class="flex shrink-0 gap-2 text-[11px] font-semibold">
        <span class="rounded-full bg-success/10 px-2 py-1 text-success"
          >검색량 {{ trend.growth }}</span
        ><span class="rounded-full bg-primary-50 px-2 py-1 text-primary-600 dark:bg-primary-900"
          >런칭 {{ trend.saves }}</span
        >
      </div>
    </div>
    <h2 class="mt-3 font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
      {{ trend.title }}
    </h2>
    <p class="mt-2 text-sm leading-6 text-body-light dark:text-body-dark">{{ trend.summary }}</p>
    <div
      class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-divider/10 pt-3 text-xs"
    >
      <div data-testid="trend-point-row" class="flex flex-wrap items-center gap-2">
        <span class="text-body-light dark:text-body-dark">{{ trend.stack }}</span>
        <span class="font-bold text-primary-600 dark:text-primary-200">{{ trend.point }}</span>
      </div>
      <RouterLink
        :to="trend.to"
        class="font-bold text-primary-600 hover:text-primary-700 dark:text-primary-200"
        >관련 인기 프로젝트 보기 →</RouterLink
      >
    </div>
  </article>
</template>
