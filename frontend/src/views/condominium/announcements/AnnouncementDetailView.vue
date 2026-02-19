<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseTextarea from '@/components/base/BaseTextarea.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { formatDateTime, timeAgo } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))
const announcementId = computed(() => Number(route.params.announcementId))

const loading = ref(true)
const sendingComment = ref(false)
const success = ref('')
const error = ref('')
const commentText = ref('')

interface Announcement {
  id: number
  title: string
  content: string
  author: string
  authorRole: string
  category: string
  categoryLabel: string
  isPinned: boolean
  isEmergency: boolean
  createdAt: string
  readCount: number
  totalRecipients: number
  comments: Comment[]
}

interface Comment {
  id: number
  author: string
  message: string
  createdAt: string
}

const announcement = ref<Announcement | null>(null)

onMounted(async () => {
  await loadAnnouncement()
})

async function loadAnnouncement() {
  loading.value = true
  try {
    // Placeholder - replace with actual API call
    await new Promise((resolve) => setTimeout(resolve, 300))

    announcement.value = {
      id: announcementId.value,
      title: 'Manutencao da piscina programada',
      content: `Prezados moradores,

Informamos que a piscina do condominio estara em manutencao nos dias 20 e 21 de fevereiro de 2025.

Durante este periodo, a area da piscina estara completamente interditada para garantir a seguranca de todos.

Os servicos incluem:
- Limpeza profunda do fundo e paredes
- Verificacao e ajuste do sistema de filtracao
- Tratamento quimico da agua
- Reparo de rejuntes danificados

A previsao e que a piscina esteja disponivel novamente a partir do dia 22 de fevereiro.

Agradecemos a compreensao de todos.

Atenciosamente,
Administracao`,
      author: 'Carlos Administrador',
      authorRole: 'SINDICO',
      category: 'MAINTENANCE',
      categoryLabel: 'Manutencao',
      isPinned: true,
      isEmergency: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
      readCount: 87,
      totalRecipients: 128,
      comments: [
        {
          id: 1,
          author: 'Maria Silva - Unidade 101',
          message: 'Obrigada pelo aviso! Vou planejar usar a piscina do clube enquanto isso.',
          createdAt: new Date(Date.now() - 1000 * 60 * 60 * 3).toISOString(),
        },
        {
          id: 2,
          author: 'Joao Santos - Unidade 205',
          message: 'A manutencao inclui troca dos azulejos soltos perto da escada? Ja reportei isso antes.',
          createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
        },
      ],
    }
  } catch {
    announcement.value = null
  } finally {
    loading.value = false
  }
}

function categoryBadgeVariant(category: string): 'blue' | 'yellow' | 'green' | 'red' | 'gray' {
  switch (category) {
    case 'GENERAL':
      return 'blue'
    case 'MAINTENANCE':
      return 'yellow'
    case 'FINANCIAL':
      return 'green'
    case 'EVENT':
      return 'blue'
    case 'RULE':
      return 'gray'
    default:
      return 'gray'
  }
}

const readPercentage = computed(() => {
  if (!announcement.value || announcement.value.totalRecipients === 0) return 0
  return Math.round((announcement.value.readCount / announcement.value.totalRecipients) * 100)
})

async function sendComment() {
  if (!commentText.value.trim()) return

  sendingComment.value = true
  error.value = ''
  try {
    // Placeholder API call
    const { apiClient } = await import('@/api')
    await apiClient.post(
      `/condominiums/${condoId.value}/announcements/${announcementId.value}/comments`,
      { message: commentText.value },
    )
    commentText.value = ''
    success.value = 'Comentario enviado com sucesso!'
    await loadAnnouncement()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao enviar comentario.'
  } finally {
    sendingComment.value = false
  }
}

function goBack() {
  router.push(`/c/${condoId.value}/announcements`)
}
</script>

<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <!-- Back button -->
    <button
      class="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
      @click="goBack"
    >
      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      Voltar para comunicados
    </button>

    <BaseLoadingSpinner v-if="loading" text="Carregando comunicado..." />

    <template v-else-if="announcement">
      <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
        {{ success }}
      </BaseAlert>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <!-- Announcement Content -->
      <BaseCard>
        <div class="space-y-4">
          <!-- Badges -->
          <div class="flex flex-wrap gap-2">
            <BaseBadge :variant="categoryBadgeVariant(announcement.category)">
              {{ announcement.categoryLabel }}
            </BaseBadge>
            <span
              v-if="announcement.isPinned"
              class="inline-flex items-center gap-1 text-xs font-medium text-primary-600"
            >
              <svg class="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path d="M10 2a1 1 0 011 1v1.323l3.954 1.582 1.599-.8a1 1 0 01.894 1.79l-1.233.616 1.738 5.42a1 1 0 01-.285 1.05A3.989 3.989 0 0115 15a3.989 3.989 0 01-2.667-1.019 1 1 0 01-.285-1.05l1.715-5.349L11 6.477V16h2a1 1 0 110 2H7a1 1 0 110-2h2V6.477L6.237 7.582l1.715 5.349a1 1 0 01-.285 1.05A3.989 3.989 0 015 15a3.989 3.989 0 01-2.667-1.019 1 1 0 01-.285-1.05l1.738-5.42-1.233-.617a1 1 0 01.894-1.789l1.599.799L9 4.323V3a1 1 0 011-1z" />
              </svg>
              Fixado
            </span>
            <span
              v-if="announcement.isEmergency"
              class="inline-flex items-center gap-1 text-xs font-medium text-red-600"
            >
              <svg class="h-3.5 w-3.5" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
              Emergencia
            </span>
          </div>

          <!-- Title -->
          <h1 class="text-xl font-bold text-gray-900 sm:text-2xl">{{ announcement.title }}</h1>

          <!-- Author and date -->
          <div class="flex flex-wrap items-center gap-3 text-sm text-gray-500">
            <span>{{ announcement.author }}</span>
            <span class="text-gray-300">|</span>
            <span>{{ formatDateTime(announcement.createdAt) }}</span>
          </div>

          <!-- Content -->
          <div class="rounded-lg bg-gray-50 p-4 sm:p-6">
            <p class="whitespace-pre-wrap text-sm leading-relaxed text-gray-700">{{ announcement.content }}</p>
          </div>
        </div>
      </BaseCard>

      <!-- Read Receipts -->
      <BaseCard title="Confirmacao de Leitura">
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <p class="text-sm text-gray-600">
              <span class="font-medium text-gray-900">{{ announcement.readCount }}</span>
              de
              <span class="font-medium text-gray-900">{{ announcement.totalRecipients }}</span>
              moradores leram
            </p>
            <span class="text-sm font-medium text-primary-600">{{ readPercentage }}%</span>
          </div>

          <!-- Progress bar -->
          <div class="h-2 w-full overflow-hidden rounded-full bg-gray-200">
            <div
              class="h-2 rounded-full bg-primary-600 transition-all duration-500"
              :style="{ width: `${readPercentage}%` }"
            />
          </div>
        </div>
      </BaseCard>

      <!-- Comments Section -->
      <BaseCard title="Comentarios">
        <div v-if="announcement.comments.length === 0" class="py-4 text-center text-sm text-gray-500">
          Nenhum comentario ainda.
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="comment in announcement.comments"
            :key="comment.id"
            class="rounded-lg border border-gray-200 p-4"
          >
            <div class="mb-2 flex items-center justify-between">
              <span class="text-sm font-medium text-gray-900">{{ comment.author }}</span>
              <span class="text-xs text-gray-400">{{ timeAgo(comment.createdAt) }}</span>
            </div>
            <p class="text-sm text-gray-700">{{ comment.message }}</p>
          </div>
        </div>

        <!-- Comment form -->
        <div class="mt-6 border-t border-gray-200 pt-4">
          <h4 class="mb-3 text-sm font-medium text-gray-900">Adicionar Comentario</h4>

          <BaseTextarea
            v-model="commentText"
            placeholder="Escreva seu comentario..."
            :rows="3"
          />

          <div class="mt-3 flex justify-end">
            <BaseButton
              variant="primary"
              size="sm"
              :loading="sendingComment"
              :disabled="!commentText.trim()"
              @click="sendComment"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
              </svg>
              Enviar
            </BaseButton>
          </div>
        </div>
      </BaseCard>
    </template>
  </div>
</template>
