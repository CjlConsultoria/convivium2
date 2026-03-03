import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { useToast } from './useToast'

describe('useToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    // Limpa estado global dos toasts entre testes
    const { toasts } = useToast()
    toasts.value = []
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('adiciona toast e remove após duração', () => {
    const { toasts, showToast } = useToast()
    showToast('Mensagem', 'success', 1000)
    expect(toasts.value).toHaveLength(1)
    expect(toasts.value[0].message).toBe('Mensagem')
    expect(toasts.value[0].type).toBe('success')

    vi.advanceTimersByTime(1000)
    expect(toasts.value).toHaveLength(0)
  })

  it('success, error, warning, info criam toasts do tipo correto', () => {
    const { toasts, success, error, warning, info } = useToast()
    success('Ok')
    expect(toasts.value[0].type).toBe('success')
    error('Erro')
    expect(toasts.value[1].type).toBe('error')
    warning('Atenção')
    expect(toasts.value[2].type).toBe('warning')
    info('Info')
    expect(toasts.value[3].type).toBe('info')
  })

  it('removeToast remove por id', () => {
    const { toasts, showToast, removeToast } = useToast()
    showToast('A', 'info', 0)
    showToast('B', 'info', 0)
    const id = toasts.value[0].id
    removeToast(id)
    expect(toasts.value).toHaveLength(1)
    expect(toasts.value[0].message).toBe('B')
  })

  it('toast com duration 0 não remove automaticamente', () => {
    const { toasts, showToast } = useToast()
    showToast('Persistente', 'info', 0)
    expect(toasts.value).toHaveLength(1)
    vi.advanceTimersByTime(10000)
    expect(toasts.value).toHaveLength(1)
  })

  it('success/error/warning/info aceitam duration opcional', () => {
    const { toasts, success } = useToast()
    success('Ok', 500)
    expect(toasts.value[0].duration).toBe(500)
  })
})
