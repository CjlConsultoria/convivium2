<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { userApi, condominiumApi } from '@/api'
import type { Unit } from '@/types/condominium.types'
import { useAuthStore } from '@/stores/auth.store'
import type { UserDetail } from '@/types/user.types'
import { ROLE_LABELS } from '@/types/user.types'
import { formatDate, formatCPF, formatPhone } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const condoId = computed(() => Number(route.params.condoId))
const userId = computed(() => Number(route.params.userId))

const loading = ref(true)
const saving = ref(false)
const isEditing = ref(false)
const success = ref('')
const error = ref('')

const user = ref<UserDetail | null>(null)

const editForm = ref({
  name: '',
  phone: '',
  unitId: null as number | null,
})

const unitsForEdit = ref<Unit[]>([])
const unitOptions = computed(() =>
  unitsForEdit.value.map((u) => ({
    value: u.id,
    label: u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier,
  })),
)
const currentUnitDisplay = computed(() => {
  if (!currentRole.value?.unitId) return null
  const u = unitsForEdit.value.find((x) => x.id === currentRole.value!.unitId)
  return u ? (u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier) : (currentRole.value?.unitIdentifier ?? null)
})

// Historico real viria da API; sem dados ficticios
const activityLog = ref<{ id: number; action: string; date: string }[]>([])

onMounted(async () => {
  await loadUser()
})

/** Backend retorna usuario "flat" (role, status, unitId no objeto). Normaliza para UserDetail com condominiumRoles. */
function normalizeUserDetail(raw: Record<string, any>, condoIdVal: number): UserDetail {
  const roles: UserDetail['condominiumRoles'] = [
    {
      id: 0,
      condominiumId: condoIdVal,
      condominiumName: '',
      role: (raw.role as UserDetail['condominiumRoles'][0]['role']) || 'MORADOR',
      unitId: raw.unitId ?? null,
      unitIdentifier: raw.unitIdentifier ?? null,
      status: raw.status || 'ACTIVE',
      approvedAt: null,
    },
  ]
  return {
    id: raw.id,
    uuid: raw.uuid ?? '',
    email: raw.email ?? '',
    name: raw.name ?? '',
    cpf: raw.cpf ?? null,
    phone: raw.phone ?? null,
    photoUrl: raw.photoUrl ?? null,
    isPlatformAdmin: raw.isPlatformAdmin ?? false,
    isActive: raw.isActive ?? true,
    emailVerified: raw.emailVerified ?? false,
    createdAt: raw.createdAt ?? '',
    condominiumRoles: roles,
  }
}

async function loadUser() {
  loading.value = true
  error.value = ''
  try {
    const response = await userApi.getUser(condoId.value, userId.value)
    const raw = response.data as Record<string, any>
    user.value = normalizeUserDetail(raw, condoId.value)
    editForm.value = {
      name: user.value.name,
      phone: user.value.phone || '',
      unitId: currentRole.value?.unitId ?? null,
    }
    await loadUnitsForEdit()
    const role = (raw.role as string) || ''
    if ((raw.status === 'PENDING_APPROVAL' || raw.status === 'PENDING') && role === 'MORADOR') {
      await loadUnitsForApprove()
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar usuario.'
    user.value = null
  } finally {
    loading.value = false
  }
}

const currentRole = computed(() => {
  if (!user.value) return null
  return user.value.condominiumRoles.find((r) => r.condominiumId === condoId.value) || null
})

const statusLabel = computed(() => {
  if (!currentRole.value) return 'Sem vinculo'
  switch (currentRole.value.status) {
    case 'ACTIVE':
      return 'Ativo'
    case 'PENDING':
    case 'PENDING_APPROVAL':
      return 'Pendente'
    case 'REJECTED':
      return 'Rejeitado'
    default:
      return currentRole.value.status
  }
})

const statusVariant = computed((): 'green' | 'yellow' | 'red' | 'gray' => {
  if (!currentRole.value) return 'gray'
  switch (currentRole.value.status) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING':
    case 'PENDING_APPROVAL':
      return 'yellow'
    case 'REJECTED':
      return 'red'
    default:
      return 'gray'
  }
})

const isPending = computed(() => currentRole.value?.status === 'PENDING' || currentRole.value?.status === 'PENDING_APPROVAL')

/** Apenas síndico ou admin da plataforma podem aprovar/rejeitar. */
const canApproveUser = computed(() => authStore.isPlatformAdmin || authStore.hasRole('SINDICO'))

/** Morador precisa de unidade ao aprovar; porteiro e outros usuários de serviço têm aprovação sem unidade. */
const isMoradorRole = computed(() => currentRole.value?.role === 'MORADOR')

const approveUnitId = ref<number | null>(null)
const unitsForApprove = ref<{ id: number; buildingName: string; identifier: string }[]>([])

async function loadUnitsForEdit() {
  try {
    const response = await condominiumApi.listUnits(condoId.value)
    unitsForEdit.value = response.data ?? []
  } catch {
    unitsForEdit.value = []
  }
}

async function loadUnitsForApprove() {
  if (!isPending.value || unitsForApprove.value.length > 0) return
  try {
    const [buildingsRes, unitsRes] = await Promise.all([
      condominiumApi.listBuildings(condoId.value),
      condominiumApi.listUnits(condoId.value),
    ])
    const buildings = buildingsRes.data ?? []
    const units = unitsRes.data ?? []
    const byBuilding = Object.fromEntries(buildings.map((b: { id: number; name: string }) => [b.id, b.name]))
    unitsForApprove.value = units.map((u: { id: number; buildingId: number; identifier: string }) => ({
      id: u.id,
      buildingName: byBuilding[u.buildingId] ?? '',
      identifier: u.identifier,
    }))
    if (currentRole.value?.unitId) approveUnitId.value = currentRole.value.unitId
    else if (unitsForApprove.value.length) approveUnitId.value = unitsForApprove.value[0].id
  } catch {
    unitsForApprove.value = []
  }
}

async function handleApprove() {
  error.value = ''
  try {
    const body = isMoradorRole.value && approveUnitId.value ? { unitId: approveUnitId.value } : undefined
    await userApi.approveUser(condoId.value, userId.value, body)
    success.value = 'Usuario aprovado com sucesso!'
    await loadUser()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao aprovar usuario.'
  }
}

async function handleReject() {
  error.value = ''
  try {
    await userApi.rejectUser(condoId.value, userId.value)
    success.value = 'Usuario rejeitado.'
    await loadUser()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao rejeitar usuario.'
  }
}

async function startEditing() {
  await loadUnitsForEdit()
  editForm.value.unitId = currentRole.value?.unitId ?? null
  isEditing.value = true
}

function cancelEditing() {
  isEditing.value = false
  if (user.value) {
    editForm.value = {
      name: user.value.name,
      phone: user.value.phone || '',
      unitId: currentRole.value?.unitId ?? null,
    }
  }
}

async function saveEditing() {
  saving.value = true
  error.value = ''
  try {
    await userApi.updateUser(condoId.value, userId.value, {
      name: editForm.value.name,
      phone: editForm.value.phone || null,
      photoUrl: null,
      unitId: editForm.value.unitId != null ? Number(editForm.value.unitId) : undefined,
    })
    success.value = 'Dados atualizados com sucesso!'
    isEditing.value = false
    await loadUser()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao atualizar dados.'
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push(`/c/${condoId.value}/users`)
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
      Voltar para lista
    </button>

    <BaseLoadingSpinner v-if="loading" text="Carregando dados do usuario..." />

    <template v-else-if="!user">
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>
      <BaseCard title="Usuario nao encontrado">
        <p class="text-sm text-gray-500">Nao foi possivel carregar os dados deste usuario. Verifique se ele pertence ao condominio.</p>
        <div class="mt-4">
          <BaseButton variant="outline" @click="loadUser">Tentar novamente</BaseButton>
        </div>
      </BaseCard>
    </template>

    <template v-else-if="user">
      <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
        {{ success }}
      </BaseAlert>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <!-- User Info Card -->
      <BaseCard title="Dados do Usuario">
        <template #header-actions>
          <BaseButton v-if="!isEditing" variant="outline" size="sm" @click="startEditing">
            <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            Editar
          </BaseButton>
        </template>

        <div class="flex flex-col gap-6 sm:flex-row">
          <!-- Photo placeholder -->
          <div class="flex shrink-0 justify-center sm:justify-start">
            <div class="flex h-24 w-24 items-center justify-center rounded-full bg-gray-200 text-2xl font-bold text-gray-500">
              {{ user.name.charAt(0).toUpperCase() }}
            </div>
          </div>

          <!-- Info -->
          <div class="flex-1 space-y-4">
            <template v-if="isEditing">
              <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <BaseInput v-model="editForm.name" label="Nome" required />
                <BaseInput v-model="editForm.phone" label="Telefone" />
                <div class="sm:col-span-2">
                  <BaseSelect
                    v-model="editForm.unitId as any"
                    label="Unidade / Bloco"
                    :options="unitOptions"
                    placeholder="Selecione a unidade (bloco - número)"
                  />
                  <p class="mt-1 text-xs text-gray-500">Altere a unidade e o bloco ao qual o usuário está vinculado.</p>
                </div>
              </div>
              <div class="flex gap-2">
                <BaseButton variant="primary" size="sm" :loading="saving" @click="saveEditing">
                  Salvar
                </BaseButton>
                <BaseButton variant="outline" size="sm" @click="cancelEditing">
                  Cancelar
                </BaseButton>
              </div>
            </template>

            <template v-else>
              <div class="grid grid-cols-1 gap-x-8 gap-y-3 sm:grid-cols-2">
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Nome</p>
                  <p class="text-sm text-gray-900">{{ user.name }}</p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Email</p>
                  <p class="text-sm text-gray-900">{{ user.email }}</p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">CPF</p>
                  <p class="text-sm text-gray-900">{{ user.cpf ? formatCPF(user.cpf) : '-' }}</p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Telefone</p>
                  <p class="text-sm text-gray-900">{{ user.phone ? formatPhone(user.phone) : '-' }}</p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Cargo</p>
                  <p class="text-sm text-gray-900">
                    {{ currentRole ? ROLE_LABELS[currentRole.role as keyof typeof ROLE_LABELS] || currentRole.role : '-' }}
                  </p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Unidade / Bloco</p>
                  <p class="text-sm text-gray-900">{{ currentUnitDisplay || currentRole?.unitIdentifier || '-' }}</p>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Status</p>
                  <BaseBadge :variant="statusVariant">{{ statusLabel }}</BaseBadge>
                </div>
                <div>
                  <p class="text-xs font-medium uppercase text-gray-500">Cadastrado em</p>
                  <p class="text-sm text-gray-900">{{ formatDate(user.createdAt) }}</p>
                </div>
              </div>
            </template>
          </div>
        </div>

        <!-- Pendente: só síndico/admin vê botões de aprovar; demais veem aviso -->
        <div v-if="isPending" class="mt-6 space-y-3 border-t border-gray-200 pt-4">
          <div v-if="canApproveUser">
            <p v-if="isMoradorRole" class="text-sm text-gray-600">
              Aprove o morador e relacione-o a uma unidade do condominio.
            </p>
            <p v-else class="text-sm text-gray-600">
              Usuarios de servico (porteiro, etc.) sao aprovados sem vincular a unidade.
            </p>
            <div class="flex flex-wrap items-end gap-3">
              <div v-if="isMoradorRole" class="min-w-[200px]">
                <label class="mb-1 block text-sm font-medium text-gray-700">Unidade</label>
                <select
                  v-model.number="approveUnitId"
                  class="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
                >
                  <option v-for="u in unitsForApprove" :key="u.id" :value="u.id">
                    {{ u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier }}
                  </option>
                </select>
              </div>
              <BaseButton variant="primary" @click="handleApprove">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
                {{ isMoradorRole ? 'Aprovar e vincular à unidade' : 'Aprovar' }}
              </BaseButton>
              <BaseButton variant="danger" @click="handleReject">
                <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
                Rejeitar
              </BaseButton>
            </div>
          </div>
          <div v-else class="rounded border border-amber-200 bg-amber-50 p-3 text-sm text-amber-800">
            Este usuário está pendente de aprovação. Apenas o síndico do condomínio (ou o administrador da plataforma) pode aprovar ou rejeitar.
          </div>
        </div>
      </BaseCard>

      <!-- Activity Log (dados reais quando houver endpoint) -->
      <BaseCard title="Historico de Atividades">
        <div v-if="activityLog.length > 0" class="divide-y divide-gray-100">
          <div
            v-for="item in activityLog"
            :key="item.id"
            class="flex items-center justify-between py-3 first:pt-0 last:pb-0"
          >
            <p class="text-sm text-gray-700">{{ item.action }}</p>
            <span class="shrink-0 text-xs text-gray-400">{{ formatDate(item.date) }}</span>
          </div>
        </div>
        <p v-else class="py-4 text-center text-sm text-gray-500">
          Nenhuma atividade registrada.
        </p>
      </BaseCard>
    </template>
  </div>
</template>
