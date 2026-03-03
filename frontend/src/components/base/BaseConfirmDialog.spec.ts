import { describe, it, expect, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseConfirmDialog from './BaseConfirmDialog.vue'

describe('BaseConfirmDialog', () => {
  afterEach(() => {
    document.body.querySelectorAll('.fixed.inset-0').forEach((el) => el.remove())
  })
  it('renderiza título e mensagem', () => {
    mount(BaseConfirmDialog, {
      props: { open: true, title: 'Confirmar', message: 'Deseja excluir?' },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Confirmar')
    expect(document.body.textContent).toContain('Deseja excluir?')
  })

  it('emite confirm ao clicar em Confirmar', async () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M' },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    buttons[buttons.length - 1]?.click()
    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })

  it('emite cancel ao clicar em Cancelar', async () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M' },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    buttons[0]?.click()
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('aplica variant danger', () => {
    mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M', variant: 'danger' },
      attachTo: document.body,
    })
    expect(document.body.innerHTML).toContain('danger')
  })
})
