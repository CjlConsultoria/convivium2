<script setup lang="ts">
interface Option {
  value: string | number
  label: string
}

interface Props {
  modelValue?: string | number
  label?: string
  options: Option[]
  placeholder?: string
  error?: string
  required?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  required: false,
  disabled: false,
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
    <select
      :value="props.modelValue"
      :required="props.required"
      :disabled="props.disabled"
      class="input-field"
      :class="{ 'input-error': props.error }"
      @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option v-if="props.placeholder" value="" disabled>{{ props.placeholder }}</option>
      <option v-for="opt in props.options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
    <p v-if="props.error" class="mt-1 text-xs text-red-600">{{ props.error }}</p>
  </div>
</template>
