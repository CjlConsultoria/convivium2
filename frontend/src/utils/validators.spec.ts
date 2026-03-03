import { describe, it, expect } from 'vitest'
import {
  isValidCPF,
  isValidCNPJ,
  isValidEmail,
  isValidPhone,
  isRequired,
  minLength,
} from './validators'

describe('validators', () => {
  describe('isValidCPF', () => {
    it('retorna true para CPF válido', () => {
      expect(isValidCPF('529.982.247-25')).toBe(true)
      expect(isValidCPF('52998224725')).toBe(true)
    })

    it('retorna false para CPF inválido', () => {
      expect(isValidCPF('111.111.111-11')).toBe(false)
      expect(isValidCPF('000.000.000-00')).toBe(false)
      expect(isValidCPF('123.456.789-00')).toBe(false)
    })

    it('retorna false para tamanho incorreto', () => {
      expect(isValidCPF('123')).toBe(false)
      expect(isValidCPF('123456789012')).toBe(false)
    })
  })

  describe('isValidCNPJ', () => {
    it('retorna true para CNPJ válido', () => {
      expect(isValidCNPJ('11.444.777/0001-61')).toBe(true)
      expect(isValidCNPJ('11444777000161')).toBe(true)
    })

    it('retorna false para CNPJ inválido', () => {
      expect(isValidCNPJ('11.111.111/1111-11')).toBe(false)
      expect(isValidCNPJ('00000000000000')).toBe(false)
    })

    it('retorna false para tamanho incorreto', () => {
      expect(isValidCNPJ('123')).toBe(false)
    })
  })

  describe('isValidEmail', () => {
    it('retorna true para emails válidos', () => {
      expect(isValidEmail('test@example.com')).toBe(true)
      expect(isValidEmail('user.name@domain.co')).toBe(true)
    })

    it('retorna false para emails inválidos', () => {
      expect(isValidEmail('invalid')).toBe(false)
      expect(isValidEmail('@domain.com')).toBe(false)
      expect(isValidEmail('user@')).toBe(false)
    })
  })

  describe('isValidPhone', () => {
    it('retorna true para telefones válidos (10 ou 11 dígitos)', () => {
      expect(isValidPhone('11999999999')).toBe(true)
      expect(isValidPhone('(11) 99999-9999')).toBe(true)
      expect(isValidPhone('1133334444')).toBe(true)
    })

    it('retorna false para telefones inválidos', () => {
      expect(isValidPhone('123')).toBe(false)
      expect(isValidPhone('123456789012')).toBe(false)
    })
  })

  describe('isRequired', () => {
    it('retorna false para null/undefined', () => {
      expect(isRequired(null)).toBe(false)
      expect(isRequired(undefined)).toBe(false)
    })

    it('retorna false para string vazia', () => {
      expect(isRequired('')).toBe(false)
      expect(isRequired('   ')).toBe(false)
    })

    it('retorna false para array vazio', () => {
      expect(isRequired([])).toBe(false)
    })

    it('retorna true para valores válidos', () => {
      expect(isRequired('text')).toBe(true)
      expect(isRequired(0)).toBe(true)
      expect(isRequired([1])).toBe(true)
    })
  })

  describe('minLength', () => {
    it('retorna true quando atinge o mínimo', () => {
      expect(minLength('abc', 3)).toBe(true)
      expect(minLength('abcd', 3)).toBe(true)
    })

    it('retorna false quando não atinge o mínimo', () => {
      expect(minLength('ab', 3)).toBe(false)
    })
  })
})
