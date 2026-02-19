export type ComplaintCategory = 'NOISE' | 'MAINTENANCE' | 'SECURITY' | 'PARKING' | 'PET' | 'COMMON_AREA' | 'OTHER'
export type ComplaintStatus = 'OPEN' | 'IN_REVIEW' | 'RESPONDED' | 'RESOLVED' | 'CLOSED'

export const COMPLAINT_CATEGORY_LABELS: Record<ComplaintCategory, string> = {
  NOISE: 'Barulho',
  MAINTENANCE: 'Manutencao',
  SECURITY: 'Seguranca',
  PARKING: 'Estacionamento',
  PET: 'Animais',
  COMMON_AREA: 'Area Comum',
  OTHER: 'Outros',
}

export const COMPLAINT_STATUS_LABELS: Record<ComplaintStatus, string> = {
  OPEN: 'Aberta',
  IN_REVIEW: 'Em Analise',
  RESPONDED: 'Respondida',
  RESOLVED: 'Resolvida',
  CLOSED: 'Encerrada',
}

export const COMPLAINT_STATUS_COLORS: Record<ComplaintStatus, string> = {
  OPEN: 'badge-blue',
  IN_REVIEW: 'badge-yellow',
  RESPONDED: 'badge-green',
  RESOLVED: 'badge-green',
  CLOSED: 'badge-gray',
}

export interface Complaint {
  id: number
  condominiumId: number
  complainantName: string | null
  isAnonymous: boolean
  category: ComplaintCategory
  title: string
  description: string
  unitIdentifier: string | null
  status: ComplaintStatus
  priority: string
  createdAt: string
  updatedAt: string
}

export interface ComplaintDetail extends Complaint {
  responses: ComplaintResponseItem[]
  attachments: ComplaintAttachment[]
}

export interface ComplaintResponseItem {
  id: number
  responderName: string
  responderRole: string
  message: string
  isInternal: boolean
  createdAt: string
}

export interface ComplaintAttachment {
  id: number
  fileName: string
  fileUrl: string
  fileType: string
  createdAt: string
}

export interface ComplaintCreateRequest {
  category: ComplaintCategory
  title: string
  description: string
  isAnonymous: boolean
  unitId: number | null
  priority: string
}

export interface ComplaintResponseRequest {
  message: string
  isInternal: boolean
}
