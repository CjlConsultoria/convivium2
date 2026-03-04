import apiClient from '@/api/client'
import type { ApiResponse } from '@/types'

export interface SupportTicket {
  id: number
  subject: string
  status: string
  userName: string
  condominiumName: string | null
  unitLabel: string | null
  roleName: string | null
  createdAt: string
  updatedAt: string
  unreadCount?: number
}

export interface SupportMessage {
  id: number
  ticketId: number
  senderId: number
  senderName: string
  message: string
  fromAdmin: boolean
  read: boolean
  createdAt: string
}

// ---- Usuario ----

export async function createTicket(subject: string): Promise<ApiResponse<SupportTicket>> {
  const response = await apiClient.post<ApiResponse<SupportTicket>>('/support/tickets', { subject })
  return response.data
}

export async function listMyTickets(): Promise<ApiResponse<SupportTicket[]>> {
  const response = await apiClient.get<ApiResponse<SupportTicket[]>>('/support/tickets')
  return response.data
}

export async function getTicket(ticketId: number): Promise<ApiResponse<SupportTicket>> {
  const response = await apiClient.get<ApiResponse<SupportTicket>>(`/support/tickets/${ticketId}`)
  return response.data
}

export async function getTicketMessages(ticketId: number): Promise<ApiResponse<SupportMessage[]>> {
  const response = await apiClient.get<ApiResponse<SupportMessage[]>>(`/support/tickets/${ticketId}/messages`)
  return response.data
}

export async function sendMessage(ticketId: number, message: string): Promise<ApiResponse<SupportMessage>> {
  const response = await apiClient.post<ApiResponse<SupportMessage>>(`/support/tickets/${ticketId}/messages`, {
    message,
  })
  return response.data
}

// ---- Admin ----

export async function adminListTickets(
  status?: string,
  page = 0,
  size = 20,
): Promise<ApiResponse<{ content: SupportTicket[]; totalElements: number }>> {
  const params: Record<string, string | number> = { page, size }
  if (status) params.status = status
  const response = await apiClient.get<ApiResponse<{ content: SupportTicket[]; totalElements: number }>>(
    '/admin/support/tickets',
    { params },
  )
  return response.data
}

export async function adminGetTicket(ticketId: number): Promise<ApiResponse<SupportTicket>> {
  const response = await apiClient.get<ApiResponse<SupportTicket>>(`/admin/support/tickets/${ticketId}`)
  return response.data
}

export async function adminGetTicketMessages(ticketId: number): Promise<ApiResponse<SupportMessage[]>> {
  const response = await apiClient.get<ApiResponse<SupportMessage[]>>(`/admin/support/tickets/${ticketId}/messages`)
  return response.data
}

export async function adminSendMessage(ticketId: number, message: string): Promise<ApiResponse<SupportMessage>> {
  const response = await apiClient.post<ApiResponse<SupportMessage>>(`/admin/support/tickets/${ticketId}/messages`, {
    message,
  })
  return response.data
}

export async function adminUpdateTicketStatus(
  ticketId: number,
  status: string,
): Promise<ApiResponse<SupportTicket>> {
  const response = await apiClient.patch<ApiResponse<SupportTicket>>(`/admin/support/tickets/${ticketId}/status`, {
    status,
  })
  return response.data
}
