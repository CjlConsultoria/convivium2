import { describe, it, expect, beforeEach } from 'vitest'
import { useConfirm } from './useConfirm'

describe('useConfirm', () => {
  let confirmApi: ReturnType<typeof useConfirm>

  beforeEach(() => {
    confirmApi = useConfirm()
  })

  it('abre o diálogo e resolve com true ao confirmar', async () => {
    const promise = confirmApi.confirm('Título', 'Mensagem')
    expect(confirmApi.isOpen.value).toBe(true)
    expect(confirmApi.title.value).toBe('Título')
    expect(confirmApi.message.value).toBe('Mensagem')

    confirmApi.resolve()
    const result = await promise
    expect(result).toBe(true)
    expect(confirmApi.isOpen.value).toBe(false)
  })

  it('resolve com false ao rejeitar', async () => {
    const promise = confirmApi.confirm('Título', 'Mensagem')
    confirmApi.reject()
    const result = await promise
    expect(result).toBe(false)
    expect(confirmApi.isOpen.value).toBe(false)
  })
})
