<script setup>
defineProps({
  tagline: {
    type: String,
    default: '인디 메이커를 위한 플랫폼',
  },
})
</script>

<template>
  <div class="flex flex-col items-center gap-3 text-center">
    <span
      class="rounded-full bg-primary-50 px-3 py-1 text-xs font-medium text-primary-700 dark:bg-primary-950 dark:text-primary-200"
    >
      ● {{ tagline }}
    </span>
    <div class="flex items-center gap-2">
      <span class="flex h-8 w-8 items-center justify-center rounded-lg bg-primary-600 text-white">
        <svg viewBox="0 0 24 24" class="h-5 w-5" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3z" stroke-linejoin="round" />
          <path d="M12 3v9m0 0l8-4.5M12 12l-8-4.5M12 12v9" stroke-linejoin="round" />
        </svg>
      </span>
      <span class="font-headline text-xl font-extrabold tracking-tight">Click-Hub</span>
    </div>
  </div>
</template>
