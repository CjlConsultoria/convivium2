import { describe, it, expect, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseModal from './BaseModal.vue'

describe('BaseModal', () => {
  afterEach(() => {
    document.body.querySelectorAll('.fixed.inset-0').forEach((el) => el.remove())
  })
  it('não renderiza quando open=false', () => {
    mount(BaseModal, { props: { open: false }, slots: { default: 'Conteúdo' }, attachTo: document.body })
    expect(document.body.querySelector('.fixed.inset-0')).toBeNull()
  })

  it('renderiza quando open=true', () => {
    mount(BaseModal, {
      props: { open: true },
      slots: { default: 'Conteúdo' },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Conteúdo')
  })

  it('renderiza título', () => {
    mount(BaseModal, {
      props: { open: true, title: 'Modal' },
      slots: { default: 'X' },
      attachTo: document.body,
    })
    expect(document.body.textContent).toContain('Modal')
  })

  it('emite close ao clicar no overlay', async () => {
    const wrapper = mount(BaseModal, {
      props: { open: true },
      slots: { default: 'X' },
      attachTo: document.body,
    })
    const overlay = document.body.querySelector('.bg-black\\/50') as HTMLElement
    overlay?.click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })

  it('emite close ao clicar no botão X', async () => {
    const wrapper = mount(BaseModal, {
      props: { open: true, title: 'T' },
      slots: { default: 'X' },
      attachTo: document.body,
    })
    const buttons = document.body.querySelectorAll('button')
    buttons[0].click()
    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})
