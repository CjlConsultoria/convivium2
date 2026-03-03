import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseCard from './BaseCard.vue'

describe('BaseCard', () => {
  it('renderiza título e subtítulo', () => {
    const wrapper = mount(BaseCard, {
      props: { title: 'Título', subtitle: 'Subtítulo' },
      slots: { default: 'Conteúdo' },
    })
    expect(wrapper.text()).toContain('Título')
    expect(wrapper.text()).toContain('Subtítulo')
    expect(wrapper.text()).toContain('Conteúdo')
  })

  it('renderiza slot default', () => {
    const wrapper = mount(BaseCard, { slots: { default: 'Corpo' } })
    expect(wrapper.text()).toContain('Corpo')
  })

  it('renderiza slot header-actions', () => {
    const wrapper = mount(BaseCard, {
      props: { title: 'T' },
      slots: { 'header-actions': '<button>Ação</button>' },
    })
    expect(wrapper.html()).toContain('Ação')
  })
})
