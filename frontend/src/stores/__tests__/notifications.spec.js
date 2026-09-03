import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const api = vi.hoisted(() => ({ getNotifications: vi.fn(), markNotificationRead: vi.fn() }))
vi.mock('@/api/notifications', () => api)

import { useNotificationStore } from '@/stores/notifications'

describe('notification store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    api.getNotifications.mockReset().mockResolvedValue([
      {
        id: 1,
        creatorName: 'Maker',
        projectTitle: 'Click HUB',
        projectId: 'project-1',
        publishedAt: '2026-09-04T00:00:00Z',
        readAt: null,
      },
    ])
    api.markNotificationRead
      .mockReset()
      .mockResolvedValue({ id: 1, readAt: '2026-09-04T01:00:00Z' })
  })

  it('loads and adapts server notifications', async () => {
    const store = useNotificationStore()
    await store.load()
    expect(store.unreadCount).toBe(1)
    expect(store.items[0]).toMatchObject({
      creator_name: 'Maker',
      detail_path: '/projects/project-1',
    })
  })

  it('persists the read state through the backend', async () => {
    const store = useNotificationStore()
    await store.load()
    await store.markRead(1)
    expect(api.markNotificationRead).toHaveBeenCalledWith(1)
    expect(store.unreadCount).toBe(0)
  })
})
