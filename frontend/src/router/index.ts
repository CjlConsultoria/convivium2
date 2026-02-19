import { createRouter, createWebHistory } from 'vue-router'
import type { RouteLocationNormalized } from 'vue-router'
import authRoutes from '@/router/modules/auth.routes'
import adminRoutes from '@/router/modules/admin.routes'
import condominiumRoutes from '@/router/modules/condominium.routes'
import { useAuthStore } from '@/stores/auth.store'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      redirect: () => {
        const token = localStorage.getItem('access_token')
        if (!token) return '/login'
        return '/admin'
      },
    },
    ...authRoutes,
    ...adminRoutes,
    ...condominiumRoutes,
    {
      path: '/profile',
      name: 'Profile',
      component: () => import('@/views/profile/ProfileView.vue'),
      meta: { layout: 'default' },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { public: true, layout: 'minimal' },
    },
  ],
})

/**
 * Global navigation guard.
 *
 * - Public routes (meta.public === true) are always accessible.
 * - All other routes require an authenticated user.
 * - Routes with meta.requiresAdmin require a platform-admin user.
 * - Routes with meta.roles (string[]) require the user to hold at least
 *   one of the listed roles in the currently selected condominium.
 */
router.beforeEach(async (to: RouteLocationNormalized) => {
  const authStore = useAuthStore()

  // Allow public pages unconditionally
  if (to.meta.public) {
    return true
  }

  // If the user is not authenticated redirect to login, preserving the
  // intended destination so we can redirect back after login (unless invalid).
  if (!authStore.isAuthenticated) {
    const redirect = to.fullPath.includes('undefined') || to.fullPath === '/profile' ? undefined : to.fullPath
    return {
      path: '/login',
      query: redirect ? { redirect } : undefined,
    }
  }

  // Ensure the user object has been loaded (handles page refreshes where the
  // token exists in localStorage but the user has not yet been fetched).
  if (!authStore.user) {
    await authStore.initialize()

    // If initialise failed (token expired / invalid) redirect to login.
    if (!authStore.isAuthenticated) {
      const redirect = to.fullPath.includes('undefined') ? undefined : to.fullPath
      return {
        path: '/login',
        query: redirect ? { redirect } : undefined,
      }
    }
  }

  // Platform-admin gate: quem não é admin vai para o condomínio ou perfil
  if (to.meta.requiresAdmin && !authStore.isPlatformAdmin) {
    const firstActive = authStore.user?.condominiumRoles?.find((r) => r.status === 'ACTIVE')
    if (firstActive) return { path: `/c/${firstActive.condominiumId}` }
    return { path: '/profile' }
  }

  // Módulo desativado: redirecionar para o dashboard do condomínio
  if (to.meta.disabledModule && to.params.condoId) {
    return { path: `/c/${to.params.condoId}` }
  }

  // Role-based gate: rotas com meta.roles exigem pelo menos um dos papéis (ou platform admin)
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequired =
      authStore.isPlatformAdmin || requiredRoles.some((role) => authStore.hasRole(role))
    if (!hasRequired) {
      const firstActive = authStore.user?.condominiumRoles?.find((r) => r.status === 'ACTIVE')
      if (firstActive) return { path: `/c/${firstActive.condominiumId}` }
      return { path: '/profile' }
    }
  }

  // Evitar rotas inválidas como /c/undefined
  if (to.path.startsWith('/c/undefined') || to.params.condoId === 'undefined') {
    if (authStore.isPlatformAdmin) return { path: '/admin' }
    const firstActive = authStore.user?.condominiumRoles?.find((r) => r.status === 'ACTIVE')
    if (firstActive) return { path: `/c/${firstActive.condominiumId}` }
    return { path: '/profile' }
  }

  // Acesso a /c/:condoId exige vínculo ATIVO nesse condomínio (não pendente)
  const condoIdParam = to.params.condoId as string
  if (to.path.startsWith('/c/') && condoIdParam && condoIdParam !== 'undefined') {
    if (!authStore.isPlatformAdmin) {
      const condoIdNum = Number(condoIdParam)
      const hasActiveInCondo = authStore.user?.condominiumRoles?.some(
        (r) => r.condominiumId === condoIdNum && r.status === 'ACTIVE',
      )
      if (!hasActiveInCondo) {
        return { path: '/login', query: { message: 'pending' } }
      }
    }
  }

  return true
})

export default router
