<script setup lang="ts">
import BaseButton from '@/components/base/BaseButton.vue'
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50 px-4">
    <div class="text-center">
      <p class="text-6xl font-bold text-primary-600">404</p>
      <h1 class="mt-4 text-3xl font-bold text-gray-900">Pagina nao encontrada</h1>
      <p class="mt-2 text-gray-500">A pagina que voce esta procurando nao existe ou foi movida.</p>
      <div class="mt-8 flex justify-center gap-4">
        <router-link to="/">
          <BaseButton>Voltar ao inicio</BaseButton>
        </router-link>
      </div>
    </div>
  </div>
</template>
