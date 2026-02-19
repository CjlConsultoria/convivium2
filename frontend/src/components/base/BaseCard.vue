<script setup lang="ts">
interface Props {
  title?: string
  subtitle?: string
  padding?: boolean
}

withDefaults(defineProps<Props>(), {
  padding: true,
})
</script>

<template>
  <div class="card">
    <div v-if="title || $slots.header" class="mb-4 flex items-center justify-between">
      <div>
        <h3 v-if="title" class="text-lg font-semibold text-gray-900">{{ title }}</h3>
        <p v-if="subtitle" class="mt-0.5 text-sm text-gray-500">{{ subtitle }}</p>
      </div>
      <slot name="header-actions" />
    </div>
    <div :class="{ 'p-0': !padding }">
      <slot />
    </div>
  </div>
</template>
