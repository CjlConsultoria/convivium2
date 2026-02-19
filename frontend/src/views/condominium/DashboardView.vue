<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useTenantStore } from '@/stores/tenant.store'
import { dashboardApi } from '@/api'
import { formatDate, timeAgo } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const tenantStore = useTenantStore()

const condoId = computed(() => Number(route.params.condoId))
const loading = ref(true)

const stats = ref({
  totalMoradores: 0,
  denunciasAbertas: 0,
  encomendasPendentes: 0,
  reservasHoje: 0,
})

const PAGE_SIZE = 10
const activityPage = ref(0)
const activityTotalPages = ref(0)
const activityTotalElements = ref(0)
const recentActivity = ref<
  Array<{
    type: string
    entityId: number
    title: string
    description: string
    date: string
    badgeLabel: string
  }>
>([])

onMounted(async () => {
  await loadDashboardData()
})

async function loadDashboardData() {
  loading.value = true
  try {
    const [statsRes, activityRes] = await Promise.all([
      dashboardApi.getDashboardStats(condoId.value),
      dashboardApi.getUnitActivity(condoId.value, 0, PAGE_SIZE),
    ])
    const data = statsRes.data
    stats.value = {
      totalMoradores: data?.totalMoradores ?? 0,
      denunciasAbertas: data?.denunciasAbertas ?? 0,
      encomendasPendentes: data?.encomendasPendentes ?? 0,
      reservasHoje: data?.reservasHoje ?? 0,
    }
    const act = activityRes.data
    if (act?.content) {
      recentActivity.value = act.content
      activityPage.value = act.page ?? 0
      activityTotalPages.value = act.totalPages ?? 0
      activityTotalElements.value = act.totalElements ?? 0
    } else {
      recentActivity.value = []
      activityTotalPages.value = 0
      activityTotalElements.value = 0
    }
  } catch {
    stats.value = {
      totalMoradores: 0,
      denunciasAbertas: 0,
      encomendasPendentes: 0,
      reservasHoje: 0,
    }
    recentActivity.value = []
    activityTotalPages.value = 0
    activityTotalElements.value = 0
  } finally {
    loading.value = false
  }
}

async function loadActivityPage(page: number) {
  try {
    const res = await dashboardApi.getUnitActivity(condoId.value, page, PAGE_SIZE)
    const act = res.data
    if (act?.content) {
      recentActivity.value = act.content
      activityPage.value = act.page ?? 0
      activityTotalPages.value = act.totalPages ?? 0
      activityTotalElements.value = act.totalElements ?? 0
    }
  } catch {
    recentActivity.value = []
  }
}

function activityLink(item: { type: string; entityId: number }) {
  if (item.type === 'COMPLAINT') return `/c/${condoId.value}/complaints/${item.entityId}`
  if (item.type === 'PARCEL') return `/c/${condoId.value}/parcels/${item.entityId}`
  return '#'
}

function badgeVariant(item: { type: string }): 'blue' | 'green' | 'yellow' | 'red' | 'gray' {
  if (item.type === 'COMPLAINT') return 'yellow'
  if (item.type === 'PARCEL') return 'green'
  return 'gray'
}

function navigateTo(path: string) {
  router.push(`/c/${condoId.value}${path}`)
}

const userName = computed(() => authStore.user?.name?.split(' ')[0] || 'Usuario')

const isMorador = computed(() => authStore.hasRole('MORADOR'))

const statCards = computed(() => {
  const cards = [
    {
      label: 'Total Moradores',
      value: stats.value.totalMoradores,
      icon: 'users',
      color: 'text-blue-600 bg-blue-100',
      to: '/users',
      showForMorador: false,
    },
    {
      label: 'Denuncias Abertas',
      value: stats.value.denunciasAbertas,
      icon: 'alert',
      color: 'text-yellow-600 bg-yellow-100',
      to: '/complaints',
      showForMorador: true,
    },
    {
      label: 'Encomendas Pendentes',
      value: stats.value.encomendasPendentes,
      icon: 'package',
      color: 'text-green-600 bg-green-100',
      to: '/parcels',
      showForMorador: true,
    },
    {
      label: 'Reservas Hoje',
      value: stats.value.reservasHoje,
      icon: 'calendar',
      color: 'text-purple-600 bg-purple-100',
      to: '/bookings',
      showForMorador: true,
    },
  ]
  if (isMorador.value) {
    return cards.filter((c) => c.showForMorador)
  }
  return cards.map(({ showForMorador, ...card }) => card)
})
</script>

<template>
  <div class="space-y-6">
    <!-- Welcome Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900 sm:text-3xl">
        Bem-vindo, {{ userName }}!
      </h1>
      <p class="mt-1 text-sm text-gray-500">
        Acompanhe as atividades do seu condominio
      </p>
    </div>

    <BaseLoadingSpinner v-if="loading" text="Carregando painel..." />

    <template v-else>
      <!-- Stat Cards (clicáveis - navegação em cascata: Dashboard → Lista → Detalhe) -->
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <button
          v-for="card in statCards"
          :key="card.label"
          type="button"
          class="rounded-xl border border-gray-200 bg-white p-4 text-left shadow-sm transition-shadow hover:border-primary-300 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 sm:p-6"
          @click="navigateTo(card.to)"
        >
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg sm:h-12 sm:w-12" :class="card.color">
              <!-- Users icon -->
              <svg v-if="card.icon === 'users'" class="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <!-- Alert icon -->
              <svg v-else-if="card.icon === 'alert'" class="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.34 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
              <!-- Package icon -->
              <svg v-else-if="card.icon === 'package'" class="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
              </svg>
              <!-- Calendar icon -->
              <svg v-else class="h-5 w-5 sm:h-6 sm:w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <div>
              <p class="text-xs text-gray-500 sm:text-sm">{{ card.label }}</p>
              <p class="text-xl font-bold text-gray-900 sm:text-2xl">{{ card.value }}</p>
            </div>
          </div>
        </button>
      </div>

      <!-- Quick Actions -->
      <div class="flex flex-wrap gap-3">
        <BaseButton variant="primary" @click="navigateTo('/complaints/new')">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          Nova Denuncia
        </BaseButton>
        <BaseButton v-if="!isMorador" variant="outline" @click="navigateTo('/announcements/new')">
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z" />
          </svg>
          Novo Comunicado
        </BaseButton>
      </div>

      <!-- Atividade Recente: últimos 30 dias da unidade (paginado) -->
      <BaseCard title="Atividade Recente (últimos 30 dias)">
        <div v-if="recentActivity.length > 0" class="divide-y divide-gray-100">
          <router-link
            v-for="(item, idx) in recentActivity"
            :key="`${item.type}-${item.entityId}-${idx}`"
            :to="activityLink(item)"
            class="flex items-start gap-3 py-3 first:pt-0 last:pb-0 text-left transition-colors hover:bg-gray-50"
          >
            <div class="mt-0.5 shrink-0">
              <BaseBadge :variant="badgeVariant(item)">{{ item.badgeLabel }}</BaseBadge>
            </div>
            <div class="min-w-0 flex-1">
              <p class="text-sm font-medium text-gray-900">{{ item.title }}</p>
              <p v-if="item.description" class="mt-0.5 text-xs text-gray-500 line-clamp-2">{{ item.description }}</p>
            </div>
            <span class="shrink-0 text-xs text-gray-400">{{ timeAgo(item.date) }}</span>
          </router-link>
        </div>
        <p v-else class="py-6 text-center text-sm text-gray-500">
          Nenhuma atividade nos últimos 30 dias.
        </p>
        <div v-if="activityTotalPages > 1" class="mt-4 flex items-center justify-between border-t border-gray-100 pt-4">
          <p class="text-xs text-gray-500">
            {{ activityTotalElements }} registro(s)
          </p>
          <div class="flex gap-2">
            <BaseButton
              variant="outline"
              size="sm"
              :disabled="activityPage <= 0"
              @click.prevent="loadActivityPage(activityPage - 1)"
            >
              Anterior
            </BaseButton>
            <span class="flex items-center px-2 text-sm text-gray-600">
              Página {{ activityPage + 1 }} de {{ activityTotalPages }}
            </span>
            <BaseButton
              variant="outline"
              size="sm"
              :disabled="activityPage >= activityTotalPages - 1"
              @click.prevent="loadActivityPage(activityPage + 1)"
            >
              Próxima
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>
  </div>
</template>
