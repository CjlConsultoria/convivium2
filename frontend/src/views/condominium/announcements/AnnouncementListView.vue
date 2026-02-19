<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { timeAgo } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(false)
const activeCategory = ref('ALL')

const categories = [
  { key: 'ALL', label: 'Todos' },
  { key: 'GENERAL', label: 'Geral' },
  { key: 'MAINTENANCE', label: 'Manutencao' },
  { key: 'FINANCIAL', label: 'Financeiro' },
  { key: 'EVENT', label: 'Eventos' },
  { key: 'RULE', label: 'Regras' },
] as const

interface Announcement {
  id: number
  title: string
  content: string
  author: string
  category: string
  categoryLabel: string
  isPinned: boolean
  isEmergency: boolean
  createdAt: string
}

const announcements = ref<Announcement[]>([])

onMounted(async () => {
  await loadAnnouncements()
})

watch(activeCategory, () => {
  loadAnnouncements()
})

async function loadAnnouncements() {
  loading.value = true
  try {
    // Placeholder - replace with actual API call
    await new Promise((resolve) => setTimeout(resolve, 300))

    announcements.value = [
      {
        id: 1,
        title: 'Manutencao da piscina programada',
        content: 'Informamos que a piscina do condominio estara em manutencao nos dias 20 e 21 de fevereiro. Pedimos a compreensao de todos.',
        author: 'Carlos Administrador',
        category: 'MAINTENANCE',
        categoryLabel: 'Manutencao',
        isPinned: true,
        isEmergency: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
      },
      {
        id: 2,
        title: 'URGENTE: Falta de agua programada',
        content: 'A concessionaria de agua informou que havera interrupcao no fornecimento amanha das 8h as 14h. Sugerimos armazenar agua.',
        author: 'Carlos Administrador',
        category: 'GENERAL',
        categoryLabel: 'Geral',
        isPinned: false,
        isEmergency: true,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 12).toISOString(),
      },
      {
        id: 3,
        title: 'Assembleia geral ordinaria',
        content: 'Convocamos todos os moradores para a assembleia geral ordinaria que sera realizada no dia 28 de fevereiro, as 19h, no salao de festas.',
        author: 'Carlos Administrador',
        category: 'EVENT',
        categoryLabel: 'Eventos',
        isPinned: true,
        isEmergency: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
      },
      {
        id: 4,
        title: 'Boleto de condominio - Fevereiro',
        content: 'Os boletos de condominio referentes ao mes de fevereiro ja estao disponiveis. O vencimento e no dia 10.',
        author: 'Ana Financeiro',
        category: 'FINANCIAL',
        categoryLabel: 'Financeiro',
        isPinned: false,
        isEmergency: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
      },
      {
        id: 5,
        title: 'Novas regras para uso da churrasqueira',
        content: 'A administracao definiu novas regras para utilizacao da churrasqueira do condominio. O horario permitido passa a ser das 10h as 22h.',
        author: 'Carlos Administrador',
        category: 'RULE',
        categoryLabel: 'Regras',
        isPinned: false,
        isEmergency: false,
        createdAt: new Date(Date.now() - 1000 * 60 * 60 * 72).toISOString(),
      },
    ]
  } catch {
    announcements.value = []
  } finally {
    loading.value = false
  }
}

const filteredAnnouncements = computed(() => {
  if (activeCategory.value === 'ALL') return announcements.value
  return announcements.value.filter((a) => a.category === activeCategory.value)
})

function categoryBadgeVariant(category: string): 'blue' | 'yellow' | 'green' | 'red' | 'gray' {
  switch (category) {
    case 'GENERAL':
      return 'blue'
    case 'MAINTENANCE':
      return 'yellow'
    case 'FINANCIAL':
      return 'green'
    case 'EVENT':
      return 'blue'
    case 'RULE':
      return 'gray'
    default:
      return 'gray'
  }
}

function truncateContent(content: string, maxLength: number = 150): string {
  if (content.length <= maxLength) return content
  return content.slice(0, maxLength) + '...'
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Comunicados</h1>
        <p class="mt-1 text-sm text-gray-500">Comunicados e avisos do condominio</p>
      </div>
      <BaseButton variant="primary" @click="router.push(`/c/${condoId}/announcements/new`)">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Novo Comunicado
      </BaseButton>
    </div>

    <!-- Filter by category -->
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex gap-4 overflow-x-auto" aria-label="Tabs">
        <button
          v-for="cat in categories"
          :key="cat.key"
          class="whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors"
          :class="
            activeCategory === cat.key
              ? 'border-primary-500 text-primary-600'
              : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
          "
          @click="activeCategory = cat.key"
        >
          {{ cat.label }}
        </button>
      </nav>
    </div>

    <BaseLoadingSpinner v-if="loading" text="Carregando comunicados..." />

    <template v-else>
      <div v-if="filteredAnnouncements.length === 0" class="py-12 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z" />
        </svg>
        <p class="mt-2 text-sm text-gray-500">Nenhum comunicado encontrado.</p>
      </div>

      <!-- Cards layout -->
      <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div
          v-for="item in filteredAnnouncements"
          :key="item.id"
          class="cursor-pointer rounded-xl border bg-white p-5 shadow-sm transition-all hover:shadow-md"
          :class="item.isEmergency ? 'border-red-300 bg-red-50' : 'border-gray-200'"
          @click="router.push(`/c/${condoId}/announcements/${item.id}`)"
        >
          <!-- Indicators -->
          <div class="mb-2 flex flex-wrap items-center gap-2">
            <BaseBadge :variant="categoryBadgeVariant(item.category)">
              {{ item.categoryLabel }}
            </BaseBadge>
            <span
              v-if="item.isPinned"
              class="inline-flex items-center gap-1 text-xs font-medium text-primary-600"
            >
              <svg class="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 2a1 1 0 011 1v1.323l3.954 1.582 1.599-.8a1 1 0 01.894 1.79l-1.233.616 1.738 5.42a1 1 0 01-.285 1.05A3.989 3.989 0 0115 15a3.989 3.989 0 01-2.667-1.019 1 1 0 01-.285-1.05l1.715-5.349L11 6.477V16h2a1 1 0 110 2H7a1 1 0 110-2h2V6.477L6.237 7.582l1.715 5.349a1 1 0 01-.285 1.05A3.989 3.989 0 015 15a3.989 3.989 0 01-2.667-1.019 1 1 0 01-.285-1.05l1.738-5.42-1.233-.617a1 1 0 01.894-1.789l1.599.799L9 4.323V3a1 1 0 011-1z" />
              </svg>
              Fixado
            </span>
            <span
              v-if="item.isEmergency"
              class="inline-flex items-center gap-1 text-xs font-medium text-red-600"
            >
              <svg class="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
              Emergencia
            </span>
          </div>

          <!-- Title -->
          <h3 class="text-base font-semibold text-gray-900">{{ item.title }}</h3>

          <!-- Content preview -->
          <p class="mt-1 text-sm text-gray-600">{{ truncateContent(item.content) }}</p>

          <!-- Footer -->
          <div class="mt-3 flex items-center justify-between text-xs text-gray-400">
            <span>{{ item.author }}</span>
            <span>{{ timeAgo(item.createdAt) }}</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
