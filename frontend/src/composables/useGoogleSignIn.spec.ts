import { describe, it, expect } from 'vitest'
import { useGoogleSignIn } from './useGoogleSignIn'

describe('useGoogleSignIn', () => {
  it('isAvailable é false quando VITE_GOOGLE_CLIENT_ID não está definido', () => {
    const { isAvailable } = useGoogleSignIn()
    expect(isAvailable).toBe(false)
  })

  it('signIn lança quando clientId não configurado', async () => {
    const { signIn } = useGoogleSignIn()
    await expect(signIn()).rejects.toThrow('Google Client ID não configurado')
  })
})
