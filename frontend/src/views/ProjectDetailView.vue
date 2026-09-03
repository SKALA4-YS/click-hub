<script setup>
import { computed, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import SiteCard from '@/components/card/SiteCard.vue'
import { mockProjectDetails, getRelatedProjects } from '@/data/mockProjectDetail'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/formatRelativeTime'

const route = useRoute()
const auth = useAuthStore()

const project = computed(() => mockProjectDetails[route.params.id])
const relatedProjects = computed(() => (project.value ? getRelatedProjects(project.value.id) : []))

// PUT /api/v1/projects/{id}/like, /favorite, /creators/{id}/subscription 자리 —
// 지금은 화면 확인용으로 로컬 상태만 토글한다.
const isLiked = ref(false)
const isFavorited = ref(false)
const isSubscribed = ref(false)
const isDescriptionExpanded = ref(false)
const commentDraft = ref('')
const localComments = ref([])

function toggleLike() {
  isLiked.value = !isLiked.value
}
function toggleFavorite() {
  isFavorited.value = !isFavorited.value
}
function toggleSubscribe() {
  isSubscribed.value = !isSubscribed.value
}

function handleVisit() {
  // POST /api/v1/projects/{id}/outbound-clicks 자리 — 북극성 지표(외부 클릭) 기록.
  window.open(project.value.site_url, '_blank', 'noopener')
}

function submitComment() {
  const body = commentDraft.value.trim()
  if (!body) return
  localComments.value.unshift({
    id: `local_${Date.now()}`,
    author: auth.user?.display_name ?? '나',
    body,
    created_at: new Date().toISOString(),
    likes: 0,
  })
  commentDraft.value = ''
}

function formatCount(value) {
  return new Intl.NumberFormat('ko-KR').format(value ?? 0)
}

const techGroupLabels = {
  FRONTEND: 'Frontend',
  BACKEND: 'Backend',
  DATABASE: 'Database',
  INFRA_DEPLOY: 'Infra/Deploy',
  AI_DATA: 'AI/Data',
}

// 1~3위는 금/은/동 뱃지로 구분 (디자인시스템 4.3, SiteCard와 동일 규칙)
const RANK_BADGE_STYLE = {
  1: 'bg-rank-gold text-heading-light',
  2: 'bg-rank-silver text-heading-light',
  3: 'bg-rank-bronze text-heading-light',
}
const rankBadgeClass = computed(() => RANK_BADGE_STYLE[project.value?.rank] ?? 'bg-primary-600 text-white')
</script>

<template>
  <div v-if="!project" class="flex flex-col items-center gap-4 py-24 text-center">
    <p class="text-lg font-semibold text-heading-light dark:text-heading-dark">프로젝트를 찾을 수 없습니다.</p>
    <RouterLink to="/" class="text-primary-600 hover:underline">홈으로 돌아가기</RouterLink>
  </div>

  <div v-else class="flex flex-col gap-8">
    <nav class="text-sm text-body-light dark:text-body-dark">
      <RouterLink to="/" class="hover:text-primary-600">홈</RouterLink>
      <span class="mx-1">/</span>
      <span>{{ project.title }}</span>
    </nav>

    <!-- 썸네일 -->
    <div
      class="aspect-video overflow-hidden rounded-2xl"
      :class="project.thumbnail_url ? 'bg-surface-light-1 dark:bg-surface-dark-2' : 'bg-gradient-to-br from-primary-500 to-blue-500'"
    >
      <img
        v-if="project.thumbnail_url"
        :src="project.thumbnail_url"
        :alt="project.title"
        class="h-full w-full object-cover"
      />
    </div>

    <!-- 제목 & 사이트 방문 -->
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div class="flex flex-col gap-2">
        <div class="flex items-center gap-2">
          <span
            v-if="project.rank"
            class="rounded-md px-2 py-0.5 text-xs font-bold"
            :class="rankBadgeClass"
          >
            {{ project.rank }}위
          </span>
          <span class="rounded-md bg-neutral-100 px-2 py-0.5 text-xs font-medium text-body-light dark:bg-surface-dark-2 dark:text-body-dark">
            {{ project.category }}
          </span>
        </div>
        <h1 class="font-headline text-2xl font-bold text-heading-light dark:text-heading-dark">
          {{ project.title }}
        </h1>
        <div class="flex flex-wrap gap-1.5">
          <span
            v-for="tag in project.tags"
            :key="tag"
            class="rounded-full border border-divider/20 px-2.5 py-0.5 text-xs text-body-light dark:border-divider/30 dark:text-body-dark"
          >
            #{{ tag }}
          </span>
        </div>
      </div>

      <button
        type="button"
        class="flex items-center gap-2 rounded-full bg-blue-500 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-600"
        @click="handleVisit"
      >
        사이트 방문하기
        <svg viewBox="0 0 20 20" fill="currentColor" class="h-4 w-4">
          <path d="M12.293 3.293a1 1 0 011.414 0l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414-1.414L14.586 9H4a1 1 0 110-2h10.586l-2.293-2.293a1 1 0 010-1.414z" transform="rotate(-45 10 10)" />
        </svg>
      </button>
    </div>

    <!-- 제작자 & 반응 -->
    <div class="flex flex-wrap items-center justify-between gap-4 rounded-xl border border-divider/20 p-4 dark:border-divider/25">
      <div class="flex items-center gap-3">
        <span class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-sm font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100">
          {{ project.owner.avatar_initial }}
        </span>
        <div>
          <p class="font-semibold text-heading-light dark:text-heading-dark">{{ project.owner.display_name }}</p>
          <p class="text-xs text-body-light dark:text-body-dark">
            팔로워 {{ formatCount(project.owner.followers) }} · 프로젝트 {{ project.owner.project_count }}개
          </p>
        </div>
        <button
          type="button"
          class="ml-2 rounded-full border px-4 py-1.5 text-sm font-medium transition-colors"
          :class="
            isSubscribed
              ? 'border-primary-600 bg-primary-600 text-white'
              : 'border-divider/30 text-body-light hover:border-primary-400 hover:text-primary-600 dark:text-body-dark'
          "
          @click="toggleSubscribe"
        >
          {{ isSubscribed ? '팔로잉' : '+ 팔로우' }}
        </button>
      </div>

      <div class="flex items-center gap-2 text-sm">
        <button
          type="button"
          class="flex items-center gap-1.5 rounded-full border px-3 py-1.5 transition-colors"
          :class="isLiked ? 'border-danger text-danger' : 'border-divider/30 text-body-light dark:text-body-dark'"
          @click="toggleLike"
        >
          <svg viewBox="0 0 20 20" :fill="isLiked ? 'currentColor' : 'none'" stroke="currentColor" class="h-4 w-4">
            <path stroke-width="1.5" d="M10 17.5s-6.5-4.1-8.4-8.1C.4 6.6 1.7 3.5 4.7 3c1.9-.3 3.6.6 4.3 2 .7-1.4 2.4-2.3 4.3-2 3 .5 4.3 3.6 3.1 6.4-1.9 4-8.4 8.1-8.4 8.1z" />
          </svg>
          {{ formatCount(project.stats.likes + (isLiked ? 1 : 0)) }}
        </button>
        <button
          type="button"
          class="flex items-center gap-1.5 rounded-full border px-3 py-1.5 transition-colors"
          :class="isFavorited ? 'border-primary-600 text-primary-600' : 'border-divider/30 text-body-light dark:text-body-dark'"
          @click="toggleFavorite"
        >
          <svg viewBox="0 0 20 20" :fill="isFavorited ? 'currentColor' : 'none'" stroke="currentColor" class="h-4 w-4">
            <path stroke-width="1.5" d="M5 3a1 1 0 00-1 1v13l6-3 6 3V4a1 1 0 00-1-1H5z" />
          </svg>
          {{ formatCount(project.stats.favorites + (isFavorited ? 1 : 0)) }}
        </button>
        <span class="px-2 text-body-light dark:text-body-dark">조회 {{ formatCount(project.stats.views) }}</span>
      </div>
    </div>

    <!-- 기술 스택 -->
    <section class="flex flex-col gap-3">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">기술 스택</h2>
      <div class="flex flex-col gap-2">
        <div v-for="(items, group) in project.tech_stack" :key="group" class="flex flex-wrap items-center gap-2">
          <span class="w-28 shrink-0 text-xs font-semibold uppercase text-body-light dark:text-body-dark">{{ techGroupLabels[group] ?? group }}</span>
          <span
            v-for="item in items"
            :key="item"
            class="rounded-md bg-neutral-100 px-2.5 py-1 text-xs font-medium text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
          >
            {{ item }}
          </span>
        </div>
      </div>
    </section>

    <!-- 소개 -->
    <section class="flex flex-col gap-3">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">프로젝트 소개</h2>
      <p
        class="whitespace-pre-line text-sm leading-relaxed text-body-light dark:text-body-dark"
        :class="!isDescriptionExpanded && 'line-clamp-3'"
      >
        {{ project.description }}
      </p>
      <button
        type="button"
        class="self-start text-sm font-medium text-primary-600 hover:underline"
        @click="isDescriptionExpanded = !isDescriptionExpanded"
      >
        {{ isDescriptionExpanded ? '접기' : '더보기' }}
      </button>
    </section>

    <!-- 댓글 -->
    <section class="flex flex-col gap-4">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        피드백 & 댓글 {{ project.stats.comments + localComments.length }}
      </h2>

      <div v-if="auth.isLoggedIn" class="flex flex-col gap-2">
        <textarea
          v-model="commentDraft"
          rows="2"
          placeholder="인디 메이커에게 피드백이나 응원의 메시지를 남겨보세요..."
          class="w-full resize-none rounded-lg border border-divider/20 bg-surface-light-1 p-3 text-sm outline-none focus:border-primary-400 focus:ring-2 focus:ring-primary-100 dark:border-divider/30 dark:bg-surface-dark-1"
        />
        <button
          type="button"
          class="self-end rounded-full bg-primary-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-primary-700 disabled:opacity-40"
          :disabled="!commentDraft.trim()"
          @click="submitComment"
        >
          등록
        </button>
      </div>
      <p v-else class="text-sm text-body-light dark:text-body-dark">
        <RouterLink to="/login" class="text-primary-600 hover:underline">로그인</RouterLink>하면 댓글을 남길 수 있어요.
      </p>

      <ul class="flex flex-col gap-4">
        <li v-for="comment in [...localComments, ...project.comments]" :key="comment.id" class="flex gap-3">
          <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-neutral-200 text-xs font-bold text-body-light dark:bg-surface-dark-2 dark:text-body-dark">
            {{ comment.author[0] }}
          </span>
          <div class="flex-1">
            <p class="text-sm text-heading-light dark:text-heading-dark">
              <span class="font-semibold">{{ comment.author }}</span>
              <span class="ml-2 text-xs text-body-light dark:text-body-dark">{{ formatRelativeTime(comment.created_at) }}</span>
            </p>
            <p class="mt-1 text-sm text-body-light dark:text-body-dark">{{ comment.body }}</p>
          </div>
        </li>
        <li v-if="project.comments.length === 0 && localComments.length === 0" class="text-sm text-body-light dark:text-body-dark">
          아직 댓글이 없어요. 첫 피드백을 남겨보세요!
        </li>
      </ul>
    </section>

    <!-- 관련 프로젝트 -->
    <section class="flex flex-col gap-4">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">이런 사이트는 어때요?</h2>
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <SiteCard v-for="item in relatedProjects" :key="item.id" :project="item" />
      </div>
    </section>
  </div>
</template>
