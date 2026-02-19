<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'

interface Props {
  userName?: string
  condoId?: number
  condominiumName?: string
  unreadCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  unreadCount: 0,
})

const authStore = useAuthStore()

const settingsTo = computed(() => {
  if (props.condoId !== undefined && props.condoId !== null && !Number.isNaN(props.condoId)) {
    return `/c/${props.condoId}/settings`
  }
  return '/admin/settings'
})

const showSettingsLink = computed(
  () => authStore.isPlatformAdmin || authStore.hasPermission('settings.view'),
)

const emit = defineEmits<{
  'toggle-sidebar': []
  'toggle-notifications': []
  logout: []
}>()

const userMenuOpen = ref(false)
</script>

<template>
  <header class="flex h-16 items-center justify-between border-b border-gray-200 bg-white px-4 lg:px-6">
    <!-- Left: Mobile hamburger + breadcrumb -->
    <div class="flex items-center gap-3">
      <button
        class="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600 lg:hidden"
        @click="emit('toggle-sidebar')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <span v-if="props.condominiumName" class="text-sm font-medium text-gray-500">
        {{ props.condominiumName }}
      </span>
    </div>

    <!-- Right: Notifications + User menu -->
    <div class="flex items-center gap-2">
      <!-- Notifications -->
      <button
        class="relative rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
        @click="emit('toggle-notifications')"
      >
        <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        <span
          v-if="props.unreadCount > 0"
          class="absolute -right-0.5 -top-0.5 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white"
        >
          {{ props.unreadCount > 99 ? '99+' : props.unreadCount }}
        </span>
      </button>

      <!-- User menu -->
      <div class="relative">
        <button
          class="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-gray-600 hover:bg-gray-100"
          @click="userMenuOpen = !userMenuOpen"
        >
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 text-sm font-semibold text-primary-700">
            {{ props.userName?.charAt(0)?.toUpperCase() || 'U' }}
          </div>
          <span class="hidden md:inline">{{ props.userName }}</span>
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
          </svg>
        </button>

        <!-- Dropdown -->
        <Transition name="dropdown">
          <div
            v-if="userMenuOpen"
            class="absolute right-0 top-full z-50 mt-1 w-48 rounded-lg border bg-white py-1 shadow-lg"
            @click="userMenuOpen = false"
          >
            <router-link to="/profile" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">
              Meu Perfil
            </router-link>
            <router-link
              v-if="showSettingsLink"
              :to="settingsTo"
              class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
            >
              Configuracoes
            </router-link>
            <div class="my-1 border-t" />
            <button
              class="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50"
              @click="emit('logout')"
            >
              Sair
            </button>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<style scoped>
.dropdown-enter-active { transition: all 0.15s ease-out; }
.dropdown-leave-active { transition: all 0.1s ease-in; }
.dropdown-enter-from { opacity: 0; transform: translateY(-4px); }
.dropdown-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
