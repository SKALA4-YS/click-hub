<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useNotificationStore } from '@/stores/notifications'
import { formatRelativeTime } from '@/utils/formatRelativeTime'

const notifications = useNotificationStore()
const isOpen = ref(false)
const rootEl = ref(null)

function handleOutsideClick(event) {
  if (rootEl.value && !rootEl.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleOutsideClick))
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))

function handleClick(notification) {
  notifications.markRead(notification.id)
  isOpen.value = false
}
</script>

<template>
  <div ref="rootEl" class="relative">
    <button
      type="button"
      class="relative rounded-full p-2 text-lg hover:bg-neutral-100 dark:hover:bg-neutral-800"
      title="알림"
      @click="isOpen = !isOpen"
    >
      🔔
      <span
        v-if="notifications.unreadCount > 0"
        class="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-primary-600 px-1 text-[10px] font-bold text-white"
      >
        {{ notifications.unreadCount }}
      </span>
    </button>

    <div
      v-if="isOpen"
      class="absolute right-0 z-20 mt-2 w-80 rounded-xl border border-neutral-200 bg-white p-2 shadow-lg dark:border-neutral-800 dark:bg-neutral-900"
    >
      <div class="flex items-center justify-between px-2 py-1">
        <p class="text-sm font-semibold">알림</p>
        <button
          type="button"
          class="text-xs text-primary-600 hover:underline"
          @click="notifications.markAllRead"
        >
          전체 읽음
        </button>
      </div>

      <ul class="mt-1 flex max-h-80 flex-col gap-1 overflow-y-auto">
        <li v-if="notifications.items.length === 0" class="px-2 py-4 text-center text-sm text-neutral-500">
          알림이 없습니다.
        </li>
        <li v-for="notification in notifications.items" :key="notification.id">
          <RouterLink
            :to="notification.detail_path"
            class="flex items-start gap-3 rounded-lg p-2 text-sm hover:bg-neutral-100 dark:hover:bg-neutral-800"
            :class="notification.read_at === null && 'bg-primary-50 dark:bg-primary-950'"
            @click="handleClick(notification)"
          >
            <span
              class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-primary-100 text-xs font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
            >
              {{ notification.thumbnail_initial }}
            </span>
            <span class="flex-1">
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
  </div>
</template>
