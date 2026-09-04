<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

import { approveProject, getPendingProjects, rejectProject } from '@/api/admin'

const router = useRouter()

const STATUS_LABEL = { PENDING_REVIEW: '미정' }
const STATUS_BADGE_CLASS = 'bg-warning/10 text-warning'

const projects = ref([])
const isLoading = ref(true)
const errorMessage = ref('')
const actioningId = ref('')
const rejectingId = ref('')
const rejectReason = ref('')

async function loadPending() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    projects.value = await getPendingProjects()
  } catch (error) {
    if (error.status === 403) {
      router.replace('/admin')
      return
    }
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function approve(id) {
  if (actioningId.value) return
  actioningId.value = id
  errorMessage.value = ''
  try {
    await approveProject(id)
    projects.value = projects.value.filter((project) => project.id !== id)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    actioningId.value = ''
  }
}

function startReject(id) {
  rejectingId.value = id
  rejectReason.value = ''
}

async function confirmReject(id) {
  if (!rejectReason.value.trim() || actioningId.value) return
  actioningId.value = id
  errorMessage.value = ''
  try {
    await rejectProject(id, rejectReason.value.trim())
    projects.value = projects.value.filter((project) => project.id !== id)
    rejectingId.value = ''
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    actioningId.value = ''
  }
}

onMounted(loadPending)
</script>

<template>
  <section
    class="mx-auto w-full max-w-[1120px] py-4 sm:py-8"
    aria-labelledby="admin-approval-heading"
  >
    <header class="rounded-2xl border border-divider/15 bg-white px-6 py-7 shadow-sm">
      <p class="text-xs font-semibold text-primary-600">ADMIN</p>
      <h1 id="admin-approval-heading" class="mt-2 font-headline text-2xl font-extrabold">
        게시물 승인 관리
      </h1>
      <p class="mt-2 text-sm text-body-light">
        검토 대기(미정) 상태인 프로젝트만 표시됩니다. 승인하면 즉시 공개 피드에 노출됩니다.
      </p>
    </header>

    <p v-if="isLoading" class="py-16 text-center text-sm text-body-light">불러오는 중입니다.</p>
    <div v-else-if="errorMessage && !projects.length" class="py-16 text-center">
      <p role="alert" class="text-sm text-danger">{{ errorMessage }}</p>
      <button type="button" class="mt-4 font-semibold text-primary-600" @click="loadPending">
        다시 시도
      </button>
    </div>
    <p v-else-if="!projects.length" class="py-16 text-center text-sm text-body-light">
      검토를 기다리는 프로젝트가 없습니다.
    </p>
    <ul v-else class="mt-5 space-y-3">
      <li
        v-for="project in projects"
        :key="project.id"
        class="rounded-xl border border-divider/20 bg-white p-5"
      >
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="flex items-center gap-2">
              <span
                :class="STATUS_BADGE_CLASS"
                class="rounded-full px-2 py-1 text-xs font-semibold"
                >{{ STATUS_LABEL[project.status] ?? project.status }}</span
              >
              <RouterLink
                :to="`/admin/projects/${project.id}`"
                class="truncate font-headline text-lg font-bold hover:underline"
              >
                {{ project.title }}
              </RouterLink>
            </div>
            <p class="mt-1 line-clamp-2 text-sm text-body-light">{{ project.description }}</p>
            <a
              :href="project.siteUrl"
              target="_blank"
              rel="noopener"
              class="mt-1 inline-block truncate text-xs text-primary-600 hover:underline"
              >{{ project.siteUrl }}</a
            >
            <p class="mt-1 text-xs text-body-light">{{ project.ownerName }}</p>
          </div>
          <div class="flex shrink-0 gap-2">
            <button
              type="button"
              :disabled="!!actioningId"
              class="rounded-lg bg-success px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
              @click="approve(project.id)"
            >
              승인
            </button>
            <button
              type="button"
              :disabled="!!actioningId"
              class="rounded-lg border border-danger px-4 py-2 text-sm font-semibold text-danger disabled:opacity-50"
              @click="startReject(project.id)"
            >
              거절
            </button>
          </div>
        </div>

        <div v-if="rejectingId === project.id" class="mt-4 flex flex-wrap items-center gap-2">
          <input
            v-model="rejectReason"
            placeholder="거절 사유를 입력하세요"
            class="min-w-64 flex-1 rounded-lg border border-divider/20 px-3 py-2 text-sm"
          />
          <button
            type="button"
            :disabled="!!actioningId"
            class="rounded-lg bg-danger px-4 py-2 text-sm font-semibold text-white disabled:opacity-50"
            @click="confirmReject(project.id)"
          >
            거절 확정
          </button>
          <button
            type="button"
            class="rounded-lg border border-divider/20 px-4 py-2 text-sm font-semibold"
            @click="rejectingId = ''"
          >
            취소
          </button>
        </div>
      </li>
    </ul>
  </section>
</template>
