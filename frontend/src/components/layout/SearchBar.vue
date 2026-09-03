<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { mockProjectList } from '@/data/mockProjectList'

// GET /api/v1/search?q=&category=&tags=&tech=&pricing= 자리.
// 자동완성 결과를 내려주는 별도 API는 명세에 없어서, 지금은 제목·카테고리 기준으로
// 화면에서 직접 후보를 뽑는다. 상세 필터 항목은 검색 API의 쿼리 파라미터에 맞춰 구성했다.
const query = ref('')
const isOpen = ref(false)
const showAdvanced = ref(false)
const recentSearchEnabled = ref(true)
const autocompleteEnabled = ref(true)
const rootEl = ref(null)
const inputEl = ref(null)

const categoryFilter = ref('')
const pricingFilter = ref('')

const categoryOptions = [
  { slug: 'developer-tools', label: '개발자 도구' },
  { slug: 'design-creative', label: '디자인/크리에이티브' },
  { slug: 'content-entertainment', label: '콘텐츠/엔터테인먼트' },
  { slug: 'ai-service', label: 'AI 서비스' },
  { slug: 'productivity-work', label: '생산성/업무' },
  { slug: 'other', label: '기타' },
]
const pricingOptions = [
  { value: 'FREE', label: '무료' },
  { value: 'FREEMIUM', label: '프리미엄' },
  { value: 'PAID', label: '유료' },
]

const suggestions = computed(() => {
  if (!autocompleteEnabled.value) return []
  const q = query.value.trim().toLowerCase()
  if (!q) return []
  return mockProjectList
    .filter((p) => p.title.toLowerCase().includes(q) || p.category.toLowerCase().includes(q))
    .slice(0, 4)
})

function handleInput() {
  isOpen.value = true
}

function clearQuery() {
  query.value = ''
  inputEl.value?.focus()
}

function applySuggestion(title) {
  query.value = title
  // TODO: 검색 결과 페이지 연동 전이라 지금은 입력창만 채운다.
}

function close() {
  isOpen.value = false
  showAdvanced.value = false
}

function handleOutsideClick(event) {
  if (rootEl.value && !rootEl.value.contains(event.target)) close()
}
function handleKeydown(event) {
  if (event.key === 'Escape') close()
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  document.addEventListener('keydown', handleKeydown)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<template>
  <div ref="rootEl" class="relative w-full">
    <div
      class="flex items-center gap-2 rounded-full border border-divider/20 bg-neutral-50 px-4 py-2 focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-100 dark:border-divider/30 dark:bg-surface-dark-1"
    >
      <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" class="h-4 w-4 shrink-0 text-body-light dark:text-body-dark">
        <circle cx="9" cy="9" r="6" stroke-width="1.5" />
        <path d="M17 17l-4-4" stroke-width="1.5" stroke-linecap="round" />
      </svg>
      <input
        ref="inputEl"
        v-model="query"
        type="search"
        placeholder="프로젝트명, 기술 스택, 키워드로 검색..."
        class="w-full bg-transparent text-sm outline-none"
        @focus="isOpen = true"
        @input="handleInput"
      />
      <button v-if="query" type="button" class="text-body-light hover:text-primary-600 dark:text-body-dark" @click="clearQuery">
        <svg viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4"><path d="M6 6l8 8M14 6l-8 8" stroke="currentColor" stroke-width="1.5" /></svg>
      </button>
    </div>

    <div
      v-if="isOpen"
      role="dialog"
      aria-label="검색 제안"
      class="absolute left-0 top-full z-30 mt-2 w-full overflow-hidden rounded-2xl border border-divider/15 bg-surface-light-1 shadow-lg dark:border-divider/25 dark:bg-surface-dark-1 sm:min-w-[420px]"
    >
      <div class="max-h-72 overflow-y-auto p-3">
      <ul v-if="suggestions.length > 0" class="flex flex-col">
        <li v-for="item in suggestions" :key="item.id">
          <button
            type="button"
            class="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-left text-sm hover:bg-neutral-100 dark:hover:bg-surface-dark-2"
            @click="applySuggestion(item.title)"
          >
            <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" class="h-4 w-4 shrink-0 text-body-light dark:text-body-dark">
              <circle cx="9" cy="9" r="6" stroke-width="1.5" />
              <path d="M17 17l-4-4" stroke-width="1.5" stroke-linecap="round" />
            </svg>
            <span class="flex-1 truncate text-heading-light dark:text-heading-dark">{{ item.title }}</span>
            <span class="shrink-0 rounded bg-neutral-100 px-1.5 py-0.5 text-[11px] text-body-light dark:bg-surface-dark-2 dark:text-body-dark">{{ item.category }}</span>
          </button>
        </li>
      </ul>
      <p v-else-if="query.trim()" class="px-2 py-3 text-sm text-body-light dark:text-body-dark">일치하는 프로젝트가 없어요.</p>
      <p v-else class="px-2 py-3 text-sm text-body-light dark:text-body-dark">프로젝트명, 카테고리, 기술 스택으로 검색해보세요.</p>

      <div class="mt-1 border-t border-divider/15 pt-2 dark:border-divider/25">
        <button
          type="button"
          class="flex w-full items-center justify-between px-2 py-2 text-sm font-medium text-heading-light hover:text-primary-600 dark:text-heading-dark"
          @click="showAdvanced = !showAdvanced"
        >
          상세 필터 조건
          <svg viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4 transition-transform" :class="showAdvanced && 'rotate-180'">
            <path d="M5.25 7.5l4.75 5 4.75-5" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>

        <div v-if="showAdvanced" class="grid grid-cols-2 gap-2 px-2 pb-2 pt-1">
          <select v-model="categoryFilter" class="rounded-lg border border-divider/20 bg-surface-light-1 px-2 py-1.5 text-sm dark:border-divider/30 dark:bg-surface-dark-2">
            <option value="">카테고리 전체</option>
            <option v-for="c in categoryOptions" :key="c.slug" :value="c.slug">{{ c.label }}</option>
          </select>
          <select v-model="pricingFilter" class="rounded-lg border border-divider/20 bg-surface-light-1 px-2 py-1.5 text-sm dark:border-divider/30 dark:bg-surface-dark-2">
            <option value="">가격 전체</option>
            <option v-for="p in pricingOptions" :key="p.value" :value="p.value">{{ p.label }}</option>
          </select>
        </div>
      </div>
      </div>

      <div class="flex flex-wrap items-center justify-between gap-2 border-t border-divider/15 px-3 py-2 text-xs text-body-light dark:border-divider/25 dark:text-body-dark">
        <div class="flex flex-wrap items-center gap-x-3 gap-y-1">
          <button type="button" class="hover:text-primary-600" @click="recentSearchEnabled = !recentSearchEnabled">
            최근검색어 {{ recentSearchEnabled ? '끄기' : '켜기' }}
          </button>
          <button type="button" class="hover:text-primary-600" @click="autocompleteEnabled = !autocompleteEnabled">
            자동완성 {{ autocompleteEnabled ? '끄기' : '켜기' }}
          </button>
        </div>
        <button type="button" aria-label="검색 닫기" class="rounded-md border border-divider/20 px-2 py-1 hover:text-primary-600 dark:border-divider/30" @click="close">
          닫기 ESC
        </button>
      </div>
    </div>
  </div>
</template>
