import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseModal from './BaseModal.vue'

describe('BaseModal', () => {
  it('não renderiza quando open=false', () => {
    const wrapper = mount(BaseModal, { props: { open: false }, slots: { default: 'Conteúdo' } })
    expect(wrapper.find('.fixed.inset-0').exists()).toBe(false)
  })

  it('renderiza quando open=true', () => {
    const wrapper = mount(BaseModal, { props: { open: true }, slots: { default: 'Conteúdo' } })
    expect(wrapper.text()).toContain('Conteúdo')
  })

  it('renderiza título', () => {
    const wrapper = mount(BaseModal, { props: { open: true, title: 'Modal' } })
    expect(wrapper.text()).toContain('Modal')
  })

  it('emite close ao clicar no overlay', async () => {
    const wrapper = mount(BaseModal, { props: { open: true } })
    await wrapper.find('.bg-black\\/50').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emite close ao clicar no botão X', async () => {
    const wrapper = mount(BaseModal, { props: { open: true, title: 'T' } })
    await wrapper.findAll('button')[0].trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})
