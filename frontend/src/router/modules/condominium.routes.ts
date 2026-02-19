import type { RouteRecordRaw } from 'vue-router'

// Rotas com roles: só funcionários (Síndico, Sub-síndico, Porteiro) podem acessar.
const STAFF_ROLES = ['SINDICO', 'SUB_SINDICO', 'PORTEIRO']

const condominiumRoutes: RouteRecordRaw[] = [
  {
    path: '/c/:condoId',
    name: 'Dashboard',
    component: () => import('@/views/condominium/DashboardView.vue'),
    meta: { layout: 'default' },
  },
  // ---- Blocos e Unidades (síndico / sub / porteiro) --------------------------
  {
    path: '/c/:condoId/buildings',
    name: 'BuildingList',
    component: () => import('@/views/condominium/BuildingsView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  // ---- Users (só funcionários) ----------------------------------------------
  {
    path: '/c/:condoId/users',
    name: 'UserList',
    component: () => import('@/views/condominium/users/UserListView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/users/new',
    name: 'UserCreate',
    component: () => import('@/views/condominium/users/UserCreateView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/users/:userId',
    name: 'UserDetail',
    component: () => import('@/views/condominium/users/UserDetailView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
    props: true,
  },
  // ---- Complaints -----------------------------------------------------------
  {
    path: '/c/:condoId/complaints',
    name: 'ComplaintList',
    component: () => import('@/views/condominium/complaints/ComplaintListView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/complaints/new',
    name: 'ComplaintCreate',
    component: () => import('@/views/condominium/complaints/ComplaintCreateView.vue'),
    meta: { layout: 'default' },
  },
  {
    path: '/c/:condoId/complaints/my',
    name: 'MyComplaints',
    component: () => import('@/views/condominium/complaints/ComplaintListView.vue'),
    meta: { layout: 'default', mineOnly: true },
  },
  {
    path: '/c/:condoId/complaints/:complaintId',
    name: 'ComplaintDetail',
    component: () => import('@/views/condominium/complaints/ComplaintDetailView.vue'),
    meta: { layout: 'default' },
    props: true,
  },
  // ---- Parcels --------------------------------------------------------------
  {
    path: '/c/:condoId/parcels',
    name: 'ParcelList',
    component: () => import('@/views/condominium/parcels/ParcelListView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/parcels/my',
    name: 'MyParcels',
    component: () => import('@/views/condominium/parcels/ParcelListView.vue'),
    meta: { layout: 'default', mineOnly: true },
  },
  {
    path: '/c/:condoId/parcels/receive',
    name: 'ParcelReceive',
    component: () => import('@/views/condominium/parcels/ParcelReceiveView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/parcels/:parcelId',
    name: 'ParcelDetail',
    component: () => import('@/views/condominium/parcels/ParcelDetailView.vue'),
    meta: { layout: 'default' },
    props: true,
  },
  // ---- Announcements --------------------------------------------------------
  {
    path: '/c/:condoId/announcements',
    name: 'AnnouncementList',
    component: () => import('@/views/condominium/announcements/AnnouncementListView.vue'),
    meta: { layout: 'default' },
  },
  {
    path: '/c/:condoId/announcements/new',
    name: 'AnnouncementCreate',
    component: () => import('@/views/condominium/announcements/AnnouncementCreateView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  {
    path: '/c/:condoId/announcements/:announcementId',
    name: 'AnnouncementDetail',
    component: () => import('@/views/condominium/announcements/AnnouncementDetailView.vue'),
    meta: { layout: 'default' },
    props: true,
  },
  // ---- Bookings (módulo desativado por enquanto para todos) -----------------
  {
    path: '/c/:condoId/bookings',
    name: 'BookingCalendar',
    component: () => import('@/views/bookings/BookingCalendarView.vue'),
    meta: { layout: 'default', disabledModule: true },
  },
  // ---- Visitors -------------------------------------------------------------
  {
    path: '/c/:condoId/visitors',
    name: 'VisitorList',
    component: () => import('@/views/visitors/VisitorListView.vue'),
    meta: { layout: 'default' },
  },
  // ---- Maintenance ----------------------------------------------------------
  {
    path: '/c/:condoId/maintenance',
    name: 'MaintenanceList',
    component: () => import('@/views/maintenance/MaintenanceListView.vue'),
    meta: { layout: 'default' },
  },
  // ---- Financial (apenas síndico – mensalidade do sistema) ------------------
  {
    path: '/c/:condoId/financial',
    name: 'FinancialDashboard',
    component: () => import('@/views/financial/FinancialDashboardView.vue'),
    meta: { layout: 'default', roles: ['SINDICO'] },
  },
  // ---- Documents (só funcionários) ------------------------------------------
  {
    path: '/c/:condoId/documents',
    name: 'DocumentList',
    component: () => import('@/views/documents/DocumentListView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
  // ---- Settings (só funcionários) -------------------------------------------
  {
    path: '/c/:condoId/settings',
    name: 'CondoSettings',
    component: () => import('@/views/settings/CondoSettingsView.vue'),
    meta: { layout: 'default', roles: STAFF_ROLES },
  },
]

export default condominiumRoutes
