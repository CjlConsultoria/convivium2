import apiClient from '@/api/client'
import type { ApiResponse } from '@/types'

export interface PlatformInvoice {
  id: number
  referenceMonth: string
  referenceDisplay: string
  amountCents: number
  status: string
  paidAt: string | null
  createdAt: string
}

export async function createCheckoutSession(
  condoId: number,
  planId?: number,
): Promise<ApiResponse<{ url: string }>> {
  const response = await apiClient.post<ApiResponse<{ url: string }>>(
    `/condos/${condoId}/payment/checkout-session`,
    planId != null ? { planId } : {},
  )
  return response.data
}

export async function listInvoices(
  condoId: number,
): Promise<ApiResponse<PlatformInvoice[]>> {
  const response = await apiClient.get<ApiResponse<PlatformInvoice[]>>(
    `/condos/${condoId}/payment/invoices`,
  )
  return response.data
}
