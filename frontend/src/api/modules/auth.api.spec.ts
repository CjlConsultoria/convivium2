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
})
