import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import BaseToast from './BaseToast.vue'

describe('BaseToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('renderiza mensagem', async () => {
    const wrapper = mount(BaseToast, { props: { message: 'Sucesso!', duration: 0 } })
    await nextTick()
    expect(wrapper.text()).toContain('Sucesso!')
  })

  it('aplica type success', async () => {
    const wrapper = mount(BaseToast, { props: { message: 'Ok', type: 'success', duration: 0 } })
    await nextTick()
    const toastDiv = wrapper.find('.bg-green-50')
    expect(toastDiv.exists()).toBe(true)
  })

  it('emite close ao clicar no botão', async () => {
    const wrapper = mount(BaseToast, { props: { message: 'Msg', duration: 0 } })
    await nextTick()
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('close')).toHaveLength(1)
  })
})
