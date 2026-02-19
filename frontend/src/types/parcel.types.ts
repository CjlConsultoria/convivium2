export type ParcelStatus = 'RECEIVED' | 'NOTIFIED' | 'PICKUP_REQUESTED' | 'VERIFIED' | 'DELIVERED'

export const PARCEL_STATUS_LABELS: Record<ParcelStatus, string> = {
  RECEIVED: 'Recebida',
  NOTIFIED: 'Notificado',
  PICKUP_REQUESTED: 'Retirada Solicitada',
  VERIFIED: 'Verificada',
  DELIVERED: 'Entregue',
}

export const PARCEL_STATUS_COLORS: Record<ParcelStatus, string> = {
  RECEIVED: 'badge-blue',
  NOTIFIED: 'badge-yellow',
  PICKUP_REQUESTED: 'badge-yellow',
  VERIFIED: 'badge-green',
  DELIVERED: 'badge-gray',
}

export interface Parcel {
  id: number
  condominiumId: number
  unitIdentifier: string
  recipientName: string | null
  receivedByName: string
  carrier: string | null
  trackingNumber: string | null
  description: string | null
  status: ParcelStatus
  deliveredAt: string | null
  createdAt: string
}

export interface ParcelDetail extends Parcel {
  pickupCode: string | null
  residentCode: string | null
  photos: ParcelPhoto[]
}

export interface ParcelPhoto {
  id: number
  photoUrl: string
  photoType: string
  createdAt: string
}

export interface ParcelCreateRequest {
  unitId: number
  recipientId: number | null
  carrier: string | null
  trackingNumber: string | null
  description: string | null
}

/** Código que o morador informa ao porteiro na retirada; só existe um código. */
export interface ParcelVerifyRequest {
  code: string
  verificationMethod?: 'CODE_MATCH' | 'QR_SCAN'
}
