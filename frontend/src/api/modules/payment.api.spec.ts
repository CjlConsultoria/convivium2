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

  it('createEmbeddedCheckout chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { clientSecret: 'cs_test_123' } } } as any)
    await paymentApi.createEmbeddedCheckout(1)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/payment/embedded-checkout', {})
  })

  it('createEmbeddedCheckout com planId chama POST com planId', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { clientSecret: 'cs_test_123' } } } as any)
    await paymentApi.createEmbeddedCheckout(1, 3)
    expect(apiClient.post).toHaveBeenCalledWith('/condos/1/payment/embedded-checkout', { planId: 3 })
  })

  it('getStripeKey chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { publishableKey: 'pk_test_abc' } } } as any)
    await paymentApi.getStripeKey(1)
    expect(apiClient.get).toHaveBeenCalledWith('/condos/1/payment/stripe-key')
  })
})
