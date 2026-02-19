<script setup lang="ts">
import { ref } from 'vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'

const email = ref('')
const loading = ref(false)
const sent = ref(false)
const error = ref('')

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    const { default: apiClient } = await import('@/api/client')
    await apiClient.post('/auth/forgot-password', { email: email.value })
    sent.value = true
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao enviar email.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900">Recuperar senha</h2>
    <p class="mt-1 text-sm text-gray-500">Informe seu email para receber o link de recuperacao</p>

    <BaseAlert v-if="sent" type="success" class="mt-4">
      Email enviado! Verifique sua caixa de entrada.
    </BaseAlert>

    <BaseAlert v-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <form v-if="!sent" class="mt-6 space-y-4" @submit.prevent="handleSubmit">
      <BaseInput v-model="email" label="Email" type="email" placeholder="seu@email.com" required />
      <BaseButton type="submit" variant="primary" class="w-full" :loading="loading">
        Enviar link
      </BaseButton>
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-500">
        Voltar ao login
      </router-link>
    </p>
  </div>
</template>
