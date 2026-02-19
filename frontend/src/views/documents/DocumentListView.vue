<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'

const categories = [
  { key: 'all', label: 'Todos' },
  { key: 'ATA', label: 'Atas' },
  { key: 'REGULAMENTO', label: 'Regulamento' },
  { key: 'CONVENCAO', label: 'Convencao' },
  { key: 'BALANCETE', label: 'Balancetes' },
  { key: 'CONTRATO', label: 'Contratos' },
]

const activeCategory = ref('all')

const documents = ref([
  { id: 1, title: 'Ata da Assembleia - Janeiro 2026', category: 'ATA', version: 1, uploadedBy: 'Sindico Jose', updatedAt: '15/01/2026' },
  { id: 2, title: 'Regulamento Interno', category: 'REGULAMENTO', version: 3, uploadedBy: 'Sindico Jose', updatedAt: '10/12/2025' },
  { id: 3, title: 'Convencao do Condominio', category: 'CONVENCAO', version: 1, uploadedBy: 'Admin', updatedAt: '01/06/2024' },
  { id: 4, title: 'Balancete Dezembro 2025', category: 'BALANCETE', version: 1, uploadedBy: 'Sindico Jose', updatedAt: '05/01/2026' },
])

const categoryColors: Record<string, 'blue' | 'green' | 'yellow' | 'red' | 'gray'> = {
  ATA: 'blue',
  REGULAMENTO: 'green',
  CONVENCAO: 'yellow',
  BALANCETE: 'gray',
  CONTRATO: 'red',
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Documentos</h1>
        <p class="text-sm text-gray-500">Documentos do condominio</p>
      </div>
      <BaseButton>Upload Documento</BaseButton>
    </div>

    <div class="flex flex-wrap gap-2">
      <button
        v-for="cat in categories"
        :key="cat.key"
        class="rounded-full px-4 py-1.5 text-sm font-medium transition-colors"
        :class="activeCategory === cat.key ? 'bg-primary-100 text-primary-700' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
        @click="activeCategory = cat.key"
      >
        {{ cat.label }}
      </button>
    </div>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <BaseCard v-for="doc in documents" :key="doc.id" class="cursor-pointer transition-shadow hover:shadow-md">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <BaseBadge :variant="categoryColors[doc.category] || 'gray'" class="mb-2">
              {{ doc.category }}
            </BaseBadge>
            <h3 class="font-medium text-gray-900">{{ doc.title }}</h3>
            <p class="mt-1 text-sm text-gray-500">Versao {{ doc.version }} - {{ doc.uploadedBy }}</p>
            <p class="text-xs text-gray-400">Atualizado em {{ doc.updatedAt }}</p>
          </div>
          <svg class="h-8 w-8 shrink-0 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
      </BaseCard>
    </div>
  </div>
</template>
