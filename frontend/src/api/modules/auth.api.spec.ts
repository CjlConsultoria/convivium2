import { describe, it, expect, vi, beforeEach } from 'vitest'
import * as authApi from './auth.api'
import apiClient from '@/api/client'

vi.mock('@/api/client', () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    patch: vi.fn(),
  },
}))

describe('auth.api', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('login chama POST /auth/login', async () => {
    const mockData = { accessToken: 'at', refreshToken: 'rt', user: {} }
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: mockData } } as any)

    await authApi.login({ email: 'a@b.com', password: 'senha' })

    expect(apiClient.post).toHaveBeenCalledWith('/auth/login', {
      email: 'a@b.com',
      password: 'senha',
    })
  })

  it('register chama POST /auth/register', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await authApi.register({
      email: 'a@b.com',
      password: 'senha',
      name: 'User',
      condominiumId: 1,
      unitId: 1,
    } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/auth/register', expect.any(Object))
  })

  it('getMe chama GET /auth/me', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await authApi.getMe()
    expect(apiClient.get).toHaveBeenCalledWith('/auth/me')
  })

  it('forgotPassword chama POST /auth/forgot-password', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await authApi.forgotPassword('a@b.com')
    expect(apiClient.post).toHaveBeenCalledWith('/auth/forgot-password', { email: 'a@b.com' })
  })

  it('refreshToken chama POST /auth/refresh', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { accessToken: 'at', refreshToken: 'rt', user: {} } } } as any)
    await authApi.refreshToken('rt')
    expect(apiClient.post).toHaveBeenCalledWith('/auth/refresh', { refreshToken: 'rt' })
  })

  it('updateMyProfile chama PATCH /auth/me', async () => {
    vi.mocked(apiClient.patch).mockResolvedValue({ data: { success: true, data: {} } } as any)
    await authApi.updateMyProfile({ name: 'New Name' })
    expect(apiClient.patch).toHaveBeenCalledWith('/auth/me', { name: 'New Name' })
  })

  it('googleLogin chama POST /auth/google', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { needsRegistration: false } } } as any)
    await authApi.googleLogin('idToken')
    expect(apiClient.post).toHaveBeenCalledWith('/auth/google', { idToken: 'idToken' })
  })

  it('listCondominiumsForRegistration chama GET /auth/condominiums', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await authApi.listCondominiumsForRegistration()
    expect(apiClient.get).toHaveBeenCalledWith('/auth/condominiums')
  })

  it('listUnitsForRegistration chama GET', async () => {
    vi.mocked(apiClient.get).mockResolvedValue({ data: { success: true, data: [] } } as any)
    await authApi.listUnitsForRegistration(1)
    expect(apiClient.get).toHaveBeenCalledWith('/auth/condominiums/1/units')
  })

  it('registerGoogle chama POST /auth/register-google', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true, data: { needsApproval: false } } } as any)
    await authApi.registerGoogle({ idToken: 't', condominiumId: 1, unitId: 1 })
    expect(apiClient.post).toHaveBeenCalledWith('/auth/register-google', expect.any(Object))
  })

  it('resetPassword chama POST /auth/reset-password', async () => {
    vi.mocked(apiClient.post).mockResolvedValue({ data: { success: true } } as any)
    await authApi.resetPassword({ token: 't', newPassword: 'p' } as any)
    expect(apiClient.post).toHaveBeenCalledWith('/auth/reset-password', expect.any(Object))
  })
})
