import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as condominiumApi from './condominium.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

describe('condominium.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listCondominiums chama GET /admin/condominiums', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await condominiumApi.listCondominiums({ page: 0, size: 20 })
    expect(apiClient.get).toHaveBeenCalledWith('/admin/condominiums', { params: { page: 0, size: 20 } })
  })

  it('getCondominiumSummary chama GET /condos/:id', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { id: 1, name: 'Condo' } } } as any)
    await condominiumApi.getCondominiumSummary(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1')
  })

  it('createCondominium chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await condominiumApi.createCondominium({ name: 'Condo', cnpj: '123', address: 'Rua 1' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/admin/condominiums', expect.any(Object))
  })

  it('deleteCondominium chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.deleteCondominium(1)
    expect(apiClient.delete).toHaveBeenCalledWith('/admin/condominiums/1')
  })

  it('listPlans chama GET /admin/plans', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await condominiumApi.listPlans()
    expect(apiClient.get).toHaveBeenCalledWith('/admin/plans')
  })
})
