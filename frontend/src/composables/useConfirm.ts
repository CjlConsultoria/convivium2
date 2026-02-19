import { ref } from 'vue'

/**
 * Composable for programmatic confirmation dialogs.
 *
 * The calling code awaits the `confirm()` promise. A UI component (e.g.
 * ConfirmDialog.vue) reads the reactive refs and calls `resolve()` or
 * `reject()` depending on the user's action.
 *
 * Usage in a component:
 * ```ts
 * const { isOpen, title, message, confirm, resolve, reject } = useConfirm()
 *
 * async function handleDelete() {
 *   const ok = await confirm('Confirmar exclusao', 'Deseja realmente excluir?')
 *   if (ok) { ... }
 * }
 * ```
 */

// Module-level state so every component that calls useConfirm() shares the
// same dialog state.
const isOpen = ref(false)
const title = ref('')
const message = ref('')

let _resolve: ((value: boolean) => void) | null = null

export function useConfirm() {
  function confirm(dialogTitle: string, dialogMessage: string): Promise<boolean> {
    title.value = dialogTitle
    message.value = dialogMessage
    isOpen.value = true

    return new Promise<boolean>((res) => {
      _resolve = res
    })
  }

  function resolve(): void {
    isOpen.value = false
    if (_resolve) {
      _resolve(true)
      _resolve = null
    }
  }

  function reject(): void {
    isOpen.value = false
    if (_resolve) {
      _resolve(false)
      _resolve = null
    }
  }

  return {
    isOpen,
    title,
    message,
    confirm,
    resolve,
    reject,
  }
}
