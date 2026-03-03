import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuth } from './useAuth'
import * as authApi from '@/api/modules/auth.api'

vi.mock('@/api/modules/auth.api')
vi.mock('@/router', () => ({ default: { push: vi.fn() } }))

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('delega login para authStore', async () => {
    vi.mocked(authApi.login).mockResolvedValue({
      data: { accessToken: 'at', refreshToken: 'rt', user: { id: 1, condominiumRoles: [] } },
    } as any)

    const { login } = useAuth()
    await login('a@b.com', 'senha')
    expect(authApi.login).toHaveBeenCalledWith({ email: 'a@b.com', password: 'senha' })
  })

  it('delega hasRole para authStore', async () => {
    const { useTenantStore } = await import('@/stores/tenant.store')
    useTenantStore().currentCondominiumId = 1

    const { useAuthStore } = await import('@/stores/auth.store')
    useAuthStore().user = {
      condominiumRoles: [{ condominiumId: 1, role: 'SINDICO', status: 'ACTIVE' }],
    } as any

    const { hasRole } = useAuth()
    expect(hasRole('SINDICO')).toBe(true)
  })

  it('delega hasPermission para authStore', async () => {
    const { useAuthStore } = await import('@/stores/auth.store')
    useAuthStore().user = { isPlatformAdmin: true } as any

    const { hasPermission } = useAuth()
    expect(hasPermission('users.view')).toBe(true)
  })

  it('isAuthenticated reflete o store', async () => {
    const { useAuthStore } = await import('@/stores/auth.store')
    useAuthStore().accessToken = 'token'

    const { isAuthenticated } = useAuth()
    expect(isAuthenticated.value).toBe(true)
  })
})
