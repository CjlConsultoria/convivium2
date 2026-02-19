export function formatCurrency(cents: number): string {
  const value = cents / 100
  return value.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  })
}

export function formatDate(iso: string): string {
  const date = new Date(iso)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}/${month}/${year}`
}

export function formatDateTime(iso: string): string {
  const date = new Date(iso)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${day}/${month}/${year} ${hours}:${minutes}`
}

export function formatCPF(cpf: string): string {
  const digits = cpf.replace(/\D/g, '')
  if (digits.length !== 11) return cpf
  return digits.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
}

export function formatCNPJ(cnpj: string): string {
  const digits = cnpj.replace(/\D/g, '')
  if (digits.length !== 14) return cnpj
  return digits.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
}

export function formatPhone(phone: string): string {
  const digits = phone.replace(/\D/g, '')
  if (digits.length === 11) {
    return digits.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
  }
  if (digits.length === 10) {
    return digits.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3')
  }
  return phone
}

export function timeAgo(iso: string): string {
  const now = new Date()
  const past = new Date(iso)
  const diffMs = now.getTime() - past.getTime()
  const diffSeconds = Math.floor(diffMs / 1000)
  const diffMinutes = Math.floor(diffSeconds / 60)
  const diffHours = Math.floor(diffMinutes / 60)
  const diffDays = Math.floor(diffHours / 24)
  const diffWeeks = Math.floor(diffDays / 7)
  const diffMonths = Math.floor(diffDays / 30)
  const diffYears = Math.floor(diffDays / 365)

  if (diffSeconds < 60) {
    return 'ha poucos segundos'
  }
  if (diffMinutes === 1) {
    return 'ha 1 minuto'
  }
  if (diffMinutes < 60) {
    return `ha ${diffMinutes} minutos`
  }
  if (diffHours === 1) {
    return 'ha 1 hora'
  }
  if (diffHours < 24) {
    return `ha ${diffHours} horas`
  }
  if (diffDays === 1) {
    return 'ha 1 dia'
  }
  if (diffDays < 7) {
    return `ha ${diffDays} dias`
  }
  if (diffWeeks === 1) {
    return 'ha 1 semana'
  }
  if (diffWeeks < 4) {
    return `ha ${diffWeeks} semanas`
  }
  if (diffMonths === 1) {
    return 'ha 1 mes'
  }
  if (diffMonths < 12) {
    return `ha ${diffMonths} meses`
  }
  if (diffYears === 1) {
    return 'ha 1 ano'
  }
  return `ha ${diffYears} anos`
}
