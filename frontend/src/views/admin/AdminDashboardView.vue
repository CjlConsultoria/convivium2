<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { condominiumApi } from '@/api'
import { formatDate, formatCurrency } from '@/utils/formatters'

const router = useRouter()

const loading = ref(true)
const error = ref('')

const stats = ref({
  totalCondominios: 0,
  condominiosAtivos: 0,
  totalUsuarios: 0,
  receitaMensal: 0,
})

const condominiums = ref<Record<string, any>[]>([])

const columns = [
  { key: 'name', label: 'Nome' },
  { key: 'status', label: 'Status' },
  { key: 'city', label: 'Cidade/UF', class: 'hidden md:table-cell' },
  { key: 'createdAt', label: 'Data Criacao', class: 'hidden lg:table-cell' },
]

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const response = await condominiumApi.listCondominiums({ page: 0, size: 10 })
    const data = response.data

    condominiums.value = data.content.map((c) => ({
      id: c.id,
      name: c.name,
      status: c.status,
      city: c.addressCity && c.addressState ? `${c.addressCity}/${c.addressState}` : '-',
      createdAt: formatDate(c.createdAt),
    }))

    stats.value = {
      totalCondominios: data.totalElements,
      condominiosAtivos: data.content.filter((c) => c.status === 'ACTIVE').length,
      totalUsuarios: 0,
      receitaMensal: 0,
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar dados do painel.'
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: string): 'green' | 'yellow' | 'red' | 'gray' {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING':
      return 'yellow'
    case 'SUSPENDED':
      return 'red'
    case 'DEACTIVATED':
      return 'gray'
    default:
      return 'gray'
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'Ativo'
    case 'PENDING':
      return 'Pendente'
    case 'SUSPENDED':
      return 'Suspenso'
    case 'DEACTIVATED':
      return 'Desativado'
    default:
      return status
  }
}

function onRowClick(row: Record<string, any>) {
  router.push(`/admin/condominiums/${row.id}`)
}

const statCards = [
  { key: 'totalCondominios', label: 'Total Condominios', color: 'text-blue-600 bg-blue-100', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4' },
  { key: 'condominiosAtivos', label: 'Condominios Ativos', color: 'text-green-600 bg-green-100', icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z' },
  { key: 'totalUsuarios', label: 'Total Usuarios', color: 'text-purple-600 bg-purple-100', icon: 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z' },
  { key: 'receitaMensal', label: 'Receita Mensal', color: 'text-yellow-600 bg-yellow-100', icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z', isCurrency: true },
]
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 sm:text-3xl">Painel Administrativo</h1>
        <p class="mt-1 text-sm text-gray-500">Visao geral da plataforma Convivium</p>
      </div>
      <button
        class="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-primary-700"
        @click="router.push('/admin/condominiums/new')"
      >
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Novo Condominio
      </button>
    </div>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseLoadingSpinner v-if="loading" text="Carregando dados..." />

    <template v-else>
      <!-- Stat Cards -->
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div
          v-for="card in statCards"
          :key="card.key"
          class="rounded-xl border border-gray-200 bg-white p-4 shadow-sm sm:p-6"
        >
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg sm:h-12 sm:w-12" :class="card.color">
              <svg class="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="card.icon" />
              </svg>
            </div>
            <div>
              <p class="text-xs text-gray-500 sm:text-sm">{{ card.label }}</p>
              <p class="text-xl font-bold text-gray-900 sm:text-2xl">
                {{ card.isCurrency ? formatCurrency(stats[card.key as keyof typeof stats]) : stats[card.key as keyof typeof stats] }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <button
          class="flex flex-col items-center gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm transition hover:border-primary-300 hover:shadow-md"
          @click="router.push('/admin/condominiums')"
        >
          <svg class="h-8 w-8 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
          </svg>
          <span class="text-sm font-medium text-gray-700">Condominios</span>
        </button>
        <button
          class="flex flex-col items-center gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm transition hover:border-primary-300 hover:shadow-md"
          @click="router.push('/admin/users')"
        >
          <svg class="h-8 w-8 text-purple-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          <span class="text-sm font-medium text-gray-700">Usuarios</span>
        </button>
        <button
          class="flex flex-col items-center gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm transition hover:border-primary-300 hover:shadow-md"
          @click="router.push('/admin/subscriptions')"
        >
          <svg class="h-8 w-8 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
          </svg>
          <span class="text-sm font-medium text-gray-700">Assinaturas</span>
        </button>
        <button
          class="flex flex-col items-center gap-2 rounded-xl border border-gray-200 bg-white p-4 shadow-sm transition hover:border-primary-300 hover:shadow-md"
          @click="router.push('/admin/audit-logs')"
        >
          <svg class="h-8 w-8 text-orange-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
          </svg>
          <span class="text-sm font-medium text-gray-700">Logs</span>
        </button>
      </div>

      <!-- Condominiums Table -->
      <BaseCard title="Ultimos Condominios" subtitle="Condominios cadastrados recentemente">
        <template #header-actions>
          <button
            class="text-sm font-medium text-primary-600 hover:text-primary-700"
            @click="router.push('/admin/condominiums')"
          >
            Ver todos
          </button>
        </template>

        <BaseTable
          :columns="columns"
          :data="condominiums"
          :loading="loading"
          empty-message="Nenhum condominio cadastrado ainda."
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
