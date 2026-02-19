import { ref, computed } from 'vue'
import type { PageResponse } from '@/types'

/**
 * Composable that manages pagination state compatible with the Spring-style
 * PageResponse returned by the backend API.
 *
 * Usage:
 * ```ts
 * const { page, size, updateFromResponse, nextPage, prevPage } = usePagination()
 * ```
 */
export function usePagination(initialSize: number = 20) {
  const page = ref(0)
  const size = ref(initialSize)
  const totalElements = ref(0)
  const totalPages = ref(0)

  const hasPrev = computed<boolean>(() => page.value > 0)
  const hasNext = computed<boolean>(() => page.value < totalPages.value - 1)

  /**
   * Synchronise local pagination state with the data returned from the API.
   */
  function updateFromResponse(response: PageResponse<unknown>): void {
    page.value = response.page
    size.value = response.size
    totalElements.value = response.totalElements
    totalPages.value = response.totalPages
  }

  function nextPage(): void {
    if (hasNext.value) {
      page.value += 1
    }
  }

  function prevPage(): void {
    if (hasPrev.value) {
      page.value -= 1
    }
  }

  function goToPage(n: number): void {
    if (n >= 0 && n < totalPages.value) {
      page.value = n
    }
  }

  return {
    page,
    size,
    totalElements,
    totalPages,
    hasPrev,
    hasNext,
    updateFromResponse,
    nextPage,
    prevPage,
    goToPage,
  }
}
