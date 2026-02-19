<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { condominiumApi } from '@/api'
import { formatDate, formatCurrency } from '@/utils/formatters'

const loading = ref(true)
const error = ref('')

const subscriptions = ref<Record<string, any>[]>([])

const columns = [
  { key: 'condominiumName', label: 'Condominio' },
  { key: 'plan', label: 'Plano' },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Desde', class: 'hidden md:table-cell' },
]

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const response = await condominiumApi.listCondominiums({ page: 0, size: 100 })
    const data = response.data

    subscriptions.value = data.content.map((c) => ({
      id: c.id,
      condominiumName: c.name,
      plan: 'Basico',
      status: c.status,
      createdAt: formatDate(c.createdAt),
    }))
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar assinaturas.'
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: string): 'green' | 'yellow' | 'red' | 'gray' {
  switch (status) {
    case 'ACTIVE': return 'green'
    case 'PENDING': return 'yellow'
    case 'SUSPENDED': return 'red'
    default: return 'gray'
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'ACTIVE': return 'Ativo'
    case 'PENDING': return 'Pendente'
    case 'SUSPENDED': return 'Suspenso'
    case 'DEACTIVATED': return 'Cancelado'
    default: return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Assinaturas</h1>
      <p class="mt-1 text-sm text-gray-500">Gerencie as assinaturas dos condominios</p>
    </div>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseLoadingSpinner v-if="loading" text="Carregando assinaturas..." />

    <template v-else>
      <!-- Stats -->
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-3">
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Total Assinaturas</p>
          <p class="text-2xl font-bold text-gray-900">{{ subscriptions.length }}</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Ativas</p>
          <p class="text-2xl font-bold text-green-600">{{ subscriptions.filter(s => s.status === 'ACTIVE').length }}</p>
        </div>
        <div class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm">
          <p class="text-xs text-gray-500">Suspensas</p>
          <p class="text-2xl font-bold text-red-600">{{ subscriptions.filter(s => s.status === 'SUSPENDED').length }}</p>
        </div>
      </div>

      <!-- Info -->
      <BaseCard>
        <div class="flex items-start gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-100">
            <svg class="h-5 w-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-900">Integracao Stripe</h3>
            <p class="mt-1 text-sm text-gray-500">
              A integracao com Stripe para pagamentos recorrentes sera configurada na Fase 5.
              Atualmente, o status das assinaturas segue o status do condominio.
            </p>
          </div>
        </div>
      </BaseCard>

      <!-- Table -->
      <BaseCard title="Assinaturas" :padding="false">
        <BaseTable
          :columns="columns"
          :data="subscriptions"
          :loading="loading"
          empty-message="Nenhuma assinatura encontrada."
        >
          <template #cell-status="{ row }">
            <BaseBadge :variant="statusBadgeVariant(row.status)">
              {{ statusLabel(row.status) }}
            </BaseBadge>
          </template>
        </BaseTable>
      </BaseCard>
    </template>
  </div>
</template>
