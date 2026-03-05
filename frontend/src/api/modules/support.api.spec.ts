import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as supportApi from './support.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
}))

describe('support.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ---- User ----

  it('createTicket chama POST /support/tickets', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.createTicket('Problema')
    expect(apiClient.post).toHaveBeenCalledWith('/support/tickets', { subject: 'Problema' })
  })

  it('listMyTickets chama GET /support/tickets', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await supportApi.listMyTickets()
    expect(apiClient.get).toHaveBeenCalledWith('/support/tickets')
  })

  it('getTicket chama GET /support/tickets/:id', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.getTicket(5)
    expect(apiClient.get).toHaveBeenCalledWith('/support/tickets/5')
  })

  it('getTicketMessages chama GET /support/tickets/:id/messages', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await supportApi.getTicketMessages(5)
    expect(apiClient.get).toHaveBeenCalledWith('/support/tickets/5/messages')
  })

  it('sendMessage chama POST /support/tickets/:id/messages', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.sendMessage(5, 'Ola!')
    expect(apiClient.post).toHaveBeenCalledWith('/support/tickets/5/messages', { message: 'Ola!' })
  })

  // ---- Admin ----

  it('adminListTickets chama GET com params', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { content: [], totalElements: 0 } } } as any)
    await supportApi.adminListTickets('OPEN', 0, 20)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/support/tickets', {
      params: { page: 0, size: 20, status: 'OPEN' },
    })
  })

  it('adminListTickets sem status nao inclui status nos params', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: { content: [], totalElements: 0 } } } as any)
    await supportApi.adminListTickets(undefined, 0, 20)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/support/tickets', {
      params: { page: 0, size: 20 },
    })
  })

  it('adminGetTicket chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.adminGetTicket(3)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/support/tickets/3')
  })

  it('adminGetTicketMessages chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await supportApi.adminGetTicketMessages(3)
    expect(apiClient.get).toHaveBeenCalledWith('/admin/support/tickets/3/messages')
  })

  it('adminSendMessage chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.adminSendMessage(3, 'Resposta')
    expect(apiClient.post).toHaveBeenCalledWith('/admin/support/tickets/3/messages', { message: 'Resposta' })
  })

  it('adminUpdateTicketStatus chama PATCH', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await supportApi.adminUpdateTicketStatus(3, 'RESOLVED')
    expect(apiClient.patch).toHaveBeenCalledWith('/admin/support/tickets/3/status', { status: 'RESOLVED' })
  })
})
