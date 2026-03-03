import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseButton from './BaseButton.vue'

describe('BaseButton', () => {
  it('renderiza com slot', () => {
    const wrapper = mount(BaseButton, {
      slots: { default: 'Clique' },
    })
    expect(wrapper.text()).toContain('Clique')
  })

  it('aplica variant primary por padrão', () => {
    const wrapper = mount(BaseButton)
    expect(wrapper.classes()).toContain('bg-primary-600')
  })

  it('aplica variant danger', () => {
    const wrapper = mount(BaseButton, { props: { variant: 'danger' } })
    expect(wrapper.classes()).toContain('bg-red-600')
  })

  it('desabilita quando loading', () => {
    const wrapper = mount(BaseButton, { props: { loading: true } })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('desabilita quando disabled', () => {
    const wrapper = mount(BaseButton, { props: { disabled: true } })
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('emite submit quando type=submit', () => {
    const wrapper = mount(BaseButton, { props: { type: 'submit' } })
    expect(wrapper.find('button').attributes('type')).toBe('submit')
  })
})
