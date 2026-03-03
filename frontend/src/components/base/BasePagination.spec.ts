import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BasePagination from './BasePagination.vue'

describe('BasePagination', () => {
  it('exibe texto de resultados', () => {
    const wrapper = mount(BasePagination, {
      props: { page: 0, totalPages: 2, totalElements: 25, size: 10 },
    })
    expect(wrapper.text()).toContain('Mostrando')
    expect(wrapper.text()).toContain('1')
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('25')
  })

  it('emite page-change ao clicar Anterior', async () => {
    const wrapper = mount(BasePagination, {
      props: { page: 1, totalPages: 3, totalElements: 30, size: 10 },
    })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('page-change')).toEqual([[0]])
  })

  it('emite page-change ao clicar Próximo', async () => {
    const wrapper = mount(BasePagination, {
      props: { page: 0, totalPages: 3, totalElements: 30, size: 10 },
    })
    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')
    expect(wrapper.emitted('page-change')).toEqual([[1]])
  })

  it('desabilita Anterior na primeira página', () => {
    const wrapper = mount(BasePagination, {
      props: { page: 0, totalPages: 2, totalElements: 20, size: 10 },
    })
    expect(wrapper.findAll('button')[0].attributes('disabled')).toBeDefined()
  })
})
