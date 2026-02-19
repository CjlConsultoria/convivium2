export type Role = 'SINDICO' | 'SUB_SINDICO' | 'CONSELHEIRO' | 'PORTEIRO' | 'ZELADOR' | 'FAXINEIRA' | 'MORADOR'

export const ROLE_LABELS: Record<Role, string> = {
  SINDICO: 'Sindico',
  SUB_SINDICO: 'Sub-Sindico',
  CONSELHEIRO: 'Conselheiro',
  PORTEIRO: 'Porteiro',
  ZELADOR: 'Zelador',
  FAXINEIRA: 'Faxineira',
  MORADOR: 'Morador',
}

export interface User {
  id: number
  uuid: string
  email: string
  name: string
  cpf: string | null
  phone: string | null
  photoUrl: string | null
  isPlatformAdmin: boolean
  isActive: boolean
  emailVerified: boolean
  createdAt: string
}

export interface UserDetail extends User {
  condominiumRoles: UserCondominiumRole[]
}

export interface UserCondominiumRole {
  id: number
  condominiumId: number
  condominiumName: string
  role: Role
  unitId: number | null
  unitIdentifier: string | null
  status: string
  approvedAt: string | null
}

export interface UserCreateRequest {
  name: string
  email: string
  password: string
  cpf: string | null
  phone: string | null
  role: Role
  unitId: number | null
}

export interface UserUpdateRequest {
  name: string
  phone: string | null
  photoUrl: string | null
  unitId?: number | null
}
