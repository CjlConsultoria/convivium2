<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  label?: string
  accept?: string
  multiple?: boolean
  maxSizeMb?: number
}

const props = withDefaults(defineProps<Props>(), {
  accept: 'image/*',
  multiple: false,
  maxSizeMb: 10,
})

const emit = defineEmits<{
  'files-selected': [files: File[]]
}>()

const fileInput = ref<HTMLInputElement>()
const isDragOver = ref(false)
const error = ref('')

function handleFiles(fileList: FileList | null) {
  if (!fileList) return
  error.value = ''
  const files = Array.from(fileList)
  const maxBytes = props.maxSizeMb * 1024 * 1024
  const oversized = files.find(f => f.size > maxBytes)
  if (oversized) {
    error.value = `Arquivo "${oversized.name}" excede o limite de ${props.maxSizeMb}MB`
    return
  }
  emit('files-selected', files)
}

function onDrop(e: DragEvent) {
  isDragOver.value = false
  handleFiles(e.dataTransfer?.files ?? null)
}
</script>

<template>
  <div>
    <label v-if="props.label" class="label">{{ props.label }}</label>
    <div
      class="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 transition-colors"
      :class="isDragOver ? 'border-primary-400 bg-primary-50' : 'border-gray-300 hover:border-gray-400'"
      @click="fileInput?.click()"
      @dragover.prevent="isDragOver = true"
      @dragleave="isDragOver = false"
      @drop.prevent="onDrop"
    >
      <svg class="mb-2 h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
      </svg>
      <p class="text-sm text-gray-600">Clique ou arraste arquivos aqui</p>
      <p class="mt-1 text-xs text-gray-400">Maximo {{ props.maxSizeMb }}MB</p>
    </div>
    <input
      ref="fileInput"
      type="file"
      class="hidden"
      :accept="props.accept"
      :multiple="props.multiple"
      @change="handleFiles(($event.target as HTMLInputElement).files)"
    />
    <p v-if="error" class="mt-1 text-xs text-red-600">{{ error }}</p>
  </div>
</template>
