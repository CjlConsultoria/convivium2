import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Condominium } from '@/types'
import * as condominiumApi from '@/api/modules/condominium.api'
import { STORAGE_KEYS } from '@/utils/constants'

export const useTenantStore = defineStore('tenant', () => {
  // ---------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------
  const storedId = localStorage.getItem(STORAGE_KEYS.CURRENT_CONDO_ID)
  const currentCondominiumId = ref<number | null>(storedId ? Number(storedId) : null)
  const currentCondominium = ref<Condominium | null>(null)

  // ---------------------------------------------------------------------------
  // Getters
  // ---------------------------------------------------------------------------
  const hasSelectedCondominium = computed<boolean>(() => currentCondominiumId.value !== null)

  // ---------------------------------------------------------------------------
  // Actions
  // ---------------------------------------------------------------------------
  async function setCondominium(condoId: number): Promise<void> {
    currentCondominiumId.value = condoId
    localStorage.setItem(STORAGE_KEYS.CURRENT_CONDO_ID, String(condoId))

    try {
      const response = await condominiumApi.getCondominiumSummary(condoId)
      currentCondominium.value = response.data
    } catch {
      currentCondominium.value = null
    }
  }

  function clearCondominium(): void {
    currentCondominiumId.value = null
    currentCondominium.value = null
    localStorage.removeItem(STORAGE_KEYS.CURRENT_CONDO_ID)
  }

  return {
    // State
    currentCondominiumId,
    currentCondominium,
    // Getters
    hasSelectedCondominium,
    // Actions
    setCondominium,
    clearCondominium,
  }
})
