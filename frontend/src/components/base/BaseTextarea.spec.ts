import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseTextarea from './BaseTextarea.vue'

describe('BaseTextarea', () => {
  it('renderiza com label', () => {
    const wrapper = mount(BaseTextarea, { props: { label: 'Descrição' } })
    expect(wrapper.text()).toContain('Descrição')
  })

  it('emite update:modelValue ao digitar', async () => {
    const wrapper = mount(BaseTextarea)
    await wrapper.find('textarea').setValue('texto')
    expect(wrapper.emitted('update:modelValue')).toEqual([['texto']])
  })

  it('exibe erro', () => {
    const wrapper = mount(BaseTextarea, { props: { error: 'Obrigatório' } })
    expect(wrapper.text()).toContain('Obrigatório')
  })
})
