export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface UserInfo {
  id: number
  uuid: string
  email: string
  name: string
  phone: string | null
  photoUrl: string | null
  isPlatformAdmin: boolean
  condominiumRoles: CondominiumRoleInfo[]
}

export interface CondominiumRoleInfo {
  condominiumId: number
  condominiumName: string
  role: string
  status?: string
  unitId: number | null
  unitIdentifier: string | null
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  cpf: string
  phone: string
  condominiumId: number
  unitId: number
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface ForgotPasswordRequest {
  email: string
}

export interface ResetPasswordRequest {
  token: string
  newPassword: string
}
