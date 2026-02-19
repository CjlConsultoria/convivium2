import apiClient from '@/api/client'
import type {
  ApiResponse,
  PageRequest,
  PageResponse,
  User,
  UserCreateRequest,
  UserDetail,
  UserUpdateRequest,
} from '@/types'

export async function listUsers(
  condoId: number,
  params?: PageRequest,
): Promise<ApiResponse<PageResponse<User>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<User>>>(
    `/condos/${condoId}/users`,
    { params },
  )
  return response.data
}

export async function createUser(
  condoId: number,
  data: UserCreateRequest,
): Promise<ApiResponse<User>> {
  const response = await apiClient.post<ApiResponse<User>>(
    `/condos/${condoId}/users`,
    data,
  )
  return response.data
}

export async function getUser(
  condoId: number,
  userId: number,
): Promise<ApiResponse<UserDetail>> {
  const response = await apiClient.get<ApiResponse<UserDetail>>(
    `/condos/${condoId}/users/${userId}`,
  )
  return response.data
}

export async function updateUser(
  condoId: number,
  userId: number,
  data: UserUpdateRequest,
): Promise<ApiResponse<User>> {
  const response = await apiClient.put<ApiResponse<User>>(
    `/condos/${condoId}/users/${userId}`,
    data,
  )
  return response.data
}

export async function deleteUser(
  condoId: number,
  userId: number,
): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(
    `/condos/${condoId}/users/${userId}`,
  )
  return response.data
}

export async function getPendingApprovals(condoId: number): Promise<ApiResponse<User[]>> {
  const response = await apiClient.get<ApiResponse<User[]>>(
    `/condos/${condoId}/users/pending`,
  )
  return response.data
}

export async function approveUser(
  condoId: number,
  userId: number,
  body?: { unitId?: number },
): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(
    `/condos/${condoId}/users/${userId}/approve`,
    body ?? {},
  )
  return response.data
}

export async function rejectUser(
  condoId: number,
  userId: number,
): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/users/${userId}/reject`,
  )
  return response.data
}

// ---- Admin-level user listing (all platform users) ----

export async function listAllUsers(
  params?: PageRequest,
): Promise<ApiResponse<PageResponse<User>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<User>>>(
    '/admin/users',
    { params },
  )
  return response.data
}
