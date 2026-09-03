<script setup>
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import { formatRelativeTime } from '@/utils/formatRelativeTime'

const auth = useAuthStore()
const notifications = useNotificationStore()
</script>

<template>
  <div v-if="!auth.isLoggedIn" class="py-16 text-center text-sm text-neutral-500">
    로그인이 필요합니다.
    <RouterLink to="/login" class="text-primary-600 hover:underline">로그인하러 가기</RouterLink>
  </div>

  <div v-else class="flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <h1 class="font-headline text-xl font-bold">알림</h1>
      <button
        type="button"
        class="text-sm text-primary-600 hover:underline"
        @click="notifications.markAllRead"
      >
        전체 읽음 처리
      </button>
    </div>

    <p v-if="notifications.items.length === 0" class="text-sm text-neutral-500">알림이 없습니다.</p>
    <ul v-else class="flex flex-col divide-y divide-neutral-200 dark:divide-neutral-800">
      <li v-for="notification in notifications.items" :key="notification.id">
        <RouterLink
          :to="notification.detail_path"
          class="flex items-start gap-3 py-3"
          :class="notification.read_at === null && 'bg-primary-50/60 dark:bg-primary-950/40'"
          @click="notifications.markRead(notification.id)"
        >
          <span
            class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-100 text-sm font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
          >
            {{ notification.thumbnail_initial }}
          </span>
          <span class="flex-1 text-sm">
            <span class="font-medium">{{ notification.creator_name }}</span
            >님이 <span class="font-medium">{{ notification.project_title }}</span
            >를 게시했어요.
            <span class="mt-0.5 block text-xs text-neutral-500">{{
              formatRelativeTime(notification.created_at)
            }}</span>
          </span>
        </RouterLink>
      </li>
    </ul>
  </div>
</template>
