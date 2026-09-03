<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col overflow-x-clip">
    <AppHeader />
    <main class="mx-auto w-full max-w-[1280px] flex-1 px-4 py-6 lg:px-6">
      <RouterView />
    </main>
    <footer
      class="border-t border-divider/20 py-6 text-sm text-body-light dark:border-divider/20 dark:text-body-dark"
    >
      <div
        class="mx-auto flex max-w-[1280px] flex-wrap items-center justify-between gap-2 px-4 lg:px-6"
      >
        <p>© 2026 CLICK-HUB. ALL RIGHTS RESERVED.</p>
        <div class="flex gap-4">
          <a href="/privacy" class="hover:text-primary-600">Privacy</a>
          <a href="/terms" class="hover:text-primary-600">Terms</a>
          <a href="mailto:contact@click-hub.example" class="hover:text-primary-600">문의하기</a>
        </div>
      </div>
    </footer>
  </div>
</template>
