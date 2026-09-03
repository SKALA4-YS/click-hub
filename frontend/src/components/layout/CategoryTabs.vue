<script setup>
// 카테고리 필터 탭. 기본값은 홈 화면 기준 자주 쓰이는 6개만 노출하고,
// 전체 목록 페이지처럼 더 많은 카테고리가 필요하면 categories prop으로 넘긴다.
// slug는 schema.sql seed 데이터와 동일해야 GET /api/v1/feed, /api/v1/projects 요청에 그대로 쓸 수 있다.
const DEFAULT_CATEGORIES = [
  { slug: null, label: '전체' },
  { slug: 'developer-tools', label: '개발도구' },
  { slug: 'design-creative', label: '디자인' },
  { slug: 'content-entertainment', label: '엔터테인먼트' },
  { slug: 'ai-service', label: 'AI' },
  { slug: 'other', label: '기타' },
]

const props = defineProps({
  categories: { type: Array, default: null },
})
const categories = props.categories ?? DEFAULT_CATEGORIES

const modelValue = defineModel({ default: null })
</script>

<template>
  <div class="flex gap-2 overflow-x-auto pb-1" role="group" aria-label="프로젝트 카테고리">
    <button
      v-for="category in categories"
      :key="category.slug ?? 'all'"
      type="button"
      :aria-label="`${category.label} 카테고리`"
      :aria-pressed="modelValue === category.slug"
      class="shrink-0 rounded-full px-4 py-2 text-sm font-semibold transition-colors"
      :class="
        modelValue === category.slug
          ? 'bg-primary-600 text-white'
          : 'border border-divider/20 bg-surface-light-1 text-body-light hover:border-primary-300 hover:text-primary-600 dark:border-divider/30 dark:bg-surface-dark-1 dark:text-body-dark'
      "
      @click="modelValue = category.slug"
    >
      {{ category.label }}
    </button>
  </div>
</template>
