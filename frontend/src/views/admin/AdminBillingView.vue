<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import * as billingApi from '@/api/modules/billing.api'
import type { PlatformInvoice } from '@/api/modules/payment.api'

const loading = ref(true)
const condos = ref<billingApi.CondoBilling[]>([])
const showInvoicesModal = ref(false)
const showBlockModal = ref(false)
const showDetailModal = ref(false)
const selectedCondo = ref<billingApi.CondoBilling | null>(null)
const condoInvoices = ref<PlatformInvoice[]>([])
const invoicesLoading = ref(false)
const blockType = ref<'PAYMENT' | 'GENERAL'>('PAYMENT')
const blockReason = ref('')
const actionLoading = ref(false)

const columns = [
  { key: 'name', label: 'Condominio' },
  { key: 'planName', label: 'Plano' },
  { key: 'priceFormatted', label: 'Valor/Mes' },
  { key: 'subscriptionDate', label: 'Assinatura desde' },
  { key: 'status', label: 'Status' },
  { key: 'blockType', label: 'Bloqueio' },
  { key: 'actions', label: 'Acoes' },
]

const invoiceColumns = [
  { key: 'referenceDisplay', label: 'Referencia' },
  { key: 'amountFormatted', label: 'Valor' },
  { key: 'createdDate', label: 'Emissao' },
  { key: 'paidDate', label: 'Pago em' },
  { key: 'status', label: 'Status' },
]

const statusVariant: Record<string, 'green' | 'yellow' | 'red' | 'gray'> = {
  ACTIVE: 'green',
  PENDING: 'yellow',
  SUSPENDED: 'red',
  DEACTIVATED: 'gray',
}

const statusLabel: Record<string, string> = {
  ACTIVE: 'Ativo',
  PENDING: 'Pendente',
  SUSPENDED: 'Suspenso',
  DEACTIVATED: 'Desativado',
}

const blockVariant: Record<string, 'red' | 'yellow' | 'gray'> = {
  PAYMENT: 'yellow',
  GENERAL: 'red',
}

const blockLabel: Record<string, string> = {
  PAYMENT: 'Inadimplencia',
  GENERAL: 'Bloqueio Total',
}

const invoiceStatusVariant: Record<string, 'green' | 'yellow' | 'red' | 'gray'> = {
  PAID: 'green',
  PENDING: 'yellow',
  OVERDUE: 'red',
  CANCELLED: 'gray',
}

const invoiceStatusLabel: Record<string, string> = {
  PAID: 'Pago',
  PENDING: 'Pendente',
  OVERDUE: 'Atrasado',
  CANCELLED: 'Cancelado',
}

function formatCurrency(cents: number | null): string {
  if (cents == null) return '—'
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
}

function formatDate(dateStr: string | null): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('pt-BR')
}

function formatDateTime(dateStr: string | null): string {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const tableData = ref<Array<Record<string, unknown>>>([])

function buildTableData() {
  tableData.value = condos.value.map((c) => ({
    id: c.id,
    name: c.name,
    planName: c.planName ?? 'Sem plano',
    priceFormatted: formatCurrency(c.planPriceCents),
    subscriptionDate: formatDate(c.subscriptionStartedAt),
    status: c.status,
    blockType: c.blockType ?? null,
    _raw: c,
  }))
}

// Totais para cards
const totalCondos = ref(0)
const totalActive = ref(0)
const totalBlocked = ref(0)
const totalRevenue = ref(0)

function computeStats() {
  totalCondos.value = condos.value.length
  totalActive.value = condos.value.filter(c => c.status === 'ACTIVE').length
  totalBlocked.value = condos.value.filter(c => c.blockType != null).length
  totalRevenue.value = condos.value
    .filter(c => c.status === 'ACTIVE' && c.planPriceCents)
    .reduce((sum, c) => sum + (c.planPriceCents ?? 0), 0)
}

async function loadCondos() {
  loading.value = true
  try {
    const res = await billingApi.listCondominiums()
    if (res.success && res.data) {
      condos.value = res.data
      buildTableData()
      computeStats()
    }
  } finally {
    loading.value = false
  }
}

async function openInvoices(condo: billingApi.CondoBilling) {
  selectedCondo.value = condo
  showInvoicesModal.value = true
  invoicesLoading.value = true
  try {
    const res = await billingApi.listCondoInvoices(condo.id)
    if (res.success && res.data) {
      condoInvoices.value = res.data
    }
  } finally {
    invoicesLoading.value = false
  }
}

function openDetail(condo: billingApi.CondoBilling) {
  selectedCondo.value = condo
  showDetailModal.value = true
}

function openBlockModal(condo: billingApi.CondoBilling) {
  selectedCondo.value = condo
  blockType.value = 'PAYMENT'
  blockReason.value = ''
  showBlockModal.value = true
}

async function confirmBlock() {
  if (!selectedCondo.value) return
  actionLoading.value = true
  try {
    await billingApi.blockCondominium(selectedCondo.value.id, blockType.value, blockReason.value)
    showBlockModal.value = false
    await loadCondos()
  } finally {
    actionLoading.value = false
  }
}

async function unblock(condo: billingApi.CondoBilling) {
  actionLoading.value = true
  try {
    await billingApi.unblockCondominium(condo.id)
    await loadCondos()
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadCondos)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Faturas & Bloqueios</h1>
      <p class="text-sm text-gray-500">Visualize faturas, valores, status de pagamento e gerencie bloqueios de condominios.</p>
    </div>

    <!-- Cards de resumo -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Total Condominios</p>
        <p class="mt-1 text-2xl font-bold text-gray-900">{{ totalCondos }}</p>
      </BaseCard>
      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Ativos</p>
        <p class="mt-1 text-2xl font-bold text-green-600">{{ totalActive }}</p>
      </BaseCard>
      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Bloqueados</p>
        <p class="mt-1 text-2xl font-bold" :class="totalBlocked > 0 ? 'text-red-600' : 'text-gray-400'">{{ totalBlocked }}</p>
      </BaseCard>
      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Receita Mensal Estimada</p>
        <p class="mt-1 text-2xl font-bold text-primary-600">{{ formatCurrency(totalRevenue) }}</p>
      </BaseCard>
    </div>

    <!-- Tabela -->
    <BaseCard>
      <BaseTable :columns="columns" :data="tableData" :loading="loading" empty-message="Nenhum condominio encontrado.">
        <template #cell-status="{ value }">
          <BaseBadge :variant="statusVariant[value as string] || 'gray'">
            {{ statusLabel[value as string] || value }}
          </BaseBadge>
        </template>
        <template #cell-blockType="{ value }">
          <BaseBadge v-if="value" :variant="blockVariant[value as string] || 'gray'">
            {{ blockLabel[value as string] || value }}
          </BaseBadge>
          <span v-else class="text-gray-400">—</span>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex gap-2">
            <button
              class="text-xs text-gray-600 hover:text-gray-900 hover:underline"
              @click="openDetail((row as Record<string, unknown>)._raw as billingApi.CondoBilling)"
            >
              Detalhes
            </button>
            <button
              class="text-xs text-primary-600 hover:underline"
              @click="openInvoices((row as Record<string, unknown>)._raw as billingApi.CondoBilling)"
            >
              Faturas
            </button>
            <button
              v-if="!((row as Record<string, unknown>)._raw as billingApi.CondoBilling).blockType"
              class="text-xs text-red-600 hover:underline"
              @click="openBlockModal((row as Record<string, unknown>)._raw as billingApi.CondoBilling)"
            >
              Bloquear
            </button>
            <button
              v-else
              class="text-xs text-green-600 hover:underline"
              @click="unblock((row as Record<string, unknown>)._raw as billingApi.CondoBilling)"
            >
              Desbloquear
            </button>
          </div>
        </template>
      </BaseTable>
    </BaseCard>

    <!-- Modal de detalhes do condominio -->
    <Teleport to="body">
      <div v-if="showDetailModal && selectedCondo" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-lg font-bold text-gray-900">{{ selectedCondo.name }}</h2>
            <button class="text-gray-400 hover:text-gray-600" @click="showDetailModal = false">✕</button>
          </div>
          <div class="space-y-3">
            <div class="grid grid-cols-2 gap-3">
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Plano</p>
                <p class="font-medium text-gray-900">{{ selectedCondo.planName ?? 'Sem plano' }}</p>
              </div>
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Valor Mensal</p>
                <p class="font-medium text-primary-600">{{ formatCurrency(selectedCondo.planPriceCents) }}</p>
              </div>
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Status</p>
                <BaseBadge :variant="statusVariant[selectedCondo.status] || 'gray'">
                  {{ statusLabel[selectedCondo.status] || selectedCondo.status }}
                </BaseBadge>
              </div>
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Bloqueio</p>
                <BaseBadge v-if="selectedCondo.blockType" :variant="blockVariant[selectedCondo.blockType] || 'gray'">
                  {{ blockLabel[selectedCondo.blockType] || selectedCondo.blockType }}
                </BaseBadge>
                <span v-else class="text-sm text-gray-400">Nenhum</span>
              </div>
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Assinatura desde</p>
                <p class="text-sm text-gray-700">{{ formatDate(selectedCondo.subscriptionStartedAt) }}</p>
              </div>
              <div>
                <p class="text-xs font-medium uppercase text-gray-500">Cadastrado em</p>
                <p class="text-sm text-gray-700">{{ formatDate(selectedCondo.createdAt) }}</p>
              </div>
            </div>

            <!-- Info de bloqueio -->
            <div v-if="selectedCondo.blockType" class="rounded-lg border border-red-200 bg-red-50 p-3">
              <p class="text-xs font-medium uppercase text-red-600">Informacoes do Bloqueio</p>
              <p class="mt-1 text-sm text-red-800"><span class="font-medium">Tipo:</span> {{ blockLabel[selectedCondo.blockType] }}</p>
              <p class="text-sm text-red-800"><span class="font-medium">Data:</span> {{ formatDateTime(selectedCondo.blockedAt) }}</p>
              <p class="text-sm text-red-800"><span class="font-medium">Motivo:</span> {{ selectedCondo.blockedReason ?? 'Nao informado' }}</p>
            </div>
          </div>

          <div class="mt-4 flex justify-end gap-3">
            <button
              class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
              @click="openInvoices(selectedCondo); showDetailModal = false"
            >
              Ver Faturas
            </button>
            <button
              v-if="selectedCondo.blockType"
              class="rounded-lg bg-green-600 px-4 py-2 text-sm text-white hover:bg-green-700"
              @click="unblock(selectedCondo); showDetailModal = false"
            >
              Desbloquear
            </button>
            <button
              v-else
              class="rounded-lg bg-red-600 px-4 py-2 text-sm text-white hover:bg-red-700"
              @click="openBlockModal(selectedCondo); showDetailModal = false"
            >
              Bloquear
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal de faturas -->
    <Teleport to="body">
      <div v-if="showInvoicesModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h2 class="text-lg font-bold text-gray-900">Faturas — {{ selectedCondo?.name }}</h2>
              <p class="text-sm text-gray-500">
                Plano: {{ selectedCondo?.planName ?? 'Sem plano' }} · {{ formatCurrency(selectedCondo?.planPriceCents ?? null) }}/mes
              </p>
            </div>
            <button class="text-gray-400 hover:text-gray-600" @click="showInvoicesModal = false">✕</button>
          </div>
          <BaseTable
            :columns="invoiceColumns"
            :data="condoInvoices.map((inv) => ({
              referenceDisplay: inv.referenceDisplay,
              amountFormatted: formatCurrency(inv.amountCents),
              createdDate: formatDate(inv.createdAt ?? null),
              paidDate: inv.paidAt ? formatDate(inv.paidAt) : '—',
              status: inv.status,
            }))"
            :loading="invoicesLoading"
            empty-message="Nenhuma fatura encontrada."
          >
            <template #cell-status="{ value }">
              <BaseBadge :variant="invoiceStatusVariant[value as string] || 'gray'">
                {{ invoiceStatusLabel[value as string] || value }}
              </BaseBadge>
            </template>
          </BaseTable>
        </div>
      </div>
    </Teleport>

    <!-- Modal de bloqueio -->
    <Teleport to="body">
      <div v-if="showBlockModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
          <h2 class="mb-4 text-lg font-bold text-gray-900">Bloquear — {{ selectedCondo?.name }}</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Tipo de bloqueio</label>
              <select v-model="blockType" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm">
                <option value="PAYMENT">Inadimplencia (sindico acessa somente pagamento)</option>
                <option value="GENERAL">Geral (ninguem acessa, apenas admin)</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Motivo</label>
              <textarea
                v-model="blockReason"
                rows="3"
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                placeholder="Motivo do bloqueio..."
              />
            </div>
            <div class="flex justify-end gap-3">
              <button
                class="rounded-lg border border-gray-300 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
                @click="showBlockModal = false"
              >
                Cancelar
              </button>
              <button
                class="rounded-lg bg-red-600 px-4 py-2 text-sm text-white hover:bg-red-700 disabled:opacity-50"
                :disabled="actionLoading"
                @click="confirmBlock"
              >
                {{ actionLoading ? 'Bloqueando...' : 'Confirmar Bloqueio' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
