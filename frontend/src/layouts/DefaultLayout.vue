<script setup>
import { inject } from 'vue'
import { routeLocationKey } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'

const route = inject(routeLocationKey, { meta: {} })
</script>

<template>
  <div class="flex min-h-screen flex-col overflow-x-clip">
    <AppHeader v-if="!route.meta.standalone" />
    <main
      :class="
        route.meta.standalone
          ? 'w-full max-w-none flex-1'
          : 'mx-auto w-full max-w-[1280px] flex-1 px-4 py-6 lg:px-6'
      "
    >
      <RouterView />
    </main>
    <footer
      v-if="!route.meta.standalone"
      data-testid="app-footer"
      class="border-t border-divider/20 py-6 text-sm text-body-light dark:border-divider/20 dark:text-body-dark"
    >
      <div
        class="mx-auto flex max-w-[1280px] flex-wrap items-center justify-between gap-2 px-4 lg:px-6"
      >
        <p>© 2026 CLICK-HUB. ALL RIGHTS RESERVED.</p>
        <div class="flex gap-4">
          <span>Privacy</span>
          <span>Terms</span>
        </div>
      </div>
    </footer>
  </div>
</template>
