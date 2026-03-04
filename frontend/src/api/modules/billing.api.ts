import apiClient from '@/api/client'
import type { ApiResponse } from '@/types'

export interface CondoBilling {
  id: number
  name: string
  slug: string
  planName: string | null
  planPriceCents: number | null
  status: string
  blockType: string | null
  blockedAt: string | null
  blockedReason: string | null
  subscriptionStartedAt: string | null
  createdAt: string
}

export async function listCondominiums(): Promise<ApiResponse<CondoBilling[]>> {
  const response = await apiClient.get<ApiResponse<CondoBilling[]>>('/admin/billing/condominiums')
  return response.data
}

export async function blockCondominium(
  id: number,
  blockType: 'PAYMENT' | 'GENERAL',
  reason?: string,
): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(`/admin/billing/condominiums/${id}/block`, {
    blockType,
    reason,
  })
  return response.data
}

export async function unblockCondominium(id: number): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(`/admin/billing/condominiums/${id}/unblock`)
  return response.data
}

export async function listCondoInvoices(id: number): Promise<ApiResponse<import('./payment.api').PlatformInvoice[]>> {
  const response = await apiClient.get<ApiResponse<import('./payment.api').PlatformInvoice[]>>(
    `/admin/billing/condominiums/${id}/invoices`,
  )
  return response.data
}
