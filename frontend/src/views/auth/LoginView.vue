<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useTenantStore } from '@/stores/tenant.store'
import { useGoogleSignIn } from '@/composables/useGoogleSignIn'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const tenantStore = useTenantStore()
const { signIn: googleSignIn } = useGoogleSignIn()

const email = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')

const PENDING_MESSAGE =
  'Seu cadastro está pendente de aprovação do síndico ou administrador. Aguarde a liberação para acessar o sistema.'

onMounted(() => {
  if (route.query.message === 'pending') {
    error.value = PENDING_MESSAGE
  }
})

async function redirectAfterLogin() {
  const redirect = route.query.redirect as string | undefined
  if (redirect) {
    router.push(redirect)
  } else if (authStore.isPlatformAdmin) {
    router.push('/admin')
  } else {
    const activeRoles = authStore.user?.condominiumRoles?.filter((r) => r.status === 'ACTIVE') ?? []
    if (activeRoles.length) {
      const condoId = activeRoles[0].condominiumId
      await tenantStore.setCondominium(condoId)
      router.push(`/c/${condoId}`)
    } else {
      const hasPending = (authStore.user?.condominiumRoles?.length ?? 0) > 0
      error.value = hasPending
        ? PENDING_MESSAGE
        : 'Sua conta ainda não tem acesso a nenhum condomínio. Se você já se cadastrou, pode ser que seu cadastro esteja pendente de aprovação do síndico — aguarde. Se ainda não se cadastrou, use "Entrar com Google" ou "Cadastre-se com e-mail" para criar sua conta como morador.'
    }
  }
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    redirectAfterLogin()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao fazer login. Verifique suas credenciais.'
  } finally {
    loading.value = false
  }
}

async function handleGoogleLogin() {
  error.value = ''
  try {
    const idToken = await googleSignIn()
    loading.value = true
    const result = await authStore.loginWithGoogle(idToken)
    if (result.needsRegistration) {
      sessionStorage.setItem('google_signup', JSON.stringify({
        idToken,
        email: result.email,
        name: result.name,
        picture: result.picture,
      }))
      router.push('/register-google')
      return
    }
    redirectAfterLogin()
  } catch (err: any) {
    error.value = err.response?.data?.message || err.message || 'Erro ao entrar com Google.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900">Entrar na sua conta</h2>
    <p class="mt-1 text-sm text-gray-500">
      Acesse o sistema do seu condominio. Quem cria o condominio e o administrador da plataforma; aqui voce so faz login com seu usuario.
    </p>

    <BaseAlert v-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <form class="mt-6 space-y-4" @submit.prevent="handleLogin">
      <BaseInput
        v-model="email"
        label="Email"
        type="email"
        placeholder="seu@email.com"
        required
      />
      <BaseInput
        v-model="password"
        label="Senha"
        type="password"
        placeholder="Sua senha"
        required
      />

      <div class="flex items-center justify-between">
        <label class="flex items-center gap-2 text-sm">
          <input type="checkbox" class="rounded border-gray-300 text-primary-600 focus:ring-primary-500" />
          <span class="text-gray-600">Lembrar de mim</span>
        </label>
        <router-link to="/forgot-password" class="text-sm font-medium text-primary-600 hover:text-primary-500">
          Esqueceu a senha?
        </router-link>
      </div>

      <BaseButton type="submit" variant="primary" class="w-full" :loading="loading">
        Entrar
      </BaseButton>

      <div class="relative my-4">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-200" />
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="bg-white px-2 text-gray-500">ou</span>
        </div>
      </div>
      <BaseButton
        type="button"
        variant="outline"
        class="w-full"
        :loading="loading"
        @click="handleGoogleLogin"
      >
        <svg class="mr-2 h-5 w-5" viewBox="0 0 24 24">
          <path
            fill="currentColor"
            d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
          />
          <path
            fill="currentColor"
            d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
          />
          <path
            fill="currentColor"
            d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
          />
          <path
            fill="currentColor"
            d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
          />
        </svg>
        Entrar com Google
      </BaseButton>
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Nao tem conta? Use <strong>Entrar com Google</strong> para criar sua conta (morador) ou
      <router-link to="/register" class="font-medium text-primary-600 hover:text-primary-500">
        cadastre-se com email
      </router-link>
      .
    </p>
  </div>
</template>
