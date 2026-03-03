import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseFileUpload from './BaseFileUpload.vue'

describe('BaseFileUpload', () => {
  it('renderiza label', () => {
    const wrapper = mount(BaseFileUpload, { props: { label: 'Foto' } })
    expect(wrapper.text()).toContain('Foto')
  })

  it('exibe maxSizeMb', () => {
    const wrapper = mount(BaseFileUpload, { props: { maxSizeMb: 5 } })
    expect(wrapper.text()).toContain('5')
  })

  it('emite files-selected ao selecionar arquivo', async () => {
    const wrapper = mount(BaseFileUpload)
    const input = wrapper.find('input[type="file"]')
    const file = new File(['x'], 'test.png', { type: 'image/png' })
    const dataTransfer = new DataTransfer()
    dataTransfer.items.add(file)

    Object.defineProperty(input.element, 'files', {
      value: dataTransfer.files,
      writable: false,
    })
    await input.trigger('change')

    expect(wrapper.emitted('files-selected')).toBeDefined()
    expect(wrapper.emitted('files-selected')![0][0]).toHaveLength(1)
  })
})
