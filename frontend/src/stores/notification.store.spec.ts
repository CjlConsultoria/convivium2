import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useNotificationStore } from './notification.store'
import * as notificationApi from '@/api/modules/notification.api'

vi.mock('@/api/modules/notification.api')

describe('notification.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('fetchNotifications', () => {
    it('carrega notificações da API', async () => {
      const mockResponse = {
        data: {
          content: [{ id: 1, title: 'Test', isRead: false }],
          page: 0,
          size: 50,
          totalElements: 1,
          totalPages: 1,
          last: true,
        },
      }
      vi.mocked(notificationApi.getNotifications).mockResolvedValue(mockResponse as any)

      const store = useNotificationStore()
      await store.fetchNotifications()

      expect(store.notifications).toEqual(mockResponse.data.content)
      expect(store.loading).toBe(false)
    })
  })

  describe('addNotification', () => {
    it('adiciona notificação no início e incrementa unreadCount', () => {
      const store = useNotificationStore()
      store.addNotification({
        id: 1,
        title: 'Nova',
        message: 'Msg',
        isRead: false,
        createdAt: new Date().toISOString(),
      } as any)
      expect(store.notifications).toHaveLength(1)
      expect(store.unreadCount).toBe(1)
    })
  })

  describe('markAllAsRead', () => {
    it('marca todas como lidas', async () => {
      vi.mocked(notificationApi.markAllAsRead).mockResolvedValue({} as any)
      const store = useNotificationStore()
      store.notifications = [
        { id: 1, isRead: false } as any,
        { id: 2, isRead: false } as any,
      ]
      store.unreadCount = 2

      await store.markAllAsRead()

      expect(store.notifications.every((n) => n.isRead)).toBe(true)
      expect(store.unreadCount).toBe(0)
    })
  })
})
