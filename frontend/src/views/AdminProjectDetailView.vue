<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import { approveProject, getAdminProjectDetail, rejectProject } from '@/api/admin'

const STATUS_LABEL = { PENDING_REVIEW: '미정', PUBLISHED: '승인', REJECTED: '거절' }
const STATUS_BADGE_CLASS = {
  PENDING_REVIEW: 'bg-warning/10 text-warning',
  PUBLISHED: 'bg-success/10 text-success',
  REJECTED: 'bg-danger/10 text-danger',
}
const PRICING_LABEL = { UNKNOWN: '미정', FREE: '무료', FREEMIUM: '부분 유료', PAID: '유료' }

const route = useRoute()
const router = useRouter()
const project = ref(null)
const isLoading = ref(true)
const errorMessage = ref('')
const isActing = ref(false)
const actionMessage = ref('')
const isRejecting = ref(false)
const rejectReason = ref('')

const canAct = computed(() => project.value?.status === 'PENDING_REVIEW')

async function loadDetail() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    project.value = await getAdminProjectDetail(route.params.id)
  } catch (error) {
    if (error.status === 403) {
      router.replace('/')
      return
    }
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function approve() {
  if (isActing.value) return
  isActing.value = true
  errorMessage.value = ''
  try {
    const result = await approveProject(route.params.id)
    project.value.status = result.status
    actionMessage.value = '승인되었습니다. 공개 피드에 노출됩니다.'
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isActing.value = false
  }
}

async function confirmReject() {
  if (!rejectReason.value.trim() || isActing.value) return
  isActing.value = true
  errorMessage.value = ''
  try {
    const result = await rejectProject(route.params.id, rejectReason.value.trim())
    project.value.status = result.status
    actionMessage.value = '거절되었습니다.'
    isRejecting.value = false
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isActing.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <p v-if="isLoading" class="mx-auto max-w-[900px] py-28 text-center text-sm text-body-light">
    불러오는 중입니다.
  </p>
  <div v-else-if="errorMessage && !project" class="mx-auto max-w-[900px] py-28 text-center">
    <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
    <RouterLink to="/admin/projects" class="mt-4 inline-block font-semibold text-primary-600"
      >목록으로 돌아가기</RouterLink
    >
  </div>

  <section v-else-if="project" class="mx-auto max-w-[900px] space-y-6 pb-14">
    <header class="border-b border-divider/20 pb-6">
      <RouterLink to="/admin/projects" class="text-sm font-semibold text-primary-600"
        >← 목록으로</RouterLink
      >
      <div class="mt-3 flex flex-wrap items-center gap-2">
        <span
          :class="STATUS_BADGE_CLASS[project.status]"
          class="rounded-full px-2 py-1 text-xs font-semibold"
          >{{ STATUS_LABEL[project.status] ?? project.status }}</span
        >
        <h1 class="font-headline text-3xl font-extrabold">{{ project.title }}</h1>
      </div>
      <p class="mt-2 text-sm text-body-light">
        {{ project.ownerName }}이(가) 등록한 프로젝트입니다.
      </p>
    </header>

    <p v-if="actionMessage" role="status" class="text-sm font-semibold text-success">
      {{ actionMessage }}
    </p>
    <p v-if="errorMessage" role="alert" class="text-sm text-danger">{{ errorMessage }}</p>

    <section class="grid gap-5 rounded-2xl border border-divider/20 bg-white p-6 sm:grid-cols-2">
      <div>
        <p class="text-sm font-semibold">대표 카테고리</p>
        <p class="mt-2 text-sm text-body-light">{{ project.categoryName ?? '미지정' }}</p>
      </div>
      <div>
        <p class="text-sm font-semibold">가격 정책</p>
        <p class="mt-2 text-sm text-body-light">
          {{ PRICING_LABEL[project.pricing] ?? project.pricing }}
        </p>
      </div>
      <div class="sm:col-span-2">
        <p class="text-sm font-semibold">프로젝트 소개</p>
        <p class="mt-2 whitespace-pre-wrap text-sm text-body-light">{{ project.description }}</p>
      </div>
      <div>
        <p class="text-sm font-semibold">서비스 URL</p>
        <a
          :href="project.siteUrl"
          target="_blank"
          rel="noopener"
          class="mt-2 block break-all text-sm text-primary-600 hover:underline"
          >{{ project.siteUrl }} ↗</a
        >
      </div>
      <div>
        <p class="text-sm font-semibold">GitHub 저장소 URL</p>
        <a
          v-if="project.repositoryUrl"
          :href="project.repositoryUrl"
          target="_blank"
          rel="noopener"
          class="mt-2 block break-all text-sm text-primary-600 hover:underline"
          >{{ project.repositoryUrl }} ↗</a
        >
        <p v-else class="mt-2 text-sm text-body-light">미등록</p>
      </div>
      <div class="sm:col-span-2">
        <p class="text-sm font-semibold">검색 태그</p>
        <div class="mt-2 flex flex-wrap gap-2">
          <span
            v-for="tag in project.tags"
            :key="tag"
            class="rounded-full bg-primary-50 px-3 py-1 text-xs"
            >#{{ tag }}</span
          >
          <span v-if="!project.tags?.length" class="text-sm text-body-light">없음</span>
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-divider/20 bg-white p-6">
      <h2 class="font-headline text-lg font-bold">기술 스택</h2>
      <div class="mt-3 flex flex-wrap gap-2">
        <span
          v-for="tech in project.techStacks"
          :key="tech.technologySlug"
          class="rounded-full border border-divider/20 px-3 py-1.5 text-xs"
          >{{ tech.technologyName }}</span
        >
        <span v-if="!project.techStacks?.length" class="text-sm text-body-light">없음</span>
      </div>
    </section>

    <section v-if="canAct" class="rounded-2xl bg-primary-50 p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <p class="text-sm font-semibold">이 프로젝트를 승인하거나 거절할 수 있습니다.</p>
        <div class="flex gap-2">
          <button
            type="button"
            :disabled="isActing"
            class="rounded-lg bg-success px-5 py-2.5 text-sm font-bold text-white disabled:opacity-50"
            @click="approve"
          >
            승인
          </button>
          <button
            type="button"
            :disabled="isActing"
            class="rounded-lg border border-danger px-5 py-2.5 text-sm font-bold text-danger disabled:opacity-50"
            @click="isRejecting = !isRejecting"
          >
            거절
          </button>
        </div>
      </div>
      <div v-if="isRejecting" class="mt-4 flex flex-wrap items-center gap-2">
        <input
          v-model="rejectReason"
          placeholder="거절 사유를 입력하세요"
          class="min-w-64 flex-1 rounded-lg border border-divider/20 px-3 py-2 text-sm"
        />
        <button
          type="button"
          :disabled="isActing"
          class="rounded-lg bg-danger px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
          @click="confirmReject"
        >
          거절 확정
        </button>
      </div>
    </section>
  </section>
</template>
