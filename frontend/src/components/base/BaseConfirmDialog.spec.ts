import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseConfirmDialog from './BaseConfirmDialog.vue'

describe('BaseConfirmDialog', () => {
  it('renderiza título e mensagem', () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'Confirmar', message: 'Deseja excluir?' },
    })
    expect(wrapper.text()).toContain('Confirmar')
    expect(wrapper.text()).toContain('Deseja excluir?')
  })

  it('emite confirm ao clicar em Confirmar', async () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M' },
    })
    const buttons = wrapper.findAll('button')
    await buttons[buttons.length - 1].trigger('click')
    expect(wrapper.emitted('confirm')).toHaveLength(1)
  })

  it('emite cancel ao clicar em Cancelar', async () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M' },
    })
    const buttons = wrapper.findAll('button')
    await buttons[0].trigger('click')
    expect(wrapper.emitted('cancel')).toHaveLength(1)
  })

  it('aplica variant danger', () => {
    const wrapper = mount(BaseConfirmDialog, {
      props: { open: true, title: 'T', message: 'M', variant: 'danger' },
    })
    expect(wrapper.html()).toContain('danger')
  })
})
