<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'

const platformName = ref('Convivium')
const supportEmail = ref('suporte@convivium.com')
const maxCondominiums = ref('100')
const maintenanceMode = ref(false)
const saving = ref(false)
const success = ref('')

async function saveSettings() {
  saving.value = true
  // TODO: Implement backend endpoint for platform settings
  await new Promise(resolve => setTimeout(resolve, 500))
  success.value = 'Configuracoes salvas com sucesso!'
  saving.value = false
}
</script>

<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Configuracoes</h1>
      <p class="mt-1 text-sm text-gray-500">Configuracoes gerais da plataforma</p>
    </div>

    <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>

    <!-- General Settings -->
    <BaseCard title="Configuracoes Gerais">
      <div class="space-y-4">
        <BaseInput v-model="platformName" label="Nome da Plataforma" />
        <BaseInput v-model="supportEmail" label="Email de Suporte" type="email" />
        <BaseInput v-model="maxCondominiums" label="Limite de Condominios" type="number" />

        <div class="flex items-center gap-3">
          <label class="relative inline-flex cursor-pointer items-center">
            <input v-model="maintenanceMode" type="checkbox" class="peer sr-only" />
            <div class="peer h-6 w-11 rounded-full bg-gray-200 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:bg-primary-600 peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300"></div>
          </label>
          <div>
            <p class="text-sm font-medium text-gray-900">Modo Manutencao</p>
            <p class="text-xs text-gray-500">Quando ativado, apenas administradores podem acessar</p>
          </div>
        </div>
      </div>

      <div class="mt-6 border-t border-gray-200 pt-4">
        <BaseButton variant="primary" :loading="saving" @click="saveSettings">
          Salvar Configuracoes
        </BaseButton>
      </div>
    </BaseCard>

    <!-- Email Settings -->
    <BaseCard title="Configuracoes de Email">
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
          <svg class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
          </svg>
        </div>
        <div>
          <h3 class="text-sm font-medium text-gray-900">SMTP / Mailhog</h3>
          <p class="mt-1 text-sm text-gray-500">
            Configuracoes de email sao gerenciadas via application.yml no backend.
            Em desenvolvimento, emails sao capturados pelo Mailhog (porta 8025).
          </p>
        </div>
      </div>
    </BaseCard>

    <!-- Storage Settings -->
    <BaseCard title="Configuracoes de Storage">
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-yellow-100">
          <svg class="h-5 w-5 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
          </svg>
        </div>
        <div>
          <h3 class="text-sm font-medium text-gray-900">MinIO (S3 Compatible)</h3>
          <p class="mt-1 text-sm text-gray-500">
            Fotos de encomendas, anexos de denuncias e documentos sao armazenados no MinIO.
            Configuracoes em application.yml. Console em http://localhost:9001.
          </p>
        </div>
      </div>
    </BaseCard>
  </div>
</template>
