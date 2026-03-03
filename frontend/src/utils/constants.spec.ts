import { describe, it, expect } from 'vitest'
import { STORAGE_KEYS, PRIORITY_LABELS, PRIORITY_COLORS } from './constants'

describe('constants', () => {
  describe('STORAGE_KEYS', () => {
    it('contém as chaves esperadas', () => {
      expect(STORAGE_KEYS.ACCESS_TOKEN).toBe('access_token')
      expect(STORAGE_KEYS.REFRESH_TOKEN).toBe('refresh_token')
      expect(STORAGE_KEYS.CURRENT_CONDO_ID).toBe('current_condo_id')
      expect(STORAGE_KEYS.SIDEBAR_COLLAPSED).toBe('sidebar_collapsed')
      expect(STORAGE_KEYS.THEME).toBe('theme')
    })
  })

  describe('PRIORITY_LABELS', () => {
    it('mapeia prioridades para labels em português', () => {
      expect(PRIORITY_LABELS.LOW).toBe('Baixa')
      expect(PRIORITY_LABELS.MEDIUM).toBe('Media')
      expect(PRIORITY_LABELS.HIGH).toBe('Alta')
      expect(PRIORITY_LABELS.URGENT).toBe('Urgente')
    })
  })

  describe('PRIORITY_COLORS', () => {
    it('mapeia prioridades para classes CSS', () => {
      expect(PRIORITY_COLORS.LOW).toBe('badge-gray')
      expect(PRIORITY_COLORS.MEDIUM).toBe('badge-blue')
      expect(PRIORITY_COLORS.HIGH).toBe('badge-yellow')
      expect(PRIORITY_COLORS.URGENT).toBe('badge-red')
    })
  })
})
