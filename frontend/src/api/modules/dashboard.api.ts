import apiClient from '@/api/client'
import type { ApiResponse, PageResponse } from '@/types'

export interface DashboardStats {
  totalMoradores: number
  denunciasAbertas: number
  encomendasPendentes: number
  reservasHoje: number
}

export interface UnitActivityItem {
  type: string
  entityId: number
  title: string
  description: string
  date: string
  badgeLabel: string
}

export async function getDashboardStats(
  condoId: number,
): Promise<ApiResponse<DashboardStats>> {
  const response = await apiClient.get<ApiResponse<DashboardStats>>(
    `/condos/${condoId}/dashboard/stats`,
  )
  return response.data
}

export async function getUnitActivity(
  condoId: number,
  page: number = 0,
  size: number = 10,
): Promise<ApiResponse<PageResponse<UnitActivityItem>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<UnitActivityItem>>>(
    `/condos/${condoId}/dashboard/activity`,
    { params: { page, size } },
  )
  return response.data
}
