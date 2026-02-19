<script setup lang="ts">
</script>

<template>
  <div class="flex min-h-screen">
    <!-- Left: Form area -->
    <div class="flex flex-1 flex-col justify-center px-4 py-12 sm:px-6 lg:px-8">
      <div class="mx-auto w-full max-w-md">
        <!-- Logo -->
        <div class="mb-8 text-center">
          <h1 class="text-3xl font-bold text-primary-600">Convivium</h1>
          <p class="mt-1 text-sm text-gray-500">Sistema de Gestao de Condominios</p>
        </div>
        <router-view />
      </div>
    </div>

    <!-- Right: Decorative panel (hidden on mobile) -->
    <div class="hidden bg-primary-600 lg:flex lg:w-1/2 lg:flex-col lg:items-center lg:justify-center lg:p-12">
      <div class="max-w-md text-center">
        <svg class="mx-auto mb-8 h-24 w-24 text-white/80" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
        </svg>
        <h2 class="text-2xl font-bold text-white">Gestao completa do seu condominio</h2>
        <p class="mt-3 text-lg text-primary-100">
          Cadastros, denuncias, encomendas, reservas e muito mais em um so lugar.
        </p>
      </div>
    </div>
  </div>
</template>
