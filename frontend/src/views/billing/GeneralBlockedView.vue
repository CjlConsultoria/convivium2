<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50 p-4">
    <div class="w-full max-w-md text-center space-y-6">
      <div class="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-red-100">
        <svg class="h-10 w-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636" />
        </svg>
      </div>

      <h1 class="text-2xl font-bold text-gray-900">Acesso Bloqueado</h1>

      <p class="text-gray-600">
        O acesso ao condominio foi bloqueado pela administracao da plataforma.
        Entre em contato para mais informacoes.
      </p>

      <div class="rounded-lg border border-gray-200 bg-white p-4 space-y-2">
        <p class="text-sm font-medium text-gray-900">Contato da Plataforma</p>
        <p class="text-sm text-gray-600">suporte@convivium.com.br</p>
      </div>

      <button
        type="button"
        class="text-sm text-primary-600 hover:text-primary-700"
        @click="goToLogin"
      >
        Voltar ao login
      </button>
    </div>
  </div>
</template>
