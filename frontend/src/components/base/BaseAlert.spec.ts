import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseAlert from './BaseAlert.vue'

describe('BaseAlert', () => {
  it('renderiza slot', () => {
    const wrapper = mount(BaseAlert, { slots: { default: 'Mensagem' } })
    expect(wrapper.text()).toContain('Mensagem')
  })

  it('aplica type info por padrão', () => {
    const wrapper = mount(BaseAlert)
    expect(wrapper.classes()).toContain('bg-blue-50')
  })

  it('aplica type error', () => {
    const wrapper = mount(BaseAlert, { props: { type: 'error' } })
    expect(wrapper.classes()).toContain('bg-red-50')
  })

  it('emite dismiss ao clicar no botão quando dismissible', async () => {
    const wrapper = mount(BaseAlert, {
      props: { dismissible: true },
      slots: { default: 'Msg' },
    })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('dismiss')).toHaveLength(1)
  })

  it('não mostra botão quando não dismissible', () => {
    const wrapper = mount(BaseAlert, { props: { dismissible: false } })
    expect(wrapper.find('button').exists()).toBe(false)
  })
})
