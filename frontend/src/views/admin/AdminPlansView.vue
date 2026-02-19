<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'

const plans = ref([
  {
    id: 1,
    name: 'Basico',
    price: 9990,
    maxUnits: 50,
    features: ['Gestao de moradores', 'Denuncias', 'Encomendas', 'Comunicados'],
    active: true,
  },
  {
    id: 2,
    name: 'Profissional',
    price: 29990,
    maxUnits: 200,
    features: ['Tudo do Basico', 'Reservas de areas comuns', 'Visitantes com QR Code', 'Manutencao', 'Relatorios'],
    active: true,
  },
  {
    id: 3,
    name: 'Empresarial',
    price: 59990,
    maxUnits: 999,
    features: ['Tudo do Profissional', 'API Personalizada', 'Suporte prioritario', 'Multi-torres', 'Dashboard avancado'],
    active: true,
  },
])

function formatPrice(cents: number): string {
  return `R$ ${(cents / 100).toFixed(2).replace('.', ',')}`
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Planos</h1>
        <p class="mt-1 text-sm text-gray-500">Gerencie os planos de assinatura da plataforma</p>
      </div>
      <BaseButton variant="primary">
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Novo Plano
      </BaseButton>
    </div>

    <!-- Info -->
    <BaseCard>
      <div class="flex items-start gap-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-100">
          <svg class="h-5 w-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <h3 class="text-sm font-medium text-gray-900">Gestao de Planos</h3>
          <p class="mt-1 text-sm text-gray-500">
            Os planos abaixo sao configuracoes iniciais. A integracao com Stripe (Fase 5) permitira gerenciar planos, precos e checkout diretamente.
          </p>
        </div>
      </div>
    </BaseCard>

    <!-- Plans grid -->
    <div class="grid grid-cols-1 gap-6 md:grid-cols-3">
      <div
        v-for="plan in plans"
        :key="plan.id"
        class="rounded-xl border border-gray-200 bg-white shadow-sm"
      >
        <div class="border-b border-gray-200 p-6">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900">{{ plan.name }}</h3>
            <BaseBadge :variant="plan.active ? 'green' : 'gray'">
              {{ plan.active ? 'Ativo' : 'Inativo' }}
            </BaseBadge>
          </div>
          <div class="mt-4">
            <span class="text-3xl font-bold text-gray-900">{{ formatPrice(plan.price) }}</span>
            <span class="text-sm text-gray-500">/mes</span>
          </div>
          <p class="mt-1 text-sm text-gray-500">Ate {{ plan.maxUnits }} unidades</p>
        </div>
        <div class="p-6">
          <ul class="space-y-2">
            <li
              v-for="feature in plan.features"
              :key="feature"
              class="flex items-center gap-2 text-sm text-gray-600"
            >
              <svg class="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
              </svg>
              {{ feature }}
            </li>
          </ul>
          <div class="mt-4 flex gap-2">
            <BaseButton variant="outline" size="sm" class="flex-1">Editar</BaseButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
