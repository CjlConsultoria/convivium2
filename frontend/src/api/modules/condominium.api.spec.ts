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

  it('getCondominium chama GET /admin/condominiums/:id', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { id: 1 } } } as any)
    await condominiumApi.getCondominium(1)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/condominiums/1')
  })

  it('updateCondominium chama PUT', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.updateCondominium(1, { name: 'Condo', cnpj: '123', address: 'Rua' } as any)
    expect(apiClient.put).toHaveBeenCalledWith('/admin/condominiums/1', expect.any(Object))
  })

  it('updateCondominiumStatus chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.updateCondominiumStatus(1, 'ACTIVE')
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/condominiums/1/status', { status: 'ACTIVE' })
  })

  it('setCondominiumPlan chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.setCondominiumPlan(1, 2)
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/condominiums/1/plan', { planId: 2 })
  })

  it('listBuildings chama GET /condos/:id/buildings', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await condominiumApi.listBuildings(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/buildings')
  })

  it('createBuilding chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.createBuilding(1, { name: 'Bloco A', floors: 5 } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/buildings', { name: 'Bloco A', floors: 5 })
  })

  it('deleteBuilding chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.deleteBuilding(1, 2)
    expect(apiClient.delete).toHaveBeenCalledWith('/condos/1/buildings/2')
  })

  it('listUnits chama GET /condos/:id/units', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await condominiumApi.listUnits(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/units')
  })

  it('createUnit chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.createUnit(1, { buildingId: 1, floor: 1, identifier: '101' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/units', expect.any(Object))
  })

  it('deleteUnit chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.deleteUnit(1, 2)
    expect(apiClient.delete).toHaveBeenCalledWith('/condos/1/units/2')
  })

  it('previewStructure chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { buildings: [], units: [] } } } as any)
    await condominiumApi.previewStructure(1, { blocksCount: 1, unitsPerFloor: 2, floorsPerBlock: 3 })
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/structure/preview', expect.any(Object))
  })

  it('applyStructure chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.applyStructure(1, { buildings: [], units: [] })
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/structure/apply', { buildings: [], units: [] })
  })

  it('generateStructure chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await condominiumApi.generateStructure(1, { blocksCount: 1, unitsPerFloor: 2, floorsPerBlock: 3 })
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/structure/generate', expect.any(Object))
  })
})
