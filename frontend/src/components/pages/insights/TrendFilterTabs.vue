<script setup>
defineProps({
  filters: { type: Array, required: true },
  selected: { type: String, required: true },
})
const emit = defineEmits(['update:selected'])
</script>

<template>
  <div class="flex flex-wrap gap-2" role="group" aria-label="AI 트렌드 필터">
    <button
      v-for="filter in filters"
      :key="filter.id"
      type="button"
      :aria-label="`${filter.label} 트렌드`"
      :aria-pressed="selected === filter.id"
      class="rounded-full border px-3 py-1.5 text-xs font-semibold transition-colors"
      :class="
        selected === filter.id
          ? 'border-primary-600 bg-primary-600 text-white'
          : 'border-divider/20 bg-white text-body-light hover:text-primary-600 dark:border-divider/30 dark:bg-surface-dark-1 dark:text-body-dark'
      "
      @click="emit('update:selected', filter.id)"
    >
      {{ filter.label }}
    </button>
  </div>
</template>
