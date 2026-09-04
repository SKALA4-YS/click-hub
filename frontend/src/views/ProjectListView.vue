<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import CategoryTabs from '@/components/layout/CategoryTabs.vue'
import SiteCard from '@/components/card/SiteCard.vue'
import { toSiteCardProject } from '@/api/adapters/projects'
import { getCategories } from '@/api/catalog'
import { getProjectRankings } from '@/api/rankings'
import { searchProjects } from '@/api/search'

const route = useRoute()

const SORT_OPTIONS = ['popular', 'latest', 'recommended']

const selectedCategory = ref(route.query.category ?? null)
const searchQuery = ref(route.query.q ?? '')
const sortOption = ref(SORT_OPTIONS.includes(route.query.sort) ? route.query.sort : 'popular')
const viewMode = ref('grid')
const categories = ref([{ slug: null, label: '전체' }])
const projects = ref([])
const rankings = ref([])
const nextCursor = ref(null)
const hasNext = ref(false)
const isLoading = ref(true)
const errorMessage = ref('')

const pageTitle = computed(() => {
  if (searchQuery.value.trim()) return `'${searchQuery.value.trim()}' 검색 결과`
  if (sortOption.value === 'recommended') return '맞춤 추천 전체보기'
  if (sortOption.value === 'latest') return '최신 프로젝트 전체보기'
  return 'Top 100 사이트 전체보기'
})

const visibleProjects = computed(() => {
  const items = [...projects.value]
  if (sortOption.value === 'latest') {
    return items.sort((a, b) => (b.published_at ?? '').localeCompare(a.published_at ?? ''))
  }
  const rankById = new Map(rankings.value.map((item) => [item.projectId, item.rank]))
  return items.sort(
    (a, b) =>
      (rankById.get(a.id) ?? Number.MAX_SAFE_INTEGER) -
      (rankById.get(b.id) ?? Number.MAX_SAFE_INTEGER),
  )
})

async function loadProjects({ append = false } = {}) {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const result = await searchProjects({
      q: searchQuery.value.trim(),
      category: selectedCategory.value,
      cursor: append ? nextCursor.value : undefined,
    })
    const items = result.items.map(toSiteCardProject)
    projects.value = append ? [...projects.value, ...items] : items
    nextCursor.value = result.nextCursor
    hasNext.value = result.hasNext
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

watch([selectedCategory, searchQuery], () => loadProjects())
watch(
  () => route.query.q,
  (q) => {
    searchQuery.value = q ?? ''
  },
)

onMounted(async () => {
  const [catalogResult, rankingResult] = await Promise.allSettled([
    getCategories(),
    getProjectRankings(),
  ])
  if (catalogResult.status === 'fulfilled') {
    categories.value = [
      { slug: null, label: '전체' },
      ...catalogResult.value.map((category) => ({ slug: category.slug, label: category.name })),
    ]
  }
  if (rankingResult.status === 'fulfilled') rankings.value = rankingResult.value
  await loadProjects()
})
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
            {{ projects.length }}개 프로젝트
          </span>
          <div
            class="flex items-center rounded-md border border-divider/20 p-0.5"
            role="group"
            aria-label="프로젝트 보기 방식"
          >
            <button
              type="button"
              aria-label="목록 보기"
              :aria-pressed="viewMode === 'list'"
              class="rounded px-2 py-1 text-xs"
              :class="
                viewMode === 'list'
                  ? 'bg-primary-600 text-white'
                  : 'text-body-light dark:text-body-dark'
              "
              @click="viewMode = 'list'"
            >
              목록
            </button>
            <button
              type="button"
              aria-label="그리드 보기"
              :aria-pressed="viewMode === 'grid'"
              class="rounded px-2 py-1 text-xs"
              :class="
                viewMode === 'grid'
                  ? 'bg-primary-600 text-white'
                  : 'text-body-light dark:text-body-dark'
              "
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
      v-if="!errorMessage"
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

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light dark:text-body-dark">
      프로젝트를 불러오는 중입니다.
    </p>

    <div v-else-if="errorMessage" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button
        type="button"
        class="mt-4 text-sm font-semibold text-primary-600"
        @click="loadProjects()"
      >
        다시 시도
      </button>
    </div>

    <p
      v-else-if="visibleProjects.length === 0"
      class="py-16 text-center text-sm text-body-light dark:text-body-dark"
    >
      조건에 맞는 프로젝트가 없습니다.
    </p>

    <button
      v-if="hasNext && !isLoading"
      type="button"
      class="mx-auto rounded-lg border border-primary-200 px-5 py-2 text-sm font-semibold text-primary-700"
      @click="loadProjects({ append: true })"
    >
      더 보기
    </button>
  </div>
</template>
