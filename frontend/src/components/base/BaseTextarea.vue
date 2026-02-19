<script setup lang="ts">
interface Props {
  modelValue?: string
  label?: string
  placeholder?: string
  error?: string
  required?: boolean
  disabled?: boolean
  rows?: number
}

const props = withDefaults(defineProps<Props>(), {
  required: false,
  disabled: false,
  rows: 4,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <div>
    <label v-if="props.label" class="label">
      {{ props.label }}
      <span v-if="props.required" class="text-red-500">*</span>
    </label>
    <textarea
      :value="props.modelValue"
      :placeholder="props.placeholder"
      :required="props.required"
      :disabled="props.disabled"
      :rows="props.rows"
      class="input-field resize-none"
      :class="{ 'input-error': props.error }"
      @input="emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    />
    <p v-if="props.error" class="mt-1 text-xs text-red-600">{{ props.error }}</p>
  </div>
</template>
