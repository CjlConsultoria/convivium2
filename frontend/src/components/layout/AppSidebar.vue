<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

interface NavItem {
  label: string
  icon: string
  to: string
  permission?: string
}

interface NavGroup {
  title?: string
  items: NavItem[]
}

interface Props {
  collapsed: boolean
  condoId?: number
  isAdmin?: boolean
  isMorador?: boolean
  isSindico?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{ toggle: [] }>()
const route = useRoute()

// Cascata: grupos expansíveis (por índice). Inicialmente abertos os que contêm a rota ativa.
const expandedGroups = ref<Set<number>>(new Set())

function toggleGroup(gIdx: number) {
  const next = new Set(expandedGroups.value)
  if (next.has(gIdx)) next.delete(gIdx)
  else next.add(gIdx)
  expandedGroups.value = next
}

function isGroupExpanded(gIdx: number): boolean {
  return expandedGroups.value.has(gIdx)
}

function groupHasActiveItem(group: NavGroup): boolean {
  return group.items.some((item) => isActive(item.to))
}

const condoNav = computed<NavGroup[]>(() => {
  const id = props.condoId
  if (id === undefined || id === null || Number.isNaN(id)) {
    return [{ items: [{ label: 'Inicio', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6', to: '/' }] }]
  }
  const base = `/c/${id}`
  const gestaoItems = props.isMorador
    ? [
        { label: 'Minhas Denuncias', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z', to: `${base}/complaints/my` },
        { label: 'Minhas Encomendas', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4', to: `${base}/parcels/my` },
        { label: 'Comunicados', icon: 'M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z', to: `${base}/announcements` },
      ]
    : [
        { label: 'Moradores', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z', to: `${base}/users` },
        { label: 'Blocos e Unidades', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', to: `${base}/buildings` },
        { label: 'Denuncias', icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z', to: `${base}/complaints` },
        { label: 'Encomendas', icon: 'M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4', to: `${base}/parcels` },
        { label: 'Comunicados', icon: 'M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z', to: `${base}/announcements` },
      ]
  return [
    {
      items: [
        { label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6', to: base },
      ],
    },
    {
      title: 'Gestao',
      items: gestaoItems,
    },
    {
      title: 'Servicos',
      items: [
        // Reservas desativado por enquanto para todos os perfis
        { label: props.isMorador ? 'Meus Visitantes' : 'Visitantes', icon: 'M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z', to: `${base}/visitors` },
        { label: 'Manutencao', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z', to: `${base}/maintenance` },
      ],
    },
    // Financeiro e Documentos: apenas síndico (mensalidade do sistema)
    ...(props.isMorador || !props.isSindico
      ? []
      : [
          {
            title: 'Financeiro',
            items: [
              { label: 'Financeiro', icon: 'M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z', to: `${base}/financial` },
              { label: 'Documentos', icon: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z', to: `${base}/documents` },
            ],
          },
        ]),
  ]
})

const adminNav: NavGroup[] = [
  {
    items: [
      { label: 'Dashboard', icon: 'M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6', to: '/admin' },
    ],
  },
  {
    title: 'Gestao',
    items: [
      { label: 'Condominios', icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', to: '/admin/condominiums' },
      { label: 'Usuarios', icon: 'M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z', to: '/admin/users' },
    ],
  },
  {
    title: 'Financeiro',
    items: [
      { label: 'Assinaturas', icon: 'M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z', to: '/admin/subscriptions' },
      { label: 'Planos', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4', to: '/admin/plans' },
    ],
  },
  {
    title: 'Sistema',
    items: [
      { label: 'Logs de Auditoria', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01', to: '/admin/audit-logs' },
      { label: 'Configuracoes', icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z', to: '/admin/settings' },
    ],
  },
]

const navGroups = computed(() => props.isAdmin ? adminNav : condoNav.value)

function isActive(path: string): boolean {
  if (path === '/admin' && route.path === '/admin') return true
  return path !== '/admin' && route.path.startsWith(path)
}

// Cascata: ao mudar rota, garantir que o grupo com item ativo está expandido
watch(
  () => route.path,
  () => {
    const groups = navGroups.value
    const next = new Set(expandedGroups.value)
    groups.forEach((group, gIdx) => {
      if (group.title && groupHasActiveItem(group)) next.add(gIdx)
    })
    expandedGroups.value = next
  },
  { immediate: true },
)

// Inicialmente expandir todos os grupos com título (níveis em cascata visíveis)
watch(
  navGroups,
  (groups) => {
    const next = new Set(expandedGroups.value)
    groups.forEach((group, gIdx) => {
      if (group.title) next.add(gIdx)
    })
    expandedGroups.value = next
  },
  { immediate: true },
)
</script>

<template>
  <aside
    class="flex h-full flex-col border-r border-gray-200 bg-white transition-all duration-300"
    :class="props.collapsed ? 'w-16' : 'w-64'"
  >
    <!-- Logo -->
    <div class="flex h-16 items-center justify-between border-b px-4">
      <span v-if="!props.collapsed" class="text-xl font-bold text-primary-600">Convivium</span>
      <span v-else class="text-xl font-bold text-primary-600">C</span>
    </div>

    <!-- Navigation (níveis em cascata: grupos expansíveis) -->
    <nav class="flex-1 overflow-y-auto px-2 py-4">
      <div v-for="(group, gIdx) in navGroups" :key="gIdx" :class="gIdx > 0 ? 'mt-4' : ''">
        <!-- Grupo sem título (ex.: Dashboard): link direto -->
        <template v-if="!group.title">
          <router-link
            v-for="item in group.items"
            :key="item.to"
            :to="item.to"
            class="mb-0.5 flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors"
            :class="isActive(item.to) ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
            :title="props.collapsed ? item.label : undefined"
          >
            <svg class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
            </svg>
            <span v-if="!props.collapsed">{{ item.label }}</span>
          </router-link>
        </template>

        <!-- Grupo com título: cascata expansível -->
        <template v-else>
          <div v-if="props.collapsed" class="mx-3 mb-1 border-t border-gray-200" />
          <button
            v-else
            type="button"
            class="mb-1 flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-[10px] font-semibold uppercase tracking-wider text-gray-500 hover:bg-gray-50 hover:text-gray-700"
            @click="toggleGroup(gIdx)"
          >
            <span>{{ group.title }}</span>
            <svg
              class="h-4 w-4 shrink-0 transition-transform"
              :class="isGroupExpanded(gIdx) ? 'rotate-180' : ''"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
            </svg>
          </button>
          <div v-show="!props.collapsed && isGroupExpanded(gIdx)" class="ml-1 space-y-0.5 border-l border-gray-200 pl-2">
            <router-link
              v-for="item in group.items"
              :key="item.to"
              :to="item.to"
              class="mb-0.5 flex items-center gap-3 rounded-lg px-2 py-2 text-sm font-medium transition-colors"
              :class="isActive(item.to) ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
            >
              <svg class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
              </svg>
              <span>{{ item.label }}</span>
            </router-link>
          </div>
          <!-- Sidebar recolhida: itens do grupo ainda acessíveis por ícone -->
          <template v-if="props.collapsed">
            <router-link
              v-for="item in group.items"
              :key="item.to"
              :to="item.to"
              class="mb-0.5 flex items-center justify-center rounded-lg px-2 py-2 text-gray-600 hover:bg-gray-50 hover:text-gray-900"
              :title="item.label"
            >
              <svg class="h-5 w-5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" :d="item.icon" />
              </svg>
            </router-link>
          </template>
        </template>
      </div>
    </nav>

    <!-- Collapse toggle -->
    <div class="border-t p-2">
      <button
        class="flex w-full items-center justify-center rounded-lg p-2 text-gray-400 hover:bg-gray-50 hover:text-gray-600"
        @click="emit('toggle')"
      >
        <svg class="h-5 w-5 transition-transform" :class="props.collapsed ? 'rotate-180' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
        </svg>
      </button>
    </div>
  </aside>
</template>
