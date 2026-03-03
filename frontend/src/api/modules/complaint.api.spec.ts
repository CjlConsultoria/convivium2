import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as complaintApi from './complaint.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), patch: vi.fn() },
}))

describe('complaint.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listComplaints chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await complaintApi.listComplaints(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/complaints', { params: undefined })
  })

  it('createComplaint chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await complaintApi.createComplaint(1, { title: 'T', description: 'D', priority: 'MEDIUM' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/complaints', expect.any(Object))
  })

  it('getComplaint chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await complaintApi.getComplaint(1, 2)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/complaints/2')
  })

  it('addResponse chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await complaintApi.addResponse(1, 2, { message: 'Resposta' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/complaints/2/responses', expect.any(Object))
  })

  it('updateStatus chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await complaintApi.updateStatus(1, 2, 'RESOLVED')
    expect(apiClient.patch).toHaveBeenCalledWith('/condos/1/complaints/2/status', { status: 'RESOLVED' })
  })
})
