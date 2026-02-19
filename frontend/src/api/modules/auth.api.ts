import apiClient from '@/api/client'
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ResetPasswordRequest,
  UserInfo,
} from '@/types'

export async function login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', data)
  return response.data
}

export async function register(data: RegisterRequest): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>('/auth/register', data)
  return response.data
}

export async function refreshToken(refreshToken: string): Promise<ApiResponse<LoginResponse>> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/refresh', {
    refreshToken,
  })
  return response.data
}

export async function forgotPassword(email: string): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>('/auth/forgot-password', { email })
  return response.data
}

export async function resetPassword(data: ResetPasswordRequest): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>('/auth/reset-password', data)
  return response.data
}

export async function getMe(): Promise<ApiResponse<UserInfo>> {
  const response = await apiClient.get<ApiResponse<UserInfo>>('/auth/me')
  return response.data
}

export interface UpdateMyProfileRequest {
  name?: string
  phone?: string
}

export async function updateMyProfile(
  data: UpdateMyProfileRequest,
): Promise<ApiResponse<UserInfo>> {
  const response = await apiClient.patch<ApiResponse<UserInfo>>('/auth/me', data)
  return response.data
}

export interface CondominiumOption {
  id: number
  name: string
}

export interface UnitOption {
  id: number
  identifier: string
  buildingName: string | null
}

export async function listCondominiumsForRegistration(): Promise<ApiResponse<CondominiumOption[]>> {
  const response = await apiClient.get<ApiResponse<CondominiumOption[]>>('/auth/condominiums')
  return response.data
}

export async function listUnitsForRegistration(condominiumId: number): Promise<ApiResponse<UnitOption[]>> {
  const response = await apiClient.get<ApiResponse<UnitOption[]>>(
    `/auth/condominiums/${condominiumId}/units`,
  )
  return response.data
}

export interface GoogleAuthResponse {
  needsRegistration: boolean
  email?: string
  name?: string
  picture?: string
  accessToken?: string
  refreshToken?: string
  expiresIn?: number
  user?: import('@/types').UserInfo
}

export async function googleLogin(idToken: string): Promise<ApiResponse<GoogleAuthResponse>> {
  const response = await apiClient.post<ApiResponse<GoogleAuthResponse>>('/auth/google', {
    idToken,
  })
  return response.data
}

export interface RegisterGoogleResponse {
  needsApproval: boolean
  message?: string
  login?: import('@/types').LoginResponse
}

export interface PendingRegistrationStatusResponse {
  hasPendingApproval: boolean
  message?: string
  condominiumName?: string
}

export async function checkPendingRegistration(idToken: string): Promise<
  ApiResponse<PendingRegistrationStatusResponse>
> {
  const response = await apiClient.post<ApiResponse<PendingRegistrationStatusResponse>>(
    '/auth/check-pending-registration',
    { idToken },
  )
  return response.data
}

export async function registerGoogle(data: {
  idToken: string
  condominiumId: number
  unitId: number
  phone?: string
}): Promise<ApiResponse<RegisterGoogleResponse>> {
  const response = await apiClient.post<ApiResponse<RegisterGoogleResponse>>(
    '/auth/register-google',
    data,
  )
  return response.data
}
