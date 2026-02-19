<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { condominiumApi } from '@/api'
import apiClient from '@/api/client'
import { formatDate } from '@/utils/formatters'

const router = useRouter()

const loading = ref(true)
const error = ref('')

// Since there's no admin/users endpoint yet, we show a summary view
// listing condominiums and their user counts
const condominiums = ref<Record<string, any>[]>([])

const columns = [
  { key: 'name', label: 'Condominio' },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Criado em', class: 'hidden md:table-cell' },
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

    condominiums.value = data.content.map((c) => ({
      id: c.id,
      name: c.name,
      status: c.status,
      createdAt: formatDate(c.createdAt),
    }))
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar dados.'
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
    case 'DEACTIVATED': return 'Desativado'
    default: return status
  }
}

function onRowClick(row: Record<string, any>) {
  router.push(`/admin/condominiums/${row.id}`)
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Usuarios da Plataforma</h1>
      <p class="mt-1 text-sm text-gray-500">Visao geral dos usuarios por condominio</p>
    </div>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseLoadingSpinner v-if="loading" text="Carregando usuarios..." />

    <template v-else>
      <!-- Info card -->
      <BaseCard>
        <div class="flex items-start gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
            <svg class="h-5 w-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <div>
            <h3 class="text-sm font-medium text-gray-900">Gestao de Usuarios</h3>
            <p class="mt-1 text-sm text-gray-500">
              Para gerenciar usuarios de um condominio especifico, clique no condominio abaixo e acesse a aba de moradores.
              Voce pode aprovar, rejeitar ou remover usuarios de cada condominio.
            </p>
          </div>
        </div>
      </BaseCard>

      <!-- Condominiums list -->
      <BaseCard title="Condominios" subtitle="Clique para gerenciar os usuarios">
        <BaseTable
          :columns="columns"
          :data="condominiums"
          :loading="loading"
          empty-message="Nenhum condominio encontrado."
          @row-click="onRowClick"
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
