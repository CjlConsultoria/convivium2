export interface ApiResponse<T> {
  success: boolean
  data: T
  message: string | null
  errorCode: string | null
  errors: FieldError[] | null
  timestamp: string
}

export interface FieldError {
  field: string
  message: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface PageRequest {
  page?: number
  size?: number
  sort?: string
}
