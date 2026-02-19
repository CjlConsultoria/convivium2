export const STORAGE_KEYS = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  CURRENT_CONDO_ID: 'current_condo_id',
  SIDEBAR_COLLAPSED: 'sidebar_collapsed',
  THEME: 'theme',
} as const

export const PRIORITY_LABELS: Record<string, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Media',
  HIGH: 'Alta',
  URGENT: 'Urgente',
}

export const PRIORITY_COLORS: Record<string, string> = {
  LOW: 'badge-gray',
  MEDIUM: 'badge-blue',
  HIGH: 'badge-yellow',
  URGENT: 'badge-red',
}
