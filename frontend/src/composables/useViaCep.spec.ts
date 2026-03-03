import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useViaCep } from './useViaCep'

describe('useViaCep', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('retorna null para CEP inválido (menos de 8 dígitos)', async () => {
    const { fetchByCep } = useViaCep()
    expect(await fetchByCep('123')).toBeNull()
    expect(await fetchByCep('')).toBeNull()
    expect(fetch).not.toHaveBeenCalled()
  })

  it('retorna null para CEP inválido (mais de 8 dígitos)', async () => {
    const { fetchByCep } = useViaCep()
    expect(await fetchByCep('123456789')).toBeNull()
  })

  it('retorna endereço quando fetch ok', async () => {
    const mockData = {
      cep: '01310-100',
      logradouro: 'Avenida Paulista',
      bairro: 'Bela Vista',
      localidade: 'São Paulo',
      uf: 'SP',
    }
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve(mockData),
    } as Response)

    const { fetchByCep } = useViaCep()
    const result = await fetchByCep('01310100')
    expect(result).toEqual(mockData)
    expect(fetch).toHaveBeenCalledWith('/viacep/ws/01310100/json/')
  })

  it('retorna null quando API retorna erro', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
    } as Response)

    const { fetchByCep } = useViaCep()
    expect(await fetchByCep('01310100')).toBeNull()
  })

  it('retorna null quando data.erro é true', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ erro: true }),
    } as Response)

    const { fetchByCep } = useViaCep()
    expect(await fetchByCep('00000000')).toBeNull()
  })

  it('retorna null em caso de exceção', async () => {
    vi.mocked(fetch).mockRejectedValue(new Error('Network error'))

    const { fetchByCep } = useViaCep()
    expect(await fetchByCep('01310100')).toBeNull()
  })
})
