export interface Notification {
  id: number
  title: string
  message: string
  type: string
  referenceType: string | null
  referenceId: number | null
  channel: string
  isRead: boolean
  readAt: string | null
  createdAt: string
}

export interface NotificationPreference {
  channel: string
  eventType: string
  isEnabled: boolean
}
