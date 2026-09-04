<script setup>
import { RouterLink } from 'vue-router'
import { useTheme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'
import NotificationBell from '@/components/layout/NotificationBell.vue'
import UserMenu from '@/components/layout/UserMenu.vue'
import SearchBar from '@/components/layout/SearchBar.vue'
import cursorIcon from '@/assets/figma/cursor.svg'
import notificationIcon from '@/assets/figma/notification.svg'
import themeIcon from '@/assets/figma/theme.svg'

const { mode, cycleMode } = useTheme()
const auth = useAuthStore()
</script>

<template>
  <header
    class="sticky top-0 z-10 border-b border-divider/20 bg-surface-light-1/90 backdrop-blur dark:border-blue-500/15 dark:bg-base-dark/90"
  >
    <div class="mx-auto flex w-full max-w-[1280px] items-center gap-3 px-4 py-3 sm:gap-4 lg:px-6">
      <RouterLink
        to="/"
        class="flex shrink-0 items-center font-headline text-xl font-extrabold tracking-tight text-primary-600 dark:text-heading-dark"
        aria-label="Click HUB 홈으로 이동"
      >
        <span>Click</span><span class="pl-0.5">HUB</span
        ><img :src="cursorIcon" alt="" class="ml-0.5 h-4 w-4 -rotate-12" />
      </RouterLink>

      <div class="min-w-0 flex-1 lg:flex-none lg:w-[462px]">
        <SearchBar />
      </div>

      <nav
        aria-label="주요 메뉴"
        class="hidden items-center gap-4 text-sm font-medium text-body-light lg:flex dark:text-body-dark"
      >
        <RouterLink
          to="/community"
          class="hover:text-primary-600"
          active-class="text-primary-600 dark:text-blue-400"
          >게시판</RouterLink
        >
        <RouterLink
          to="/rankings"
          class="hover:text-primary-600"
          active-class="text-primary-600 dark:text-blue-400"
          >메이커 랭킹</RouterLink
        >
        <RouterLink
          to="/tutorials"
          class="hover:text-primary-600"
          active-class="text-primary-600 dark:text-blue-400"
          >튜토리얼</RouterLink
        >
        <RouterLink
          to="/insights"
          class="hover:text-primary-600"
          active-class="text-primary-600 dark:text-blue-400"
          >AI 트렌드</RouterLink
        >
      </nav>

      <button
        type="button"
        class="rounded-full p-2 text-lg text-body-light hover:bg-neutral-100 dark:text-body-dark dark:hover:bg-surface-dark-2"
        :title="`테마: ${mode}`"
        @click="cycleMode"
      >
        <img :src="themeIcon" alt="" class="h-5 w-5" />
      </button>

      <template v-if="!auth.isLoggedIn">
        <RouterLink
          to="/login"
          class="rounded-full p-2 text-lg text-body-light hover:bg-neutral-100 dark:text-body-dark dark:hover:bg-surface-dark-2"
          title="알림 (로그인 필요)"
        >
          <img :src="notificationIcon" alt="" class="h-5 w-5" />
        </RouterLink>
        <RouterLink
          to="/login"
          class="hidden shrink-0 rounded-full bg-primary-600 px-4 py-2 text-sm font-semibold text-white hover:bg-primary-700 sm:inline-flex"
        >
          로그인
        </RouterLink>
      </template>
      <template v-else>
        <NotificationBell />
        <UserMenu />
      </template>
    </div>
  </header>
</template>
