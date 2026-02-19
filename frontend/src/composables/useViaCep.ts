/**
 * ViaCEP: busca endereco por CEP.
 * Em desenvolvimento o Vite faz proxy de /viacep para https://viacep.com.br
 */
export interface ViaCepAddress {
  cep: string
  logradouro: string
  complemento: string
  bairro: string
  localidade: string
  uf: string
  ibge: string
  gia: string
  ddd: string
  siafi: string
  erro?: boolean
}

// Sempre usar /viacep para evitar CORS (proxy no Vite em dev; em producao configurar proxy no servidor)
const BASE = '/viacep'

export function useViaCep() {
  async function fetchByCep(cep: string): Promise<ViaCepAddress | null> {
    const digits = (cep || '').replace(/\D/g, '')
    if (digits.length !== 8) return null
    const url = `${BASE}/ws/${digits}/json/`
    try {
      const res = await fetch(url)
      if (!res.ok) return null
      const data: ViaCepAddress & { erro?: boolean } = await res.json()
      if (data.erro) return null
      return data
    } catch {
      return null
    }
  }

  return { fetchByCep }
}
