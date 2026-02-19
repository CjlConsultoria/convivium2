import apiClient from '@/api/client'
import type {
  ApiResponse,
  PageRequest,
  PageResponse,
  Complaint,
  ComplaintCreateRequest,
  ComplaintDetail,
  ComplaintResponseRequest,
} from '@/types'

export async function listComplaints(
  condoId: number,
  params?: PageRequest & { status?: string },
): Promise<ApiResponse<PageResponse<Complaint>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Complaint>>>(
    `/condos/${condoId}/complaints`,
    { params },
  )
  return response.data
}

export async function createComplaint(
  condoId: number,
  data: ComplaintCreateRequest,
): Promise<ApiResponse<Complaint>> {
  const response = await apiClient.post<ApiResponse<Complaint>>(
    `/condos/${condoId}/complaints`,
    data,
  )
  return response.data
}

export async function getComplaint(
  condoId: number,
  id: number,
): Promise<ApiResponse<ComplaintDetail>> {
  const response = await apiClient.get<ApiResponse<ComplaintDetail>>(
    `/condos/${condoId}/complaints/${id}`,
  )
  return response.data
}

export async function addResponse(
  condoId: number,
  complaintId: number,
  data: ComplaintResponseRequest,
): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/complaints/${complaintId}/responses`,
    data,
  )
  return response.data
}

export async function updateStatus(
  condoId: number,
  complaintId: number,
  status: string,
): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(
    `/condos/${condoId}/complaints/${complaintId}/status`,
    { status },
  )
  return response.data
}

export async function getMyComplaints(
  condoId: number,
  params?: PageRequest & { status?: string },
): Promise<ApiResponse<PageResponse<Complaint>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Complaint>>>(
    `/condos/${condoId}/complaints/mine`,
    { params },
  )
  return response.data
}
