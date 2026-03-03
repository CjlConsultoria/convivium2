import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseTable from './BaseTable.vue'

describe('BaseTable', () => {
  const columns = [
    { key: 'name', label: 'Nome' },
    { key: 'email', label: 'Email' },
  ]

  it('renderiza colunas e dados', () => {
    const data = [{ name: 'João', email: 'joao@test.com' }]
    const wrapper = mount(BaseTable, { props: { columns, data } })
    expect(wrapper.text()).toContain('Nome')
    expect(wrapper.text()).toContain('Email')
    expect(wrapper.text()).toContain('João')
    expect(wrapper.text()).toContain('joao@test.com')
  })

  it('exibe emptyMessage quando data vazia', () => {
    const wrapper = mount(BaseTable, {
      props: { columns, data: [], emptyMessage: 'Sem dados' },
    })
    expect(wrapper.text()).toContain('Sem dados')
  })

  it('emite row-click ao clicar na linha', async () => {
    const data = [{ name: 'A', email: 'a@b.com' }]
    const wrapper = mount(BaseTable, { props: { columns, data } })
    await wrapper.find('tbody tr').trigger('click')
    expect(wrapper.emitted('row-click')).toEqual([[data[0]]])
  })

  it('exibe loading quando loading=true', () => {
    const wrapper = mount(BaseTable, { props: { columns, data: [], loading: true } })
    expect(wrapper.find('svg.animate-spin').exists()).toBe(true)
  })
})
