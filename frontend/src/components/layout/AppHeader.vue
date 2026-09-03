<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/layout/NotificationBell.vue'
import UserMenu from '@/components/layout/UserMenu.vue'
import SearchDropdown from '@/components/layout/SearchDropdown.vue'

const { mode, cycleMode } = useTheme()
const auth = useAuthStore()

const themeIcon = { light: '☀️', dark: '🌙', system: '💻' }

const searchQuery = ref('')
const isSearchOpen = ref(false)
const searchRootEl = ref(null)

function handleOutsideClick(event) {
  if (searchRootEl.value && !searchRootEl.value.contains(event.target)) {
    isSearchOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleOutsideClick))
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))
</script>

<template>
  <header
    class="sticky top-0 z-10 border-b border-neutral-200 bg-white/90 backdrop-blur dark:border-white/10 dark:bg-secondary/95"
  >
    <div class="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3">
      <RouterLink
        to="/"
        class="font-headline shrink-0 text-xl font-extrabold tracking-tight text-primary-600 dark:text-white"
      >
        Click-Hub
      </RouterLink>

      <div ref="searchRootEl" class="relative flex-1">
        <input
          v-model="searchQuery"
          type="search"
          placeholder="프로젝트명, 기술 스택, 키워드로 검색..."
          class="w-full rounded-full border border-neutral-200 bg-neutral-50 px-4 py-2 text-sm outline-none focus:border-primary-400 focus:ring-2 focus:ring-primary-100 dark:border-neutral-700 dark:bg-white/5 dark:focus:ring-primary-900"
          @focus="isSearchOpen = true"
        />
        <button
          v-if="searchQuery"
          type="button"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-400 hover:text-neutral-600"
          @click="searchQuery = ''"
        >
          ✕
        </button>
        <SearchDropdown v-if="isSearchOpen" />
      </div>

      <nav
        class="hidden items-center gap-4 text-sm font-medium text-neutral-600 sm:flex dark:text-neutral-300"
      >
        <RouterLink to="/" class="hover:text-primary-600" active-class="text-primary-600"
          >홈</RouterLink
        >
        <RouterLink to="/rankings" class="hover:text-primary-600" active-class="text-primary-600"
          >Top 100</RouterLink
        >
      </nav>

      <button
        type="button"
        class="rounded-full p-2 text-lg hover:bg-neutral-100 dark:hover:bg-neutral-800"
        :title="`테마: ${mode}`"
        @click="cycleMode"
      >
        {{ themeIcon[mode] }}
      </button>

      <RouterLink
        v-if="!auth.isLoggedIn"
        to="/login"
        class="shrink-0 rounded-full bg-primary-600 px-4 py-2 text-sm font-semibold text-white hover:bg-primary-700"
      >
        로그인
      </RouterLink>
      <template v-else>
        <NotificationBell />
        <UserMenu />
      </template>
    </div>
  </header>
</template>
