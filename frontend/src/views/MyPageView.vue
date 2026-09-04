<script setup>
import { reactive, ref, watchEffect } from 'vue'
import { RouterLink } from 'vue-router'

import DeveloperDetailView from '@/views/DeveloperDetailView.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const isEditingProfile = ref(false)
const isSaving = ref(false)
const profileMessage = ref('')
const profile = reactive({ displayName: '', theme: 'LIGHT', newProjectNotifications: true })

watchEffect(() => {
  if (!isEditingProfile.value && auth.user) {
    profile.displayName = auth.user.displayName ?? ''
    profile.theme = auth.user.theme ?? 'LIGHT'
    profile.newProjectNotifications = auth.user.newProjectNotifications ?? true
  }
})

async function saveProfile() {
  isSaving.value = true
  profileMessage.value = ''
  try {
    await auth.saveProfile({
      display_name: profile.displayName,
      theme: profile.theme,
      new_project_notifications: profile.newProjectNotifications,
    })
    profileMessage.value = '프로필이 저장되었습니다.'
    isEditingProfile.value = false
  } catch (error) {
    profileMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light dark:text-body-dark">
      마이페이지를 확인하려면 로그인이 필요합니다.
    </p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>
  <div v-else>
    <section
      class="mx-auto mb-6 max-w-[1120px] rounded-xl border border-divider/20 bg-white p-5 dark:border-divider/30 dark:bg-surface-dark-1"
    >
      <div class="flex items-center justify-between gap-4">
        <div>
          <h1 class="font-headline text-lg font-bold text-heading-light dark:text-heading-dark">
            프로필 설정
          </h1>
          <p
            v-if="profileMessage"
            role="status"
            class="mt-1 text-xs text-body-light dark:text-body-dark"
          >
            {{ profileMessage }}
          </p>
        </div>
        <button
          type="button"
          class="text-sm font-semibold text-primary-700"
          @click="isEditingProfile = !isEditingProfile"
        >
          {{ isEditingProfile ? '취소' : '수정' }}
        </button>
      </div>
      <form
        v-if="isEditingProfile"
        class="mt-4 grid gap-4 sm:grid-cols-3"
        @submit.prevent="saveProfile"
      >
        <label class="text-sm font-semibold"
          >표시 이름<input
            v-model="profile.displayName"
            required
            maxlength="100"
            class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
        /></label>
        <label class="text-sm font-semibold"
          >테마<select
            v-model="profile.theme"
            class="mt-2 w-full rounded-lg border border-divider/20 px-3 py-2"
          >
            <option value="LIGHT">라이트</option>
            <option value="DARK">다크</option>
            <option value="SYSTEM">시스템</option>
          </select></label
        >
        <label
          class="flex items-center gap-2 self-end rounded-lg border border-divider/20 px-3 py-2 text-sm"
          ><input v-model="profile.newProjectNotifications" type="checkbox" />신규 프로젝트
          알림</label
        >
        <button
          type="submit"
          :disabled="isSaving"
          class="rounded-lg bg-primary-600 px-5 py-2 text-sm font-bold text-white sm:col-span-3"
        >
          {{ isSaving ? '저장 중...' : '프로필 저장' }}
        </button>
      </form>
    </section>
    <DeveloperDetailView />
  </div>
</template>
