<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import * as supportApi from '@/api/modules/support.api'

const loading = ref(true)
const tickets = ref<supportApi.SupportTicket[]>([])
const selectedTicket = ref<supportApi.SupportTicket | null>(null)
const messages = ref<supportApi.SupportMessage[]>([])
const messagesLoading = ref(false)
const newMessage = ref('')
const sendingMessage = ref(false)
const statusFilter = ref('')
const messagesContainer = ref<HTMLDivElement | null>(null)

let pollingInterval: ReturnType<typeof setInterval> | null = null

const statusLabel: Record<string, string> = {
  OPEN: 'Aberto',
  IN_PROGRESS: 'Em Andamento',
  RESOLVED: 'Resolvido',
  CLOSED: 'Fechado',
}

const statusVariant: Record<string, 'green' | 'yellow' | 'red' | 'gray'> = {
  OPEN: 'yellow',
  IN_PROGRESS: 'green',
  RESOLVED: 'green',
  CLOSED: 'gray',
}

async function loadTickets() {
  loading.value = true
  try {
    const res = await supportApi.adminListTickets(statusFilter.value || undefined)
    if (res.success && res.data) {
      tickets.value = res.data.content
    }
  } finally {
    loading.value = false
  }
}

async function selectTicket(ticket: supportApi.SupportTicket) {
  selectedTicket.value = ticket
  messagesLoading.value = true
  try {
    const res = await supportApi.adminGetTicketMessages(ticket.id)
    if (res.success && res.data) {
      messages.value = res.data
      await nextTick()
      scrollToBottom()
    }
  } finally {
    messagesLoading.value = false
  }
  startPolling()
}

async function sendMsg() {
  if (!selectedTicket.value || !newMessage.value.trim()) return
  sendingMessage.value = true
  try {
    const res = await supportApi.adminSendMessage(selectedTicket.value.id, newMessage.value.trim())
    if (res.success && res.data) {
      messages.value.push(res.data)
      newMessage.value = ''
      await nextTick()
      scrollToBottom()
    }
  } finally {
    sendingMessage.value = false
  }
}

async function changeStatus(status: string) {
  if (!selectedTicket.value) return
  const res = await supportApi.adminUpdateTicketStatus(selectedTicket.value.id, status)
  if (res.success && res.data) {
    selectedTicket.value = res.data
    await loadTickets()
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function startPolling() {
  stopPolling()
  pollingInterval = setInterval(async () => {
    if (selectedTicket.value) {
      const res = await supportApi.adminGetTicketMessages(selectedTicket.value.id)
      if (res.success && res.data && res.data.length > messages.value.length) {
        messages.value = res.data
        await nextTick()
        scrollToBottom()
      }
    }
  }, 5000)
}

function stopPolling() {
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

function formatTime(dateStr: string): string {
  return new Date(dateStr).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

onMounted(loadTickets)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Suporte — Administracao</h1>
      <p class="text-sm text-gray-500">Gerencie chamados de suporte dos usuarios.</p>
    </div>

    <!-- Filtro -->
    <div class="flex gap-2">
      <select v-model="statusFilter" class="rounded-md border-gray-300 text-sm" @change="loadTickets">
        <option value="">Todos</option>
        <option value="OPEN">Abertos</option>
        <option value="IN_PROGRESS">Em Andamento</option>
        <option value="RESOLVED">Resolvidos</option>
        <option value="CLOSED">Fechados</option>
      </select>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Lista de tickets -->
      <div class="lg:col-span-1">
        <BaseCard title="Chamados">
          <div v-if="loading" class="text-center text-gray-500 py-8">Carregando...</div>
          <div v-else-if="tickets.length === 0" class="text-center text-gray-500 py-8">Nenhum chamado.</div>
          <div v-else class="space-y-2 max-h-[600px] overflow-y-auto">
            <button
              v-for="ticket in tickets"
              :key="ticket.id"
              class="w-full rounded-lg border p-3 text-left transition-colors"
              :class="selectedTicket?.id === ticket.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:bg-gray-50'"
              @click="selectTicket(ticket)"
            >
              <p class="font-medium text-gray-900 text-sm">{{ ticket.subject }}</p>
              <p class="text-xs text-gray-500 mt-1">
                {{ ticket.userName }} — {{ ticket.condominiumName || 'N/A' }}
                <span v-if="ticket.unitLabel"> | {{ ticket.unitLabel }}</span>
                <span v-if="ticket.roleName"> | {{ ticket.roleName }}</span>
              </p>
              <div class="mt-1 flex items-center gap-2">
                <BaseBadge :variant="statusVariant[ticket.status] || 'gray'" class="text-xs">
                  {{ statusLabel[ticket.status] || ticket.status }}
                </BaseBadge>
                <span class="text-xs text-gray-400">{{ formatTime(ticket.createdAt) }}</span>
              </div>
            </button>
          </div>
        </BaseCard>
      </div>

      <!-- Chat -->
      <div class="lg:col-span-2">
        <BaseCard v-if="selectedTicket">
          <div class="mb-4 flex items-center justify-between">
            <div>
              <h3 class="font-bold text-gray-900">{{ selectedTicket.subject }}</h3>
              <p class="text-xs text-gray-500">
                {{ selectedTicket.userName }} — {{ selectedTicket.condominiumName }}
                <span v-if="selectedTicket.unitLabel"> | {{ selectedTicket.unitLabel }}</span>
                <span v-if="selectedTicket.roleName"> | {{ selectedTicket.roleName }}</span>
              </p>
            </div>
            <select
              :value="selectedTicket.status"
              class="rounded-md border-gray-300 text-sm"
              @change="changeStatus(($event.target as HTMLSelectElement).value)"
            >
              <option value="OPEN">Aberto</option>
              <option value="IN_PROGRESS">Em Andamento</option>
              <option value="RESOLVED">Resolvido</option>
              <option value="CLOSED">Fechado</option>
            </select>
          </div>

          <div
            ref="messagesContainer"
            class="h-96 space-y-3 overflow-y-auto rounded-lg border border-gray-100 bg-gray-50 p-4"
          >
            <div v-if="messagesLoading" class="text-center text-gray-500 py-12">Carregando mensagens...</div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="flex"
              :class="msg.fromAdmin ? 'justify-end' : 'justify-start'"
            >
              <div
                class="max-w-xs rounded-lg px-4 py-2 text-sm lg:max-w-md"
                :class="msg.fromAdmin ? 'bg-primary-600 text-white' : 'bg-white text-gray-900 shadow-sm'"
              >
                <p class="text-xs font-medium mb-1" :class="msg.fromAdmin ? 'text-primary-200' : 'text-gray-500'">
                  {{ msg.senderName }}
                </p>
                <p>{{ msg.message }}</p>
                <p class="mt-1 text-xs" :class="msg.fromAdmin ? 'text-primary-200' : 'text-gray-400'">
                  {{ formatTime(msg.createdAt) }}
                </p>
              </div>
            </div>
          </div>

          <div class="mt-4 flex gap-2">
            <input
              v-model="newMessage"
              type="text"
              placeholder="Responder..."
              class="flex-1 rounded-lg border border-gray-300 px-4 py-2 text-sm"
              @keyup.enter="sendMsg"
            />
            <button
              class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700 disabled:opacity-50"
              :disabled="sendingMessage || !newMessage.trim()"
              @click="sendMsg"
            >
              Enviar
            </button>
          </div>
        </BaseCard>

        <BaseCard v-else>
          <div class="text-center text-gray-500 py-12">
            Selecione um chamado para visualizar.
          </div>
        </BaseCard>
      </div>
    </div>
  </div>
</template>
