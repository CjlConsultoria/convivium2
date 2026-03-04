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
const selectedCondo = ref<billingApi.CondoBilling | null>(null)
const condoInvoices = ref<PlatformInvoice[]>([])
const invoicesLoading = ref(false)
const blockType = ref<'PAYMENT' | 'GENERAL'>('PAYMENT')
const blockReason = ref('')
const actionLoading = ref(false)

const columns = [
  { key: 'name', label: 'Condominio' },
  { key: 'planName', label: 'Plano' },
  { key: 'priceFormatted', label: 'Valor' },
  { key: 'status', label: 'Status' },
  { key: 'blockType', label: 'Bloqueio' },
  { key: 'actions', label: 'Acoes' },
]

const invoiceColumns = [
  { key: 'referenceDisplay', label: 'Referencia' },
  { key: 'amountFormatted', label: 'Valor' },
  { key: 'status', label: 'Status' },
  { key: 'paidAt', label: 'Pago em' },
]

const statusVariant: Record<string, 'green' | 'yellow' | 'red' | 'gray'> = {
  ACTIVE: 'green',
  PENDING: 'yellow',
  SUSPENDED: 'red',
  DEACTIVATED: 'gray',
}

const blockVariant: Record<string, 'red' | 'yellow' | 'gray'> = {
  PAYMENT: 'yellow',
  GENERAL: 'red',
}

function formatCurrency(cents: number | null): string {
  if (cents == null) return '–'
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
}

const tableData = ref<Array<Record<string, unknown>>>([])

function buildTableData() {
  tableData.value = condos.value.map((c) => ({
    id: c.id,
    name: c.name,
    planName: c.planName ?? 'Sem plano',
    priceFormatted: formatCurrency(c.planPriceCents),
    status: c.status,
    blockType: c.blockType ?? '–',
    _raw: c,
  }))
}

async function loadCondos() {
  loading.value = true
  try {
    const res = await billingApi.listCondominiums()
    if (res.success && res.data) {
      condos.value = res.data
      buildTableData()
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
      <p class="text-sm text-gray-500">Gerencie faturas e bloqueios de condominios.</p>
    </div>

    <BaseCard>
      <BaseTable :columns="columns" :data="tableData" :loading="loading" empty-message="Nenhum condominio encontrado.">
        <template #cell-status="{ value }">
          <BaseBadge :variant="statusVariant[value as string] || 'gray'">
            {{ value }}
          </BaseBadge>
        </template>
        <template #cell-blockType="{ value }">
          <BaseBadge v-if="value !== '–'" :variant="blockVariant[value as string] || 'gray'">
            {{ value }}
          </BaseBadge>
          <span v-else class="text-gray-400">–</span>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex gap-2">
            <button
              class="text-xs text-primary-600 hover:underline"
              @click="openInvoices((row as Record<string, unknown>)._raw as billingApi.CondoBilling)"
            >
              Faturas
            </button>
            <button
              v-if="!(row as Record<string, unknown>)._raw || !((row as Record<string, unknown>)._raw as billingApi.CondoBilling).blockType"
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

    <!-- Modal de faturas -->
    <Teleport to="body">
      <div v-if="showInvoicesModal" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl">
          <div class="mb-4 flex items-center justify-between">
            <h2 class="text-lg font-bold">Faturas — {{ selectedCondo?.name }}</h2>
            <button class="text-gray-400 hover:text-gray-600" @click="showInvoicesModal = false">✕</button>
          </div>
          <BaseTable
            :columns="invoiceColumns"
            :data="condoInvoices.map((inv) => ({
              referenceDisplay: inv.referenceDisplay,
              amountFormatted: formatCurrency(inv.amountCents),
              status: inv.status,
              paidAt: inv.paidAt ? new Date(inv.paidAt).toLocaleDateString('pt-BR') : '–',
            }))"
            :loading="invoicesLoading"
            empty-message="Nenhuma fatura."
          >
            <template #cell-status="{ value }">
              <BaseBadge :variant="value === 'PAID' ? 'green' : value === 'OVERDUE' ? 'red' : 'yellow'">
                {{ value }}
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
          <h2 class="mb-4 text-lg font-bold">Bloquear — {{ selectedCondo?.name }}</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Tipo de bloqueio</label>
              <select v-model="blockType" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm">
                <option value="PAYMENT">Inadimplencia (sindico acessa pagamento)</option>
                <option value="GENERAL">Geral (ninguem acessa)</option>
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
