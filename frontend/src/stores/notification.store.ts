import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Notification } from '@/types'
import * as notificationApi from '@/api/modules/notification.api'

export const useNotificationStore = defineStore('notification', () => {
  // ---------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const loading = ref(false)

  // ---------------------------------------------------------------------------
  // Actions
  // ---------------------------------------------------------------------------
  async function fetchNotifications(): Promise<void> {
    loading.value = true
    try {
      const response = await notificationApi.getNotifications({ page: 0, size: 50 })
      notifications.value = response.data.content
    } finally {
      loading.value = false
    }
  }

  async function fetchUnreadCount(): Promise<void> {
    try {
      const response = await notificationApi.getUnreadCount()
      unreadCount.value = response.data.count
    } catch {
      // Silently fail -- count will remain stale
    }
  }

  async function markAsRead(id: number): Promise<void> {
    await notificationApi.markAsRead(id)

    const notification = notifications.value.find((n) => n.id === id)
    if (notification && !notification.isRead) {
      notification.isRead = true
      notification.readAt = new Date().toISOString()
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  async function markAllAsRead(): Promise<void> {
    await notificationApi.markAllAsRead()

    notifications.value.forEach((n) => {
      if (!n.isRead) {
        n.isRead = true
        n.readAt = new Date().toISOString()
      }
    })
    unreadCount.value = 0
  }

  /**
   * Add a notification received via WebSocket push.
   * Prepends to the list and increments the unread counter.
   */
  function addNotification(notification: Notification): void {
    notifications.value.unshift(notification)
    if (!notification.isRead) {
      unreadCount.value += 1
    }
  }

  return {
    // State
    notifications,
    unreadCount,
    loading,
    // Actions
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    addNotification,
  }
})
