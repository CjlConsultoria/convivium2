import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUiStore } from './ui.store'

describe('ui.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  describe('toggleSidebar', () => {
    it('alterna o estado collapsed', () => {
      const store = useUiStore()
      expect(store.sidebarCollapsed).toBe(false)
      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(true)
      store.toggleSidebar()
      expect(store.sidebarCollapsed).toBe(false)
    })
  })

  describe('setSidebarMobileOpen', () => {
    it('atualiza o estado', () => {
      const store = useUiStore()
      store.setSidebarMobileOpen(true)
      expect(store.sidebarMobileOpen).toBe(true)
      store.setSidebarMobileOpen(false)
      expect(store.sidebarMobileOpen).toBe(false)
    })
  })

  describe('setTheme', () => {
    it('atualiza o tema', () => {
      const store = useUiStore()
      store.setTheme('dark')
      expect(store.theme).toBe('dark')
      store.setTheme('light')
      expect(store.theme).toBe('light')
    })

    it('adiciona classe dark no document quando tema dark', () => {
      const store = useUiStore()
      document.documentElement.classList.remove('dark')
      store.setTheme('dark')
      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })

    it('remove classe dark do document quando tema light', () => {
      const store = useUiStore()
      document.documentElement.classList.add('dark')
      store.setTheme('light')
      expect(document.documentElement.classList.contains('dark')).toBe(false)
    })
  })
})
