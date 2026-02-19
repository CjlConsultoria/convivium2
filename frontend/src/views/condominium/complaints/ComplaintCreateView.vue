<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseFileUpload from '@/components/base/BaseFileUpload.vue'
import { complaintApi } from '@/api'
import type { ComplaintCreateRequest, ComplaintCategory } from '@/types/complaint.types'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(false)
const success = ref('')
const error = ref('')
const attachments = ref<File[]>([])

const form = ref<ComplaintCreateRequest>({
  category: '' as ComplaintCategory,
  title: '',
  description: '',
  isAnonymous: false,
  unitId: null,
  priority: 'MEDIUM',
})

const categoryOptions = [
  { value: 'NOISE', label: 'Barulho' },
  { value: 'MAINTENANCE', label: 'Manutencao' },
  { value: 'SECURITY', label: 'Seguranca' },
  { value: 'PARKING', label: 'Estacionamento' },
  { value: 'PET', label: 'Animais' },
  { value: 'COMMON_AREA', label: 'Area Comum' },
  { value: 'OTHER', label: 'Outros' },
]

const priorityOptions = [
  { value: 'LOW', label: 'Baixa' },
  { value: 'MEDIUM', label: 'Media' },
  { value: 'HIGH', label: 'Alta' },
  { value: 'URGENT', label: 'Urgente' },
]

function onFilesSelected(files: File[]) {
  attachments.value = [...attachments.value, ...files]
}

function removeAttachment(index: number) {
  attachments.value.splice(index, 1)
}

async function handleSubmit() {
  error.value = ''
  success.value = ''

  if (!form.value.category || !form.value.title || !form.value.description) {
    error.value = 'Preencha todos os campos obrigatorios.'
    return
  }

  loading.value = true
  try {
    await complaintApi.createComplaint(condoId.value, form.value)
    success.value = 'Denuncia registrada com sucesso!'

    setTimeout(() => {
      router.push(`/c/${condoId.value}/complaints`)
    }, 1500)
  } catch (err: any) {
    error.value =
      err.response?.data?.message || 'Erro ao registrar denuncia. Tente novamente.'
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.push(`/c/${condoId.value}/complaints`)
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Nova Denuncia</h1>
      <p class="mt-1 text-sm text-gray-500">Registre uma nova denuncia para o condominio</p>
    </div>

    <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseCard title="Detalhes da Denuncia">
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseSelect
            v-model="form.category"
            label="Categoria"
            :options="categoryOptions"
            placeholder="Selecione a categoria"
            required
          />
          <BaseSelect
            v-model="form.priority"
            label="Prioridade"
            :options="priorityOptions"
            placeholder="Selecione a prioridade"
            required
          />
        </div>

        <BaseInput
          v-model="form.title"
          label="Titulo"
          placeholder="Descreva brevemente o problema"
          required
        />

        <BaseTextarea
          v-model="form.description"
          label="Descricao"
          placeholder="Descreva o problema com o maximo de detalhes possivel..."
          :rows="6"
          required
        />

        <!-- Anonymous Toggle -->
        <div class="flex items-center gap-3">
          <button
            type="button"
            class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2"
            :class="form.isAnonymous ? 'bg-primary-600' : 'bg-gray-200'"
            role="switch"
            :aria-checked="form.isAnonymous"
            @click="form.isAnonymous = !form.isAnonymous"
          >
            <span
              class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
              :class="form.isAnonymous ? 'translate-x-5' : 'translate-x-0'"
            />
          </button>
          <div>
            <p class="text-sm font-medium text-gray-700">Denuncia anonima</p>
            <p class="text-xs text-gray-500">Seu nome nao sera exibido na denuncia</p>
          </div>
        </div>

        <!-- File Upload -->
        <BaseFileUpload
          label="Anexos (opcional)"
          accept="image/*,.pdf,.doc,.docx"
          :multiple="true"
          :max-size-mb="10"
          @files-selected="onFilesSelected"
        />

        <!-- Attached files list -->
        <div v-if="attachments.length > 0" class="space-y-2">
          <p class="text-xs font-medium text-gray-500">Arquivos selecionados:</p>
          <div
            v-for="(file, index) in attachments"
            :key="index"
            class="flex items-center justify-between rounded-lg border border-gray-200 px-3 py-2"
          >
            <div class="flex items-center gap-2 text-sm text-gray-700">
              <svg class="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
              </svg>
              {{ file.name }}
            </div>
            <button
              type="button"
              class="text-gray-400 hover:text-red-500"
              @click="removeAttachment(index)"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex flex-col gap-3 border-t border-gray-200 pt-4 sm:flex-row sm:justify-end">
          <BaseButton variant="outline" type="button" @click="handleCancel">
            Cancelar
          </BaseButton>
          <BaseButton variant="primary" type="submit" :loading="loading">
            Registrar Denuncia
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>
