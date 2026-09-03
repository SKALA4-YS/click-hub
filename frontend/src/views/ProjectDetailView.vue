<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchProjectById, fetchCommentsByProjectId, recordOutboundClick } from '@/api/projects'

const route = useRoute()
const project = ref(null)
const comments = ref([])
const isLoading = ref(true)

const techGroupLabel = {
  FRONTEND: 'Frontend',
  BACKEND: 'Backend',
  DATABASE: 'Database',
  INFRA_DEPLOY: 'Infra/Deploy',
  AI_DATA: 'AI/Data',
}

onMounted(async () => {
  const id = route.params.id
  ;[project.value, comments.value] = await Promise.all([
    fetchProjectById(id),
    fetchCommentsByProjectId(id),
  ])
  isLoading.value = false
})

function visitSite() {
  if (!project.value) return
  recordOutboundClick(project.value.id)
  window.open(project.value.site_url, '_blank', 'noopener')
}
</script>

<template>
  <p v-if="isLoading" class="text-sm text-neutral-500">불러오는 중...</p>
  <p v-else-if="!project" class="text-sm text-neutral-500">프로젝트를 찾을 수 없습니다.</p>

  <article v-else class="flex flex-col gap-6">
    <div
      class="flex aspect-[21/9] items-center justify-center rounded-2xl bg-gradient-to-br from-primary-100 to-primary-300 text-2xl font-bold text-primary-800 dark:from-primary-900 dark:to-primary-700 dark:text-primary-100"
    >
      {{ project.title }}
    </div>

    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="font-headline text-2xl font-extrabold">{{ project.title }}</h1>
        <p class="mt-1 text-sm text-neutral-500">by {{ project.owner.display_name }}</p>
      </div>
      <button
        type="button"
        class="rounded-full bg-primary-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-primary-700"
        @click="visitSite"
      >
        사이트 방문하기 ↗
      </button>
    </div>

    <div class="flex flex-wrap gap-2">
      <span
        v-for="tag in project.tags"
        :key="tag"
        class="rounded-full bg-neutral-100 px-3 py-1 text-xs font-medium text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300"
      >
        #{{ tag }}
      </span>
    </div>

    <section>
      <h2 class="mb-2 text-lg font-bold">프로젝트 소개</h2>
      <p class="text-sm leading-relaxed text-neutral-700 dark:text-neutral-300">
        {{ project.description }}
      </p>
    </section>

    <section>
      <h2 class="mb-2 text-lg font-bold">기술 스택</h2>
      <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
        <div
          v-for="(items, group) in project.tech_stack"
          :key="group"
          v-show="items.length"
          class="rounded-lg border border-neutral-200 p-3 dark:border-neutral-800"
        >
          <p class="mb-1 text-xs font-semibold text-neutral-500">{{ techGroupLabel[group] }}</p>
          <p class="text-sm">{{ items.join(', ') }}</p>
        </div>
      </div>
    </section>

    <section
      class="flex items-center gap-6 border-y border-neutral-200 py-3 text-sm text-neutral-600 dark:border-neutral-800 dark:text-neutral-400"
    >
      <span>♡ 좋아요 {{ project.stats.unique_likes.toLocaleString() }}</span>
      <span>💬 댓글 {{ project.stats.unique_commenters }}</span>
      <span>👁 방문자 {{ project.stats.unique_visitors.toLocaleString() }}</span>
    </section>

    <section>
      <h2 class="mb-3 text-lg font-bold">피드백 & 응원 ({{ comments.length }})</h2>
      <ul class="flex flex-col gap-3">
        <li
          v-for="comment in comments"
          :key="comment.id"
          class="rounded-lg bg-neutral-100 p-3 text-sm dark:bg-neutral-800"
        >
          <p class="font-semibold">{{ comment.author }}</p>
          <p class="mt-1 text-neutral-700 dark:text-neutral-300">{{ comment.body }}</p>
        </li>
        <li v-if="comments.length === 0" class="text-sm text-neutral-500">아직 댓글이 없습니다.</li>
      </ul>
    </section>
  </article>
</template>
