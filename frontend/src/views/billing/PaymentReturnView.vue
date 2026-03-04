<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))
const sessionId = computed(() => route.query.session_id as string)

const status = ref<'success' | 'processing' | 'error'>('processing')

onMounted(() => {
  // O webhook processa o pagamento assincronamente.
  // Se chegou aqui com session_id, consideramos sucesso.
  if (sessionId.value) {
    status.value = 'success'
  } else {
    status.value = 'error'
  }
})

function goToFinancial() {
  router.push(`/c/${condoId.value}/financial`)
}

function goToDashboard() {
  router.push(`/c/${condoId.value}`)
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50 p-4">
    <div class="w-full max-w-md text-center">
      <!-- Sucesso -->
      <div v-if="status === 'success'" class="space-y-4">
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
          <svg class="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Pagamento Realizado!</h1>
        <p class="text-gray-600">
          Seu pagamento foi processado com sucesso. O acesso ao condominio sera restaurado em instantes.
        </p>
        <div class="flex justify-center gap-3 pt-4">
          <button
            type="button"
            class="rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
            @click="goToDashboard"
          >
            Ir para o Painel
          </button>
          <button
            type="button"
            class="rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
            @click="goToFinancial"
          >
            Ver Faturas
          </button>
        </div>
      </div>

      <!-- Processando -->
      <div v-else-if="status === 'processing'" class="space-y-4">
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-blue-100">
          <svg class="h-8 w-8 animate-spin text-blue-600" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Processando...</h1>
        <p class="text-gray-600">Verificando o status do seu pagamento.</p>
      </div>

      <!-- Erro -->
      <div v-else class="space-y-4">
        <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-red-100">
          <svg class="h-8 w-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Erro no Pagamento</h1>
        <p class="text-gray-600">Nao foi possivel confirmar o pagamento. Tente novamente.</p>
        <button
          type="button"
          class="rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700"
          @click="$router.back()"
        >
          Voltar
        </button>
      </div>
    </div>
  </div>
</template>
