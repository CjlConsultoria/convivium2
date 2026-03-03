import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as userApi from './user.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn(), patch: vi.fn() },
}))

describe('user.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listUsers chama GET /condos/:id/users', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({
      data: { success: true, data: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } },
    } as any)
    await userApi.listUsers(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/users', { params: undefined })
  })

  it('createUser chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await userApi.createUser(1, { name: 'User', email: 'a@b.com', role: 'MORADOR' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/users', expect.any(Object))
  })

  it('getUser chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await userApi.getUser(1, 2)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/users/2')
  })

  it('updateUser chama PUT', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await userApi.updateUser(1, 2, { name: 'New' } as any)
    expect(apiClient.put).toHaveBeenCalledWith('/condos/1/users/2', expect.any(Object))
  })

  it('deleteUser chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await userApi.deleteUser(1, 2)
    expect(apiClient.delete).toHaveBeenCalledWith('/condos/1/users/2')
  })
})
