import apiClient from '@/api/client'
import type {
  ApiResponse,
  PageRequest,
  PageResponse,
  Notification,
  NotificationPreference,
} from '@/types'

export async function getNotifications(
  params?: PageRequest,
): Promise<ApiResponse<PageResponse<Notification>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Notification>>>(
    '/notifications',
    { params },
  )
  return response.data
}

export async function markAsRead(id: number): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(`/notifications/${id}/read`)
  return response.data
}

export async function markAllAsRead(): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>('/notifications/read-all')
  return response.data
}

export async function getUnreadCount(): Promise<ApiResponse<{ count: number }>> {
  const response = await apiClient.get<ApiResponse<{ count: number }>>(
    '/notifications/unread-count',
  )
  return response.data
}

export async function getPreferences(): Promise<ApiResponse<NotificationPreference[]>> {
  const response = await apiClient.get<ApiResponse<NotificationPreference[]>>(
    '/notifications/preferences',
  )
  return response.data
}

export async function updatePreferences(
  prefs: NotificationPreference[],
): Promise<ApiResponse<void>> {
  const response = await apiClient.put<ApiResponse<void>>('/notifications/preferences', prefs)
  return response.data
}
