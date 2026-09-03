<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import CategoryTabs from '@/components/layout/CategoryTabs.vue'
import SiteCard from '@/components/card/SiteCard.vue'
import { mockProjectList } from '@/data/mockProjectList'

// 전체 사이트 목록 — GET /api/v1/projects?category=&tags=&tech=&pricing=&cursor= 자리.
// 실제 API는 커서 기반 페이지네이션이라, 번호 페이지 대신 "더 불러오기" 방식으로 맞춘다.
const route = useRoute()

const categories = [
  { slug: null, label: '전체' },
  { slug: 'developer-tools', label: '개발도구' },
  { slug: 'design-creative', label: '디자인' },
  { slug: 'content-entertainment', label: '엔터테인먼트' },
  { slug: 'ai-service', label: 'AI' },
  { slug: 'productivity-work', label: '생산성' },
  { slug: 'other', label: '기타' },
]

const selectedCategory = ref(route.query.category ?? null)
const sortOption = ref(route.query.sort === 'recommended' ? 'recommended' : 'popular')
const PAGE_SIZE = 8
const visibleCount = ref(PAGE_SIZE)

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

const visibleProjects = computed(() => filteredProjects.value.slice(0, visibleCount.value))
const hasMore = computed(() => visibleCount.value < filteredProjects.value.length)

watch([selectedCategory, sortOption], () => {
  visibleCount.value = PAGE_SIZE
})

function loadMore() {
  visibleCount.value += PAGE_SIZE
}
</script>

<template>
  <div class="flex flex-col gap-6">
    <nav class="text-sm text-body-light dark:text-body-dark">
      <RouterLink to="/" class="hover:text-primary-600">홈</RouterLink>
      <span class="mx-1">/</span>
      <span>Top 100</span>
    </nav>

    <div>
      <h1 class="font-headline text-2xl font-bold text-heading-light dark:text-heading-dark">{{ pageTitle }}</h1>
      <p class="mt-1 text-sm text-body-light dark:text-body-dark">개발자들의 인기 프로덕트를 한눈에 탐색해보세요.</p>
    </div>

    <div class="flex flex-wrap items-center justify-between gap-4">
      <CategoryTabs v-model="selectedCategory" :categories="categories" />

      <div class="flex items-center gap-3">
        <span class="text-sm text-body-light dark:text-body-dark">{{ filteredProjects.length }}개 프로젝트</span>
        <select
          v-model="sortOption"
          class="rounded-lg border border-divider/20 bg-surface-light-1 px-3 py-1.5 text-sm outline-none dark:border-divider/30 dark:bg-surface-dark-1"
        >
          <option value="popular">인기순 (Top 100)</option>
          <option value="latest">최신순</option>
        </select>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
      <SiteCard
        v-for="(item, index) in visibleProjects"
        :key="item.id"
        :project="item"
        :rank="sortOption === 'popular' ? index + 1 : null"
      />
    </div>

    <p v-if="visibleProjects.length === 0" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      조건에 맞는 프로젝트가 없습니다.
    </p>

    <button
      v-if="hasMore"
      type="button"
      class="mx-auto rounded-full border border-divider/30 px-6 py-2.5 text-sm font-medium text-body-light hover:border-primary-400 hover:text-primary-600 dark:text-body-dark"
      @click="loadMore"
    >
      더 불러오기
    </button>
  </div>
</template>
