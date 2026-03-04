<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import * as supportApi from '@/api/modules/support.api'

const loading = ref(true)
const tickets = ref<supportApi.SupportTicket[]>([])
const selectedTicket = ref<supportApi.SupportTicket | null>(null)
const messages = ref<supportApi.SupportMessage[]>([])
const messagesLoading = ref(false)
const newMessage = ref('')
const sendingMessage = ref(false)
const showNewTicketForm = ref(false)
const newTicketSubject = ref('')
const creatingTicket = ref(false)
const messagesContainer = ref<HTMLDivElement | null>(null)

// Polling interval
let pollingInterval: ReturnType<typeof setInterval> | null = null

const statusLabel: Record<string, string> = {
  OPEN: 'Aberto',
  IN_PROGRESS: 'Em Andamento',
  RESOLVED: 'Resolvido',
  CLOSED: 'Fechado',
}

const statusColor: Record<string, string> = {
  OPEN: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-800',
  RESOLVED: 'bg-green-100 text-green-800',
  CLOSED: 'bg-gray-100 text-gray-800',
}

async function loadTickets() {
  loading.value = true
  try {
    const res = await supportApi.listMyTickets()
    if (res.success && res.data) {
      tickets.value = res.data
    }
  } finally {
    loading.value = false
  }
}

async function selectTicket(ticket: supportApi.SupportTicket) {
  selectedTicket.value = ticket
  messagesLoading.value = true
  try {
    const res = await supportApi.getTicketMessages(ticket.id)
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
    const res = await supportApi.sendMessage(selectedTicket.value.id, newMessage.value.trim())
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

async function createTicket() {
  if (!newTicketSubject.value.trim()) return
  creatingTicket.value = true
  try {
    const res = await supportApi.createTicket(newTicketSubject.value.trim())
    if (res.success && res.data) {
      showNewTicketForm.value = false
      newTicketSubject.value = ''
      await loadTickets()
      selectTicket(res.data)
    }
  } finally {
    creatingTicket.value = false
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
      const res = await supportApi.getTicketMessages(selectedTicket.value.id)
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
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Suporte</h1>
        <p class="text-sm text-gray-500">Fale com a administracao da plataforma.</p>
      </div>
      <button
        class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700"
        @click="showNewTicketForm = true"
      >
        Novo Chamado
      </button>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Lista de tickets -->
      <div class="lg:col-span-1">
        <BaseCard title="Meus Chamados">
          <div v-if="loading" class="text-center text-gray-500 py-8">Carregando...</div>
          <div v-else-if="tickets.length === 0" class="text-center text-gray-500 py-8">
            Nenhum chamado aberto.
          </div>
          <div v-else class="space-y-2">
            <button
              v-for="ticket in tickets"
              :key="ticket.id"
              class="w-full rounded-lg border p-3 text-left transition-colors"
              :class="selectedTicket?.id === ticket.id ? 'border-primary-500 bg-primary-50' : 'border-gray-200 hover:bg-gray-50'"
              @click="selectTicket(ticket)"
            >
              <p class="font-medium text-gray-900 text-sm">{{ ticket.subject }}</p>
              <div class="mt-1 flex items-center gap-2">
                <span
                  class="inline-flex rounded-full px-2 py-0.5 text-xs font-medium"
                  :class="statusColor[ticket.status] || 'bg-gray-100 text-gray-800'"
                >
                  {{ statusLabel[ticket.status] || ticket.status }}
                </span>
                <span class="text-xs text-gray-400">{{ formatTime(ticket.createdAt) }}</span>
              </div>
            </button>
          </div>
        </BaseCard>
      </div>

      <!-- Chat -->
      <div class="lg:col-span-2">
        <BaseCard v-if="selectedTicket" :title="selectedTicket.subject">
          <div
            ref="messagesContainer"
            class="h-96 space-y-3 overflow-y-auto rounded-lg border border-gray-100 bg-gray-50 p-4"
          >
            <div v-if="messagesLoading" class="text-center text-gray-500 py-12">Carregando mensagens...</div>
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="flex"
              :class="msg.fromAdmin ? 'justify-start' : 'justify-end'"
            >
              <div
                class="max-w-xs rounded-lg px-4 py-2 text-sm lg:max-w-md"
                :class="msg.fromAdmin ? 'bg-white text-gray-900 shadow-sm' : 'bg-primary-600 text-white'"
              >
                <p class="text-xs font-medium mb-1" :class="msg.fromAdmin ? 'text-gray-500' : 'text-primary-200'">
                  {{ msg.senderName }}
                </p>
                <p>{{ msg.message }}</p>
                <p class="mt-1 text-xs" :class="msg.fromAdmin ? 'text-gray-400' : 'text-primary-200'">
                  {{ formatTime(msg.createdAt) }}
                </p>
              </div>
            </div>
          </div>

          <div class="mt-4 flex gap-2">
            <input
              v-model="newMessage"
              type="text"
              placeholder="Digite sua mensagem..."
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
            Selecione um chamado ou crie um novo para iniciar.
          </div>
        </BaseCard>
      </div>
    </div>

    <!-- New Ticket Modal -->
    <Teleport to="body">
      <div v-if="showNewTicketForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
          <h2 class="mb-4 text-lg font-bold">Novo Chamado</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Assunto</label>
              <input
                v-model="newTicketSubject"
                type="text"
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                placeholder="Descreva brevemente o assunto..."
              />
            </div>
            <div class="flex justify-end gap-3">
              <button class="rounded-lg border px-4 py-2 text-sm text-gray-700 hover:bg-gray-50" @click="showNewTicketForm = false">
                Cancelar
              </button>
              <button
                class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700 disabled:opacity-50"
                :disabled="creatingTicket || !newTicketSubject.trim()"
                @click="createTicket"
              >
                {{ creatingTicket ? 'Criando...' : 'Criar Chamado' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
