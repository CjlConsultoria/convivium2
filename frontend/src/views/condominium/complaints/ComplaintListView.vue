<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import { complaintApi } from '@/api'
import type { ComplaintStatus } from '@/types/complaint.types'
import {
  COMPLAINT_STATUS_LABELS,
  COMPLAINT_CATEGORY_LABELS,
} from '@/types/complaint.types'
import { PRIORITY_LABELS } from '@/utils/constants'
import { formatDate } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))
const mineOnly = computed(() => !!route.meta.mineOnly)

const loading = ref(false)
const activeTab = ref<string>('ALL')
const complaints = ref<Record<string, any>[]>([])
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const pageSize = ref(15)

const tabs = [
  { key: 'ALL', label: 'Todas' },
  { key: 'OPEN', label: 'Abertas' },
  { key: 'IN_REVIEW', label: 'Em Análise' },
  { key: 'RESPONDED', label: 'Respondidas' },
  { key: 'RESOLVED', label: 'Resolvidas' },
  { key: 'CLOSED', label: 'Encerradas' },
] as const

const columns = [
  { key: 'title', label: 'Título' },
  { key: 'status', label: 'Status' },
  { key: 'category', label: 'Categoria', class: 'hidden sm:table-cell' },
  { key: 'priority', label: 'Prioridade', class: 'hidden md:table-cell' },
  { key: 'createdAt', label: 'Data', class: 'hidden lg:table-cell' },
]

const emptyMessageByTab = computed(() => {
  const key = activeTab.value
  if (key === 'ALL') return 'Nenhuma denúncia encontrada.'
  const messages: Record<string, string> = {
    OPEN: 'Nenhuma denúncia aberta.',
    IN_REVIEW: 'Nenhuma denúncia em análise.',
    RESPONDED: 'Nenhuma denúncia respondida.',
    RESOLVED: 'Nenhuma denúncia resolvida.',
    CLOSED: 'Nenhuma denúncia encerrada.',
  }
  return messages[key] ?? 'Nenhuma denúncia encontrada.'
})

onMounted(async () => {
  await loadComplaints()
})

watch(activeTab, () => {
  page.value = 0
  loadComplaints()
})

async function loadComplaints() {
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
      ? await complaintApi.getMyComplaints(condoId.value, params)
      : await complaintApi.listComplaints(condoId.value, params)
    const data = response.data

    complaints.value = data.content.map((c) => ({
      id: c.id,
      title: c.title,
      category: COMPLAINT_CATEGORY_LABELS[c.category] || c.category,
      status: COMPLAINT_STATUS_LABELS[c.status] || c.status,
      rawStatus: c.status,
      priority: PRIORITY_LABELS[c.priority] || c.priority,
      rawPriority: c.priority,
      createdAt: formatDate(c.createdAt),
    }))

    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } catch {
    complaints.value = []
    totalPages.value = 1
    totalElements.value = 0
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: string): 'blue' | 'yellow' | 'green' | 'gray' {
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

function onRowClick(row: Record<string, any>) {
  router.push(`/c/${condoId.value}/complaints/${row.id}`)
}

function onPageChange(newPage: number) {
  page.value = newPage
  loadComplaints()
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ mineOnly ? 'Minhas Denuncias' : 'Denuncias' }}</h1>
        <p class="mt-1 text-sm text-gray-500">{{ mineOnly ? 'Suas denuncias e reclamacoes' : 'Acompanhe e gerencie as denuncias do condominio' }}</p>
      </div>
      <BaseButton variant="primary" @click="router.push(`/c/${condoId}/complaints/new`)">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Nova Denuncia
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
        :data="complaints"
        :loading="loading"
        :empty-message="emptyMessageByTab"
        @row-click="onRowClick"
      >
        <template #cell-status="{ row }">
          <BaseBadge :variant="statusBadgeVariant(row.rawStatus)">
            {{ row.status }}
          </BaseBadge>
        </template>

        <template #cell-priority="{ row }">
          <BaseBadge :variant="priorityBadgeVariant(row.rawPriority)">
            {{ row.priority }}
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
