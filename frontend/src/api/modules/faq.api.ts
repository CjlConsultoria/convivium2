import apiClient from '@/api/client'
import type { ApiResponse } from '@/types'

export interface FaqCategory {
  id: number
  name: string
  sortOrder: number
  active: boolean
  items: FaqItem[]
}

export interface FaqItem {
  id: number
  categoryId: number
  categoryName?: string
  question: string
  answer: string
  sortOrder: number
  published: boolean
}

// ---- Publico (leitura) ----

export async function listFaq(): Promise<ApiResponse<FaqCategory[]>> {
  const response = await apiClient.get<ApiResponse<FaqCategory[]>>('/faq')
  return response.data
}

// ---- Admin CRUD ----

export async function adminListCategories(): Promise<ApiResponse<FaqCategory[]>> {
  const response = await apiClient.get<ApiResponse<FaqCategory[]>>('/admin/faq/categories')
  return response.data
}

export async function adminCreateCategory(data: {
  name: string
  sortOrder?: number
}): Promise<ApiResponse<FaqCategory>> {
  const response = await apiClient.post<ApiResponse<FaqCategory>>('/admin/faq/categories', data)
  return response.data
}

export async function adminUpdateCategory(
  id: number,
  data: { name: string; sortOrder?: number; active?: boolean },
): Promise<ApiResponse<FaqCategory>> {
  const response = await apiClient.put<ApiResponse<FaqCategory>>(`/admin/faq/categories/${id}`, data)
  return response.data
}

export async function adminDeleteCategory(id: number): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(`/admin/faq/categories/${id}`)
  return response.data
}

export async function adminListItems(): Promise<ApiResponse<FaqItem[]>> {
  const response = await apiClient.get<ApiResponse<FaqItem[]>>('/admin/faq/items')
  return response.data
}

export async function adminCreateItem(data: {
  categoryId: number
  question: string
  answer: string
  sortOrder?: number
}): Promise<ApiResponse<FaqItem>> {
  const response = await apiClient.post<ApiResponse<FaqItem>>('/admin/faq/items', data)
  return response.data
}

export async function adminUpdateItem(
  id: number,
  data: { categoryId?: number; question?: string; answer?: string; sortOrder?: number; published?: boolean },
): Promise<ApiResponse<FaqItem>> {
  const response = await apiClient.put<ApiResponse<FaqItem>>(`/admin/faq/items/${id}`, data)
  return response.data
}

export async function adminDeleteItem(id: number): Promise<ApiResponse<void>> {
  const response = await apiClient.delete<ApiResponse<void>>(`/admin/faq/items/${id}`)
  return response.data
}
