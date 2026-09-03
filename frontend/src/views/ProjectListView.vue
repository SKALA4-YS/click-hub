<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import CategoryTabs from '@/components/layout/CategoryTabs.vue'
import SiteCard from '@/components/card/SiteCard.vue'
import { mockProjectList } from '@/data/mockProjectList'

const route = useRoute()

const categories = [
  { slug: null, label: '전체' },
  { slug: 'developer-tools', label: '개발도구' },
  { slug: 'design-creative', label: '디자인' },
  { slug: 'content-entertainment', label: '엔터테인먼트' },
  { slug: 'ai-service', label: 'AI' },
  { slug: 'productivity-work', label: '생산성' },
  { slug: 'marketing', label: '마케팅' },
  { slug: 'other', label: '기타' },
]

const selectedCategory = ref(route.query.category ?? null)
const sortOption = ref(route.query.sort === 'recommended' ? 'recommended' : 'popular')
const viewMode = ref('grid')
const PAGE_SIZE = 8
const TOTAL_COUNT = 148
const TOTAL_PAGES = 12
const currentPage = ref(1)
const displayedPages = [1, 2, 3, 4, 'ellipsis', 12]

const pageTitle = computed(() =>
  sortOption.value === 'recommended' ? '맞춤 추천 전체보기' : 'Top 100 사이트 전체보기',
)

const filteredProjects = computed(() => {
  let items = mockProjectList
  if (selectedCategory.value) {
    items = items.filter((item) => item.category_slug === selectedCategory.value)
  }
  items = [...items]
  if (sortOption.value === 'popular' || sortOption.value === 'recommended') {
    items.sort((a, b) => b.stats.likes - a.stats.likes)
  } else {
    items.reverse()
  }
  return items
})

const visibleProjects = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE
  return filteredProjects.value.slice(start, start + PAGE_SIZE)
})
const projectCount = computed(() =>
  selectedCategory.value ? filteredProjects.value.length : TOTAL_COUNT,
)

watch([selectedCategory, sortOption], () => {
  currentPage.value = 1
})

function goToPage(page) {
  currentPage.value = Math.min(Math.max(page, 1), TOTAL_PAGES)
}
</script>

<template>
  <div class="flex flex-col gap-5 pb-10">
    <nav
      class="flex items-center gap-1 text-xs text-body-light dark:text-body-dark"
      aria-label="현재 위치"
    >
      <RouterLink to="/" class="hover:text-primary-600">홈</RouterLink>
      <span aria-hidden="true">›</span>
      <span>Top 100</span>
    </nav>

    <div>
      <h1
        class="font-headline text-[26px] font-extrabold tracking-[-0.04em] text-heading-light dark:text-heading-dark"
      >
        {{ pageTitle }}
      </h1>
      <p class="mt-1.5 text-sm text-body-light dark:text-body-dark">
        개발자들의 인기 프로덕트를 한눈에 탐색해보세요.
      </p>
    </div>

    <div class="border-y border-divider/10 py-4 dark:border-divider/20">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <CategoryTabs v-model="selectedCategory" :categories="categories" />

        <div class="flex items-center gap-2">
          <span aria-label="프로젝트 수" class="text-xs text-body-light dark:text-body-dark">
            {{ projectCount }}개 프로젝트
          </span>
          <div class="flex items-center rounded-md border border-divider/20 p-0.5" role="group" aria-label="프로젝트 보기 방식">
            <button
              type="button"
              aria-label="목록 보기"
              :aria-pressed="viewMode === 'list'"
              class="rounded px-2 py-1 text-xs"
              :class="viewMode === 'list' ? 'bg-primary-600 text-white' : 'text-body-light'"
              @click="viewMode = 'list'"
            >
              목록
            </button>
            <button
              type="button"
              aria-label="그리드 보기"
              :aria-pressed="viewMode === 'grid'"
              class="rounded px-2 py-1 text-xs"
              :class="viewMode === 'grid' ? 'bg-primary-600 text-white' : 'text-body-light'"
              @click="viewMode = 'grid'"
            >
              그리드
            </button>
          </div>
          <select
            v-model="sortOption"
            aria-label="프로젝트 정렬"
            class="rounded-md border border-divider/20 bg-surface-light-1 px-3 py-1.5 text-xs font-medium text-body-light outline-none focus:border-primary-500 dark:border-divider/30 dark:bg-surface-dark-1 dark:text-body-dark"
          >
            <option value="popular">인기순 (Top 100)</option>
            <option value="latest">최신순</option>
          </select>
        </div>
      </div>
    </div>

    <div
      class="project-grid grid gap-6"
      :class="viewMode === 'grid' ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4' : 'grid-cols-1'"
    >
      <SiteCard
        v-for="(item, index) in visibleProjects"
        :key="item.id"
        :project="item"
        :rank="sortOption === 'popular' ? index + 1 : null"
      />
    </div>

    <p
      v-if="visibleProjects.length === 0"
      class="py-16 text-center text-sm text-body-light dark:text-body-dark"
    >
      조건에 맞는 프로젝트가 없습니다.
    </p>

    <nav
      v-if="filteredProjects.length > 0"
      class="mx-auto flex items-center gap-1"
      aria-label="프로젝트 페이지"
      data-testid="pagination"
    >
      <button
        type="button"
        class="rounded px-2 py-1 text-xs text-body-light disabled:opacity-40 dark:text-body-dark"
        :disabled="currentPage === 1"
        @click="goToPage(currentPage - 1)"
      >
        이전
      </button>
      <template v-for="page in displayedPages" :key="page">
        <span v-if="page === 'ellipsis'" class="px-1 text-xs text-body-light" aria-hidden="true">…</span>
        <button
          v-else
        type="button"
        :aria-label="`${page}페이지`"
        class="h-7 min-w-7 rounded px-2 text-xs font-semibold"
        :class="
          page === currentPage
            ? 'bg-primary-600 text-white'
            : 'border border-divider/15 text-body-light dark:border-divider/30 dark:text-body-dark'
        "
        :aria-current="page === currentPage ? 'page' : undefined"
        @click="goToPage(page)"
      >
        {{ page }}
      </button>
      </template>
      <button
        type="button"
        class="rounded px-2 py-1 text-xs text-body-light disabled:opacity-40 dark:text-body-dark"
        :disabled="currentPage === TOTAL_PAGES"
        @click="goToPage(currentPage + 1)"
      >
        다음
      </button>
    </nav>
  </div>
</template>
