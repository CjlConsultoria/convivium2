import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseToast from './BaseToast.vue'

describe('BaseToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('renderiza mensagem', () => {
    const wrapper = mount(BaseToast, { props: { message: 'Sucesso!', duration: 0 } })
    expect(wrapper.text()).toContain('Sucesso!')
  })

  it('aplica type success', () => {
    const wrapper = mount(BaseToast, { props: { message: 'Ok', type: 'success', duration: 0 } })
    expect(wrapper.classes()).toContain('bg-green-50')
  })

  it('emite close ao clicar no botão', async () => {
    const wrapper = mount(BaseToast, { props: { message: 'Msg', duration: 0 } })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})
