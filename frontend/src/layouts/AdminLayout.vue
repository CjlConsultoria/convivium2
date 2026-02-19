<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppTopbar from '@/components/layout/AppTopbar.vue'

const router = useRouter()
const sidebarCollapsed = ref(false)
const mobileMenuOpen = ref(false)

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
    <Transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="fixed inset-0 z-40 bg-black/50 lg:hidden"
        @click="mobileMenuOpen = false"
      />
    </Transition>

    <div
      class="fixed inset-y-0 left-0 z-50 lg:relative lg:z-0"
      :class="mobileMenuOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'"
      style="transition: transform 0.3s ease;"
    >
      <AppSidebar
        :collapsed="sidebarCollapsed"
        :is-admin="true"
        @toggle="toggleSidebar"
      />
    </div>

    <div class="flex flex-1 flex-col overflow-hidden">
      <AppTopbar
        user-name="Admin Max"
        condominium-name="Painel Administrativo"
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
