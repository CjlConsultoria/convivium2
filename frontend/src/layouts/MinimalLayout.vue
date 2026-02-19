<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="flex min-h-screen flex-col bg-gray-50">
    <header class="border-b border-gray-200 bg-white px-4 py-3">
      <div class="flex items-center justify-between">
        <router-link to="/" class="text-xl font-bold text-primary-600">Convivium</router-link>
        <button
          type="button"
          class="text-sm font-medium text-gray-600 hover:text-gray-900"
          @click="goHome"
        >
          Início
        </button>
      </div>
    </header>
    <main class="flex-1">
      <router-view />
    </main>
  </div>
</template>
