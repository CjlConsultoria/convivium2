import { describe, it, expect } from 'vitest'
import { usePagination } from './usePagination'

describe('usePagination', () => {
  it('inicializa com valores padrão', () => {
    const { page, size, totalElements, totalPages, hasPrev, hasNext } = usePagination()
    expect(page.value).toBe(0)
    expect(size.value).toBe(20)
    expect(totalElements.value).toBe(0)
    expect(totalPages.value).toBe(0)
    expect(hasPrev.value).toBe(false)
    expect(hasNext.value).toBe(false)
  })

  it('aceita size inicial customizado', () => {
    const { size } = usePagination(10)
    expect(size.value).toBe(10)
  })

  it('atualiza estado a partir de PageResponse', () => {
    const { page, size, totalElements, totalPages, updateFromResponse } = usePagination()
    updateFromResponse({
      content: [1, 2, 3],
      page: 1,
      size: 10,
      totalElements: 25,
      totalPages: 3,
      last: false,
    })
    expect(page.value).toBe(1)
    expect(size.value).toBe(10)
    expect(totalElements.value).toBe(25)
    expect(totalPages.value).toBe(3)
  })

  it('nextPage incrementa quando há próxima página', () => {
    const { page, totalPages, updateFromResponse, nextPage } = usePagination()
    updateFromResponse({
      content: [],
      page: 0,
      size: 10,
      totalElements: 25,
      totalPages: 3,
      last: false,
    })
    nextPage()
    expect(page.value).toBe(1)
    nextPage()
    expect(page.value).toBe(2)
    nextPage()
    expect(page.value).toBe(2)
  })

  it('prevPage decrementa quando há página anterior', () => {
    const { page, updateFromResponse, prevPage, nextPage } = usePagination()
    updateFromResponse({
      content: [],
      page: 0,
      size: 10,
      totalElements: 25,
      totalPages: 3,
      last: false,
    })
    nextPage()
    nextPage()
    prevPage()
    expect(page.value).toBe(1)
    prevPage()
    expect(page.value).toBe(0)
    prevPage()
    expect(page.value).toBe(0)
  })

  it('goToPage navega para página válida', () => {
    const { page, updateFromResponse, goToPage } = usePagination()
    updateFromResponse({
      content: [],
      page: 0,
      size: 10,
      totalElements: 25,
      totalPages: 3,
      last: false,
    })
    goToPage(2)
    expect(page.value).toBe(2)
    goToPage(-1)
    expect(page.value).toBe(2)
    goToPage(10)
    expect(page.value).toBe(2)
  })
})
