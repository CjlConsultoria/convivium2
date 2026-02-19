import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, CondominiumRoleInfo } from '@/types'
import * as authApi from '@/api/modules/auth.api'
import { STORAGE_KEYS } from '@/utils/constants'
import { useTenantStore } from '@/stores/tenant.store'
import router from '@/router'

/**
 * Role-to-permission matrix.
 * Each role grants a set of permissions that can be checked via hasPermission().
 */
const ROLE_PERMISSIONS: Record<string, string[]> = {
  SINDICO: [
    'users.view',
    'users.create',
    'users.edit',
    'users.delete',
    'complaints.view',
    'complaints.manage',
    'complaints.respond',
    'parcels.view',
    'parcels.receive',
    'parcels.deliver',
    'announcements.view',
    'announcements.create',
    'announcements.edit',
    'announcements.delete',
    'bookings.view',
    'bookings.manage',
    'visitors.view',
    'visitors.manage',
    'maintenance.view',
    'maintenance.manage',
    'financial.view',
    'financial.manage',
    'documents.view',
    'documents.manage',
    'settings.view',
    'settings.edit',
  ],
  SUB_SINDICO: [
    'users.view',
    'users.create',
    'users.edit',
    'complaints.view',
    'complaints.manage',
    'complaints.respond',
    'parcels.view',
    'parcels.receive',
    'parcels.deliver',
    'announcements.view',
    'announcements.create',
    'announcements.edit',
    'bookings.view',
    'bookings.manage',
    'visitors.view',
    'visitors.manage',
    'maintenance.view',
    'maintenance.manage',
    'financial.view',
    'documents.view',
    'documents.manage',
    'settings.view',
  ],
  CONSELHEIRO: [
    'users.view',
    'complaints.view',
    'complaints.respond',
    'parcels.view',
    'announcements.view',
    'bookings.view',
    'visitors.view',
    'maintenance.view',
    'financial.view',
    'documents.view',
    'settings.view',
  ],
  PORTEIRO: [
    'parcels.view',
    'parcels.receive',
    'parcels.deliver',
    'visitors.view',
    'visitors.manage',
    'complaints.view',
    'announcements.view',
  ],
  ZELADOR: [
    'complaints.view',
    'maintenance.view',
    'maintenance.manage',
    'parcels.view',
    'announcements.view',
  ],
  FAXINEIRA: [
    'announcements.view',
    'maintenance.view',
  ],
  MORADOR: [
    'complaints.view',
    'complaints.create',
    'parcels.view',
    'announcements.view',
    'bookings.view',
    'bookings.create',
    'visitors.view',
    'visitors.create',
    'documents.view',
    'financial.view',
  ],
}

export const useAuthStore = defineStore('auth', () => {
  // ---------------------------------------------------------------------------
  // State
  // ---------------------------------------------------------------------------
  const user = ref<UserInfo | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN))
  const refreshToken = ref<string | null>(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN))
  const loading = ref(false)

  // ---------------------------------------------------------------------------
  // Getters
  // ---------------------------------------------------------------------------
  const isAuthenticated = computed<boolean>(() => !!accessToken.value)

  const isPlatformAdmin = computed<boolean>(() => !!user.value?.isPlatformAdmin)

  const currentRoles = computed<CondominiumRoleInfo[]>(() => {
    if (!user.value) return []
    const tenantStore = useTenantStore()
    const condoId = tenantStore.currentCondominiumId
    if (!condoId) return []
    return user.value.condominiumRoles.filter(
      (r) => r.condominiumId === condoId && r.status === 'ACTIVE',
    )
  })

  function hasRole(role: string): boolean {
    return currentRoles.value.some((r) => r.role === role)
  }

  function hasPermission(permission: string): boolean {
    if (isPlatformAdmin.value) return true
    return currentRoles.value.some((r) => {
      const perms = ROLE_PERMISSIONS[r.role]
      return perms ? perms.includes(permission) : false
    })
  }

  // ---------------------------------------------------------------------------
  // Actions
  // ---------------------------------------------------------------------------
  async function login(email: string, password: string): Promise<void> {
    loading.value = true
    try {
      const response = await authApi.login({ email, password })
      const data = response.data

      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = data.user

      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.accessToken)
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken)
    } finally {
      loading.value = false
    }
  }

  /** Retorna { needsRegistration, email, name, picture } se precisar completar cadastro; senão já faz login. */
  async function loginWithGoogle(idToken: string): Promise<{
    needsRegistration: boolean
    email?: string
    name?: string
    picture?: string
  }> {
    loading.value = true
    try {
      const response = await authApi.googleLogin(idToken)
      const data = response.data
      if (data.needsRegistration) {
        return {
          needsRegistration: true,
          email: data.email ?? undefined,
          name: data.name ?? undefined,
          picture: data.picture ?? undefined,
        }
      }
      if (data.accessToken && data.refreshToken) {
        accessToken.value = data.accessToken
        refreshToken.value = data.refreshToken
        user.value = data.user ?? null
        localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.accessToken)
        localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken)
      }
      return { needsRegistration: false }
    } finally {
      loading.value = false
    }
  }

  /** Retorna true se o cadastro ficou pendente de aprovação (sem login); false se logou. */
  async function completeGoogleRegistration(data: {
    idToken: string
    condominiumId: number
    unitId: number
    phone?: string
  }): Promise<{ needsApproval: boolean; message?: string }> {
    loading.value = true
    try {
      const response = await authApi.registerGoogle(data)
      const resData = response.data
      if (resData?.needsApproval) {
        return { needsApproval: true, message: resData.message ?? 'Aguardando aprovação do síndico.' }
      }
      const login = resData?.login
      if (login?.accessToken && login?.refreshToken) {
        accessToken.value = login.accessToken
        refreshToken.value = login.refreshToken
        user.value = login.user ?? null
        localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, login.accessToken)
        localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, login.refreshToken)
      }
      return { needsApproval: false }
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    accessToken.value = null
    refreshToken.value = null
    user.value = null

    localStorage.removeItem(STORAGE_KEYS.ACCESS_TOKEN)
    localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN)

    router.push('/login')
  }

  async function refreshAccessToken(): Promise<void> {
    if (!refreshToken.value) {
      await logout()
      return
    }

    try {
      const response = await authApi.refreshToken(refreshToken.value)
      const data = response.data

      accessToken.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = data.user

      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, data.accessToken)
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, data.refreshToken)
    } catch {
      await logout()
    }
  }

  async function fetchUser(): Promise<void> {
    loading.value = true
    try {
      const response = await authApi.getMe()
      user.value = response.data
    } catch {
      await logout()
    } finally {
      loading.value = false
    }
  }

  async function updateMyProfile(data: { name?: string; phone?: string }): Promise<void> {
    const response = await authApi.updateMyProfile(data)
    if (response.data) user.value = response.data
  }

  async function initialize(): Promise<void> {
    const storedToken = localStorage.getItem(STORAGE_KEYS.ACCESS_TOKEN)
    if (storedToken) {
      accessToken.value = storedToken
      refreshToken.value = localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN)
      await fetchUser()
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loading,
    // Getters
    isAuthenticated,
    isPlatformAdmin,
    currentRoles,
    // Actions
    hasRole,
    hasPermission,
    login,
    loginWithGoogle,
    completeGoogleRegistration,
    logout,
    refreshAccessToken,
    fetchUser,
    updateMyProfile,
    initialize,
  }
})
