<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories } from '@/api/catalog'
import { searchProjects } from '@/api/search'
import clearIcon from '@/assets/figma/clear.svg'
import diagonalArrowIcon from '@/assets/figma/diagonal-arrow.svg'
import filterChevronIcon from '@/assets/figma/filter-chevron.svg'
import headerSearchIcon from '@/assets/figma/header-search.svg'
import helpIcon from '@/assets/figma/help.svg'
import searchIcon from '@/assets/figma/search.svg'
import slidersIcon from '@/assets/figma/sliders.svg'
import suggestionSearchIcon from '@/assets/figma/suggestion-search.svg'

const router = useRouter()
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
const suggestions = ref([])
const searchError = ref('')
let searchTimer

const categoryOptions = ref([])

async function loadSuggestions() {
  const searchTerm = query.value.trim()
  if (!autocompleteEnabled.value || !searchTerm) {
    suggestions.value = []
    searchError.value = ''
    return
  }
  try {
    const page = await searchProjects({
      q: searchTerm,
      category: categoryFilter.value || undefined,
    })
    suggestions.value = page.items.slice(0, 4)
    searchError.value = ''
  } catch (error) {
    suggestions.value = []
    searchError.value = error.message
  }
}

watch([query, categoryFilter, autocompleteEnabled], () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => void loadSuggestions(), 200)
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

function handleEndControl() {
  if (query.value) {
    clearQuery()
    return
  }
  close('button')
}

function goToSearchResults() {
  const searchTerm = query.value.trim()
  if (!searchTerm) return
  close('button')
  router.push({
    path: '/rankings',
    query: { q: searchTerm, category: categoryFilter.value || undefined },
  })
}

function selectSuggestion(item) {
  close('selection')
  router.push(`/projects/${item.id}`)
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

  if (event.key === 'Enter') {
    event.preventDefault()
    if (activeIndex.value >= 0) {
      const selectedSuggestion = suggestions.value[activeIndex.value]
      if (selectedSuggestion) selectSuggestion(selectedSuggestion)
      return
    }
    goToSearchResults()
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

onMounted(async () => {
  document.addEventListener('click', handleOutsideClick)
  try {
    categoryOptions.value = await getCategories()
  } catch {
    categoryOptions.value = []
  }
})
onBeforeUnmount(() => {
  clearTimeout(searchTimer)
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<template>
  <div ref="rootEl" class="relative w-full">
    <div
      :role="isOpen ? 'dialog' : undefined"
      :aria-label="isOpen ? '검색 제안' : undefined"
      class="flex items-center gap-2 border border-divider/20 focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-100 dark:border-divider/30"
      :class="
        isOpen
          ? 'absolute left-0 top-0 z-30 w-full flex-col overflow-hidden rounded-[17px] bg-surface-light-1 p-0 shadow-lg lg:h-[227.9px] lg:w-[503px] dark:border-divider/25 dark:bg-surface-dark-1'
          : 'rounded-full bg-neutral-50 px-4 py-2 dark:bg-surface-dark-1'
      "
    >
      <div class="flex w-full min-w-0 items-center gap-2" :class="isOpen && 'px-4 py-3'">
        <img
          :src="isOpen ? searchIcon : headerSearchIcon"
          alt=""
          class="shrink-0"
          :class="isOpen ? 'h-[19.787px] w-[20.828px]' : 'h-4 w-4'"
        />
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
          v-if="query || isOpen"
          type="button"
          :aria-label="query ? '검색어 지우기' : '검색 닫기'"
          class="text-body-light hover:text-primary-600 dark:text-body-dark"
          @click="handleEndControl"
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
                @click="selectSuggestion(item)"
              >
                <span class="flex min-w-0 items-center gap-2">
                  <img
                    :src="suggestionSearchIcon"
                    alt=""
                    class="suggestion-search-icon h-4 w-4 shrink-0"
                  />
                  <span class="truncate text-heading-light dark:text-heading-dark"
                    ><span>{{ suggestionParts(item.title).prefix }}</span
                    ><strong class="font-bold text-primary-600">{{
                      suggestionParts(item.title).match
                    }}</strong
                    ><span>{{ suggestionParts(item.title).suffix }}</span></span
                  >
                </span>
                <img
                  :src="diagonalArrowIcon"
                  alt=""
                  class="suggestion-arrow-icon h-3 w-3 shrink-0"
                />
              </button>
            </li>
          </ul>
          <p v-if="searchError" role="alert" class="px-4 py-3 text-sm text-danger">
            {{ searchError }}
          </p>
          <p
            v-else-if="suggestions.length === 0 && query.trim()"
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
          <button
            v-if="query.trim()"
            type="button"
            class="w-full border-t border-divider/15 px-4 py-3 text-left text-sm font-semibold text-primary-600 hover:bg-neutral-100 dark:border-divider/25 dark:hover:bg-surface-dark-2"
            @click="goToSearchResults"
          >
            '{{ query.trim() }}' 전체 검색결과 보기
          </button>
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
          <div v-if="showAdvanced" class="grid gap-2 pt-2">
            <select
              v-model="categoryFilter"
              class="rounded-lg border border-divider/20 bg-surface-light-1 px-2 py-1.5 text-sm dark:border-divider/30 dark:bg-surface-dark-2"
            >
              <option value="">카테고리 전체</option>
              <option v-for="c in categoryOptions" :key="c.slug" :value="c.slug">
                {{ c.name }}
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
        </div>
      </template>
    </div>
  </div>
</template>
