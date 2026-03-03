import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import {
  formatCurrency,
  formatDate,
  formatDateTime,
  formatCPF,
  formatCNPJ,
  formatPhone,
  timeAgo,
} from './formatters'

describe('formatters', () => {
  describe('formatCurrency', () => {
    it('formata centavos para BRL', () => {
      const result100 = formatCurrency(10000)
      expect(result100).toContain('R$')
      expect(result100).toContain('100,00')
      const result0 = formatCurrency(0)
      expect(result0).toContain('R$')
      expect(result0).toContain('0,00')
    })
  })

  describe('formatDate', () => {
    it('formata ISO para dd/mm/yyyy', () => {
      expect(formatDate('2025-03-02T12:00:00Z')).toBe('02/03/2025')
    })
  })

  describe('formatDateTime', () => {
    it('formata ISO para dd/mm/yyyy HH:mm', () => {
      const result = formatDateTime('2025-03-02T14:30:00Z')
      expect(result).toMatch(/\d{2}\/\d{2}\/\d{4} \d{2}:\d{2}/)
    })
  })

  describe('formatCPF', () => {
    it('formata 11 dígitos', () => {
      expect(formatCPF('52998224725')).toBe('529.982.247-25')
    })

    it('retorna original se não tiver 11 dígitos', () => {
      expect(formatCPF('123')).toBe('123')
    })
  })

  describe('formatCNPJ', () => {
    it('formata 14 dígitos', () => {
      expect(formatCNPJ('11444777000161')).toBe('11.444.777/0001-61')
    })

    it('retorna original se não tiver 14 dígitos', () => {
      expect(formatCNPJ('123')).toBe('123')
    })
  })

  describe('formatPhone', () => {
    it('formata celular (11 dígitos)', () => {
      expect(formatPhone('11999999999')).toBe('(11) 99999-9999')
    })

    it('formata fixo (10 dígitos)', () => {
      expect(formatPhone('1133334444')).toBe('(11) 3333-4444')
    })

    it('retorna original se inválido', () => {
      expect(formatPhone('123')).toBe('123')
    })
  })

  describe('timeAgo', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2025-03-02T12:00:00Z'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('retorna "ha poucos segundos" para menos de 1 minuto', () => {
      expect(timeAgo('2025-03-02T11:59:30Z')).toBe('ha poucos segundos')
    })

    it('retorna minutos', () => {
      expect(timeAgo('2025-03-02T11:58:00Z')).toBe('ha 2 minutos')
    })

    it('retorna horas', () => {
      expect(timeAgo('2025-03-02T10:00:00Z')).toBe('ha 2 horas')
    })

    it('retorna dias', () => {
      expect(timeAgo('2025-02-28T12:00:00Z')).toBe('ha 2 dias')
    })

    it('retorna semanas', () => {
      expect(timeAgo('2025-02-23T12:00:00Z')).toBe('ha 1 semana')
    })

    it('retorna meses', () => {
      expect(timeAgo('2024-12-02T12:00:00Z')).toBe('ha 3 meses')
    })

    it('retorna anos', () => {
      expect(timeAgo('2024-03-02T12:00:00Z')).toBe('ha 1 ano')
    })

    it('retorna 1 minuto', () => {
      expect(timeAgo('2025-03-02T11:59:00Z')).toBe('ha 1 minuto')
    })

    it('retorna 1 hora', () => {
      expect(timeAgo('2025-03-02T11:00:00Z')).toBe('ha 1 hora')
    })

    it('retorna 1 dia', () => {
      expect(timeAgo('2025-03-01T12:00:00Z')).toBe('ha 1 dia')
    })

    it('retorna múltiplas semanas', () => {
      expect(timeAgo('2025-02-16T12:00:00Z')).toBe('ha 2 semanas')
    })

    it('retorna múltiplos anos', () => {
      expect(timeAgo('2023-03-02T12:00:00Z')).toBe('ha 2 anos')
    })
  })

  describe('formatCPF com caracteres não numéricos', () => {
    it('remove caracteres e formata', () => {
      expect(formatCPF('529.982.247-25')).toBe('529.982.247-25')
    })
  })

  describe('formatPhone com dígitos', () => {
    it('formata com parênteses e hífen', () => {
      expect(formatPhone('(11) 99999-9999')).toBe('(11) 99999-9999')
    })
  })
})
