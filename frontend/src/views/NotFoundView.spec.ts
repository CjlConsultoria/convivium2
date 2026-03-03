import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import NotFoundView from './NotFoundView.vue'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/:pathMatch(.*)*', component: NotFoundView }],
})

describe('NotFoundView', () => {
  it('renderiza mensagem 404', async () => {
    await router.push('/pagina-inexistente')
    const wrapper = mount(NotFoundView, {
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('404')
    expect(wrapper.text()).toContain('Pagina nao encontrada')
  })
})
