<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { complaintApi } from '@/api'
import { useAuthStore } from '@/stores/auth.store'
import type { ComplaintDetail, ComplaintStatus } from '@/types/complaint.types'
import {
  COMPLAINT_STATUS_LABELS,
  COMPLAINT_CATEGORY_LABELS,
} from '@/types/complaint.types'
import { PRIORITY_LABELS } from '@/utils/constants'
import { formatDateTime, timeAgo } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const condoId = computed(() => Number(route.params.condoId))
const complaintId = computed(() => Number(route.params.complaintId))

const loading = ref(true)
const sendingResponse = ref(false)
const updatingStatus = ref(false)
const success = ref('')
const error = ref('')
const complaint = ref<ComplaintDetail | null>(null)

const responseMessage = ref('')
const isInternalResponse = ref(false)

const isSindico = computed(() => authStore.hasRole('SINDICO') || authStore.hasRole('SUB_SINDICO'))

onMounted(async () => {
  await loadComplaint()
})

async function loadComplaint() {
  loading.value = true
  try {
    const response = await complaintApi.getComplaint(condoId.value, complaintId.value)
    complaint.value = response.data
  } catch {
    // Placeholder data
    complaint.value = {
      id: complaintId.value,
      condominiumId: condoId.value,
      complainantName: 'Maria Silva',
      isAnonymous: false,
      category: 'NOISE',
      title: 'Barulho excessivo no Bloco A',
      description:
        'Todos os fins de semana, a unidade 302 do Bloco A realiza festas com som alto ate altas horas da madrugada. Isso tem atrapalhado o descanso dos moradores vizinhos. Ja tentamos conversar diretamente mas sem sucesso.',
      unitIdentifier: '301',
      status: 'IN_REVIEW',
      priority: 'HIGH',
      createdAt: '2025-02-10T14:30:00Z',
      updatedAt: '2025-02-11T09:00:00Z',
      responses: [
        {
          id: 1,
          responderName: 'Carlos Administrador',
          responderRole: 'SINDICO',
          message: 'Obrigado por reportar. Vamos investigar a situacao e tomar as providencias cabiveis.',
          isInternal: false,
          createdAt: '2025-02-11T09:00:00Z',
        },
        {
          id: 2,
          responderName: 'Carlos Administrador',
          responderRole: 'SINDICO',
          message: 'Verificar historico de denuncias contra a unidade 302. Considerar envio de notificacao formal.',
          isInternal: true,
          createdAt: '2025-02-11T09:15:00Z',
        },
      ],
      attachments: [],
    }
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: ComplaintStatus): 'blue' | 'yellow' | 'green' | 'gray' {
  switch (status) {
    case 'OPEN':
      return 'blue'
    case 'IN_REVIEW':
      return 'yellow'
    case 'RESPONDED':
    case 'RESOLVED':
      return 'green'
    case 'CLOSED':
      return 'gray'
    default:
      return 'gray'
  }
}

function priorityBadgeVariant(priority: string): 'gray' | 'blue' | 'yellow' | 'red' {
  switch (priority) {
    case 'LOW':
      return 'gray'
    case 'MEDIUM':
      return 'blue'
    case 'HIGH':
      return 'yellow'
    case 'URGENT':
      return 'red'
    default:
      return 'gray'
  }
}

async function sendResponse() {
  if (!responseMessage.value.trim()) return

  sendingResponse.value = true
  error.value = ''
  try {
    await complaintApi.addResponse(condoId.value, complaintId.value, {
      message: responseMessage.value,
      isInternal: isInternalResponse.value,
    })
    responseMessage.value = ''
    isInternalResponse.value = false
    success.value = 'Resposta enviada com sucesso!'
    await loadComplaint()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao enviar resposta.'
  } finally {
    sendingResponse.value = false
  }
}

async function changeStatus(newStatus: string) {
  updatingStatus.value = true
  error.value = ''
  try {
    await complaintApi.updateStatus(condoId.value, complaintId.value, newStatus)
    success.value = 'Status atualizado com sucesso!'
    await loadComplaint()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao atualizar status.'
  } finally {
    updatingStatus.value = false
  }
}

function goBack() {
  router.push(`/c/${condoId.value}/complaints`)
}
</script>

<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <!-- Back button -->
    <button
      class="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
      @click="goBack"
    >
      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      Voltar para lista
    </button>

    <BaseLoadingSpinner v-if="loading" text="Carregando denuncia..." />

    <template v-else-if="complaint">
      <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
        {{ success }}
      </BaseAlert>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <!-- Complaint Info Card -->
      <BaseCard>
        <div class="space-y-4">
          <!-- Title and badges -->
          <div>
            <h1 class="text-xl font-bold text-gray-900 sm:text-2xl">{{ complaint.title }}</h1>
            <div class="mt-2 flex flex-wrap gap-2">
              <BaseBadge :variant="statusBadgeVariant(complaint.status)">
                {{ COMPLAINT_STATUS_LABELS[complaint.status] }}
              </BaseBadge>
              <BaseBadge :variant="priorityBadgeVariant(complaint.priority)">
                {{ PRIORITY_LABELS[complaint.priority] || complaint.priority }}
              </BaseBadge>
              <BaseBadge variant="gray">
                {{ COMPLAINT_CATEGORY_LABELS[complaint.category] }}
              </BaseBadge>
            </div>
          </div>

          <!-- Description -->
          <div class="rounded-lg bg-gray-50 p-4">
            <p class="whitespace-pre-wrap text-sm text-gray-700">{{ complaint.description }}</p>
          </div>

          <!-- Meta info -->
          <div class="grid grid-cols-2 gap-4 text-sm sm:grid-cols-4">
            <div>
              <p class="font-medium text-gray-500">Status</p>
              <p class="mt-0.5">
                <BaseBadge :variant="statusBadgeVariant(complaint.status)">
                  {{ COMPLAINT_STATUS_LABELS[complaint.status] }}
                </BaseBadge>
              </p>
            </div>
            <div>
              <p class="font-medium text-gray-500">Denunciante</p>
              <p class="text-gray-900">{{ complaint.isAnonymous ? 'Anônimo' : complaint.complainantName || '-' }}</p>
            </div>
            <div>
              <p class="font-medium text-gray-500">Unidade</p>
              <p class="text-gray-900">{{ complaint.unitIdentifier || '-' }}</p>
            </div>
            <div>
              <p class="font-medium text-gray-500">Criada em</p>
              <p class="text-gray-900">{{ formatDateTime(complaint.createdAt) }}</p>
            </div>
            <div>
              <p class="font-medium text-gray-500">Atualizada em</p>
              <p class="text-gray-900">{{ formatDateTime(complaint.updatedAt) }}</p>
            </div>
          </div>

          <!-- Status change buttons (sindico only) -->
          <div v-if="isSindico && complaint.status !== 'CLOSED'" class="flex flex-wrap gap-2 border-t border-gray-200 pt-4">
            <BaseButton
              v-if="complaint.status === 'OPEN'"
              variant="outline"
              size="sm"
              :loading="updatingStatus"
              @click="changeStatus('IN_REVIEW')"
            >
              Marcar em Analise
            </BaseButton>
            <BaseButton
              v-if="complaint.status !== 'RESOLVED' && (complaint.status as ComplaintStatus) !== 'CLOSED'"
              variant="primary"
              size="sm"
              :loading="updatingStatus"
              @click="changeStatus('RESOLVED')"
            >
              Resolver
            </BaseButton>
            <BaseButton
              variant="secondary"
              size="sm"
              :loading="updatingStatus"
              @click="changeStatus('CLOSED')"
            >
              Encerrar
            </BaseButton>
          </div>
        </div>
      </BaseCard>

      <!-- Response Thread -->
      <BaseCard title="Respostas">
        <div v-if="complaint.responses.length === 0" class="py-4 text-center text-sm text-gray-500">
          Nenhuma resposta ainda.
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="response in complaint.responses"
            :key="response.id"
            class="rounded-lg border p-4"
            :class="response.isInternal ? 'border-yellow-200 bg-yellow-50' : 'border-gray-200 bg-white'"
          >
            <div class="mb-2 flex flex-wrap items-center gap-2">
              <span class="text-sm font-medium text-gray-900">{{ response.responderName }}</span>
              <BaseBadge variant="gray">{{ response.responderRole }}</BaseBadge>
              <span v-if="response.isInternal" class="text-xs font-medium text-yellow-700">
                (Interno)
              </span>
              <span class="ml-auto text-xs text-gray-400">{{ timeAgo(response.createdAt) }}</span>
            </div>
            <p class="whitespace-pre-wrap text-sm text-gray-700">{{ response.message }}</p>
          </div>
        </div>

        <!-- Response form -->
        <div v-if="complaint.status !== 'CLOSED'" class="mt-6 border-t border-gray-200 pt-4">
          <h4 class="mb-3 text-sm font-medium text-gray-900">Responder</h4>

          <BaseTextarea
            v-model="responseMessage"
            placeholder="Escreva sua resposta..."
            :rows="3"
          />

          <div class="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <label v-if="isSindico" class="flex items-center gap-2 text-sm">
              <input
                v-model="isInternalResponse"
                type="checkbox"
                class="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
              />
              <span class="text-gray-600">Resposta interna (somente administracao)</span>
            </label>
            <div v-else />

            <BaseButton
              variant="primary"
              size="sm"
              :loading="sendingResponse"
              :disabled="!responseMessage.trim()"
              @click="sendResponse"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              Enviar
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>
  </div>
</template>
