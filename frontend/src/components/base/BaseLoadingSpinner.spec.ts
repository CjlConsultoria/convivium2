import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseLoadingSpinner from './BaseLoadingSpinner.vue'

describe('BaseLoadingSpinner', () => {
  it('renderiza sem texto', () => {
    const wrapper = mount(BaseLoadingSpinner)
    expect(wrapper.find('svg').exists()).toBe(true)
  })

  it('renderiza com texto', () => {
    const wrapper = mount(BaseLoadingSpinner, { props: { text: 'Carregando...' } })
    expect(wrapper.text()).toContain('Carregando...')
  })

  it('aplica size md por padrão', () => {
    const wrapper = mount(BaseLoadingSpinner)
    expect(wrapper.find('svg').classes()).toContain('h-8')
  })

  it('aplica size lg', () => {
    const wrapper = mount(BaseLoadingSpinner, { props: { size: 'lg' } })
    expect(wrapper.find('svg').classes()).toContain('h-12')
  })
})
