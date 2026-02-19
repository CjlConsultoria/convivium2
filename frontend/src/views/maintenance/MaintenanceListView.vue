<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'

const route = useRoute()
const condoId = route.params.condoId

const columns = [
  { key: 'title', label: 'Titulo' },
  { key: 'category', label: 'Categoria' },
  { key: 'priority', label: 'Prioridade' },
  { key: 'status', label: 'Status' },
  { key: 'assignedTo', label: 'Responsavel' },
  { key: 'createdAt', label: 'Criado em' },
]

const requests = ref([
  { title: 'Vazamento no corredor 3o andar', category: 'Hidraulica', priority: 'HIGH', status: 'OPEN', assignedTo: '-', createdAt: '15/02/2026' },
  { title: 'Lampada queimada garagem', category: 'Eletrica', priority: 'LOW', status: 'IN_PROGRESS', assignedTo: 'Ze Carlos', createdAt: '14/02/2026' },
])

const activeTab = ref('all')

const statusVariant: Record<string, 'blue' | 'yellow' | 'green' | 'gray'> = {
  OPEN: 'blue',
  IN_PROGRESS: 'yellow',
  COMPLETED: 'green',
  CANCELLED: 'gray',
}

const priorityVariant: Record<string, 'gray' | 'blue' | 'yellow' | 'red'> = {
  LOW: 'gray',
  MEDIUM: 'blue',
  HIGH: 'yellow',
  URGENT: 'red',
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Manutencao</h1>
        <p class="text-sm text-gray-500">Solicitacoes de manutencao do condominio</p>
      </div>
      <router-link :to="`/c/${condoId}/maintenance/new`">
        <BaseButton>Nova Solicitacao</BaseButton>
      </router-link>
    </div>

    <div class="flex gap-1 rounded-lg bg-gray-100 p-1">
      <button
        v-for="tab in [{ key: 'all', label: 'Todas' }, { key: 'open', label: 'Abertas' }, { key: 'progress', label: 'Em Andamento' }, { key: 'done', label: 'Concluidas' }]"
        :key="tab.key"
        class="rounded-md px-4 py-2 text-sm font-medium transition-colors"
        :class="activeTab === tab.key ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-600 hover:text-gray-900'"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <BaseTable :columns="columns" :data="requests">
      <template #cell-priority="{ value }">
        <BaseBadge :variant="priorityVariant[value] || 'gray'">
          {{ value === 'LOW' ? 'Baixa' : value === 'MEDIUM' ? 'Media' : value === 'HIGH' ? 'Alta' : 'Urgente' }}
        </BaseBadge>
      </template>
      <template #cell-status="{ value }">
        <BaseBadge :variant="statusVariant[value] || 'gray'">
          {{ value === 'OPEN' ? 'Aberta' : value === 'IN_PROGRESS' ? 'Em Andamento' : 'Concluida' }}
        </BaseBadge>
      </template>
    </BaseTable>
  </div>
</template>
