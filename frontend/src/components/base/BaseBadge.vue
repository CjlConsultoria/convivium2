<script setup lang="ts">
interface Props {
  variant?: 'blue' | 'green' | 'yellow' | 'red' | 'gray'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'gray',
})

const classes: Record<string, string> = {
  blue: 'bg-blue-100 text-blue-800',
  green: 'bg-green-100 text-green-800',
  yellow: 'bg-yellow-100 text-yellow-800',
  red: 'bg-red-100 text-red-800',
  gray: 'bg-gray-100 text-gray-800',
}
</script>

<template>
  <span
    class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
    :class="classes[props.variant]"
  >
    <slot />
  </span>
</template>
