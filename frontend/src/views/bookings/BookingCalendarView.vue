<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'

const route = useRoute()
const condoId = route.params.condoId

const areas = ref([
  { id: 1, name: 'Piscina', maxCapacity: 30, isActive: true },
  { id: 2, name: 'Churrasqueira', maxCapacity: 20, isActive: true },
  { id: 3, name: 'Salao de Festas', maxCapacity: 80, isActive: true },
  { id: 4, name: 'Academia', maxCapacity: 15, isActive: true },
])

const bookings = ref([
  { id: 1, area: 'Churrasqueira', user: 'Joao Silva', date: '22/02/2026', time: '14:00 - 22:00', status: 'CONFIRMED' },
  { id: 2, area: 'Salao de Festas', user: 'Maria Santos', date: '25/02/2026', time: '18:00 - 23:00', status: 'PENDING' },
])

const statusVariant: Record<string, 'green' | 'yellow' | 'gray'> = {
  CONFIRMED: 'green',
  PENDING: 'yellow',
  CANCELLED: 'gray',
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Reservas</h1>
        <p class="text-sm text-gray-500">Gerencie as reservas de areas comuns</p>
      </div>
      <router-link :to="`/c/${condoId}/bookings/new`">
        <BaseButton>Nova Reserva</BaseButton>
      </router-link>
    </div>

    <!-- Areas comuns -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <BaseCard v-for="area in areas" :key="area.id">
        <div class="text-center">
          <h3 class="font-semibold text-gray-900">{{ area.name }}</h3>
          <p class="mt-1 text-sm text-gray-500">Capacidade: {{ area.maxCapacity }} pessoas</p>
          <BaseBadge :variant="area.isActive ? 'green' : 'red'" class="mt-2">
            {{ area.isActive ? 'Disponivel' : 'Indisponivel' }}
          </BaseBadge>
        </div>
      </BaseCard>
    </div>

    <!-- Proximas reservas -->
    <BaseCard title="Proximas Reservas">
      <div class="divide-y">
        <div v-for="booking in bookings" :key="booking.id" class="flex items-center justify-between py-3">
          <div>
            <p class="font-medium text-gray-900">{{ booking.area }}</p>
            <p class="text-sm text-gray-500">{{ booking.user }} - {{ booking.date }} {{ booking.time }}</p>
          </div>
          <BaseBadge :variant="statusVariant[booking.status] || 'gray'">
            {{ booking.status === 'CONFIRMED' ? 'Confirmada' : 'Pendente' }}
          </BaseBadge>
        </div>
      </div>
    </BaseCard>
  </div>
</template>
