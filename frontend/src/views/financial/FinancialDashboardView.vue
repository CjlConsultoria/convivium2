<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import { useTenantStore } from '@/stores/tenant.store'
import * as paymentApi from '@/api/modules/payment.api'

const route = useRoute()
const tenantStore = useTenantStore()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(true)
const invoicesLoading = ref(true)
const payLoading = ref(false)
const payError = ref<string | null>(null)

// Dados do plano
const planName = computed(() => tenantStore.currentCondominium?.planName ?? null)
const planId = computed(() => tenantStore.currentCondominium?.planId ?? null)
const planPriceCents = computed(() => tenantStore.currentCondominium?.planPriceCents ?? null)
const condoStatus = computed(() => tenantStore.currentCondominium?.status ?? null)
const blockType = computed(() => tenantStore.currentCondominium?.blockType ?? null)
const subscriptionStartedAt = computed(() => tenantStore.currentCondominium?.subscriptionStartedAt ?? null)

const planPriceFormatted = computed(() => {
  const cents = planPriceCents.value
  if (cents == null) return '—'
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
})

const subscriptionStartFormatted = computed(() => {
  if (!subscriptionStartedAt.value) return '—'
  return new Date(subscriptionStartedAt.value).toLocaleDateString('pt-BR')
})

// Calcula proximo vencimento
const nextDueDate = computed(() => {
  const pending = rawInvoices.value.filter(inv => inv.status === 'PENDING' || inv.status === 'OVERDUE')
  if (pending.length > 0) {
    const oldest = pending[pending.length - 1]
    if (oldest.createdAt) {
      const due = new Date(oldest.createdAt)
      due.setDate(due.getDate() + 15)
      return due.toLocaleDateString('pt-BR')
    }
    return oldest.referenceDisplay
  }
  if (subscriptionStartedAt.value) {
    const lastPaid = rawInvoices.value.find(inv => inv.status === 'PAID')
    if (lastPaid?.paidAt) {
      const next = new Date(lastPaid.paidAt)
      next.setDate(next.getDate() + 30)
      return next.toLocaleDateString('pt-BR')
    }
    const start = new Date(subscriptionStartedAt.value)
    start.setDate(start.getDate() + 30)
    return start.toLocaleDateString('pt-BR')
  }
  return '—'
})

const statusPayment = computed(() => {
  if (blockType.value === 'PAYMENT') return 'Bloqueado'
  const hasOverdue = rawInvoices.value.some(inv => inv.status === 'OVERDUE')
  if (hasOverdue) return 'Atrasado'
  const hasPending = rawInvoices.value.some(inv => inv.status === 'PENDING')
  if (hasPending) return 'Pendente'
  return 'Em dia'
})

const statusPaymentColor = computed(() => {
  if (statusPayment.value === 'Em dia') return 'text-green-600'
  if (statusPayment.value === 'Pendente') return 'text-yellow-600'
  return 'text-red-600'
})

const columns = [
  { key: 'referenceDisplay', label: 'Referencia' },
  { key: 'description', label: 'Descricao' },
  { key: 'amountFormatted', label: 'Valor' },
  { key: 'createdDate', label: 'Emissao' },
  { key: 'paidDate', label: 'Pago em' },
  { key: 'status', label: 'Status' },
]

const rawInvoices = ref<Array<{
  referenceMonth: string
  referenceDisplay: string
  amountCents: number
  status: string
  paidAt: string | null
  createdAt: string | null
}>>([])

const invoices = ref<Array<Record<string, unknown>>>([])

const statusVariant: Record<string, 'green' | 'yellow' | 'red' | 'gray'> = {
  PAID: 'green',
  PENDING: 'yellow',
  OVERDUE: 'red',
  CANCELLED: 'gray',
}

const statusLabel: Record<string, string> = {
  PAID: 'Pago',
  PENDING: 'Pendente',
  OVERDUE: 'Atrasado',
  CANCELLED: 'Cancelado',
}

function formatCurrency(cents: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
}

async function loadInvoices() {
  invoicesLoading.value = true
  try {
    const res = await paymentApi.listInvoices(condoId.value)
    if (res.success && res.data) {
      rawInvoices.value = res.data.map((inv) => ({
        referenceMonth: inv.referenceMonth,
        referenceDisplay: inv.referenceDisplay,
        amountCents: inv.amountCents,
        status: inv.status,
        paidAt: inv.paidAt,
        createdAt: inv.createdAt ?? null,
      }))
      invoices.value = res.data.map((inv) => ({
        referenceDisplay: inv.referenceDisplay,
        description: `Mensalidade - ${planName.value ?? 'Plano Convivium'}`,
        amountFormatted: formatCurrency(inv.amountCents),
        createdDate: inv.createdAt ? new Date(inv.createdAt).toLocaleDateString('pt-BR') : '—',
        paidDate: inv.paidAt ? new Date(inv.paidAt).toLocaleDateString('pt-BR') : '—',
        status: inv.status,
      }))
    }
  } catch {
    rawInvoices.value = []
    invoices.value = []
  } finally {
    invoicesLoading.value = false
  }
}

async function payRedirect() {
  await startPayment((url) => {
    window.location.href = url
  })
}

async function payNewTab() {
  await startPayment((url) => {
    window.open(url, '_blank', 'noopener,noreferrer')
  })
}

async function startPayment(openUrl: (url: string) => void) {
  payError.value = null
  if (planId.value == null) {
    payError.value = 'Condominio sem plano associado.'
    return
  }
  payLoading.value = true
  try {
    const res = await paymentApi.createCheckoutSession(condoId.value, planId.value)
    if (res.success && res.data?.url) {
      openUrl(res.data.url)
    } else {
      payError.value = (res as { message?: string }).message ?? 'Nao foi possivel iniciar o pagamento.'
    }
  } catch (e: unknown) {
    const res = e && typeof e === 'object' && 'response' in e
      ? (e as { response?: { data?: { message?: string; errorCode?: string } } }).response?.data
      : null
    const code = res?.errorCode
    const msg = res?.message
    if (code === 'STRIPE_NOT_CONFIGURED') {
      payError.value = 'Pagamentos ainda nao estao disponiveis. Entre em contato com o suporte.'
    } else if (code === 'INVOICE_ALREADY_PAID') {
      payError.value = 'A fatura deste mes ja foi paga.'
    } else {
      payError.value = msg ?? 'Erro ao criar sessao de pagamento. Tente novamente.'
    }
  } finally {
    payLoading.value = false
  }
}

const paymentSuccess = computed(() => route.query.payment === 'success')
const paymentCancel = computed(() => route.query.payment === 'cancel')

const hasPendingOrOverdue = computed(() =>
  rawInvoices.value.some(inv => inv.status === 'PENDING' || inv.status === 'OVERDUE')
)

onMounted(async () => {
  if (tenantStore.currentCondominiumId !== condoId.value) {
    await tenantStore.setCondominium(condoId.value)
  }
  await loadInvoices()
  loading.value = false
  if (route.query.payment === 'success') {
    setTimeout(() => loadInvoices(), 2500)
    setTimeout(() => loadInvoices(), 6000)
  }
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Financeiro</h1>
      <p class="text-sm text-gray-500">
        Gerencie o plano, faturas e pagamentos da mensalidade do condominio na plataforma Convivium.
      </p>
    </div>

    <!-- Mensagem de retorno do Stripe -->
    <div v-if="paymentSuccess" class="rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-green-800">
      <span class="font-medium">Pagamento concluido com sucesso!</span> A fatura sera atualizada em instantes.
    </div>
    <div v-else-if="paymentCancel" class="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
      Pagamento cancelado. Voce pode tentar novamente quando quiser.
    </div>

    <!-- Alerta de bloqueio -->
    <div v-if="blockType === 'PAYMENT'" class="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-800">
      <span class="font-medium">Acesso restrito!</span> O condominio esta bloqueado por falta de pagamento. Regularize a situacao abaixo para restaurar o acesso completo.
    </div>

    <!-- Cards de resumo -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Plano Atual</p>
        <p class="mt-1 text-xl font-bold text-gray-900">{{ planName ?? 'Sem plano' }}</p>
        <p v-if="condoStatus" class="mt-1">
          <span class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium"
            :class="condoStatus === 'ACTIVE' ? 'bg-green-100 text-green-800' : condoStatus === 'SUSPENDED' ? 'bg-red-100 text-red-800' : 'bg-yellow-100 text-yellow-800'">
            {{ condoStatus === 'ACTIVE' ? 'Ativo' : condoStatus === 'SUSPENDED' ? 'Suspenso' : condoStatus }}
          </span>
        </p>
      </BaseCard>

      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Valor Mensal</p>
        <p class="mt-1 text-xl font-bold text-primary-600">{{ planPriceFormatted }}</p>
        <p class="mt-1 text-xs text-gray-400">Cobrado mensalmente</p>
      </BaseCard>

      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Proximo Vencimento</p>
        <p class="mt-1 text-xl font-bold text-gray-700">{{ nextDueDate }}</p>
        <p class="mt-1 text-xs text-gray-400">Assinatura desde {{ subscriptionStartFormatted }}</p>
      </BaseCard>

      <BaseCard>
        <p class="text-xs font-medium uppercase tracking-wider text-gray-500">Situacao</p>
        <p class="mt-1 text-xl font-bold" :class="statusPaymentColor">{{ statusPayment }}</p>
        <p v-if="statusPayment === 'Em dia'" class="mt-1 text-xs text-green-500">Nenhuma pendencia</p>
        <p v-else-if="statusPayment === 'Pendente'" class="mt-1 text-xs text-yellow-500">Fatura aguardando pagamento</p>
        <p v-else class="mt-1 text-xs text-red-500">Regularize para restaurar o acesso</p>
      </BaseCard>
    </div>

    <!-- Pagar mensalidade -->
    <BaseCard>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-lg font-semibold text-gray-900">Pagar Mensalidade</h2>
          <p class="mt-1 text-sm text-gray-600">
            Formas aceitas: cartao, Apple Pay, Google Pay, Link e boleto.
          </p>
        </div>
        <div v-if="planPriceCents" class="text-right">
          <p class="text-2xl font-bold text-primary-600">{{ planPriceFormatted }}</p>
          <p class="text-xs text-gray-400">/mes</p>
        </div>
      </div>

      <div class="mt-4 flex flex-wrap gap-2">
        <span class="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">
          <svg class="h-3.5 w-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/></svg>
          Cartao
        </span>
        <span class="inline-flex items-center rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">Apple Pay</span>
        <span class="inline-flex items-center rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">Google Pay</span>
        <span class="inline-flex items-center rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">Link</span>
        <span class="inline-flex items-center rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700">Boleto</span>
        <span class="inline-flex items-center rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800">Pix — Em breve</span>
      </div>

      <div class="mt-4 flex flex-wrap gap-3">
        <button
          type="button"
          class="inline-flex items-center rounded-lg bg-primary-600 px-5 py-2.5 text-sm font-medium text-white shadow-sm hover:bg-primary-700 disabled:opacity-50"
          :disabled="payLoading || planId == null || !hasPendingOrOverdue"
          @click="payRedirect"
        >
          <svg v-if="!payLoading" class="mr-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          {{ payLoading ? 'Abrindo...' : hasPendingOrOverdue ? 'Pagar Agora' : 'Sem faturas pendentes' }}
        </button>
        <button
          v-if="hasPendingOrOverdue"
          type="button"
          class="inline-flex items-center rounded-lg border border-gray-300 bg-white px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          :disabled="payLoading || planId == null"
          @click="payNewTab"
        >
          Abrir em nova aba
        </button>
      </div>
      <p v-if="payError" class="mt-3 text-sm text-red-600">{{ payError }}</p>
    </BaseCard>

    <!-- Historico de faturas -->
    <BaseCard>
      <h2 class="mb-1 text-lg font-semibold text-gray-900">Historico de Faturas</h2>
      <p class="mb-4 text-sm text-gray-500">
        Faturas referentes a mensalidade do plano. Para alterar o plano, entre em contato com a administracao.
      </p>
      <BaseTable :columns="columns" :data="invoices" :loading="invoicesLoading" empty-message="Nenhuma fatura encontrada.">
        <template #cell-status="{ value }">
          <BaseBadge :variant="statusVariant[value as string] || 'gray'">
            {{ statusLabel[value as string] || value }}
          </BaseBadge>
        </template>
      </BaseTable>
    </BaseCard>
  </div>
</template>
