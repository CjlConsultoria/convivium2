<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopbar from '@/components/layout/AppTopbar.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useTenantStore } from '@/stores/tenant.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const tenantStore = useTenantStore()

const sidebarCollapsed = ref(false)
const mobileMenuOpen = ref(false)

// condoId: da rota (/c/:condoId), do store ou do primeiro condomínio do usuário (evita /c/undefined)
const condoId = computed(() => {
  const fromRoute = route.params.condoId
  if (fromRoute && fromRoute !== 'undefined') {
    const id = Number(fromRoute)
    if (!Number.isNaN(id)) return id
  }
  return tenantStore.currentCondominiumId ?? authStore.user?.condominiumRoles?.[0]?.condominiumId ?? undefined
})

// Ao entrar numa rota /c/:condoId, manter o tenant store em sync
watch(
  () => route.params.condoId,
  (id) => {
    const num = Number(id)
    if (id && !Number.isNaN(num)) tenantStore.setCondominium(num)
  },
  { immediate: true },
)

const isAdmin = computed(() => authStore.isPlatformAdmin)
const isSindico = computed(() => authStore.hasRole('SINDICO'))
const condominiumName = computed(() => tenantStore.currentCondominium?.name ?? '')
// Morador: so tem role MORADOR (nao ve lista de moradores, so minhas denuncias e minhas encomendas)
const isMorador = computed(() => {
  const roles = authStore.currentRoles.map((r) => r.role)
  return roles.length > 0 && roles.length === 1 && roles[0] === 'MORADOR'
})

function toggleSidebar() {
  if (window.innerWidth < 1024) {
    mobileMenuOpen.value = !mobileMenuOpen.value
  } else {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
}

function handleLogout() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-gray-50">
    <!-- Mobile overlay -->
    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="fixed inset-0 z-40 bg-black/50 lg:hidden"
        @click="mobileMenuOpen = false"
      />
    </Transition>

    <!-- Sidebar (desktop: inline, mobile: overlay) -->
    <div
      class="fixed inset-y-0 left-0 z-50 lg:relative lg:z-0"
      :class="mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'"
      style="transition: transform 0.3s ease;"
    >
      <AppSidebar
        :collapsed="sidebarCollapsed"
        :condo-id="condoId"
        :is-admin="isAdmin"
        :is-morador="isMorador"
        :is-sindico="isSindico"
        @toggle="toggleSidebar"
      />
    </div>

    <!-- Main content -->
    <div class="flex flex-1 flex-col overflow-hidden">
      <AppTopbar
        :user-name="authStore.user?.name ?? 'Usuario'"
        :condo-id="condoId"
        :condominium-name="condominiumName"
        :unread-count="0"
        @toggle-sidebar="toggleSidebar"
        @logout="handleLogout"
      />
      <main class="flex-1 overflow-y-auto p-4 lg:p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
