<script setup lang="ts">
interface Props {
  type?: 'success' | 'error' | 'warning' | 'info'
  dismissible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  dismissible: false,
})

const emit = defineEmits<{
  dismiss: []
}>()

const styles: Record<string, string> = {
  success: 'bg-green-50 border-green-200 text-green-800',
  error: 'bg-red-50 border-red-200 text-red-800',
  warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800',
}
</script>

<template>
  <div class="rounded-lg border p-4" :class="styles[props.type]">
    <div class="flex items-start justify-between">
      <div class="flex-1 text-sm">
        <slot />
      </div>
      <button
        v-if="props.dismissible"
        class="ml-3 shrink-0 opacity-60 hover:opacity-100"
        @click="emit('dismiss')"
      >
        <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>
  </div>
</template>
