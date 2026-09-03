import { defineStore } from 'pinia'
import { mockNotifications } from '@/data/mockNotifications'

// GET /api/v1/notifications, PATCH /api/v1/notifications/{id}/read 자리 — 현재는 목업 상태만 관리
export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: mockNotifications.map((item) => ({ ...item })),
  }),
  getters: {
    unreadCount: (state) => state.items.filter((item) => item.read_at === null).length,
  },
  actions: {
    markRead(id) {
      const target = this.items.find((item) => item.id === id)
      if (target && target.read_at === null) target.read_at = new Date().toISOString()
    },
    markAllRead() {
      const now = new Date().toISOString()
      this.items.forEach((item) => {
        if (item.read_at === null) item.read_at = now
      })
    },
  },
})
