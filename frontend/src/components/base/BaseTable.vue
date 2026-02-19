<script setup lang="ts">
interface Column {
  key: string
  label: string
  sortable?: boolean
  class?: string
}

interface Props {
  columns: Column[]
  data: Record<string, any>[]
  loading?: boolean
  emptyMessage?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  emptyMessage: 'Nenhum registro encontrado.',
})

defineEmits<{
  'row-click': [row: Record<string, any>]
}>()
</script>

<template>
  <div class="overflow-x-auto rounded-lg border border-gray-200">
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th
            v-for="col in props.columns"
            :key="col.key"
            class="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500"
            :class="col.class"
          >
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-200 bg-white">
        <tr v-if="props.loading">
          <td :colspan="props.columns.length" class="px-4 py-8 text-center text-sm text-gray-500">
            <svg class="mx-auto h-6 w-6 animate-spin text-primary-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
          </td>
        </tr>
        <tr v-else-if="props.data.length === 0">
          <td :colspan="props.columns.length" class="px-4 py-8 text-center text-sm text-gray-500">
            {{ props.emptyMessage }}
          </td>
        </tr>
        <tr
          v-else
          v-for="(row, index) in props.data"
          :key="index"
          class="cursor-pointer transition-colors hover:bg-gray-50"
          @click="$emit('row-click', row)"
        >
          <td
            v-for="col in props.columns"
            :key="col.key"
            class="whitespace-nowrap px-4 py-3 text-sm text-gray-700"
            :class="col.class"
          >
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              {{ row[col.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
