<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  page: number
  totalPages: number
  totalElements: number
  size: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'page-change': [page: number]
}>()

const startItem = computed(() => props.page * props.size + 1)
const endItem = computed(() => Math.min((props.page + 1) * props.size, props.totalElements))
const hasPrev = computed(() => props.page > 0)
const hasNext = computed(() => props.page < props.totalPages - 1)
</script>

<template>
  <div class="flex items-center justify-between px-2 py-3">
    <p class="text-sm text-gray-600">
      Mostrando <span class="font-medium">{{ startItem }}</span> a
      <span class="font-medium">{{ endItem }}</span> de
      <span class="font-medium">{{ props.totalElements }}</span> resultados
    </p>
    <div class="flex gap-2">
      <button
        class="btn-outline btn-sm"
        :disabled="!hasPrev"
        @click="emit('page-change', props.page - 1)"
      >
        Anterior
      </button>
      <button
        class="btn-outline btn-sm"
        :disabled="!hasNext"
        @click="emit('page-change', props.page + 1)"
      >
        Proximo
      </button>
    </div>
  </div>
</template>
