import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseInput from './BaseInput.vue'

describe('BaseInput', () => {
  it('renderiza com label', () => {
    const wrapper = mount(BaseInput, { props: { label: 'Email' } })
    expect(wrapper.text()).toContain('Email')
  })

  it('emite update:modelValue ao digitar', async () => {
    const wrapper = mount(BaseInput, { props: { modelValue: '' } })
    await wrapper.find('input').setValue('teste')
    expect(wrapper.emitted('update:modelValue')).toEqual([['teste']])
  })

  it('exibe erro quando prop error está definida', () => {
    const wrapper = mount(BaseInput, { props: { error: 'Campo obrigatório' } })
    expect(wrapper.text()).toContain('Campo obrigatório')
  })

  it('exibe asterisco quando required', () => {
    const wrapper = mount(BaseInput, { props: { label: 'Nome', required: true } })
    expect(wrapper.html()).toContain('*')
  })
})
