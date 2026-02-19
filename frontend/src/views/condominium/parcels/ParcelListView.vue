<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import { parcelApi } from '@/api'
import { PARCEL_STATUS_LABELS } from '@/types/parcel.types'
import { formatDateTime } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))
const mineOnly = computed(() => !!route.meta.mineOnly)

const loading = ref(false)
const activeTab = ref<string>('ALL')
const parcels = ref<Record<string, any>[]>([])
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const pageSize = ref(15)

const tabs = [
  { key: 'ALL', label: 'Todas' },
  { key: 'RECEIVED', label: 'Recebidas' },
  { key: 'NOTIFIED', label: 'Notificadas' },
  { key: 'DELIVERED', label: 'Entregues' },
] as const

const columns = [
  { key: 'unitIdentifier', label: 'Unidade' },
  { key: 'recipientName', label: 'Destinatario', class: 'hidden sm:table-cell' },
  { key: 'carrier', label: 'Transportadora', class: 'hidden md:table-cell' },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Recebida em', class: 'hidden lg:table-cell' },
]

onMounted(async () => {
  await loadParcels()
})

watch(activeTab, () => {
  page.value = 0
  loadParcels()
})

async function loadParcels() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: page.value,
      size: pageSize.value,
    }
    if (activeTab.value !== 'ALL') {
      params.status = activeTab.value
    }

    const response = mineOnly.value
      ? await parcelApi.getMyParcels(condoId.value, params)
      : await parcelApi.listParcels(condoId.value, params)
    const data = response.data

    parcels.value = data.content.map((p) => ({
      id: p.id,
      unitIdentifier: p.unitIdentifier,
      recipientName: p.recipientName || '-',
      carrier: p.carrier || '-',
      status: PARCEL_STATUS_LABELS[p.status] || p.status,
      rawStatus: p.status,
      createdAt: formatDateTime(p.createdAt),
    }))

    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch {
    parcels.value = []
    totalPages.value = 1
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: string): 'blue' | 'yellow' | 'green' | 'gray' {
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

function onRowClick(row: Record<string, any>) {
  router.push(`/c/${condoId.value}/parcels/${row.id}`)
}

function onPageChange(newPage: number) {
  page.value = newPage
  loadParcels()
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ mineOnly ? 'Minhas Encomendas' : 'Encomendas' }}</h1>
        <p class="mt-1 text-sm text-gray-500">{{ mineOnly ? 'Encomendas destinadas a voce' : 'Gerencie as encomendas do condominio' }}</p>
      </div>
      <BaseButton v-if="!mineOnly" variant="primary" @click="router.push(`/c/${condoId}/parcels/receive`)">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Registrar Encomenda
      </BaseButton>
    </div>

    <!-- Filter Tabs -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex gap-4 overflow-x-auto" aria-label="Tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors"
          :class="
            activeTab === tab.key
              ? 'border-primary-500 text-primary-600'
              : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
          "
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
      </nav>
    </div>

    <!-- Table -->
    <BaseCard :padding="false">
      <BaseTable
        :columns="columns"
        :data="parcels"
        :loading="loading"
        empty-message="Nenhuma encomenda encontrada."
        @row-click="onRowClick"
      >
        <template #cell-status="{ row }">
          <BaseBadge :variant="statusBadgeVariant(row.rawStatus)">
            {{ row.status }}
          </BaseBadge>
        </template>
      </BaseTable>

      <BasePagination
        v-if="totalPages > 1"
        :page="page"
        :total-pages="totalPages"
        :total-elements="totalElements"
        :size="pageSize"
        class="border-t border-gray-200"
        @page-change="onPageChange"
      />
    </BaseCard>
  </div>
</template>
