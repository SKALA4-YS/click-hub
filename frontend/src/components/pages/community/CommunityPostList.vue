<script setup>
defineProps({
  posts: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="space-y-4">
    <article
      v-for="post in posts"
      :key="post.id"
      class="rounded-2xl border border-divider/15 bg-surface-light-1 px-5 py-5 shadow-[0_8px_24px_rgba(15,14,71,0.04)] transition-shadow hover:shadow-[0_12px_30px_rgba(15,14,71,0.09)] dark:border-blue-500/15 dark:bg-surface-dark-1 dark:shadow-none sm:px-6"
    >
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <span
          class="rounded-md px-2 py-1 text-xs font-semibold"
          :class="
            post.board === 'question'
              ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300'
              : post.board === 'share'
                ? 'bg-cyan-50 text-cyan-700 dark:bg-cyan-500/15 dark:text-cyan-200'
                : 'bg-primary-50 text-primary-600 dark:bg-primary-900 dark:text-blue-200'
          "
          >{{ post.label }}</span
        >
        <span
          v-if="post.pinned"
          class="rounded-md bg-primary-50 px-2 py-1 text-xs font-semibold text-primary-600 dark:bg-primary-900 dark:text-blue-200"
          >공지글</span
        >
        <span
          v-if="post.resolved"
          class="rounded-md bg-emerald-50 px-2 py-1 text-xs font-semibold text-emerald-700 dark:bg-emerald-500/15 dark:text-emerald-300"
          >해결됨</span
        >
        <span
          v-for="tag in post.tags"
          :key="tag"
          class="rounded-md bg-neutral-100 px-2 py-1 text-xs text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
          >#{{ tag }}</span
        >
      </div>
      <h2 class="text-lg font-bold tracking-tight text-heading-light dark:text-heading-dark">
        {{ post.title }}
      </h2>
      <p class="mt-2 line-clamp-2 text-sm leading-6 text-body-light dark:text-body-dark">
        {{ post.summary }}
      </p>
      <div
        class="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-divider/15 pt-3 text-xs text-body-light dark:border-blue-500/10 dark:text-body-dark"
      >
        <span
          class="flex min-w-0 items-center gap-2 font-semibold text-heading-light dark:text-heading-dark"
          ><span
            class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-neutral-200 text-[10px] font-bold text-secondary dark:bg-surface-dark-2 dark:text-blue-100"
            >{{ post.author.slice(0, 1) }}</span
          ><span class="truncate"
            >{{ post.author }}
            <span class="ml-1 font-normal text-body-light dark:text-body-dark"
              >· {{ post.role }} · {{ post.time }}</span
            ></span
          ></span
        >
        <span class="flex gap-3"
          ><span>좋아요 {{ post.likes }}</span
          ><span>댓글 {{ post.comments }}</span
          ><span>조회 {{ post.views }}</span></span
        >
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
