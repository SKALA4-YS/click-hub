<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { mockFollowing } from '@/data/mockFollowing'

const auth = useAuthStore()
const following = ref([...mockFollowing])

function unfollow(creatorId) {
  following.value = following.value.filter((item) => item.creator_id !== creatorId)
}
</script>

<template>
  <div v-if="!auth.isLoggedIn" class="py-16 text-center text-sm text-neutral-500">
    로그인이 필요합니다.
    <RouterLink to="/login" class="text-primary-600 hover:underline">로그인하러 가기</RouterLink>
  </div>

  <div v-else class="flex flex-col gap-4">
    <h1 class="text-xl font-bold">팔로잉 관리</h1>

    <p v-if="following.length === 0" class="text-sm text-neutral-500">
      구독 중인 제작자가 없습니다.
    </p>
    <ul v-else class="flex flex-col divide-y divide-neutral-200 dark:divide-neutral-800">
      <li
        v-for="creator in following"
        :key="creator.creator_id"
        class="flex items-center justify-between py-3"
      >
        <div class="flex items-center gap-3">
          <span
            class="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100 text-sm font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >
            {{ creator.avatar_initial }}
          </span>
          <div>
            <p class="font-medium">{{ creator.display_name }}</p>
            <p class="text-xs text-neutral-500">활성 프로젝트 {{ creator.active_projects }}개</p>
          </div>
        </div>
        <button
          type="button"
          class="rounded-full border border-neutral-200 px-3 py-1.5 text-xs font-medium hover:bg-neutral-100 dark:border-neutral-700 dark:hover:bg-neutral-800"
          @click="unfollow(creator.creator_id)"
        >
          구독 해제
        </button>
      </li>
    </ul>
  </div>
</template>
