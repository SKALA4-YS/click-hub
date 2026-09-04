<script setup>
defineProps({
  project: {
    type: Object,
    required: true,
  },
})

const categoryLabel = {
  'developer-tools': '개발도구',
  'design-creative': '디자인',
  'content-entertainment': '엔터테인먼트',
  'ai-service': 'AI',
  'productivity-work': '생산성',
  marketing: '마케팅',
  other: '기타',
}

const categoryBadgeClass = {
  'developer-tools': 'bg-secondary text-white',
  'design-creative': 'bg-pink-100 text-pink-700 dark:bg-pink-950 dark:text-pink-200',
  'content-entertainment': 'bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-200',
  'ai-service': 'bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-200',
  'productivity-work': 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-200',
  marketing: 'bg-accent-500/15 text-accent-600 dark:text-accent-400',
  other: 'bg-neutral-100 text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300',
}
</script>

<template>
  <RouterLink
    :to="{ name: 'project-detail', params: { id: project.id } }"
    class="group flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-white transition hover:-translate-y-0.5 hover:shadow-lg dark:border-white/10 dark:bg-secondary"
  >
    <div
      class="relative flex aspect-video items-center justify-center bg-gradient-to-br from-primary-100 to-primary-300 text-primary-800 dark:from-primary-900 dark:to-secondary-hover dark:text-primary-100"
    >
      <span class="text-lg font-bold">{{ project.title }}</span>
    </div>

    <div class="flex flex-1 flex-col gap-3 p-4">
      <div class="flex items-start justify-between gap-2">
        <h3 class="line-clamp-1 font-semibold text-neutral-900 dark:text-white">
          {{ project.title }}
        </h3>
      </div>
      <p class="line-clamp-2 text-sm text-neutral-600 dark:text-neutral-300">
        {{ project.description }}
      </p>

      <div
        class="mt-auto flex items-center justify-between pt-2 text-xs text-neutral-500 dark:text-neutral-400"
      >
        <span
          class="rounded-full px-2 py-1 font-medium"
          :class="categoryBadgeClass[project.category]"
        >
          {{ categoryLabel[project.category] ?? '기타' }}
        </span>
        <span class="flex items-center gap-3">
          <span>🔖 {{ project.stats.unique_favorites.toLocaleString() }}</span>
          <span>💬 {{ project.stats.unique_commenters }}</span>
          <span>👁 {{ project.stats.unique_visitors.toLocaleString() }}</span>
        </span>
      </div>
    </div>
  </RouterLink>
</template>
