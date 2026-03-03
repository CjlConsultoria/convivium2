import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as dashboardApi from './dashboard.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn() },
}))

describe('dashboard.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('getDashboardStats chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { totalMoradores: 10, denunciasAbertas: 2, encomendasPendentes: 1, reservasHoje: 0 } },
    } as any)
    await dashboardApi.getDashboardStats(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/dashboard/stats')
  })

  it('getUnitActivity chama GET com params', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await dashboardApi.getUnitActivity(1, 1, 20)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/dashboard/activity', { params: { page: 1, size: 20 } })
  })
})
