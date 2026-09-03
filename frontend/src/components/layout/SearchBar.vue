<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { mockProjectList } from '@/data/mockProjectList'
import clearIcon from '@/assets/figma/clear.svg'
import diagonalArrowIcon from '@/assets/figma/diagonal-arrow.svg'
import filterChevronIcon from '@/assets/figma/filter-chevron.svg'
import headerSearchIcon from '@/assets/figma/header-search.svg'
import helpIcon from '@/assets/figma/help.svg'
import searchIcon from '@/assets/figma/search.svg'
import slidersIcon from '@/assets/figma/sliders.svg'
import suggestionSearchIcon from '@/assets/figma/suggestion-search.svg'

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
const restoringFocus = ref(false)
const activeIndex = ref(-1)

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

watch(suggestions, (items) => {
  if (activeIndex.value >= items.length) activeIndex.value = -1
})

function open() {
  if (restoringFocus.value) return
  isOpen.value = true
}

function clearQuery() {
  query.value = ''
  inputEl.value?.focus()
}

function applySuggestion(title) {
  query.value = title
  close('selection')
}

function close(reason) {
  isOpen.value = false
  showAdvanced.value = false
  activeIndex.value = -1
  if (reason !== 'escape' && reason !== 'button') return
  nextTick(() => {
    restoringFocus.value = true
    inputEl.value?.focus()
    restoringFocus.value = false
  })
}

function handleOutsideClick(event) {
  if (rootEl.value && !rootEl.value.contains(event.target)) close('outside')
}
function handleKeydown(event) {
  if (event.key === 'Escape') {
    event.preventDefault()
    close('escape')
    return
  }

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    open()
    activeIndex.value = Math.min(activeIndex.value + 1, suggestions.value.length - 1)
    return
  }

  if (event.key === 'ArrowUp') {
    event.preventDefault()
    activeIndex.value = Math.max(activeIndex.value - 1, -1)
    return
  }

  if (event.key === 'Enter' && activeIndex.value >= 0) {
    event.preventDefault()
    const selectedSuggestion = suggestions.value[activeIndex.value]
    if (selectedSuggestion) applySuggestion(selectedSuggestion.title)
  }
}

function suggestionParts(title) {
  const searchTerm = query.value.trim()
  const index = title.toLocaleLowerCase().indexOf(searchTerm.toLocaleLowerCase())
  if (index < 0 || !searchTerm) return { prefix: '', match: '', suffix: title }
  return {
    prefix: title.slice(0, index),
    match: title.slice(index, index + searchTerm.length),
    suffix: title.slice(index + searchTerm.length),
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <div ref="rootEl" class="relative w-full">
    <div
      :role="isOpen ? 'dialog' : undefined"
      :aria-label="isOpen ? '검색 제안' : undefined"
      class="flex items-center gap-2 rounded-full border border-divider/20 bg-neutral-50 px-4 py-2 focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-100 dark:border-divider/30 dark:bg-surface-dark-1"
      :class="
        isOpen &&
        'absolute left-0 top-0 z-30 w-full flex-col overflow-hidden rounded-2xl bg-surface-light-1 p-0 shadow-lg dark:border-divider/25 dark:bg-surface-dark-1'
      "
    >
      <div class="flex w-full min-w-0 items-center gap-2" :class="isOpen && 'px-4 py-3'">
        <img :src="isOpen ? searchIcon : headerSearchIcon" alt="" class="h-4 w-4 shrink-0" />
        <input
          ref="inputEl"
          v-model="query"
          type="search"
          role="combobox"
          aria-label="통합 검색"
          :aria-expanded="isOpen"
          aria-controls="search-suggestions"
          aria-autocomplete="list"
          :aria-activedescendant="activeIndex >= 0 ? `search-option-${activeIndex}` : undefined"
          placeholder="검색어를 입력하세요..."
          class="w-full bg-transparent text-sm outline-none"
          @focus="open"
          @input="open"
          @keydown="handleKeydown"
        />
        <button
          v-if="query"
          type="button"
          aria-label="검색어 지우기"
          class="text-body-light hover:text-primary-600 dark:text-body-dark"
          @click="clearQuery"
        >
          <img :src="clearIcon" alt="" class="h-3 w-3" />
        </button>
      </div>

      <template v-if="isOpen">
        <div
          class="max-h-72 w-full overflow-y-auto border-t border-divider/15 py-2 dark:border-divider/25"
        >
          <ul id="search-suggestions" role="listbox" aria-label="검색 제안" class="flex flex-col">
            <li v-for="(item, index) in suggestions" :key="item.id">
              <button
                :id="`search-option-${index}`"
                type="button"
                role="option"
                :aria-label="item.title"
                :aria-selected="activeIndex === index"
                class="flex w-full items-center justify-between px-4 py-2 text-left text-sm hover:bg-neutral-100 dark:hover:bg-surface-dark-2"
                @click="applySuggestion(item.title)"
              >
                <span class="flex min-w-0 items-center gap-2">
                  <img :src="suggestionSearchIcon" alt="" class="h-4 w-4 shrink-0" />
                  <span class="truncate text-heading-light dark:text-heading-dark"
                    ><span>{{ suggestionParts(item.title).prefix }}</span
                    ><strong class="font-bold text-primary-600">{{
                      suggestionParts(item.title).match
                    }}</strong
                    ><span>{{ suggestionParts(item.title).suffix }}</span></span
                  >
                </span>
                <img :src="diagonalArrowIcon" alt="" class="h-3 w-3 shrink-0" />
              </button>
            </li>
          </ul>
          <p
            v-if="suggestions.length === 0 && query.trim()"
            class="px-4 py-3 text-sm text-body-light dark:text-body-dark"
          >
            일치하는 프로젝트가 없어요.
          </p>
          <p
            v-if="suggestions.length === 0 && !query.trim()"
            class="px-4 py-3 text-sm text-body-light dark:text-body-dark"
          >
            검색어를 입력하세요.
          </p>
        </div>

        <div
          class="border-y border-divider/15 bg-neutral-50 px-3 py-2 dark:border-divider/25 dark:bg-surface-dark-2"
        >
          <div class="flex items-center justify-between gap-2">
            <button
              type="button"
              class="flex items-center gap-1 text-xs font-semibold text-heading-light dark:text-heading-dark"
              @click="showAdvanced = !showAdvanced"
            >
              상세 필터 조건
              <img
                :src="filterChevronIcon"
                alt=""
                class="h-3 w-3 transition-transform"
                :class="showAdvanced && 'rotate-180'"
              />
            </button>
            <span class="flex items-center gap-1 text-xs text-body-light dark:text-body-dark"
              ><img :src="slidersIcon" alt="" class="h-3 w-3" />원하는 조건이 더 있으신가요?</span
            >
          </div>
          <div v-if="showAdvanced" class="grid grid-cols-2 gap-2 pt-2">
            <select
              v-model="categoryFilter"
              class="rounded-lg border border-divider/20 bg-surface-light-1 px-2 py-1.5 text-sm dark:border-divider/30 dark:bg-surface-dark-2"
            >
              <option value="">카테고리 전체</option>
              <option v-for="c in categoryOptions" :key="c.slug" :value="c.slug">
                {{ c.label }}
              </option>
            </select>
            <select
              v-model="pricingFilter"
              class="rounded-lg border border-divider/20 bg-surface-light-1 px-2 py-1.5 text-sm dark:border-divider/30 dark:bg-surface-dark-2"
            >
              <option value="">가격 전체</option>
              <option v-for="p in pricingOptions" :key="p.value" :value="p.value">
                {{ p.label }}
              </option>
            </select>
          </div>
        </div>

        <div
          class="flex flex-wrap items-center justify-between gap-2 border-t border-divider/15 px-3 py-2 text-xs text-body-light dark:border-divider/25 dark:text-body-dark"
        >
          <div class="flex flex-wrap items-center gap-x-2 gap-y-1">
            <button
              type="button"
              class="hover:text-primary-600"
              @click="recentSearchEnabled = !recentSearchEnabled"
            >
              최근검색어 {{ recentSearchEnabled ? '끄기' : '켜기' }}
            </button>
            <span aria-hidden="true" class="text-divider/30">|</span>
            <button
              type="button"
              class="hover:text-primary-600"
              @click="autocompleteEnabled = !autocompleteEnabled"
            >
              자동완성 {{ autocompleteEnabled ? '끄기' : '켜기' }}
            </button>
            <span aria-hidden="true" class="text-divider/30">|</span>
            <a href="/tutorials" class="inline-flex items-center gap-1 hover:text-primary-600"
              >도움말 <img :src="helpIcon" alt="" class="h-3 w-3"
            /></a>
          </div>
          <button
            type="button"
            aria-label="검색 닫기"
            class="inline-flex items-center gap-1 rounded-md px-1 py-1 hover:text-primary-600"
            @click="close('button')"
          >
            닫기
            <kbd
              class="rounded border border-divider/20 bg-neutral-50 px-1 py-0.5 font-mono text-[10px] text-body-light dark:border-divider/30 dark:bg-surface-dark-2 dark:text-body-dark"
              >ESC</kbd
            >
          </button>
        </div>
      </template>
    </div>
  </div>
</template>
