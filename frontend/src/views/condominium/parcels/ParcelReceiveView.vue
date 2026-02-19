<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseFileUpload from '@/components/base/BaseFileUpload.vue'
import { parcelApi, condominiumApi, userApi } from '@/api'
import type { Unit } from '@/types/condominium.types'
import type { User } from '@/types/user.types'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(false)
const success = ref(false)
const error = ref('')

const units = ref<Unit[]>([])
const residents = ref<User[]>([])
const photos = ref<File[]>([])

const selectedUnitId = ref<string>('')
const selectedRecipientId = ref<string>('')
const carrier = ref('')
const trackingNumber = ref('')
const description = ref('')

const generatedCodes = ref<{ pickupCode: string; residentCode: string } | null>(null)

const unitOptions = computed(() =>
  units.value.map((u) => ({
    value: String(u.id),
    label: u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier,
  })),
)

const recipientOptions = computed(() => [
  { value: '', label: 'Selecione o morador' },
  ...residents.value.map((r) => ({
    value: String(r.id),
    label: r.name,
  })),
])

onMounted(async () => {
  await loadUnits()
})

watch(selectedUnitId, async (unitId) => {
  selectedRecipientId.value = ''
  if (unitId) {
    await loadResidents(unitId)
  } else {
    residents.value = []
  }
})

async function loadUnits() {
  try {
    const response = await condominiumApi.listUnits(condoId.value)
    units.value = response.data
  } catch {
    units.value = [
      { id: 1, condominiumId: condoId.value, buildingId: 1, buildingName: 'Bloco A', identifier: '101', floor: 1, type: 'APARTMENT', areaSqm: null, isOccupied: true },
      { id: 2, condominiumId: condoId.value, buildingId: 1, buildingName: 'Bloco A', identifier: '201', floor: 2, type: 'APARTMENT', areaSqm: null, isOccupied: true },
      { id: 3, condominiumId: condoId.value, buildingId: 1, buildingName: 'Bloco A', identifier: '301', floor: 3, type: 'APARTMENT', areaSqm: null, isOccupied: true },
      { id: 4, condominiumId: condoId.value, buildingId: 2, buildingName: 'Bloco B', identifier: '101', floor: 1, type: 'APARTMENT', areaSqm: null, isOccupied: true },
      { id: 5, condominiumId: condoId.value, buildingId: 2, buildingName: 'Bloco B', identifier: '201', floor: 2, type: 'APARTMENT', areaSqm: null, isOccupied: true },
    ]
  }
}

/** Carrega apenas moradores da unidade selecionada (API retorna unitId no objeto). */
async function loadResidents(unitId: string) {
  if (!unitId) {
    residents.value = []
    return
  }
  try {
    const response = await userApi.listUsers(condoId.value, { page: 0, size: 500 })
    const content = response.data?.content ?? []
    residents.value = content.filter((u: any) => {
      const uid = u.unitId ?? u.condominiumRoles?.[0]?.unitId
      return uid != null && String(uid) === unitId
    })
  } catch {
    residents.value = []
  }
}

function onPhotosSelected(files: File[]) {
  photos.value = [...photos.value, ...files]
}

function removePhoto(index: number) {
  photos.value.splice(index, 1)
}

async function handleSubmit() {
  error.value = ''

  if (!selectedUnitId.value) {
    error.value = 'Selecione a unidade.'
    return
  }

  loading.value = true
  try {
    const createResponse = await parcelApi.createParcel(condoId.value, {
      unitId: Number(selectedUnitId.value),
      recipientId: selectedRecipientId.value ? Number(selectedRecipientId.value) : null,
      carrier: carrier.value || null,
      trackingNumber: trackingNumber.value || null,
      description: description.value || null,
    })

    const parcelId = createResponse.data.id

    // Upload photos if any
    for (const photo of photos.value) {
      await parcelApi.uploadPhoto(condoId.value, parcelId, photo, 'RECEIPT')
    }

    // Um único código é gerado na criação; o morador vê em Minhas Encomendas e informa ao porteiro na retirada. Aqui não exibimos nenhum código.
    generatedCodes.value = { pickupCode: '', residentCode: '' }

    success.value = true
  } catch (err: any) {
    error.value =
      err.response?.data?.message || 'Erro ao registrar encomenda. Tente novamente.'
  } finally {
    loading.value = false
  }
}

function handleNewParcel() {
  success.value = false
  generatedCodes.value = null
  selectedUnitId.value = ''
  selectedRecipientId.value = ''
  carrier.value = ''
  trackingNumber.value = ''
  description.value = ''
  photos.value = []
}

function goToList() {
  router.push(`/c/${condoId.value}/parcels`)
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Registrar Nova Encomenda</h1>
      <p class="mt-1 text-sm text-gray-500">Registre o recebimento de uma encomenda na portaria</p>
    </div>

    <!-- Success: nenhum código é exibido ao porteiro; só o morador vê o código em Minhas Encomendas -->
    <template v-if="success && generatedCodes">
      <BaseAlert type="success">
        Encomenda registrada com sucesso! O morador vera o codigo em Minhas Encomendas e o informara a voce na hora da retirada; digite esse codigo no sistema para validar a entrega.
      </BaseAlert>

      <BaseCard title="Proximo passo">
        <div class="space-y-4">
          <p class="text-sm text-gray-600">
            Existe apenas um codigo, que so o morador conhece (ele ve em Minhas Encomendas). Na hora da retirada, o morador informa esse codigo a voce; voce digita no sistema para confirmar a entrega. Assim evitamos fraude.
          </p>

          <div class="flex flex-col gap-3 border-t border-gray-200 pt-4 sm:flex-row">
            <BaseButton variant="primary" @click="handleNewParcel">
              Registrar Outra Encomenda
            </BaseButton>
            <BaseButton variant="outline" @click="goToList">
              Ver Lista de Encomendas
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>

    <!-- Form -->
    <template v-else>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <BaseCard title="Dados da Encomenda">
        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <BaseSelect
              v-model="selectedUnitId"
              label="Unidade"
              :options="unitOptions"
              placeholder="Selecione a unidade"
              required
            />
            <BaseSelect
              v-model="selectedRecipientId"
              label="Destinatario"
              :options="recipientOptions"
              placeholder="Selecione o morador"
              :disabled="!selectedUnitId"
            />
          </div>

          <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <BaseInput
              v-model="carrier"
              label="Transportadora"
              placeholder="Ex: Correios, FedEx, DHL..."
            />
            <BaseInput
              v-model="trackingNumber"
              label="Codigo de Rastreamento"
              placeholder="Codigo de rastreio"
            />
          </div>

          <BaseInput
            v-model="description"
            label="Descricao"
            placeholder="Descricao da encomenda (tamanho, tipo, etc.)"
          />

          <!-- Photo Upload -->
          <BaseFileUpload
            label="Foto da Encomenda (opcional)"
            accept="image/*"
            :multiple="true"
            :max-size-mb="10"
            @files-selected="onPhotosSelected"
          />

          <!-- Photo list -->
          <div v-if="photos.length > 0" class="space-y-2">
            <p class="text-xs font-medium text-gray-500">Fotos selecionadas:</p>
            <div
              v-for="(photo, index) in photos"
              :key="index"
              class="flex items-center justify-between rounded-lg border border-gray-200 px-3 py-2"
            >
              <div class="flex items-center gap-2 text-sm text-gray-700">
                <svg class="h-4 w-4 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {{ photo.name }}
              </div>
              <button
                type="button"
                class="text-gray-400 hover:text-red-500"
                @click="removePhoto(index)"
              >
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex flex-col gap-3 border-t border-gray-200 pt-4 sm:flex-row sm:justify-end">
            <BaseButton variant="outline" type="button" @click="goToList">
              Cancelar
            </BaseButton>
            <BaseButton variant="primary" type="submit" :loading="loading">
              Registrar Encomenda
            </BaseButton>
          </div>
        </form>
      </BaseCard>
    </template>
  </div>
</template>
