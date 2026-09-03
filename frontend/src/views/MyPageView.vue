<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useTheme } from '@/composables/useTheme'
import { mockMyProjects } from '@/data/mockMyProjects'

const auth = useAuthStore()
const { mode, setMode } = useTheme()

const activeTab = ref('projects')
const displayNameDraft = ref(auth.user?.display_name ?? '')

const statusLabel = {
  DRAFT: '작성중',
  PENDING_REVIEW: '심사중',
  PUBLISHED: '운영중',
  REJECTED: '반려됨',
  ARCHIVED: '보관됨',
}

function saveProfile() {
  auth.updateProfile({ display_name: displayNameDraft.value })
}
</script>

<template>
  <div v-if="!auth.isLoggedIn" class="py-16 text-center text-sm text-neutral-500">
    로그인이 필요합니다.
    <RouterLink to="/login" class="text-primary-600 hover:underline">로그인하러 가기</RouterLink>
  </div>

  <div v-else class="flex flex-col gap-6">
    <section
      class="flex items-center gap-4 rounded-xl border border-neutral-200 p-5 dark:border-neutral-800"
    >
      <div
        class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-primary-100 text-2xl font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
      >
        {{ auth.user.avatar_initial }}
      </div>
      <div>
        <h1 class="text-lg font-bold">{{ auth.user.display_name }}</h1>
        <p class="text-sm text-neutral-500">등록한 프로젝트 {{ mockMyProjects.length }}개</p>
      </div>
    </section>

    <nav class="flex gap-2 border-b border-neutral-200 dark:border-neutral-800">
      <button
        type="button"
        class="border-b-2 px-3 py-2 text-sm font-medium"
        :class="
          activeTab === 'projects'
            ? 'border-primary-600 text-primary-600'
            : 'border-transparent text-neutral-500'
        "
        @click="activeTab = 'projects'"
      >
        내 프로젝트
      </button>
      <button
        type="button"
        class="border-b-2 px-3 py-2 text-sm font-medium"
        :class="
          activeTab === 'settings'
            ? 'border-primary-600 text-primary-600'
            : 'border-transparent text-neutral-500'
        "
        @click="activeTab = 'settings'"
      >
        계정 설정
      </button>
    </nav>

    <section v-if="activeTab === 'projects'" class="flex flex-col gap-3">
      <p v-if="mockMyProjects.length === 0" class="text-sm text-neutral-500">
        등록한 프로젝트가 없습니다.
      </p>
      <div
        v-for="project in mockMyProjects"
        :key="project.id"
        class="flex items-center justify-between rounded-lg border border-neutral-200 p-4 dark:border-neutral-800"
      >
        <div>
          <p class="font-medium">{{ project.title }}</p>
          <p class="mt-1 text-xs text-neutral-500">
            주간 방문자 {{ project.weekly_visitors.toLocaleString() }} · 좋아요
            {{ project.unique_likes }}
          </p>
        </div>
        <span class="rounded-full bg-neutral-100 px-3 py-1 text-xs font-medium dark:bg-neutral-800">
          {{ statusLabel[project.status] }}
        </span>
      </div>
    </section>

    <section v-else class="flex max-w-md flex-col gap-6">
      <div>
        <label class="mb-1 block text-sm font-medium">닉네임</label>
        <div class="flex gap-2">
          <input
            v-model="displayNameDraft"
            type="text"
            class="w-full rounded-lg border border-neutral-200 px-3 py-2 text-sm outline-none focus:border-primary-400 dark:border-neutral-700 dark:bg-neutral-900"
          />
          <button
            type="button"
            class="shrink-0 rounded-lg bg-primary-600 px-4 py-2 text-sm font-semibold text-white hover:bg-primary-700"
            @click="saveProfile"
          >
            저장
          </button>
        </div>
      </div>

      <div>
        <label class="mb-1 block text-sm font-medium">테마</label>
        <div class="flex gap-2">
          <button
            v-for="option in ['light', 'dark', 'system']"
            :key="option"
            type="button"
            class="rounded-full border px-3 py-1.5 text-sm"
            :class="
              mode === option
                ? 'border-primary-500 bg-primary-600 text-white'
                : 'border-neutral-200 dark:border-neutral-800'
            "
            @click="setMode(option)"
          >
            {{ option === 'light' ? '라이트' : option === 'dark' ? '다크' : '시스템' }}
          </button>
        </div>
      </div>

      <label class="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          :checked="auth.user.new_project_notifications"
          @change="auth.updateProfile({ new_project_notifications: $event.target.checked })"
        />
        구독한 제작자의 신규 프로젝트 알림 받기
      </label>
    </section>
  </div>
</template>
