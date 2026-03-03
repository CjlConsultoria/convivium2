import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as parcelApi from './parcel.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn() },
}))

describe('parcel.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listParcels chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await parcelApi.listParcels(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/parcels', { params: undefined })
  })

  it('createParcel chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await parcelApi.createParcel(1, { recipientName: 'A', recipientPhone: '11999999999' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/parcels', expect.any(Object))
  })

  it('getParcel chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await parcelApi.getParcel(1, 2)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/parcels/2')
  })
})
