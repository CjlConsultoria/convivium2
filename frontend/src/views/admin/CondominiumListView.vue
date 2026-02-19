<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { condominiumApi } from '@/api'
import { formatDate, formatCNPJ } from '@/utils/formatters'

const router = useRouter()

const loading = ref(false)
const error = ref('')
const condominiums = ref<Record<string, any>[]>([])
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const pageSize = ref(15)

const columns = [
  { key: 'name', label: 'Nome' },
  { key: 'cnpj', label: 'CNPJ', class: 'hidden sm:table-cell' },
  { key: 'city', label: 'Cidade/UF', class: 'hidden md:table-cell' },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Criado em', class: 'hidden lg:table-cell' },
  { key: 'actions', label: '', class: 'w-10' },
]

onMounted(async () => {
  await loadCondominiums()
})

async function loadCondominiums() {
  loading.value = true
  error.value = ''
  try {
    const response = await condominiumApi.listCondominiums({
      page: page.value,
      size: pageSize.value,
    })
    const data = response.data

    condominiums.value = data.content.map((c) => ({
      id: c.id,
      name: c.name,
      cnpj: c.cnpj ? formatCNPJ(c.cnpj) : '-',
      city: c.addressCity && c.addressState ? `${c.addressCity}/${c.addressState}` : '-',
      status: c.status,
      createdAt: formatDate(c.createdAt),
    }))

    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar condominios.'
    condominiums.value = []
    totalPages.value = 1
    totalElements.value = 0
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

function onPageChange(newPage: number) {
  page.value = newPage
  loadCondominiums()
}

async function handleDelete(id: number, name: string) {
  if (!confirm(`Tem certeza que deseja excluir o condominio "${name}"? Esta acao nao pode ser desfeita.`)) return
  try {
    await condominiumApi.deleteCondominium(id)
    await loadCondominiums()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao excluir condominio.'
  }
}

async function handleStatusChange(id: number, newStatus: string) {
  try {
    await condominiumApi.updateCondominiumStatus(id, newStatus)
    await loadCondominiums()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao alterar status.'
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Condominios</h1>
        <p class="mt-1 text-sm text-gray-500">
          Gerencie todos os condominios da plataforma
          <span v-if="totalElements > 0" class="font-medium">({{ totalElements }} total)</span>
        </p>
      </div>
      <BaseButton variant="primary" @click="router.push('/admin/condominiums/new')">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Novo Condominio
      </BaseButton>
    </div>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <!-- Table -->
    <BaseCard :padding="false">
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

        <template #cell-actions="{ row }">
          <div class="flex items-center gap-1" @click.stop>
            <button
              class="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-blue-600"
              title="Editar"
              @click="router.push(`/admin/condominiums/${row.id}`)"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </button>
            <div class="relative group">
              <button
                class="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                title="Mais opcoes"
              >
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z" />
                </svg>
              </button>
              <div class="invisible group-hover:visible absolute right-0 z-10 mt-1 w-44 rounded-lg border border-gray-200 bg-white py-1 shadow-lg">
                <button
                  v-if="row.status !== 'ACTIVE'"
                  class="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                  @click="handleStatusChange(row.id, 'ACTIVE')"
                >
                  <svg class="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4" /></svg>
                  Ativar
                </button>
                <button
                  v-if="row.status !== 'SUSPENDED'"
                  class="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
                  @click="handleStatusChange(row.id, 'SUSPENDED')"
                >
                  <svg class="h-4 w-4 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 9v6m4-6v6m7-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                  Suspender
                </button>
                <button
                  class="flex w-full items-center gap-2 px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                  @click="handleDelete(row.id, row.name)"
                >
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                  Excluir
                </button>
              </div>
            </div>
          </div>
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
