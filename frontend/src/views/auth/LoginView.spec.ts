import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import LoginView from './LoginView.vue'
import { setActivePinia, createPinia } from 'pinia'

vi.mock('@/stores/auth.store', () => ({
  useAuthStore: vi.fn(),
}))
vi.mock('@/stores/tenant.store', () => ({
  useTenantStore: vi.fn(),
}))
vi.mock('@/composables/useGoogleSignIn', () => ({
  useGoogleSignIn: () => ({ signIn: vi.fn(), isAvailable: false }),
}))

const router = createRouter({
  history: createMemoryHistory(),
  routes: [{ path: '/login', component: LoginView }],
})

describe('LoginView', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    const { useAuthStore } = await import('@/stores/auth.store')
    vi.mocked(useAuthStore).mockReturnValue({
      login: vi.fn(),
      loginWithGoogle: vi.fn(),
      isPlatformAdmin: false,
      user: null,
    } as any)
    const { useTenantStore } = await import('@/stores/tenant.store')
    vi.mocked(useTenantStore).mockReturnValue({
      setCondominium: vi.fn(),
    } as any)
    await router.push('/login')
  })

  it('renderiza formulário de login', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [router],
      },
    })
    expect(wrapper.text()).toContain('Entrar')
    expect(wrapper.find('input[type="email"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
  })

  it('chama login ao submeter formulário', async () => {
    const mockLogin = vi.fn().mockResolvedValue(undefined)
    const { useAuthStore } = await import('@/stores/auth.store')
    vi.mocked(useAuthStore).mockReturnValue({
      login: mockLogin,
      isPlatformAdmin: false,
      user: null,
    } as any)

    const wrapper = mount(LoginView, {
      global: { plugins: [router] },
    })
    await wrapper.find('input[type="email"]').setValue('a@b.com')
    await wrapper.find('input[type="password"]').setValue('senha123')
    await wrapper.find('form').trigger('submit.prevent')

    await wrapper.vm.$nextTick()
    await wrapper.vm.$nextTick()
    expect(mockLogin).toHaveBeenCalledWith('a@b.com', 'senha123')
  })
})
