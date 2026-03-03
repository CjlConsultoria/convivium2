import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseEmptyState from './BaseEmptyState.vue'

describe('BaseEmptyState', () => {
  it('renderiza título e descrição', () => {
    const wrapper = mount(BaseEmptyState, {
      props: { title: 'Nenhum item', description: 'Adicione um item' },
    })
    expect(wrapper.text()).toContain('Nenhum item')
    expect(wrapper.text()).toContain('Adicione um item')
  })

  it('renderiza slot action', () => {
    const wrapper = mount(BaseEmptyState, {
      props: { title: 'T' },
      slots: { action: '<button>Adicionar</button>' },
    })
    expect(wrapper.html()).toContain('Adicionar')
  })
})
