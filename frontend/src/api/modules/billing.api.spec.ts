import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as billingApi from './billing.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
}))

describe('billing.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('listCondominiums chama GET /admin/billing/condominiums', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await billingApi.listCondominiums()
    expect(apiClient.get).toHaveBeenCalledWith('/admin/billing/condominiums')
  })

  it('blockCondominium chama PATCH com blockType', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await billingApi.blockCondominium(1, 'PAYMENT', 'Inadimplente')
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/billing/condominiums/1/block', {
      blockType: 'PAYMENT',
      reason: 'Inadimplente',
    })
  })

  it('blockCondominium sem motivo chama PATCH sem reason', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await billingApi.blockCondominium(2, 'GENERAL')
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/billing/condominiums/2/block', {
      blockType: 'GENERAL',
      reason: undefined,
    })
  })

  it('unblockCondominium chama PATCH /unblock', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true } } as any)
    await billingApi.unblockCondominium(1)
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/billing/condominiums/1/unblock')
  })

  it('listCondoInvoices chama GET com id', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await billingApi.listCondoInvoices(5)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/billing/condominiums/5/invoices')
  })
})
