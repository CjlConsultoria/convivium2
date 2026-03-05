import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as faqApi from './faq.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn(), delete: vi.fn() },
}))

describe('faq.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ---- Public ----

  it('listFaq chama GET /faq', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await faqApi.listFaq()
    expect(apiClient.get).toHaveBeenCalledWith('/faq')
  })

  // ---- Admin Categories ----

  it('adminListCategories chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await faqApi.adminListCategories()
    expect(apiClient.get).toHaveBeenCalledWith('/admin/faq/categories')
  })

  it('adminCreateCategory chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await faqApi.adminCreateCategory({ name: 'Nova', sortOrder: 1 })
    expect(apiClient.post).toHaveBeenCalledWith('/admin/faq/categories', { name: 'Nova', sortOrder: 1 })
  })

  it('adminUpdateCategory chama PUT', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await faqApi.adminUpdateCategory(1, { name: 'Atualizada', sortOrder: 2, active: false })
    expect(apiClient.put).toHaveBeenCalledWith('/admin/faq/categories/1', {
      name: 'Atualizada',
      sortOrder: 2,
      active: false,
    })
  })

  it('adminDeleteCategory chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await faqApi.adminDeleteCategory(1)
    expect(apiClient.delete).toHaveBeenCalledWith('/admin/faq/categories/1')
  })

  // ---- Admin Items ----

  it('adminListItems chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await faqApi.adminListItems()
    expect(apiClient.get).toHaveBeenCalledWith('/admin/faq/items')
  })

  it('adminCreateItem chama POST', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await faqApi.adminCreateItem({ categoryId: 1, question: 'Q?', answer: 'A', sortOrder: 0 })
    expect(apiClient.post).toHaveBeenCalledWith('/admin/faq/items', {
      categoryId: 1,
      question: 'Q?',
      answer: 'A',
      sortOrder: 0,
    })
  })

  it('adminUpdateItem chama PUT', async () => {
    vi.mocked(apiClient.put).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await faqApi.adminUpdateItem(1, { question: 'New Q?', published: false })
    expect(apiClient.put).toHaveBeenCalledWith('/admin/faq/items/1', {
      question: 'New Q?',
      published: false,
    })
  })

  it('adminDeleteItem chama DELETE', async () => {
    vi.mocked(apiClient.delete).mockResolvedValue({ data: { success: true } } as any)
    await faqApi.adminDeleteItem(1)
    expect(apiClient.delete).toHaveBeenCalledWith('/admin/faq/items/1')
  })
})
