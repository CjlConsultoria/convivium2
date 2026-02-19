<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { parcelApi } from '@/api'
import { useAuthStore } from '@/stores/auth.store'
import type { ParcelDetail, ParcelStatus } from '@/types/parcel.types'
import { PARCEL_STATUS_LABELS } from '@/types/parcel.types'
import { formatDateTime } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const condoId = computed(() => Number(route.params.condoId))
const parcelId = computed(() => Number(route.params.parcelId))

const loading = ref(true)
const verifying = ref(false)
const showVerification = ref(false)
const success = ref('')
const error = ref('')

const parcel = ref<ParcelDetail | null>(null)

const verificationCode = ref('')

const isPorteiro = computed(() => authStore.hasRole('PORTEIRO') || authStore.hasRole('SINDICO') || authStore.hasRole('SUB_SINDICO'))
const isMorador = computed(() => authStore.hasRole('MORADOR'))
const canVerify = computed(() => {
  if (!parcel.value) return false
  return isPorteiro.value && (parcel.value.status === 'RECEIVED' || parcel.value.status === 'NOTIFIED' || parcel.value.status === 'PICKUP_REQUESTED')
})
const showResidentCode = computed(() => isMorador.value && parcel.value && parcel.value.residentCode)

onMounted(async () => {
  await loadParcel()
})

async function loadParcel() {
  loading.value = true
  try {
    const response = await parcelApi.getParcel(condoId.value, parcelId.value)
    parcel.value = response.data
  } catch {
    // Placeholder data
    parcel.value = {
      id: parcelId.value,
      condominiumId: condoId.value,
      unitIdentifier: '205',
      recipientName: 'Joao Santos',
      receivedByName: 'Pedro Porteiro',
      carrier: 'Correios',
      trackingNumber: 'BR123456789SP',
      description: 'Caixa media, aparenta ser eletronico',
      status: 'NOTIFIED',
      deliveredAt: null,
      createdAt: '2025-02-15T10:15:00Z',
      pickupCode: null,
      residentCode: null,
      photos: [
        {
          id: 1,
          photoUrl: '',
          photoType: 'RECEIPT',
          createdAt: '2025-02-15T10:15:00Z',
        },
      ],
    }
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: ParcelStatus): 'blue' | 'yellow' | 'green' | 'gray' {
  switch (status) {
    case 'RECEIVED':
      return 'blue'
    case 'NOTIFIED':
    case 'PICKUP_REQUESTED':
      return 'yellow'
    case 'VERIFIED':
      return 'green'
    case 'DELIVERED':
      return 'gray'
    default:
      return 'gray'
  }
}

function startVerification() {
  showVerification.value = true
  verificationCode.value = ''
}

async function handleVerify() {
  error.value = ''
  success.value = ''

  if (!verificationCode.value?.trim()) {
    error.value = 'Digite o codigo que o morador informou.'
    return
  }

  verifying.value = true
  try {
    await parcelApi.verifyPickup(condoId.value, parcelId.value, {
      code: verificationCode.value.trim(),
      verificationMethod: 'CODE_MATCH',
    })
    success.value = 'Verificacao realizada com sucesso! Encomenda entregue.'
    showVerification.value = false
    await loadParcel()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Codigos invalidos. Verifique e tente novamente.'
  } finally {
    verifying.value = false
  }
}

function goBack() {
  router.push(`/c/${condoId.value}/parcels`)
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

    <BaseLoadingSpinner v-if="loading" text="Carregando encomenda..." />

    <template v-else-if="parcel">
      <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
        {{ success }}
      </BaseAlert>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <!-- Parcel Info Card -->
      <BaseCard title="Dados da Encomenda">
        <template #header-actions>
          <BaseBadge :variant="statusBadgeVariant(parcel.status)">
            {{ PARCEL_STATUS_LABELS[parcel.status] }}
          </BaseBadge>
        </template>

        <div class="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Unidade</p>
            <p class="text-sm text-gray-900">{{ parcel.unitIdentifier }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Destinatario</p>
            <p class="text-sm text-gray-900">{{ parcel.recipientName || '-' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Transportadora</p>
            <p class="text-sm text-gray-900">{{ parcel.carrier || '-' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Rastreamento</p>
            <p class="text-sm text-gray-900">{{ parcel.trackingNumber || '-' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Descricao</p>
            <p class="text-sm text-gray-900">{{ parcel.description || '-' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Recebido por</p>
            <p class="text-sm text-gray-900">{{ parcel.receivedByName }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Recebida em</p>
            <p class="text-sm text-gray-900">{{ formatDateTime(parcel.createdAt) }}</p>
          </div>
          <div v-if="parcel.deliveredAt">
            <p class="text-xs font-medium uppercase text-gray-500">Entregue em</p>
            <p class="text-sm text-gray-900">{{ formatDateTime(parcel.deliveredAt) }}</p>
          </div>
        </div>

        <!-- Verify button -->
        <div v-if="canVerify && !showVerification" class="mt-6 border-t border-gray-200 pt-4">
          <BaseButton variant="primary" @click="startVerification">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            Iniciar Verificacao
          </BaseButton>
        </div>
      </BaseCard>

      <!-- Photo Gallery -->
      <BaseCard v-if="parcel.photos.length > 0" title="Fotos">
        <div class="grid grid-cols-2 gap-4 sm:grid-cols-3">
          <div
            v-for="photo in parcel.photos"
            :key="photo.id"
            class="overflow-hidden rounded-lg border border-gray-200"
          >
            <div v-if="photo.photoUrl" class="aspect-square bg-gray-100">
              <img
                :src="photo.photoUrl"
                :alt="'Foto da encomenda'"
                class="h-full w-full object-cover"
              />
            </div>
            <div v-else class="flex aspect-square items-center justify-center bg-gray-100">
              <svg class="h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p class="px-2 py-1 text-xs text-gray-500">
              {{ formatDateTime(photo.createdAt) }}
            </p>
          </div>
        </div>
      </BaseCard>

      <!-- Um único código: só o morador vê; na retirada ele informa ao porteiro, que digita no sistema -->
      <BaseCard
        v-if="showResidentCode"
        title="Seu codigo para retirada"
      >
        <p class="mb-4 text-sm text-gray-600">
          Existe apenas um codigo, que so voce conhece. Na hora da retirada, informe este codigo ao porteiro; ele digitará no sistema para validar a entrega.
        </p>
        <div class="rounded-lg border-2 border-primary-200 bg-primary-50 p-6 text-center">
          <p class="text-xs font-medium uppercase text-primary-600">Seu codigo</p>
          <p class="mt-2 font-mono text-4xl font-bold text-primary-900">{{ parcel.residentCode }}</p>
          <p class="mt-2 text-sm text-primary-700">Diga este codigo ao porteiro na portaria.</p>
        </div>
      </BaseCard>

      <!-- Verificação: porteiro digita o único código que o morador informou (porteiro não vê o código antes) -->
      <BaseCard v-if="showVerification" title="Verificacao de Entrega">
        <div class="space-y-4">
          <p class="text-sm text-gray-600">
            Peça ao morador o codigo dele (ele ve em Minhas Encomendas). Digite abaixo o codigo que o morador informar para confirmar a retirada.
          </p>

          <BaseInput
            v-model="verificationCode"
            label="Codigo informado pelo morador"
            placeholder="Digite o codigo que o morador informar"
            required
          />

          <div class="flex flex-col gap-3 sm:flex-row">
            <BaseButton
              variant="primary"
              :loading="verifying"
              :disabled="!verificationCode?.trim()"
              @click="handleVerify"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              Verificar e Entregar
            </BaseButton>
            <BaseButton variant="outline" @click="showVerification = false">
              Cancelar
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>
  </div>
</template>
