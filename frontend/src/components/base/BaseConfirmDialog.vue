<script setup lang="ts">
import BaseModal from './BaseModal.vue'
import BaseButton from './BaseButton.vue'

interface Props {
  open: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  variant?: 'danger' | 'primary'
}

const props = withDefaults(defineProps<Props>(), {
  confirmText: 'Confirmar',
  cancelText: 'Cancelar',
  variant: 'primary',
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <BaseModal :open="props.open" :title="props.title" size="sm" @close="emit('cancel')">
    <p class="text-sm text-gray-600">{{ props.message }}</p>
    <template #footer>
      <div class="flex justify-end gap-3">
        <BaseButton variant="secondary" @click="emit('cancel')">{{ props.cancelText }}</BaseButton>
        <BaseButton :variant="props.variant === 'danger' ? 'danger' : 'primary'" @click="emit('confirm')">
          {{ props.confirmText }}
        </BaseButton>
      </div>
    </template>
  </BaseModal>
</template>
