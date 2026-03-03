import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth.store'
import * as authApi from '@/api/modules/auth.api'
import router from '@/router'
import { STORAGE_KEYS } from '@/utils/constants'

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

    it('retorna true quando role tem a permissão', async () => {
      const { useTenantStore } = await import('./tenant.store')
      const tenantStore = useTenantStore()
      tenantStore.currentCondominiumId = 1

      const store = useAuthStore()
      store.user = {
        isPlatformAdmin: false,
        condominiumRoles: [{ condominiumId: 1, role: 'SINDICO', status: 'ACTIVE' }],
      } as any
      expect(store.hasPermission('users.view')).toBe(true)
    })
  })

  describe('loginWithGoogle', () => {
    it('retorna needsRegistration quando precisa cadastrar', async () => {
      vi.mocked(authApi.googleLogin).mockResolvedValue({
        data: { needsRegistration: true, email: 'a@b.com', name: 'User' },
      } as any)

      const store = useAuthStore()
      const result = await store.loginWithGoogle('idToken')

      expect(result.needsRegistration).toBe(true)
      expect(result.email).toBe('a@b.com')
    })

    it('faz login quando já tem conta', async () => {
      vi.mocked(authApi.googleLogin).mockResolvedValue({
        data: {
          needsRegistration: false,
          accessToken: 'at',
          refreshToken: 'rt',
          user: { id: 1, email: 'a@b.com', condominiumRoles: [] },
        },
      } as any)

      const store = useAuthStore()
      const result = await store.loginWithGoogle('idToken')

      expect(result.needsRegistration).toBe(false)
      expect(store.accessToken).toBe('at')
    })
  })

  describe('refreshAccessToken', () => {
    it('faz logout quando não há refreshToken', async () => {
      const store = useAuthStore()
      store.refreshToken = null
      await store.refreshAccessToken()
      expect(router.push).toHaveBeenCalledWith('/login')
    })

    it('atualiza tokens quando refresh ok', async () => {
      vi.mocked(authApi.refreshToken).mockResolvedValue({
        data: {
          accessToken: 'newAt',
          refreshToken: 'newRt',
          user: { id: 1, condominiumRoles: [] },
        },
      } as any)

      const store = useAuthStore()
      store.refreshToken = 'rt'
      await store.refreshAccessToken()

      expect(store.accessToken).toBe('newAt')
      expect(store.refreshToken).toBe('newRt')
    })

    it('faz logout quando refresh falha', async () => {
      vi.mocked(authApi.refreshToken).mockRejectedValue(new Error())

      const store = useAuthStore()
      store.refreshToken = 'rt'
      await store.refreshAccessToken()

      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('fetchUser', () => {
    it('atualiza user quando ok', async () => {
      vi.mocked(authApi.getMe).mockResolvedValue({
        data: { id: 1, email: 'a@b.com', name: 'User', condominiumRoles: [] },
      } as any)

      const store = useAuthStore()
      await store.fetchUser()

      expect(store.user).toEqual(expect.objectContaining({ email: 'a@b.com' }))
    })

    it('faz logout quando getMe falha', async () => {
      vi.mocked(authApi.getMe).mockRejectedValue(new Error())

      const store = useAuthStore()
      await store.fetchUser()

      expect(router.push).toHaveBeenCalledWith('/login')
    })
  })

  describe('updateMyProfile', () => {
    it('atualiza user', async () => {
      vi.mocked(authApi.updateMyProfile).mockResolvedValue({
        data: { id: 1, name: 'Novo Nome', email: 'a@b.com', condominiumRoles: [] },
      } as any)

      const store = useAuthStore()
      store.user = { id: 1, name: 'Old', email: 'a@b.com' } as any
      await store.updateMyProfile({ name: 'Novo Nome' })

      expect(store.user?.name).toBe('Novo Nome')
    })
  })

  describe('initialize', () => {
    it('busca user quando há token no localStorage', async () => {
      const getMeMock = vi.mocked(authApi.getMe)
      getMeMock.mockResolvedValue({
        data: { id: 1, email: 'a@b.com', condominiumRoles: [] },
      } as any)

      const store = useAuthStore()
      localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, 'at')
      localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, 'rt')
      await store.initialize()

      expect(getMeMock).toHaveBeenCalled()
      expect(store.user).toEqual(expect.objectContaining({ email: 'a@b.com' }))
    })
  })

  describe('completeGoogleRegistration', () => {
    it('retorna needsApproval quando cadastro pendente', async () => {
      vi.mocked(authApi.registerGoogle).mockResolvedValue({
        data: { needsApproval: true, message: 'Aguardando aprovação do síndico.' },
      } as any)

      const store = useAuthStore()
      const result = await store.completeGoogleRegistration({
        idToken: 'token',
        condominiumId: 1,
        unitId: 1,
      })

      expect(result.needsApproval).toBe(true)
      expect(result.message).toBe('Aguardando aprovação do síndico.')
    })

    it('faz login quando já tem vínculo ativo', async () => {
      vi.mocked(authApi.registerGoogle).mockResolvedValue({
        data: {
          needsApproval: false,
          login: {
            accessToken: 'at',
            refreshToken: 'rt',
            user: { id: 1, email: 'a@b.com', condominiumRoles: [] },
          },
        },
      } as any)

      const store = useAuthStore()
      const result = await store.completeGoogleRegistration({
        idToken: 'token',
        condominiumId: 1,
        unitId: 1,
      })

      expect(result.needsApproval).toBe(false)
      expect(store.accessToken).toBe('at')
      expect(store.refreshToken).toBe('rt')
    })
  })

  describe('isPlatformAdmin', () => {
    it('retorna true quando user é platform admin', () => {
      const store = useAuthStore()
      store.user = { isPlatformAdmin: true } as any
      expect(store.isPlatformAdmin).toBe(true)
    })

    it('retorna false quando user não é platform admin', () => {
      const store = useAuthStore()
      store.user = { isPlatformAdmin: false } as any
      expect(store.isPlatformAdmin).toBe(false)
    })
  })

  describe('currentRoles', () => {
    it('retorna roles do condomínio atual', async () => {
      const { useTenantStore } = await import('./tenant.store')
      const tenantStore = useTenantStore()
      tenantStore.currentCondominiumId = 1

      const store = useAuthStore()
      store.user = {
        condominiumRoles: [
          { condominiumId: 1, role: 'SINDICO', status: 'ACTIVE' },
          { condominiumId: 2, role: 'MORADOR', status: 'ACTIVE' },
        ],
      } as any

      expect(store.currentRoles).toHaveLength(1)
      expect(store.currentRoles[0].role).toBe('SINDICO')
    })

    it('retorna vazio quando não há condomínio selecionado', () => {
      const store = useAuthStore()
      store.user = { condominiumRoles: [{ condominiumId: 1, role: 'SINDICO', status: 'ACTIVE' }] } as any
      expect(store.currentRoles).toHaveLength(0)
    })
  })
})
