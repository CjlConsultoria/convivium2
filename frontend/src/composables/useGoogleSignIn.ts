/**
 * Carrega o script do Google Identity Services e expõe o fluxo de login.
 * Defina VITE_GOOGLE_CLIENT_ID no .env com o Client ID do Google Cloud Console (tipo "Aplicativo da Web").
 */
const SCRIPT_URL = 'https://accounts.google.com/gsi/client'

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: {
            client_id: string
            callback: (response: { credential: string }) => void
            auto_select?: boolean
          }) => void
          renderButton: (
            element: HTMLElement,
            options: { type?: string; theme?: string; size?: string; text?: string }
          ) => void
          prompt: () => void
        }
      }
    }
  }
}

function loadScript(): Promise<void> {
  if (document.querySelector(`script[src="${SCRIPT_URL}"]`)) {
    return Promise.resolve()
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = SCRIPT_URL
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Falha ao carregar Google Sign-In'))
    document.head.appendChild(script)
  })
}

export function useGoogleSignIn() {
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID as string | undefined
  const isAvailable = !!clientId

  async function signIn(): Promise<string> {
    if (!clientId) {
      throw new Error('Google Client ID não configurado (VITE_GOOGLE_CLIENT_ID)')
    }
    await loadScript()
    if (!window.google?.accounts?.id) {
      throw new Error('Google Sign-In não disponível')
    }
    return new Promise((resolve, reject) => {
      window.google!.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => {
          if (response?.credential) resolve(response.credential)
          else reject(new Error('Resposta inválida do Google'))
        },
      })
      window.google!.accounts.id.prompt()
    })
  }

  return { signIn, isAvailable, clientId }
}
