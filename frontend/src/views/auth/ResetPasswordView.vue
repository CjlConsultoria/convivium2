<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'

const route = useRoute()
const router = useRouter()
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

async function handleReset() {
  error.value = ''
  if (password.value !== confirmPassword.value) {
    error.value = 'As senhas nao conferem.'
    return
  }
  loading.value = true
  try {
    const { default: apiClient } = await import('@/api/client')
    await apiClient.post('/auth/reset-password', {
      token: route.query.token,
      newPassword: password.value,
    })
    router.push('/login')
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao redefinir senha.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900">Nova senha</h2>
    <p class="mt-1 text-sm text-gray-500">Defina sua nova senha</p>

    <BaseAlert v-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <form class="mt-6 space-y-4" @submit.prevent="handleReset">
      <BaseInput v-model="password" label="Nova senha" type="password" placeholder="Minimo 6 caracteres" required />
      <BaseInput v-model="confirmPassword" label="Confirmar senha" type="password" placeholder="Repita a senha" required />
      <BaseButton type="submit" variant="primary" class="w-full" :loading="loading">
        Redefinir senha
      </BaseButton>
    </form>
  </div>
</template>
