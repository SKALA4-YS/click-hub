<script setup>
const suggestions = ['Vue.js 포트폴리오', 'AI 프롬프트 관리', '무료 칸반 보드']
</script>

<template>
  <div
    class="absolute left-0 right-0 top-full z-20 mt-2 rounded-xl border border-neutral-200 bg-white p-3 shadow-lg dark:border-white/10 dark:bg-secondary"
  >
    <ul class="flex flex-col gap-1">
      <li v-for="term in suggestions" :key="term">
        <button
          type="button"
          class="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-left text-sm hover:bg-neutral-100 dark:hover:bg-white/5"
        >
          <span aria-hidden="true">🔍</span>
          {{ term }}
          <span class="ml-auto text-neutral-400">↗</span>
        </button>
      </li>
    </ul>

    <div
      class="mt-2 flex items-center justify-between border-t border-neutral-100 pt-2 text-xs text-neutral-400 dark:border-white/10"
    >
      <button type="button" class="hover:text-neutral-600 dark:hover:text-neutral-300">
        상세 필터 조건 ⌄
      </button>
      <span>최근검색어 끄기 · 자동완성 끄기</span>
    </div>
  </div>
</template>
