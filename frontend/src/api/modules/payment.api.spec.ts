import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as paymentApi from './payment.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn() },
}))

describe('payment.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('createCheckoutSession chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { url: 'https://stripe.com/...' } } } as any)
    await paymentApi.createCheckoutSession(1)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/payment/checkout-session', {})
  })

  it('createCheckoutSession com planId chama POST com planId', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { url: '...' } } } as any)
    await paymentApi.createCheckoutSession(1, 2)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/payment/checkout-session', { planId: 2 })
  })

  it('listInvoices chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await paymentApi.listInvoices(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/payment/invoices')
  })
})
