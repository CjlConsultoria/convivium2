<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(false)
const success = ref('')
const error = ref('')

const title = ref('')
const content = ref('')
const category = ref('')
const isEmergency = ref(false)
const isPinned = ref(false)

const targetRoles = ref<string[]>([])

const categoryOptions = [
  { value: 'GENERAL', label: 'Geral' },
  { value: 'MAINTENANCE', label: 'Manutencao' },
  { value: 'FINANCIAL', label: 'Financeiro' },
  { value: 'EVENT', label: 'Eventos' },
  { value: 'RULE', label: 'Regras' },
]

const roleCheckboxes = [
  { value: 'SINDICO', label: 'Sindico' },
  { value: 'SUB_SINDICO', label: 'Sub-Sindico' },
  { value: 'CONSELHEIRO', label: 'Conselheiro' },
  { value: 'PORTEIRO', label: 'Porteiro' },
  { value: 'ZELADOR', label: 'Zelador' },
  { value: 'FAXINEIRA', label: 'Faxineira' },
  { value: 'MORADOR', label: 'Morador' },
]

function toggleRole(role: string) {
  const index = targetRoles.value.indexOf(role)
  if (index === -1) {
    targetRoles.value.push(role)
  } else {
    targetRoles.value.splice(index, 1)
  }
}

async function handleSubmit() {
  error.value = ''
  success.value = ''

  if (!title.value || !content.value || !category.value) {
    error.value = 'Preencha todos os campos obrigatorios.'
    return
  }

  loading.value = true
  try {
    // Placeholder API call - replace with actual implementation
    const { apiClient } = await import('@/api')
    await apiClient.post(`/condominiums/${condoId.value}/announcements`, {
      title: title.value,
      content: content.value,
      category: category.value,
      targetRoles: targetRoles.value.length > 0 ? targetRoles.value : null,
      isEmergency: isEmergency.value,
      isPinned: isPinned.value,
    })

    success.value = 'Comunicado publicado com sucesso!'
    setTimeout(() => {
      router.push(`/c/${condoId.value}/announcements`)
    }, 1500)
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao publicar comunicado. Tente novamente.'
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.push(`/c/${condoId.value}/announcements`)
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Novo Comunicado</h1>
      <p class="mt-1 text-sm text-gray-500">Crie um novo comunicado para os moradores</p>
    </div>

    <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseCard title="Detalhes do Comunicado">
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <BaseInput
          v-model="title"
          label="Titulo"
          placeholder="Titulo do comunicado"
          required
        />

        <BaseTextarea
          v-model="content"
          label="Conteudo"
          placeholder="Escreva o conteudo do comunicado..."
          :rows="8"
          required
        />

        <BaseSelect
          v-model="category"
          label="Categoria"
          :options="categoryOptions"
          placeholder="Selecione a categoria"
          required
        />

        <!-- Target Roles -->
        <div>
          <label class="label">Destinatarios (deixe vazio para todos)</label>
          <div class="mt-2 grid grid-cols-2 gap-2 sm:grid-cols-3">
            <label
              v-for="role in roleCheckboxes"
              :key="role.value"
              class="flex items-center gap-2 rounded-lg border border-gray-200 px-3 py-2 text-sm transition-colors"
              :class="targetRoles.includes(role.value) ? 'border-primary-300 bg-primary-50' : 'hover:bg-gray-50'"
            >
              <input
                type="checkbox"
                :checked="targetRoles.includes(role.value)"
                class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                @change="toggleRole(role.value)"
              />
              <span class="text-gray-700">{{ role.label }}</span>
            </label>
          </div>
        </div>

        <!-- Toggles -->
        <div class="space-y-3">
          <!-- Emergency toggle -->
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
              :class="isEmergency ? 'bg-red-600' : 'bg-gray-200'"
              role="switch"
              :aria-checked="isEmergency"
              @click="isEmergency = !isEmergency"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
                :class="isEmergency ? 'translate-x-5' : 'translate-x-0'"
              />
            </button>
            <div>
              <p class="text-sm font-medium text-gray-700">Comunicado de emergencia</p>
              <p class="text-xs text-gray-500">Sera destacado com alerta vermelho</p>
            </div>
          </div>

          <!-- Pinned toggle -->
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
              :class="isPinned ? 'bg-primary-600' : 'bg-gray-200'"
              role="switch"
              :aria-checked="isPinned"
              @click="isPinned = !isPinned"
            >
              <span
                class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
                :class="isPinned ? 'translate-x-5' : 'translate-x-0'"
              />
            </button>
            <div>
              <p class="text-sm font-medium text-gray-700">Fixar comunicado</p>
              <p class="text-xs text-gray-500">Aparecera no topo da lista de comunicados</p>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex flex-col gap-3 border-t border-gray-200 pt-4 sm:flex-row sm:justify-end">
          <BaseButton variant="outline" type="button" @click="handleCancel">
            Cancelar
          </BaseButton>
          <BaseButton variant="primary" type="submit" :loading="loading">
            Publicar Comunicado
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>
