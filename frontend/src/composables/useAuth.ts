import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import type { UserInfo } from '@/types'

/**
 * Convenience composable that wraps the auth store.
 * Provides reactive computed refs and action methods for use in components.
 */
export function useAuth() {
  const authStore = useAuthStore()

  const user = computed<UserInfo | null>(() => authStore.user)
  const isAuthenticated = computed<boolean>(() => authStore.isAuthenticated)
  const isPlatformAdmin = computed<boolean>(() => authStore.isPlatformAdmin)

  async function login(email: string, password: string): Promise<void> {
    await authStore.login(email, password)
  }

  async function logout(): Promise<void> {
    await authStore.logout()
  }

  function hasRole(role: string): boolean {
    return authStore.hasRole(role)
  }

  function hasPermission(permission: string): boolean {
    return authStore.hasPermission(permission)
  }

  return {
    user,
    isAuthenticated,
    isPlatformAdmin,
    login,
    logout,
    hasRole,
    hasPermission,
  }
}
