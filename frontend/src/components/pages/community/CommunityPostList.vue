<script setup>
import { RouterLink } from 'vue-router'

defineProps({ posts: { type: Array, required: true } })

function formatDate(value) {
  return value
    ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
    : ''
}
</script>

<template>
  <section class="space-y-4">
    <article
      v-for="post in posts"
      :key="post.id"
      class="rounded-2xl border border-divider/15 bg-surface-light-1 px-5 py-5 shadow-[0_8px_24px_rgba(15,14,71,0.04)] transition-shadow hover:shadow-[0_12px_30px_rgba(15,14,71,0.09)] dark:border-blue-500/15 dark:bg-surface-dark-1 dark:shadow-none sm:px-6"
    >
      <RouterLink
        :to="`/community/posts/${post.id}`"
        class="text-lg font-bold tracking-tight text-heading-light hover:text-primary-600 dark:text-heading-dark"
        >{{ post.title }}</RouterLink
      >
      <div
        class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-divider/15 pt-3 text-xs text-body-light dark:border-blue-500/10 dark:text-body-dark"
      >
        <span
          class="flex min-w-0 items-center gap-2 font-semibold text-heading-light dark:text-heading-dark"
          ><span
            class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-neutral-200 text-[10px] font-bold text-secondary dark:bg-surface-dark-2 dark:text-blue-100"
            >{{ post.authorName.slice(0, 1) }}</span
          ><span class="truncate">{{ post.authorName }}</span></span
        >
        <span>{{ formatDate(post.createdAt) }} · 조회 {{ post.viewCount }}</span>
      </div>
    </article>
    <p
      v-if="posts.length === 0"
      class="rounded-2xl border border-divider/20 px-6 py-16 text-center text-sm text-body-light dark:border-blue-500/15 dark:text-body-dark"
    >
      아직 게시글이 없습니다.
    </p>
  </section>
</template>
