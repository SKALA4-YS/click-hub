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
      class="rounded-2xl border border-divider/15 bg-white px-5 py-5 shadow-sm"
    >
      <RouterLink
        :to="`/community/posts/${post.id}`"
        class="text-lg font-bold tracking-tight hover:text-primary-600"
        >{{ post.title }}</RouterLink
      >
      <div
        class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-divider/15 pt-3 text-xs text-body-light"
      >
        <span class="font-semibold">{{ post.authorName }}</span>
        <span>{{ formatDate(post.createdAt) }} · 조회 {{ post.viewCount }}</span>
      </div>
    </article>
    <p
      v-if="posts.length === 0"
      class="rounded-2xl border border-divider/20 px-6 py-16 text-center text-sm text-body-light"
    >
      아직 게시글이 없습니다.
    </p>
  </section>
</template>
