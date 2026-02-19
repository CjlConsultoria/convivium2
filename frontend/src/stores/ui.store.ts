import { defineStore } from 'pinia'
import { ref } from 'vue'
import { STORAGE_KEYS } from '@/utils/constants'

export const useUiStore = defineStore('ui', () => {
  // ---------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------
  const storedCollapsed = localStorage.getItem(STORAGE_KEYS.SIDEBAR_COLLAPSED)
  const sidebarCollapsed = ref<boolean>(storedCollapsed === 'true')

  const sidebarMobileOpen = ref(false)

  const storedTheme = localStorage.getItem(STORAGE_KEYS.THEME)
  const theme = ref<'light' | 'dark'>(
    storedTheme === 'dark' ? 'dark' : 'light',
  )

  // ---------------------------------------------------------------------------
  // Actions
  // ---------------------------------------------------------------------------
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem(STORAGE_KEYS.SIDEBAR_COLLAPSED, String(sidebarCollapsed.value))
  }

  function setSidebarMobileOpen(open: boolean): void {
    sidebarMobileOpen.value = open
  }

  function setTheme(newTheme: 'light' | 'dark'): void {
    theme.value = newTheme
    localStorage.setItem(STORAGE_KEYS.THEME, newTheme)

    // Toggle the dark class on the document element for Tailwind dark mode
    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  return {
    // State
    sidebarCollapsed,
    sidebarMobileOpen,
    theme,
    // Actions
    toggleSidebar,
    setSidebarMobileOpen,
    setTheme,
  }
})
