import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as notificationApi from './notification.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), patch: vi.fn(), put: vi.fn() },
}))

describe('notification.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getNotifications chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 50, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await notificationApi.getNotifications({ page: 0, size: 50 })
    expect(apiClient.get).toHaveBeenCalledWith('/notifications', { params: { page: 0, size: 50 } })
  })

  it('markAsRead chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await notificationApi.markAsRead(1)
    expect(apiClient.patch).toHaveBeenCalledWith('/notifications/1/read')
  })

  it('markAllAsRead chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await notificationApi.markAllAsRead()
    expect(apiClient.patch).toHaveBeenCalledWith('/notifications/read-all')
  })

  it('getUnreadCount chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { count: 5 } } } as any)
    await notificationApi.getUnreadCount()
    expect(apiClient.get).toHaveBeenCalledWith('/notifications/unread-count')
  })
})
