import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/admin/AdminDashboardView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/condominiums',
    name: 'AdminCondominiumList',
    component: () => import('@/views/admin/CondominiumListView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/condominiums/new',
    name: 'AdminCondominiumCreate',
    component: () => import('@/views/admin/CondominiumCreateView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/condominiums/:id',
    name: 'AdminCondominiumDetail',
    component: () => import('@/views/admin/CondominiumDetailView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
    props: true,
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: () => import('@/views/admin/AdminUsersView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/subscriptions',
    name: 'AdminSubscriptions',
    component: () => import('@/views/admin/AdminSubscriptionsView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/plans',
    name: 'AdminPlans',
    component: () => import('@/views/admin/AdminPlansView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/audit-logs',
    name: 'AdminAuditLogs',
    component: () => import('@/views/admin/AdminAuditLogsView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
  {
    path: '/admin/settings',
    name: 'AdminSettings',
    component: () => import('@/views/admin/AdminSettingsView.vue'),
    meta: { requiresAdmin: true, layout: 'admin' },
  },
]

export default adminRoutes
