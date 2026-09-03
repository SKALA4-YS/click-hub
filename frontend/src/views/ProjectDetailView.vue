<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import SiteCard from '@/components/card/SiteCard.vue'
import { toProjectDetailViewModel, toSiteCardProject } from '@/api/adapters/projects'
import {
  createProjectComment,
  getProject,
  getProjectComments,
  recordOutboundClick,
  toggleProjectFavorite,
  toggleProjectLike,
} from '@/api/projects'
import { searchProjects } from '@/api/search'
import { getCreator, toggleCreatorSubscription } from '@/api/users'
import { useAuthStore } from '@/stores/auth'
import { formatRelativeTime } from '@/utils/formatRelativeTime'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const project = ref(null)
const relatedProjects = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

const isLiked = ref(false)
const isFavorited = ref(false)
const isSubscribed = ref(false)
const isDescriptionExpanded = ref(false)
const commentDraft = ref('')
const isSaving = ref(false)

function requireLogin() {
  if (auth.isLoggedIn) return true
  router.push({ name: 'login', query: { redirect: route.fullPath } })
  return false
}

async function toggleLike() {
  if (!requireLogin() || isSaving.value) return
  isSaving.value = true
  try {
    const response = await toggleProjectLike(project.value.id)
    isLiked.value = response.liked
    project.value.stats.likes = response.likeCount
  } finally {
    isSaving.value = false
  }
}
async function toggleFavorite() {
  if (!requireLogin() || isSaving.value) return
  isSaving.value = true
  try {
    const previous = isFavorited.value
    const response = await toggleProjectFavorite(project.value.id)
    isFavorited.value = response.favorited
    project.value.stats.favorites += response.favorited ? (previous ? 0 : 1) : previous ? -1 : 0
  } finally {
    isSaving.value = false
  }
}
async function toggleSubscribe() {
  if (!requireLogin() || isSaving.value) return
  isSaving.value = true
  try {
    const response = await toggleCreatorSubscription(project.value.owner.id)
    isSubscribed.value = response.subscribed
  } finally {
    isSaving.value = false
  }
}

function handleVisit() {
  window.open(project.value.site_url, '_blank', 'noopener')
  void recordOutboundClick(project.value.id).catch(() => {})
}

async function submitComment() {
  const body = commentDraft.value.trim()
  if (!body || !requireLogin() || isSaving.value) return
  isSaving.value = true
  try {
    const comment = await createProjectComment(project.value.id, body)
    project.value.comments.unshift({
      id: comment.id,
      author: comment.authorName,
      author_id: comment.authorId,
      body: comment.body,
      created_at: comment.createdAt,
    })
    project.value.stats.comments += 1
    commentDraft.value = ''
  } finally {
    isSaving.value = false
  }
}

async function loadProject() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const detail = await getProject(route.params.id)
    const [comments, creator, related] = await Promise.all([
      getProjectComments(detail.id),
      getCreator(detail.ownerId),
      searchProjects({ category: detail.categorySlug }),
    ])
    project.value = toProjectDetailViewModel(detail, comments, creator)
    isLiked.value = project.value.liked_by_me
    isFavorited.value = project.value.favorited_by_me
    isSubscribed.value = project.value.subscribed_by_me
    relatedProjects.value = related.items
      .filter((item) => item.id !== detail.id)
      .slice(0, 3)
      .map(toSiteCardProject)
  } catch (error) {
    project.value = null
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

onMounted(loadProject)
watch(() => route.params.id, loadProject)

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

const RANK_BADGE_STYLE = {
  1: 'bg-rank-gold text-heading-light',
  2: 'bg-rank-silver text-heading-light',
  3: 'bg-rank-bronze text-heading-light',
}
const rankBadgeClass = computed(
  () => RANK_BADGE_STYLE[project.value?.rank] ?? 'bg-primary-600 text-white',
)
</script>

<template>
  <div v-if="isLoading" class="py-24 text-center text-sm text-body-light">
    프로젝트를 불러오는 중입니다.
  </div>

  <div v-else-if="!project" class="flex flex-col items-center gap-4 py-24 text-center">
    <p class="text-lg font-semibold text-heading-light dark:text-heading-dark">
      {{ errorMessage || '프로젝트를 찾을 수 없습니다.' }}
    </p>
    <RouterLink to="/" class="text-primary-600 hover:underline">홈으로 돌아가기</RouterLink>
  </div>

  <div v-else class="mx-auto flex max-w-[1120px] flex-col gap-6 pb-14">
    <nav class="text-xs text-body-light dark:text-body-dark" aria-label="현재 위치">
      <RouterLink to="/" class="hover:text-primary-600">홈</RouterLink>
      <span class="mx-2">›</span
      ><RouterLink to="/rankings" class="hover:text-primary-600">프로젝트</RouterLink
      ><span class="mx-2">›</span>
      <span>{{ project.title }}</span>
    </nav>

    <div
      data-testid="project-showcase"
      class="relative aspect-video max-h-[480px] overflow-hidden rounded-2xl border border-divider/15 bg-[#171717] shadow-sm"
    >
      <img
        v-if="project.thumbnail_url"
        :src="project.thumbnail_url"
        :alt="project.title"
        class="h-full w-full object-cover"
      />
      <template v-else>
        <div
          class="absolute left-5 top-5 flex items-center gap-2 rounded-full bg-black/50 px-3 py-1.5 text-xs font-semibold text-emerald-300"
        >
          <span class="h-2 w-2 rounded-full bg-emerald-400"></span> Live showcase
        </div>
        <div class="grid h-full place-items-center text-center text-white">
          <div>
            <p class="text-xs font-bold uppercase tracking-[0.28em] text-blue-300">Product demo</p>
            <p class="mt-4 font-headline text-3xl font-extrabold">{{ project.title }}</p>
            <p class="mt-3 text-sm text-neutral-400">서비스의 핵심 흐름을 한눈에 확인하세요</p>
          </div>
        </div>
        <span
          class="absolute bottom-5 right-5 grid h-9 w-9 place-items-center rounded-lg bg-white/10 text-white"
          >↗</span
        >
      </template>
    </div>

    <section class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
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
            <span
              class="rounded-md bg-neutral-100 px-2 py-0.5 text-xs font-medium text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
            >
              {{ project.category }}
            </span>
          </div>
          <h1
            class="font-headline text-3xl font-extrabold tracking-tight text-heading-light dark:text-heading-dark"
          >
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
          사이트 방문하기 ↗
        </button>
      </div>

      <div
        class="mt-5 flex flex-wrap items-center justify-between gap-4 border-t border-divider/15 pt-5"
      >
        <div class="flex items-center gap-3">
          <span
            class="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-sm font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >
            {{ project.owner.avatar_initial }}
          </span>
          <div>
            <p class="font-semibold text-heading-light dark:text-heading-dark">
              {{ project.owner.display_name }}
            </p>
            <p class="text-xs text-body-light dark:text-body-dark">
              팔로워 {{ formatCount(project.owner.followers) }} · 프로젝트
              {{ project.owner.project_count }}개
            </p>
          </div>
          <button
            type="button"
            aria-label="개발자 팔로우"
            :aria-pressed="isSubscribed"
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
            aria-label="프로젝트 좋아요"
            :aria-pressed="isLiked"
            class="flex items-center gap-1.5 rounded-full border px-3 py-1.5 transition-colors"
            :class="
              isLiked
                ? 'border-danger text-danger'
                : 'border-divider/30 text-body-light dark:text-body-dark'
            "
            @click="toggleLike"
          >
            <svg
              viewBox="0 0 20 20"
              :fill="isLiked ? 'currentColor' : 'none'"
              stroke="currentColor"
              class="h-4 w-4"
            >
              <path
                stroke-width="1.5"
                d="M10 17.5s-6.5-4.1-8.4-8.1C.4 6.6 1.7 3.5 4.7 3c1.9-.3 3.6.6 4.3 2 .7-1.4 2.4-2.3 4.3-2 3 .5 4.3 3.6 3.1 6.4-1.9 4-8.4 8.1-8.4 8.1z"
              />
            </svg>
            {{ formatCount(project.stats.likes) }}
          </button>
          <button
            type="button"
            aria-label="프로젝트 북마크"
            :aria-pressed="isFavorited"
            class="flex items-center gap-1.5 rounded-full border px-3 py-1.5 transition-colors"
            :class="
              isFavorited
                ? 'border-primary-600 text-primary-600'
                : 'border-divider/30 text-body-light dark:text-body-dark'
            "
            @click="toggleFavorite"
          >
            <svg
              viewBox="0 0 20 20"
              :fill="isFavorited ? 'currentColor' : 'none'"
              stroke="currentColor"
              class="h-4 w-4"
            >
              <path stroke-width="1.5" d="M5 3a1 1 0 00-1 1v13l6-3 6 3V4a1 1 0 00-1-1H5z" />
            </svg>
            북마크 {{ formatCount(project.stats.favorites) }}
          </button>
          <span class="px-2 text-body-light dark:text-body-dark"
            >외부 클릭 {{ formatCount(project.stats.views) }}</span
          >
          <span class="px-2 text-body-light dark:text-body-dark"
            >피드백 {{ project.stats.comments }}</span
          >
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-divider/20 bg-white p-6">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        기술 스택
      </h2>
      <div class="flex flex-col gap-2">
        <div
          v-for="(items, group) in project.tech_stack"
          :key="group"
          class="flex flex-wrap items-center gap-2"
        >
          <span
            class="w-28 shrink-0 text-xs font-semibold uppercase text-body-light dark:text-body-dark"
            >{{ techGroupLabels[group] ?? group }}</span
          >
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

    <section class="rounded-2xl border border-divider/20 bg-white p-6">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        프로젝트 소개
      </h2>
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

    <section class="rounded-2xl border border-divider/20 bg-white p-6">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        도움이 되는 피드백
        <span class="ml-1 text-sm font-medium text-body-light">{{ project.stats.comments }}</span>
      </h2>

      <div v-if="auth.isLoggedIn" class="flex flex-col gap-2">
        <textarea
          v-model="commentDraft"
          name="feedback"
          rows="2"
          placeholder="인디 메이커에게 피드백이나 응원의 메시지를 남겨보세요..."
          class="w-full resize-none rounded-lg border border-divider/20 bg-surface-light-1 p-3 text-sm outline-none focus:border-primary-400 focus:ring-2 focus:ring-primary-100 dark:border-divider/30 dark:bg-surface-dark-1"
        />
        <button
          type="button"
          name="submit-feedback"
          class="self-end rounded-full bg-primary-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-primary-700 disabled:opacity-40"
          :disabled="!commentDraft.trim()"
          @click="submitComment"
        >
          등록
        </button>
      </div>
      <p v-else class="text-sm text-body-light dark:text-body-dark">
        <RouterLink to="/login" class="text-primary-600 hover:underline">로그인</RouterLink>하면
        댓글을 남길 수 있어요.
      </p>

      <ul class="flex flex-col gap-4">
        <li v-for="comment in project.comments" :key="comment.id" class="flex gap-3">
          <span
            class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-neutral-200 text-xs font-bold text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
          >
            {{ comment.author[0] }}
          </span>
          <div class="flex-1">
            <p class="text-sm text-heading-light dark:text-heading-dark">
              <span class="font-semibold">{{ comment.author }}</span>
              <span class="ml-2 text-xs text-body-light dark:text-body-dark">{{
                formatRelativeTime(comment.created_at)
              }}</span>
            </p>
            <p class="mt-1 text-sm text-body-light dark:text-body-dark">{{ comment.body }}</p>
          </div>
        </li>
        <li
          v-if="project.comments.length === 0"
          class="text-sm text-body-light dark:text-body-dark"
        >
          아직 댓글이 없어요. 첫 피드백을 남겨보세요!
        </li>
      </ul>
    </section>

    <section class="flex flex-col gap-4">
      <h2 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
        이런 사이트는 어때요?
      </h2>
      <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <SiteCard v-for="item in relatedProjects" :key="item.id" :project="item" />
      </div>
    </section>
  </div>
</template>
