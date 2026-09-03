import { defineStore } from 'pinia'

import { getNotifications, markNotificationRead } from '@/api/notifications'

function toNotificationViewModel(notification) {
  return {
    ...notification,
    creator_name: notification.creatorName,
    project_title: notification.projectTitle,
    created_at: notification.publishedAt,
    read_at: notification.readAt,
    detail_path: notification.projectId ? `/projects/${notification.projectId}` : '/notifications',
    thumbnail_initial: notification.projectTitle?.slice(0, 1) || 'N',
  }
}

export const useNotificationStore = defineStore('notifications', {
  state: () => ({ items: [], loading: false, loaded: false, error: null }),
  getters: {
    unreadCount: (state) => state.items.filter((item) => item.read_at === null).length,
  },
  actions: {
    async load({ force = false } = {}) {
      if ((this.loaded && !force) || this.loading) return this.items
      this.loading = true
      this.error = null
      try {
        this.items = (await getNotifications()).map(toNotificationViewModel)
        this.loaded = true
        return this.items
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    async markRead(id) {
      const target = this.items.find((item) => item.id === id)
      if (!target || target.read_at !== null) return
      const result = await markNotificationRead(id)
      target.readAt = result.readAt
      target.read_at = result.readAt
    },
    async markAllRead() {
      await Promise.all(
        this.items.filter((item) => item.read_at === null).map((item) => this.markRead(item.id)),
      )
    },
    reset() {
      this.items = []
      this.loaded = false
      this.error = null
    },
  },
})
