<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { userApi } from '@/api'
import { useAuthStore } from '@/stores/auth.store'
import { ROLE_LABELS } from '@/types/user.types'
import { formatDate } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const condoId = computed(() => Number(route.params.condoId))

const canApproveUsers = computed(() => authStore.isPlatformAdmin || authStore.hasRole('SINDICO'))

const loading = ref(false)
const activeTab = ref<'ALL' | 'MORADOR' | 'FUNCIONARIO' | 'PENDING'>('ALL')
const users = ref<Record<string, any>[]>([])
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const pageSize = ref(15)

const allTabs = [
  { key: 'ALL', label: 'Todos' },
  { key: 'MORADOR', label: 'Moradores' },
  { key: 'FUNCIONARIO', label: 'Funcionarios' },
  { key: 'PENDING', label: 'Pendentes' },
] as const
const tabs = computed(() =>
  canApproveUsers.value ? allTabs : allTabs.filter((t) => t.key !== 'PENDING'),
)

const columns = [
  { key: 'name', label: 'Nome' },
  { key: 'email', label: 'Email', class: 'hidden sm:table-cell' },
  { key: 'role', label: 'Cargo', class: 'hidden md:table-cell' },
  { key: 'unit', label: 'Unidade', class: 'hidden md:table-cell' },
  { key: 'status', label: 'Status' },
  { key: 'createdAt', label: 'Data Cadastro', class: 'hidden lg:table-cell' },
  { key: 'actions', label: 'Acoes', class: 'text-right' },
]

onMounted(async () => {
  await loadUsers()
})

watch(activeTab, () => {
  page.value = 0
  loadUsers()
})

watch(
  () => tabs.value.some((t) => t.key === activeTab.value),
  (visible) => {
    if (!visible) activeTab.value = 'ALL'
  },
  { immediate: true },
)

function mapUserToRow(u: Record<string, any>) {
  const role = u.role as string
  const status = u.status as string
  return {
    id: u.id,
    name: u.name,
    email: u.email,
    role: ROLE_LABELS[role as keyof typeof ROLE_LABELS] || '-',
    unit: u.unitIdentifier ?? '-',
    status: status || (u.isActive ? 'ACTIVE' : 'PENDING_APPROVAL'),
    createdAt: formatDate(u.createdAt),
    rawStatus: status || (u.isActive ? 'ACTIVE' : 'PENDING_APPROVAL'),
  }
}

async function loadUsers() {
  loading.value = true
  listError.value = ''
  try {
    if (activeTab.value === 'PENDING') {
      const response = await userApi.getPendingApprovals(condoId.value)
      const list = response.data ?? []
      users.value = list.map((u: Record<string, any>) => mapUserToRow(u))
      totalPages.value = 1
      totalElements.value = list.length
    } else {
      const response = await userApi.listUsers(condoId.value, {
        page: page.value,
        size: pageSize.value,
      })
      const data = response.data
      users.value = (data.content ?? []).map((u: Record<string, any>) => mapUserToRow(u))
      totalPages.value = data.totalPages ?? 1
      totalElements.value = data.totalElements ?? 0
    }
    listError.value = ''
  } catch (err: any) {
    users.value = []
    totalPages.value = 1
    totalElements.value = 0
    listError.value = err.response?.data?.message || 'Erro ao carregar lista de usuarios.'
  } finally {
    loading.value = false
  }
}

function filteredUsers() {
  if (activeTab.value === 'PENDING') return users.value
  if (activeTab.value === 'ALL') return users.value
  if (activeTab.value === 'MORADOR') return users.value.filter((u) => u.role === 'Morador')
  if (activeTab.value === 'FUNCIONARIO') {
    return users.value.filter(
      (u) => u.role !== 'Morador' && u.role !== '-',
    )
  }
  return users.value
}

const deleting = ref(false)
const listError = ref('')

function onRowClick(row: Record<string, any>) {
  router.push(`/c/${condoId.value}/users/${row.id}`)
}

function goEdit(row: Record<string, any>) {
  router.push(`/c/${condoId.value}/users/${row.id}`)
}

async function askDelete(row: Record<string, any>) {
  const ok = window.confirm(
    `Excluir o usuario "${row.name}"? Esta acao remove o vinculo com o condominio e nao pode ser desfeita.`,
  )
  if (!ok) return
  deleting.value = true
  listError.value = ''
  try {
    await userApi.deleteUser(condoId.value, row.id)
    await loadUsers()
  } catch (err: any) {
    listError.value = err.response?.data?.message || 'Erro ao excluir usuario.'
  } finally {
    deleting.value = false
  }
}

function onPageChange(newPage: number) {
  page.value = newPage
  loadUsers()
}

function statusBadgeVariant(status: string): 'green' | 'yellow' | 'red' | 'gray' {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING':
    case 'PENDING_APPROVAL':
      return 'yellow'
    case 'REJECTED':
      return 'red'
    default:
      return 'gray'
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'Ativo'
    case 'PENDING':
    case 'PENDING_APPROVAL':
      return 'Pendente'
    case 'REJECTED':
      return 'Rejeitado'
    default:
      return status
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Moradores e Funcionarios</h1>
        <p class="mt-1 text-sm text-gray-500">Gerencie os usuarios do condominio</p>
      </div>
      <BaseButton variant="primary" @click="router.push(`/c/${condoId}/users/new`)">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Novo Cadastro
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

    <BaseAlert v-if="listError" type="error" dismissible @dismiss="listError = ''">
      {{ listError }}
    </BaseAlert>

    <!-- Table -->
    <BaseCard :padding="false">
      <BaseTable
        :columns="columns"
        :data="filteredUsers()"
        :loading="loading"
        empty-message="Nenhum usuario encontrado."
        @row-click="onRowClick"
      >
        <template #cell-status="{ row }">
          <BaseBadge :variant="statusBadgeVariant(row.rawStatus)">
            {{ statusLabel(row.rawStatus) }}
          </BaseBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex justify-end gap-2" @click.stop>
            <BaseButton variant="outline" size="sm" title="Editar" @click="goEdit(row)">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            </BaseButton>
            <BaseButton variant="danger" size="sm" title="Excluir" :disabled="deleting" @click="askDelete(row)">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            </BaseButton>
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
