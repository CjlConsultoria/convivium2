import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseBadge from './BaseBadge.vue'

describe('BaseBadge', () => {
  it('renderiza slot', () => {
    const wrapper = mount(BaseBadge, { slots: { default: 'Badge' } })
    expect(wrapper.text()).toContain('Badge')
  })

  it('aplica variant gray por padrão', () => {
    const wrapper = mount(BaseBadge)
    expect(wrapper.classes()).toContain('bg-gray-100')
  })

  it('aplica variant red', () => {
    const wrapper = mount(BaseBadge, { props: { variant: 'red' } })
    expect(wrapper.classes()).toContain('bg-red-100')
  })

  it('aplica variant green', () => {
    const wrapper = mount(BaseBadge, { props: { variant: 'green' } })
    expect(wrapper.classes()).toContain('bg-green-100')
  })
})
