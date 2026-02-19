<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'
import AdminLayout from '@/layouts/AdminLayout.vue'
import MinimalLayout from '@/layouts/MinimalLayout.vue'

const route = useRoute()

const layoutComponents: Record<string, typeof DefaultLayout> = {
  default: DefaultLayout,
  auth: AuthLayout,
  admin: AdminLayout,
  minimal: MinimalLayout,
}

const layout = computed(() => {
  const name = (route.meta.layout as string) || 'default'
  return layoutComponents[name] || DefaultLayout
})
</script>

<template>
  <component :is="layout" />
</template>
