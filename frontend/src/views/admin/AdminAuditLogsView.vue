<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'

// Audit logs will be populated once the backend audit logging module is implemented (Phase 2)
const logs = ref<Record<string, any>[]>([])
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Logs de Auditoria</h1>
      <p class="mt-1 text-sm text-gray-500">Historico de acoes realizadas na plataforma</p>
    </div>

    <BaseCard>
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-orange-100">
          <svg class="h-5 w-5 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <h3 class="text-sm font-medium text-gray-900">Modulo de Auditoria</h3>
          <p class="mt-1 text-sm text-gray-500">
            O sistema de logs de auditoria com AOP sera implementado na Fase 2.
            Aqui voce podera visualizar todas as acoes dos usuarios: logins, CRUD de dados,
            alteracoes de status, e muito mais.
          </p>
        </div>
      </div>
    </BaseCard>

    <!-- Preview of what audit logs will look like -->
    <BaseCard title="Preview - Formato dos Logs" subtitle="Exemplo de como os logs aparecerão">
      <div class="space-y-3">
        <div class="flex items-center gap-3 rounded-lg border border-gray-100 bg-gray-50 p-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100">
            <svg class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
          </div>
          <div class="flex-1">
            <p class="text-sm text-gray-900">Login realizado</p>
            <p class="text-xs text-gray-500">admin@convivium.com - IP: 192.168.1.1</p>
          </div>
          <span class="text-xs text-gray-400">Agora</span>
        </div>
        <div class="flex items-center gap-3 rounded-lg border border-gray-100 bg-gray-50 p-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-green-100">
            <svg class="h-4 w-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
          </div>
          <div class="flex-1">
            <p class="text-sm text-gray-900">Condominio criado: "Residencial Solar"</p>
            <p class="text-xs text-gray-500">admin@convivium.com</p>
          </div>
          <span class="text-xs text-gray-400">2 min atras</span>
        </div>
        <div class="flex items-center gap-3 rounded-lg border border-gray-100 bg-gray-50 p-3">
          <div class="flex h-8 w-8 items-center justify-center rounded-full bg-yellow-100">
            <svg class="h-4 w-4 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
          </div>
          <div class="flex-1">
            <p class="text-sm text-gray-900">Usuario aprovado: joao@email.com</p>
            <p class="text-xs text-gray-500">sindico@residencialsolar.com</p>
          </div>
          <span class="text-xs text-gray-400">5 min atras</span>
        </div>
      </div>
    </BaseCard>
  </div>
</template>
