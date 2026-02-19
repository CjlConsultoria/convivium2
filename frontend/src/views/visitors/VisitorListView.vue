<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const authStore = useAuthStore()
const condoId = route.params.condoId

// Morador: so ve os visitantes da sua unidade (quando houver API). Sem dados ficticios.
const isMorador = computed(() => {
  const roles = authStore.currentRoles.map((r) => r.role)
  return roles.length === 1 && roles[0] === 'MORADOR'
})

const columns = [
  { key: 'name', label: 'Nome' },
  { key: 'document', label: 'Documento' },
  { key: 'unit', label: 'Unidade' },
  { key: 'type', label: 'Tipo' },
  { key: 'entryTime', label: 'Entrada' },
  { key: 'status', label: 'Status' },
]

const visitors = ref<Record<string, any>[]>([])
const activeTab = ref('today')
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ isMorador ? 'Meus Visitantes' : 'Visitantes' }}</h1>
        <p class="text-sm text-gray-500">{{ isMorador ? 'Visitantes que voce registrou para a sua unidade' : 'Controle de acesso de visitantes' }}</p>
      </div>
      <div class="flex gap-2">
        <router-link :to="`/c/${condoId}/visitors/register`">
          <BaseButton>Registrar Visitante</BaseButton>
        </router-link>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 rounded-lg bg-gray-100 p-1">
      <button
        v-for="tab in [{ key: 'today', label: 'Hoje' }, { key: 'all', label: 'Todos' }, { key: 'recurring', label: 'Recorrentes' }]"
        :key="tab.key"
        class="rounded-md px-4 py-2 text-sm font-medium transition-colors"
        :class="activeTab === tab.key ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-600 hover:text-gray-900'"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <BaseTable
      :columns="columns"
      :data="visitors"
      empty-message="Nenhum visitante registrado."
    >
      <template #cell-status="{ value }">
        <BaseBadge :variant="value === 'NO_PREDIO' ? 'green' : 'gray'">
          {{ value === 'NO_PREDIO' ? 'No predio' : 'Saiu' }}
        </BaseBadge>
      </template>
    </BaseTable>
  </div>
</template>
