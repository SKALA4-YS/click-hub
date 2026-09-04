<script setup>
import { computed, onMounted, reactive, ref, watch, watchEffect } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { toSiteCardProject } from '@/api/adapters/projects'
import { deleteProject, submitProject } from '@/api/projects'
import { getProjectRankings } from '@/api/rankings'
import { getCreator, getMyProjects, toggleCreatorSubscription } from '@/api/users'
import SiteCard from '@/components/card/SiteCard.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const creator = ref(null)
const projects = ref([])
const rankByProjectId = ref(new Map())
const isLoading = ref(true)
const errorMessage = ref('')
const isSaving = ref(false)
const activeTab = ref('projects')
const sortKey = ref('recent')

const creatorId = computed(() => route.params.id ?? auth.user?.id)
const isMine = computed(() => Boolean(auth.user?.id && auth.user.id === creatorId.value))

// 디자인시스템에 project_status 배지 색상 정의가 없어 danger/warning/success 시맨틱 토큰으로 매핑한다.
const STATUS_META = {
  DRAFT: {
    label: '초안',
    badgeClass: 'bg-neutral-100 text-body-light dark:bg-surface-dark-2 dark:text-body-dark',
  },
  PENDING_REVIEW: { label: '심사중', badgeClass: 'bg-warning/10 text-warning' },
  PUBLISHED: { label: '운영중', badgeClass: 'bg-success/10 text-success' },
  ARCHIVED: {
    label: '보관됨',
    badgeClass: 'bg-neutral-100 text-body-light dark:bg-surface-dark-2 dark:text-body-dark',
  },
  REJECTED: { label: '반려됨', badgeClass: 'bg-danger/10 text-danger' },
}
function statusMeta(status) {
  return STATUS_META[status] ?? STATUS_META.DRAFT
}

const TABS = [
  { key: 'projects', label: '내 프로젝트 관리' },
  { key: 'activity', label: '활동 내역/통계' },
  { key: 'daily', label: '일일 설정' },
  { key: 'account', label: '계정 설정' },
]

const sortedProjects = computed(() => {
  const list = [...projects.value]
  if (sortKey.value === 'popular') {
    return list.sort((a, b) => (b.likeCount ?? 0) - (a.likeCount ?? 0))
  }
  return list.sort(
    (a, b) => new Date(b.publishedAt ?? 0).getTime() - new Date(a.publishedAt ?? 0).getTime(),
  )
})

// 백엔드가 실제로 제공하는 필드만 집계한다 — 방문자/조회수/설문 응답 같은 값은
// CreatorDetailResponse/ProjectItem에 없어서 만들어 낼 수 없다 (스코프 합의 사항).
const stats = computed(() => {
  const list = projects.value
  const publishedCount = list.filter((project) => project.status === 'PUBLISHED').length
  return [
    {
      key: 'sites',
      label: '등록한 사이트',
      value: `${list.length}개`,
      hint: `공개 ${publishedCount} · 심사 ${list.length - publishedCount}`,
    },
    {
      key: 'likes',
      label: '총 좋아요',
      value: `${list.reduce((sum, project) => sum + (project.likeCount ?? 0), 0)}회`,
    },
    {
      key: 'favorites',
      label: '총 즐겨찾기',
      value: `${list.reduce((sum, project) => sum + (project.favoriteCount ?? 0), 0)}회`,
    },
    {
      key: 'subscribers',
      label: '구독자',
      value: `${creator.value?.subscriberCount ?? 0}명`,
    },
  ]
})

async function loadCreator() {
  if (!creatorId.value) {
    errorMessage.value = '로그인이 필요합니다.'
    isLoading.value = false
    return
  }
  isLoading.value = true
  errorMessage.value = ''
  try {
    creator.value = await getCreator(creatorId.value)
    const items = isMine.value ? await getMyProjects() : creator.value.projects
    projects.value = items

    if (isMine.value) {
      const rankings = await getProjectRankings()
      rankByProjectId.value = new Map(rankings.map((entry) => [entry.projectId, entry.rank]))
    }
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function toggleSubscription() {
  if (!auth.isLoggedIn) {
    await router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (isSaving.value) return
  isSaving.value = true
  try {
    const result = await toggleCreatorSubscription(creatorId.value)
    creator.value.subscribedByMe = result.subscribed
    creator.value.subscriberCount += result.subscribed ? 1 : -1
  } finally {
    isSaving.value = false
  }
}

async function submitDraft(project) {
  if (isSaving.value) return
  isSaving.value = true
  errorMessage.value = ''
  try {
    const result = await submitProject(project.id)
    project.status = result.status
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

async function removeProject(project) {
  if (isSaving.value) return
  isSaving.value = true
  errorMessage.value = ''
  try {
    await deleteProject(project.id)
    projects.value = projects.value.filter((item) => item.id !== project.id)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

// 프로필 수정 — 기존에는 MyPageView가 별도 폼으로 들고 있었으나, 새 디자인은
// 이 화면의 "프로필 수정" 버튼 하나로 통일한다 (중복 폼 제거).
const isEditingProfile = ref(false)
const isSavingProfile = ref(false)
const profileMessage = ref('')
const profileForm = reactive({ displayName: '', theme: 'LIGHT', newProjectNotifications: true })

watchEffect(() => {
  if (!isEditingProfile.value && auth.user) {
    profileForm.displayName = auth.user.displayName ?? ''
    profileForm.theme = auth.user.theme ?? 'LIGHT'
    profileForm.newProjectNotifications = auth.user.newProjectNotifications ?? true
  }
})

async function saveProfile() {
  isSavingProfile.value = true
  profileMessage.value = ''
  try {
    await auth.saveProfile({
      display_name: profileForm.displayName,
      theme: profileForm.theme,
      new_project_notifications: profileForm.newProjectNotifications,
    })
    isEditingProfile.value = false
    await loadCreator()
  } catch (error) {
    profileMessage.value = error.message
  } finally {
    isSavingProfile.value = false
  }
}

onMounted(loadCreator)
watch(creatorId, loadCreator)
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-14" aria-labelledby="developer-detail-heading">
    <p v-if="isLoading" class="py-24 text-center text-sm text-body-light dark:text-body-dark">
      프로필을 불러오는 중입니다.
    </p>
    <div v-else-if="errorMessage" class="py-24 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <RouterLink to="/login" class="mt-4 inline-block font-semibold text-primary-600"
        >로그인하기</RouterLink
      >
    </div>
    <template v-else>
      <header
        class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm dark:border-divider/30 dark:bg-surface-dark-1"
      >
        <div class="flex flex-wrap items-center gap-5">
          <img
            v-if="creator.avatarUrl"
            :src="creator.avatarUrl"
            :alt="creator.displayName"
            class="h-20 w-20 rounded-full object-cover"
          />
          <span
            v-else
            class="grid h-20 w-20 place-items-center rounded-full bg-primary-50 text-xl font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >
            {{ creator.displayName.slice(0, 1) }}
          </span>

          <form
            v-if="isMine && isEditingProfile"
            class="grid min-w-0 flex-1 gap-3 sm:grid-cols-3"
            @submit.prevent="saveProfile"
          >
            <label class="text-sm font-semibold sm:col-span-3"
              >표시 이름
              <input
                v-model="profileForm.displayName"
                required
                maxlength="100"
                class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2 text-sm dark:border-divider/30 dark:bg-surface-dark-2"
            /></label>
            <label class="text-sm font-semibold"
              >테마
              <select
                v-model="profileForm.theme"
                class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2 text-sm dark:border-divider/30 dark:bg-surface-dark-2"
              >
                <option value="LIGHT">라이트</option>
                <option value="DARK">다크</option>
                <option value="SYSTEM">시스템</option>
              </select></label
            >
            <label
              class="flex items-center gap-2 self-end rounded-lg border border-divider/20 px-3 py-2 text-sm dark:border-divider/30"
              ><input v-model="profileForm.newProjectNotifications" type="checkbox" />신규
              알림</label
            >
            <div class="flex items-center gap-3 sm:col-span-3">
              <button
                type="submit"
                :disabled="isSavingProfile"
                class="rounded-lg bg-primary-600 px-4 py-2 text-sm font-bold text-white"
              >
                {{ isSavingProfile ? '저장 중...' : '저장' }}
              </button>
              <button
                type="button"
                class="text-sm font-semibold text-body-light dark:text-body-dark"
                @click="isEditingProfile = false"
              >
                취소
              </button>
              <p v-if="profileMessage" role="alert" class="text-xs text-danger">
                {{ profileMessage }}
              </p>
            </div>
          </form>

          <div v-else class="min-w-0 flex-1">
            <h1
              id="developer-detail-heading"
              class="font-headline text-2xl font-extrabold text-heading-light dark:text-heading-dark"
            >
              {{ creator.displayName }}
            </h1>
            <p class="mt-2 text-sm text-body-light dark:text-body-dark">
              구독자 {{ creator.subscriberCount }}명 · {{ isMine ? '등록한' : '공개' }} 프로젝트
              {{ projects.length }}개
            </p>
          </div>

          <div v-if="isMine && !isEditingProfile" class="flex shrink-0 flex-wrap gap-3">
            <RouterLink
              to="/projects/new"
              class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
            >
              + 새 사이트 등록하기
            </RouterLink>
            <button
              type="button"
              class="rounded-lg border border-divider/20 px-5 py-3 text-sm font-bold text-heading-light dark:border-divider/30 dark:text-heading-dark"
              @click="isEditingProfile = true"
            >
              프로필 수정
            </button>
          </div>
          <button
            v-else-if="!isMine"
            type="button"
            :aria-pressed="creator.subscribedByMe"
            class="shrink-0 rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
            @click="toggleSubscription"
          >
            {{ creator.subscribedByMe ? '구독 중' : '구독하기' }}
          </button>
        </div>
      </header>

      <template v-if="isMine">
        <div class="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <div
            v-for="stat in stats"
            :key="stat.key"
            class="rounded-xl border border-divider/20 bg-white p-4 dark:border-divider/30 dark:bg-surface-dark-1"
          >
            <p class="text-xs font-semibold text-body-light dark:text-body-dark">
              {{ stat.label }}
            </p>
            <p
              class="mt-1 font-headline text-xl font-extrabold text-heading-light dark:text-heading-dark"
            >
              {{ stat.value }}
            </p>
            <p v-if="stat.hint" class="mt-1 text-xs text-body-light dark:text-body-dark">
              {{ stat.hint }}
            </p>
          </div>
        </div>

        <div class="mt-8 lg:grid lg:grid-cols-[1fr_320px] lg:items-start lg:gap-8">
          <div class="min-w-0">
            <nav
              class="flex gap-5 border-b border-divider/20 dark:border-divider/30"
              role="tablist"
            >
              <button
                v-for="tab in TABS"
                :key="tab.key"
                type="button"
                role="tab"
                :aria-selected="activeTab === tab.key"
                class="-mb-px border-b-2 px-1 pb-3 text-sm font-bold"
                :class="
                  activeTab === tab.key
                    ? 'border-primary-600 text-primary-700 dark:text-primary-200'
                    : 'border-transparent text-body-light dark:text-body-dark'
                "
                @click="activeTab = tab.key"
              >
                {{ tab.label }}
                <span v-if="tab.key === 'projects'">{{ projects.length }}</span>
              </button>
            </nav>

            <template v-if="activeTab === 'projects'">
              <div class="mt-4 flex items-center justify-end">
                <label class="flex items-center gap-2 text-xs text-body-light dark:text-body-dark">
                  정렬
                  <select
                    v-model="sortKey"
                    class="rounded-lg border border-divider/20 px-2 py-1 dark:border-divider/30 dark:bg-surface-dark-2"
                  >
                    <option value="recent">최신 등록순</option>
                    <option value="popular">좋아요순</option>
                  </select>
                </label>
              </div>

              <p
                v-if="projects.length === 0"
                class="py-16 text-center text-sm text-body-light dark:text-body-dark"
              >
                등록된 프로젝트가 없습니다.
              </p>
              <ul v-else class="mt-4 flex flex-col gap-4">
                <li
                  v-for="project in sortedProjects"
                  :key="project.id"
                  class="rounded-xl border border-divider/20 bg-white p-5 dark:border-divider/30 dark:bg-surface-dark-1"
                >
                  <div class="flex items-start gap-4">
                    <img
                      v-if="project.thumbnailUrl"
                      :src="project.thumbnailUrl"
                      :alt="project.title"
                      class="h-12 w-12 shrink-0 rounded-lg object-cover"
                    />
                    <span
                      v-else
                      class="grid h-12 w-12 shrink-0 place-items-center rounded-lg bg-primary-50 font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
                    >
                      {{ project.title.slice(0, 1) }}
                    </span>

                    <div class="min-w-0 flex-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <h3
                          class="font-headline font-bold text-heading-light dark:text-heading-dark"
                        >
                          {{ project.title }}
                        </h3>
                        <span
                          class="rounded-full px-2 py-0.5 text-xs font-semibold"
                          :class="statusMeta(project.status).badgeClass"
                        >
                          {{ statusMeta(project.status).label }}
                        </span>
                        <span
                          v-if="rankByProjectId.get(project.id)"
                          class="rounded-full bg-rank-gold/20 px-2 py-0.5 text-xs font-semibold text-heading-light dark:text-heading-dark"
                        >
                          랭킹 {{ rankByProjectId.get(project.id) }}위
                        </span>
                        <span
                          v-if="project.categoryName"
                          class="rounded-full bg-neutral-100 px-2 py-0.5 text-xs text-body-light dark:bg-surface-dark-2 dark:text-body-dark"
                        >
                          {{ project.categoryName }}
                        </span>
                      </div>

                      <p
                        v-if="project.description"
                        class="mt-2 line-clamp-2 text-sm text-body-light dark:text-body-dark"
                      >
                        {{ project.description }}
                      </p>

                      <div v-if="project.tags?.length" class="mt-2 flex flex-wrap gap-2">
                        <span
                          v-for="tag in project.tags"
                          :key="tag"
                          class="text-xs text-primary-700 dark:text-primary-200"
                          >#{{ tag }}</span
                        >
                      </div>

                      <p
                        v-if="project.status === 'PENDING_REVIEW'"
                        class="mt-3 rounded-lg bg-warning/10 px-3 py-2 text-xs text-warning"
                      >
                        심사 중입니다. 검수는 통상 1~2일 정도 소요됩니다.
                      </p>

                      <div class="mt-3 flex flex-wrap items-center gap-3 text-xs">
                        <RouterLink
                          :to="`/projects/${project.id}`"
                          class="font-semibold text-primary-700 dark:text-primary-200"
                          >관리/통계 보기</RouterLink
                        >
                        <RouterLink
                          :to="`/projects/${project.id}/edit`"
                          class="font-semibold text-primary-700 dark:text-primary-200"
                          >정보 수정</RouterLink
                        >
                        <button
                          v-if="project.status === 'DRAFT'"
                          type="button"
                          class="font-semibold text-primary-700 dark:text-primary-200"
                          @click="submitDraft(project)"
                        >
                          검토 요청
                        </button>
                        <button
                          type="button"
                          class="font-semibold text-danger"
                          @click="removeProject(project)"
                        >
                          삭제
                        </button>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
            </template>
            <p v-else class="py-16 text-center text-sm text-body-light dark:text-body-dark">
              준비 중인 기능입니다.
            </p>
          </div>

          <aside class="mt-8 flex flex-col gap-4 lg:mt-0">
            <div class="rounded-xl bg-primary-600 p-5 text-white">
              <h2 class="font-headline text-sm font-extrabold">인디 메이커 성장 지원 안내</h2>
              <p class="mt-2 text-xs text-primary-100">
                Click HUB 커뮤니티와 튜토리얼에서 프로젝트를 더 많은 사용자에게 알릴 수 있는 방법을
                확인해보세요.
              </p>
              <RouterLink to="/tutorials" class="mt-3 inline-block text-xs font-bold underline">
                자세히 보기 →
              </RouterLink>
            </div>

            <nav
              class="rounded-xl border border-divider/20 bg-white p-4 dark:border-divider/30 dark:bg-surface-dark-1"
            >
              <h2
                class="px-1 pb-2 font-headline text-sm font-extrabold text-heading-light dark:text-heading-dark"
              >
                마이 액션 센터
              </h2>
              <RouterLink
                to="/favorites"
                class="block rounded-lg px-1 py-2 text-sm text-body-light hover:text-primary-600 dark:text-body-dark"
                >즐겨찾기 보관함</RouterLink
              >
              <RouterLink
                to="/following"
                class="block rounded-lg px-1 py-2 text-sm text-body-light hover:text-primary-600 dark:text-body-dark"
                >내 팔로잉 관리</RouterLink
              >
              <RouterLink
                to="/community"
                class="block rounded-lg px-1 py-2 text-sm text-body-light hover:text-primary-600 dark:text-body-dark"
                >커뮤니티 바로가기</RouterLink
              >
            </nav>
          </aside>
        </div>
      </template>

      <section v-else class="mt-8">
        <h2 class="font-headline text-xl font-extrabold text-heading-light dark:text-heading-dark">
          공개 프로젝트
          <span class="text-sm text-primary-700 dark:text-primary-200"
            >{{ projects.length }}건</span
          >
        </h2>
        <p
          v-if="projects.length === 0"
          class="py-16 text-center text-sm text-body-light dark:text-body-dark"
        >
          등록된 프로젝트가 없습니다.
        </p>
        <div v-else class="mt-5 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <SiteCard
            v-for="project in projects.map(toSiteCardProject)"
            :key="project.id"
            :project="project"
          />
        </div>
      </section>
    </template>
  </section>
</template>
