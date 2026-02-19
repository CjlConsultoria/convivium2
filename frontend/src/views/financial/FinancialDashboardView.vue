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

// Dados do plano e faturas (mensalidade que o condomínio paga ao sistema)
const planName = computed(() => tenantStore.currentCondominium?.planName ?? 'Plano Convivium')
const planId = computed(() => tenantStore.currentCondominium?.planId ?? null)
const planPriceCents = computed(() => tenantStore.currentCondominium?.planPriceCents ?? null)
const planPriceFormatted = computed(() => {
  const cents = planPriceCents.value
  if (cents == null) return 'Conforme o plano associado ao condomínio'
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
})
const nextDueDate = ref<string | null>(null)
const statusEmDia = ref(true)

const stats = computed(() => [
  { label: 'Plano atual', value: planName.value, color: 'text-gray-900' },
  { label: 'Valor mensal', value: planPriceFormatted.value, color: 'text-primary-600' },
  { label: 'Próximo vencimento', value: nextDueDate.value ?? '–', color: 'text-gray-700' },
  { label: 'Status', value: statusEmDia.value ? 'Em dia' : 'Pendente', color: statusEmDia.value ? 'text-green-600' : 'text-red-600' },
])

const columns = [
  { key: 'referenceDisplay', label: 'Referência' },
  { key: 'description', label: 'Descrição' },
  { key: 'amountFormatted', label: 'Valor' },
  { key: 'dueDate', label: 'Vencimento' },
  { key: 'status', label: 'Status' },
]

const rawInvoices = ref<Array<{
  referenceMonth: string
  referenceDisplay: string
  amountCents: number
  status: string
  paidAt: string | null
}>>([])

const invoices = ref<Array<{
  referenceDisplay: string
  description: string
  amountFormatted: string
  dueDate: string
  status: string
}>>([])

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

// Formas de pagamento disponíveis (conforme Stripe: segunda imagem). Pix em breve.
const paymentMethodsAvailable = [
  { name: 'Cartão', icon: 'credit-card' },
  { name: 'Apple Pay', icon: 'apple' },
  { name: 'Google Pay', icon: 'google' },
  { name: 'Link', icon: 'link' },
  { name: 'Boleto', icon: 'barcode' },
]
const paymentMethodsComingSoon = [{ name: 'Pix', label: 'Em breve disponível' }]

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
      }))
      invoices.value = res.data.map((inv) => ({
        referenceDisplay: inv.referenceDisplay,
        description: 'Mensalidade do sistema',
        amountFormatted: new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(inv.amountCents / 100),
        dueDate: inv.paidAt ? new Date(inv.paidAt).toLocaleDateString('pt-BR') : '–',
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
    payError.value = 'Condomínio sem plano associado.'
    return
  }
  payLoading.value = true
  try {
    const res = await paymentApi.createCheckoutSession(condoId.value, planId.value)
    if (res.success && res.data?.url) {
      openUrl(res.data.url)
    } else {
      payError.value = (res as { message?: string }).message ?? 'Não foi possível iniciar o pagamento.'
    }
  } catch (e: unknown) {
    const res = e && typeof e === 'object' && 'response' in e
      ? (e as { response?: { data?: { message?: string; errorCode?: string } } }).response?.data
      : null
    const code = res?.errorCode
    const msg = res?.message
    if (code === 'STRIPE_NOT_CONFIGURED') {
      payError.value = 'Pagamentos ainda não estão disponíveis. A plataforma está configurando o gateway de pagamento. Entre em contato com o suporte ou tente mais tarde.'
    } else {
      payError.value = msg ?? 'Erro ao criar sessão de pagamento. Tente novamente.'
    }
  } finally {
    payLoading.value = false
  }
}

const paymentSuccess = computed(() => route.query.payment === 'success')
const paymentCancel = computed(() => route.query.payment === 'cancel')

onMounted(async () => {
  if (tenantStore.currentCondominiumId !== condoId.value) {
    await tenantStore.setCondominium(condoId.value)
  }
  await loadInvoices()
  loading.value = false
  // Depois de pagar: refaz a lista em alguns segundos para o webhook ter atualizado a fatura para Pago
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
        Plano do sistema e faturas do condomínio — o que o condomínio paga mensalmente pela plataforma Convivium.
      </p>
    </div>

    <!-- Mensagem de retorno do Stripe -->
    <div v-if="paymentSuccess" class="rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-green-800">
      Pagamento concluído com sucesso. A fatura será atualizada em instantes.
    </div>
    <div v-else-if="paymentCancel" class="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
      Pagamento cancelado. Você pode tentar novamente quando quiser.
    </div>

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <BaseCard v-for="stat in stats" :key="stat.label">
        <p class="text-sm text-gray-500">{{ stat.label }}</p>
        <p class="mt-1 text-2xl font-bold" :class="stat.color">{{ stat.value }}</p>
      </BaseCard>
    </div>

    <!-- Pagar: no sistema (Stripe hospedado) ou redirecionar -->
    <BaseCard title="Pagar mensalidade">
      <p class="mb-4 text-sm text-gray-600">
        Formas de pagamento disponíveis na página do Stripe: cartão, Apple Pay, Google Pay, Link e boleto. O status da fatura é atualizado em tempo real via webhook quando o pagamento é confirmado.
      </p>
      <div class="mb-4 flex flex-wrap gap-2">
        <span
          v-for="pm in paymentMethodsAvailable"
          :key="pm.name"
          class="inline-flex items-center rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-700"
        >
          {{ pm.name }}
        </span>
        <span
          v-for="pm in paymentMethodsComingSoon"
          :key="pm.name"
          class="inline-flex items-center rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-800"
        >
          {{ pm.name }} — {{ pm.label }}
        </span>
      </div>
      <div class="flex flex-wrap gap-3">
        <button
          type="button"
          class="inline-flex items-center rounded-lg bg-primary-600 px-4 py-2 text-sm font-medium text-white hover:bg-primary-700 disabled:opacity-50"
          :disabled="payLoading || planId == null"
          @click="payRedirect"
        >
          {{ payLoading ? 'Abrindo...' : 'Pagar na página do Stripe' }}
        </button>
        <button
          type="button"
          class="inline-flex items-center rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
          :disabled="payLoading || planId == null"
          @click="payNewTab"
        >
          Abrir página de pagamento em nova aba
        </button>
      </div>
      <p v-if="payError" class="mt-3 text-sm text-red-600">{{ payError }}</p>
    </BaseCard>

    <BaseCard title="Faturas do sistema">
      <p class="mb-4 text-sm text-gray-600">
        Faturas referentes à mensalidade do plano Convivium. Para alterar o plano ou dúvidas de cobrança, entre em contato com a administração da plataforma.
      </p>
      <BaseTable :columns="columns" :data="invoices" :loading="invoicesLoading" empty-message="Nenhuma fatura encontrada.">
        <template #cell-status="{ value }">
          <BaseBadge :variant="statusVariant[value] || 'gray'">
            {{ statusLabel[value] || value }}
          </BaseBadge>
        </template>
      </BaseTable>
    </BaseCard>
  </div>
</template>
