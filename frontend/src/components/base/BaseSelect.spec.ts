import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseSelect from './BaseSelect.vue'

describe('BaseSelect', () => {
  it('renderiza options', () => {
    const wrapper = mount(BaseSelect, {
      props: {
        options: [
          { value: 'a', label: 'Opção A' },
          { value: 'b', label: 'Opção B' },
        ],
      },
    })
    expect(wrapper.text()).toContain('Opção A')
    expect(wrapper.text()).toContain('Opção B')
  })

  it('emite update:modelValue ao mudar', async () => {
    const wrapper = mount(BaseSelect, {
      props: {
        options: [{ value: 'a', label: 'A' }],
      },
    })
    await wrapper.find('select').setValue('a')
    expect(wrapper.emitted('update:modelValue')).toEqual([['a']])
  })

  it('exibe erro', () => {
    const wrapper = mount(BaseSelect, {
      props: { options: [], error: 'Campo obrigatório' },
    })
    expect(wrapper.text()).toContain('Campo obrigatório')
  })
})
