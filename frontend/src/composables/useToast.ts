import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

export interface Toast {
  id: number
  message: string
  type: ToastType
  duration: number
}

const DEFAULT_DURATION = 5000

// Shared reactive state so toasts are globally visible across all components
// that call useToast(). The array and counter live at module scope.
const toasts = ref<Toast[]>([])
let nextId = 1

export function useToast() {
  function showToast(message: string, type: ToastType, duration: number = DEFAULT_DURATION): void {
    const id = nextId++
    const toast: Toast = { id, message, type, duration }
    toasts.value.push(toast)

    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }
  }

  function removeToast(id: number): void {
    const index = toasts.value.findIndex((t) => t.id === id)
    if (index !== -1) {
      toasts.value.splice(index, 1)
    }
  }

  function success(message: string, duration?: number): void {
    showToast(message, 'success', duration)
  }

  function error(message: string, duration?: number): void {
    showToast(message, 'error', duration)
  }

  function warning(message: string, duration?: number): void {
    showToast(message, 'warning', duration)
  }

  function info(message: string, duration?: number): void {
    showToast(message, 'info', duration)
  }

  return {
    toasts,
    showToast,
    removeToast,
    success,
    error,
    warning,
    info,
  }
}
