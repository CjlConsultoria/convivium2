<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as paymentApi from '@/api/modules/payment.api'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(true)
const error = ref<string | null>(null)
const invoices = ref<paymentApi.PlatformInvoice[]>([])
const checkoutLoading = ref(false)
const checkoutError = ref<string | null>(null)
const showCheckout = ref(false)
const checkoutContainerRef = ref<HTMLDivElement | null>(null)
let stripeCheckout: { destroy: () => void } | null = null

const pendingInvoices = computed(() =>
  invoices.value.filter((inv) => inv.status === 'PENDING' || inv.status === 'OVERDUE'),
)

const statusLabel: Record<string, string> = {
  PAID: 'Pago',
  PENDING: 'Pendente',
  OVERDUE: 'Atrasado',
  CANCELLED: 'Cancelado',
}

const statusColor: Record<string, string> = {
  PAID: 'bg-green-100 text-green-800',
  PENDING: 'bg-yellow-100 text-yellow-800',
  OVERDUE: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-gray-100 text-gray-800',
}

function formatCurrency(cents: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(cents / 100)
}

async function loadInvoices() {
  loading.value = true
  try {
    const res = await paymentApi.listInvoices(condoId.value)
    if (res.success && res.data) {
      invoices.value = res.data
    }
  } catch {
    error.value = 'Erro ao carregar faturas.'
  } finally {
    loading.value = false
  }
}

async function startEmbeddedCheckout() {
  checkoutError.value = null
  checkoutLoading.value = true

  try {
    // 1. Get Stripe publishable key
    const keyRes = await paymentApi.getStripeKey(condoId.value)
    if (!keyRes.success || !keyRes.data?.publishableKey) {
      checkoutError.value = 'Stripe nao configurado. Entre em contato com a plataforma.'
      return
    }

    // 2. Create embedded checkout session
    const sessionRes = await paymentApi.createEmbeddedCheckout(condoId.value)
    if (!sessionRes.success || !sessionRes.data?.clientSecret) {
      checkoutError.value = 'Nao foi possivel criar a sessao de pagamento.'
      return
    }

    // 3. Load Stripe.js and mount embedded checkout
    const { loadStripe } = await import('@stripe/stripe-js')
    const stripe = await loadStripe(keyRes.data.publishableKey)
    if (!stripe) {
      checkoutError.value = 'Erro ao carregar Stripe.'
      return
    }

    showCheckout.value = true

    // Wait for Vue to render the container
    await new Promise((resolve) => setTimeout(resolve, 100))

    const checkout = await stripe.initEmbeddedCheckout({
      clientSecret: sessionRes.data.clientSecret,
    })

    if (checkoutContainerRef.value) {
      checkout.mount(checkoutContainerRef.value)
      stripeCheckout = checkout
    }
  } catch (e: unknown) {
    const msg =
      e && typeof e === 'object' && 'response' in e
        ? (e as { response?: { data?: { message?: string } } }).response?.data?.message
        : null
    checkoutError.value = msg ?? 'Erro ao iniciar pagamento. Tente novamente.'
  } finally {
    checkoutLoading.value = false
  }
}

function goBack() {
  router.push(`/c/${condoId.value}`)
}

onMounted(loadInvoices)

onUnmounted(() => {
  if (stripeCheckout) {
    stripeCheckout.destroy()
  }
})
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-gray-50 p-4">
    <div class="w-full max-w-2xl space-y-6">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-red-100">
          <svg class="h-8 w-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-gray-900">Pagamento Pendente</h1>
        <p class="mt-2 text-gray-600">
          O acesso ao condominio esta bloqueado por falta de pagamento.
          Regularize a situacao para restaurar o acesso completo.
        </p>
      </div>

      <!-- Faturas pendentes -->
      <div v-if="!loading && pendingInvoices.length > 0" class="rounded-lg bg-white p-6 shadow-sm">
        <h2 class="mb-4 text-lg font-semibold text-gray-900">Faturas Pendentes</h2>
        <div class="space-y-3">
          <div
            v-for="inv in pendingInvoices"
            :key="inv.id"
            class="flex items-center justify-between rounded-lg border border-gray-200 p-4"
          >
            <div>
              <p class="font-medium text-gray-900">{{ inv.referenceDisplay }}</p>
              <p class="text-sm text-gray-500">{{ formatCurrency(inv.amountCents) }}</p>
            </div>
            <span
              class="inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium"
              :class="statusColor[inv.status] || 'bg-gray-100 text-gray-800'"
            >
              {{ statusLabel[inv.status] || inv.status }}
            </span>
          </div>
        </div>
      </div>

      <!-- Botao de pagamento -->
      <div v-if="!showCheckout" class="text-center">
        <button
          type="button"
          class="inline-flex items-center rounded-lg bg-primary-600 px-6 py-3 text-base font-medium text-white shadow-sm hover:bg-primary-700 disabled:opacity-50"
          :disabled="checkoutLoading"
          @click="startEmbeddedCheckout"
        >
          {{ checkoutLoading ? 'Carregando...' : 'Pagar Agora' }}
        </button>
        <p v-if="checkoutError" class="mt-3 text-sm text-red-600">{{ checkoutError }}</p>
      </div>

      <!-- Container do Stripe Embedded Checkout -->
      <div v-if="showCheckout" class="rounded-lg bg-white p-4 shadow-sm">
        <div ref="checkoutContainerRef" id="checkout-container" />
      </div>

      <!-- Loading -->
      <div v-if="loading" class="text-center text-gray-500">
        Carregando...
      </div>

      <div v-if="error" class="text-center text-sm text-red-600">
        {{ error }}
      </div>

      <div class="text-center">
        <button type="button" class="text-sm text-gray-500 hover:text-gray-700" @click="goBack">
          Voltar ao painel
        </button>
      </div>
    </div>
  </div>
</template>
