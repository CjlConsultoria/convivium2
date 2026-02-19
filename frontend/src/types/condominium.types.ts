export type CondominiumStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'

export interface Condominium {
  id: number
  name: string
  slug: string
  cnpj: string | null
  email: string | null
  phone: string | null
  addressStreet: string | null
  addressNumber: string | null
  addressComplement: string | null
  addressNeighborhood: string | null
  addressCity: string | null
  addressState: string | null
  addressZip: string | null
  logoUrl: string | null
  planId: number | null
  planName: string | null
  planPriceCents: number | null
  status: CondominiumStatus
  createdAt: string
}

export interface Plan {
  id: number
  name: string
  slug: string
  priceCents: number
  description: string | null
  maxUnits: number
  maxUsers: number
  active: boolean
}

export interface CondominiumCreateRequest {
  name: string
  cnpj: string | null
  email: string | null
  phone: string | null
  addressStreet: string | null
  addressNumber: string | null
  addressComplement: string | null
  addressNeighborhood: string | null
  addressCity: string | null
  addressState: string | null
  addressZip: string | null
}

export interface Building {
  id: number
  condominiumId: number
  name: string
  floors: number | null
  createdAt: string
}

export interface UnitResidentInfo {
  name: string
  email: string
}

export interface Unit {
  id: number
  condominiumId: number
  buildingId: number | null
  buildingName: string | null
  identifier: string
  floor: number | null
  type: string
  areaSqm: number | null
  isOccupied: boolean
  residents?: UnitResidentInfo[]
}

export interface BuildingCreateRequest {
  name: string
  floors: number | null
}

export interface UnitCreateRequest {
  buildingId: number | null
  identifier: string
  floor: number | null
  type: string
  areaSqm: number | null
}
