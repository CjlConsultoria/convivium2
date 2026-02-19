import apiClient from '@/api/client'
import type {
  ApiResponse,
  PageRequest,
  PageResponse,
  Condominium,
  CondominiumCreateRequest,
  Building,
  BuildingCreateRequest,
  Unit,
  UnitCreateRequest,
  Plan,
} from '@/types'

// ---- Admin endpoints (PLATFORM_ADMIN) ----

export async function listCondominiums(
  params?: PageRequest,
): Promise<ApiResponse<PageResponse<Condominium>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Condominium>>>('/admin/condominiums', {
    params,
  })
  return response.data
}

export async function createCondominium(
  data: CondominiumCreateRequest,
): Promise<ApiResponse<Condominium>> {
  const response = await apiClient.post<ApiResponse<Condominium>>('/admin/condominiums', data)
  return response.data
}

export async function getCondominium(id: number): Promise<ApiResponse<Condominium>> {
  const response = await apiClient.get<ApiResponse<Condominium>>(`/admin/condominiums/${id}`)
  return response.data
}

/** Dados do condomínio para o tenant (síndico/morador). Inclui plano e valor do plano. */
export async function getCondominiumSummary(condoId: number): Promise<ApiResponse<Condominium>> {
  const response = await apiClient.get<ApiResponse<Condominium>>(`/condos/${condoId}`)
  return response.data
}

export async function updateCondominium(
  id: number,
  data: CondominiumCreateRequest,
): Promise<ApiResponse<Condominium>> {
  const response = await apiClient.put<ApiResponse<Condominium>>(`/admin/condominiums/${id}`, data)
  return response.data
}

export async function deleteCondominium(id: number): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(`/admin/condominiums/${id}`)
  return response.data
}

export async function updateCondominiumStatus(
  id: number,
  status: string,
): Promise<ApiResponse<void>> {
  const response = await apiClient.patch<ApiResponse<void>>(`/admin/condominiums/${id}/status`, {
    status,
  })
  return response.data
}

export async function setCondominiumPlan(
  id: number,
  planId: number | null,
): Promise<ApiResponse<Condominium>> {
  const response = await apiClient.patch<ApiResponse<Condominium>>(
    `/admin/condominiums/${id}/plan`,
    { planId },
  )
  return response.data
}

export async function listPlans(): Promise<ApiResponse<Plan[]>> {
  const response = await apiClient.get<ApiResponse<Plan[]>>('/admin/plans')
  return response.data
}

// ---- Tenant-scoped endpoints (within a condominium) ----

export async function listBuildings(condoId: number): Promise<ApiResponse<Building[]>> {
  const response = await apiClient.get<ApiResponse<Building[]>>(
    `/condos/${condoId}/buildings`,
  )
  return response.data
}

export async function createBuilding(
  condoId: number,
  data: BuildingCreateRequest,
): Promise<ApiResponse<Building>> {
  const response = await apiClient.post<ApiResponse<Building>>(
    `/condos/${condoId}/buildings`,
    data,
  )
  return response.data
}

export async function deleteBuilding(
  condoId: number,
  buildingId: number,
): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(
    `/condos/${condoId}/buildings/${buildingId}`,
  )
  return response.data
}

export async function listUnits(condoId: number): Promise<ApiResponse<Unit[]>> {
  const response = await apiClient.get<ApiResponse<Unit[]>>(`/condos/${condoId}/units`)
  return response.data
}

export async function createUnit(
  condoId: number,
  data: UnitCreateRequest,
): Promise<ApiResponse<Unit>> {
  const response = await apiClient.post<ApiResponse<Unit>>(
    `/condos/${condoId}/units`,
    data,
  )
  return response.data
}

export async function deleteUnit(
  condoId: number,
  unitId: number,
): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(
    `/condos/${condoId}/units/${unitId}`,
  )
  return response.data
}

export interface GenerateStructureRequest {
  blocksCount: number
  unitsPerFloor: number
  floorsPerBlock: number
  /** '1' = 1,2,3... | '01' = 01,02... | '101' = 101,102... (andar + 2 dígitos) */
  identifierFormat?: string
  /** Número em que a sequência começa (ex: 11 → 11,12,13... ou 111,112 no formato 101) */
  identifierStart?: number
}

export interface BuildingPreviewItem {
  name: string
  floors: number
}

export interface UnitPreviewItem {
  buildingName: string
  floor: number
  identifier: string
}

export interface StructurePreviewResponse {
  buildings: BuildingPreviewItem[]
  units: UnitPreviewItem[]
}

export interface ApplyStructureRequest {
  buildings: BuildingPreviewItem[]
  units: UnitPreviewItem[]
}

export async function previewStructure(
  condoId: number,
  data: GenerateStructureRequest,
): Promise<ApiResponse<StructurePreviewResponse>> {
  const response = await apiClient.post<ApiResponse<StructurePreviewResponse>>(
    `/condos/${condoId}/structure/preview`,
    data,
  )
  return response.data
}

export async function applyStructure(
  condoId: number,
  data: ApplyStructureRequest,
): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/structure/apply`,
    data,
  )
  return response.data
}

export async function generateStructure(
  condoId: number,
  data: GenerateStructureRequest,
): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/structure/generate`,
    data,
  )
  return response.data
}
