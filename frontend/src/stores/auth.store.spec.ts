import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import * as authApi from '@/api/modules/auth.api'
import router from '@/router'

vi.mock('@/api/modules/auth.api')
vi.mock('@/router', () => ({ default: { push: vi.fn() } }))

describe('auth.store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('isAuthenticated', () => {
    it('retorna false quando não há token', () => {
      const store = useAuthStore()
      expect(store.isAuthenticated).toBe(false)
    })

    it('retorna true quando há token', () => {
      const store = useAuthStore()
      store.accessToken = 'token'
      expect(store.isAuthenticated).toBe(true)
    })
  })

  describe('hasRole', () => {
    it('retorna true quando usuário tem o role no condomínio atual', async () => {
      const { useTenantStore } = await import('./tenant.store')
      const tenantStore = useTenantStore()
      tenantStore.currentCondominiumId = 1

      const store = useAuthStore()
      store.user = {
        id: 1,
        uuid: 'uuid',
        email: 'a@b.com',
        name: 'User',
        condominiumRoles: [
          { condominiumId: 1, role: 'SINDICO', status: 'ACTIVE', unitId: 1, unitIdentifier: '101' },
        ],
      } as any

      expect(store.hasRole('SINDICO')).toBe(true)
      expect(store.hasRole('MORADOR')).toBe(false)
    })
  })

  describe('login', () => {
    it('chama API e atualiza estado', async () => {
      const mockResponse = {
        data: {
          accessToken: 'at',
          refreshToken: 'rt',
          user: { id: 1, email: 'a@b.com', name: 'User', condominiumRoles: [] },
        },
      }
      vi.mocked(authApi.login).mockResolvedValue(mockResponse as any)

      const store = useAuthStore()
      await store.login('a@b.com', 'senha')

      expect(authApi.login).toHaveBeenCalledWith({ email: 'a@b.com', password: 'senha' })
      expect(store.accessToken).toBe('at')
      expect(store.refreshToken).toBe('rt')
      expect(store.user).toEqual(mockResponse.data.user)
    })
  })

  describe('logout', () => {
    it('limpa estado e redireciona para login', async () => {
      const store = useAuthStore()
      store.accessToken = 'token'
      store.refreshToken = 'rt'
      store.user = {} as any

      await store.logout()

      expect(store.accessToken).toBeNull()
      expect(store.refreshToken).toBeNull()
      expect(store.user).toBeNull()
      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('hasPermission', () => {
    it('retorna true para platform admin', () => {
      const store = useAuthStore()
      store.user = { isPlatformAdmin: true } as any
      expect(store.hasPermission('users.delete')).toBe(true)
    })
  })
})
