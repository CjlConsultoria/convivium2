import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTenantStore } from './tenant.store'
import * as condominiumApi from '@/api/modules/condominium.api'

vi.mock('@/api/modules/condominium.api')

describe('tenant.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('hasSelectedCondominium', () => {
    it('retorna false quando não há condomínio selecionado', () => {
      const store = useTenantStore()
      expect(store.hasSelectedCondominium).toBe(false)
    })
  })

  describe('setCondominium', () => {
    it('atualiza ID e busca dados do condomínio', async () => {
      const mockCondo = { id: 1, name: 'Condo Test' }
      vi.mocked(condominiumApi.getCondominiumSummary).mockResolvedValue({ data: mockCondo } as any)

      const store = useTenantStore()
      await store.setCondominium(1)

      expect(store.currentCondominiumId).toBe(1)
      expect(store.currentCondominium).toEqual(mockCondo)
    })

    it('limpa condomínio em caso de erro', async () => {
      vi.mocked(condominiumApi.getCondominiumSummary).mockRejectedValue(new Error())

      const store = useTenantStore()
      await store.setCondominium(1)

      expect(store.currentCondominiumId).toBe(1)
      expect(store.currentCondominium).toBeNull()
    })
  })

  describe('clearCondominium', () => {
    it('limpa estado e localStorage', () => {
      const store = useTenantStore()
      store.currentCondominiumId = 1
      store.currentCondominium = {} as any

      store.clearCondominium()

      expect(store.currentCondominiumId).toBeNull()
      expect(store.currentCondominium).toBeNull()
    })
  })
})
