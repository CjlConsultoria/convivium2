import apiClient from '@/api/client'
import type {
  ApiResponse,
  PageRequest,
  PageResponse,
  Parcel,
  ParcelCreateRequest,
  ParcelDetail,
  ParcelVerifyRequest,
} from '@/types'

export async function listParcels(
  condoId: number,
  params?: PageRequest & { status?: string },
): Promise<ApiResponse<PageResponse<Parcel>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Parcel>>>(
    `/condos/${condoId}/parcels`,
    { params },
  )
  return response.data
}

export async function createParcel(
  condoId: number,
  data: ParcelCreateRequest,
): Promise<ApiResponse<Parcel>> {
  const response = await apiClient.post<ApiResponse<Parcel>>(
    `/condos/${condoId}/parcels`,
    data,
  )
  return response.data
}

export async function getParcel(
  condoId: number,
  id: number,
): Promise<ApiResponse<ParcelDetail>> {
  const response = await apiClient.get<ApiResponse<ParcelDetail>>(
    `/condos/${condoId}/parcels/${id}`,
  )
  return response.data
}

export async function uploadPhoto(
  condoId: number,
  parcelId: number,
  file: File,
  photoType: string,
): Promise<ApiResponse<void>> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('photoType', photoType)

  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/parcels/${parcelId}/photos`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  )
  return response.data
}

export async function generateCode(
  condoId: number,
  parcelId: number,
): Promise<ApiResponse<{ pickupCode: string; residentCode: string }>> {
  const response = await apiClient.post<
    ApiResponse<{ pickupCode: string; residentCode: string }>
  >(`/condos/${condoId}/parcels/${parcelId}/generate-code`)
  return response.data
}

export async function verifyPickup(
  condoId: number,
  parcelId: number,
  data: ParcelVerifyRequest,
): Promise<ApiResponse<void>> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/condos/${condoId}/parcels/${parcelId}/verify`,
    data,
  )
  return response.data
}

export async function getMyParcels(
  condoId: number,
  params?: PageRequest,
): Promise<ApiResponse<PageResponse<Parcel>>> {
  const response = await apiClient.get<ApiResponse<PageResponse<Parcel>>>(
    `/condos/${condoId}/parcels/mine`,
    { params },
  )
  return response.data
}
