<script setup>
defineProps({
  memberCount: {
    type: String,
    default: '1,400+',
  },
})
</script>

<template>
  <div
    class="flex items-center justify-between rounded-xl border border-neutral-200 bg-white px-4 py-3 text-xs text-neutral-500 dark:border-neutral-800 dark:bg-neutral-900"
  >
    <span>{{ memberCount }}명의 인디 메이커 활동 중</span>
    <span class="flex items-center gap-1 font-medium text-primary-600">
      ● 주간 핫 프로젝트 #1
    </span>
  </div>
</template>
