<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { condominiumApi, userApi } from '@/api'
import type {
  BuildingPreviewItem,
  UnitPreviewItem,
} from '@/api/modules/condominium.api'
import type { Condominium, Building, Unit } from '@/types/condominium.types'
import { formatDate, formatCNPJ, formatPhone } from '@/utils/formatters'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.id))

const loading = ref(true)
const saving = ref(false)
const success = ref('')
const error = ref('')

const condominium = ref<Condominium | null>(null)
const buildings = ref<Building[]>([])
const units = ref<Unit[]>([])
const userCount = ref(0)
const plans = ref<{ id: number; name: string; priceCents: number }[]>([])
const selectedPlanId = ref<string>('')
const savingPlan = ref(false)

const activeTab = ref<'buildings' | 'residents' | 'subscription'>('buildings')

// Gerar estrutura em lote (formulario aberto por padrao para ficar visivel ao abrir a aba)
const showStructureGenerator = ref(true)
const structureBlocks = ref(2)
const structureUnitsPerFloor = ref(7)
const structureFloors = ref(17)
const structureIdentifierFormat = ref<'1' | '01' | '101'>('101')
const structureIdentifierStart = ref(1)

const structurePreview = computed(() => {
  const blocks = Math.max(1, Math.min(26, structureBlocks.value))
  const perFloor = Math.max(1, Math.min(50, structureUnitsPerFloor.value))
  const floors = Math.max(1, Math.min(100, structureFloors.value))
  const totalUnits = blocks * floors * perFloor
  const blockNames = Array.from({ length: blocks }, (_, i) => String.fromCharCode(65 + i)).join(', ')
  return {
    blocks,
    perFloor,
    floors,
    totalUnits,
    blockNames,
  }
})

// Review antes de salvar (preview da API, editavel)
const reviewBuildings = ref<BuildingPreviewItem[]>([])
const reviewUnits = ref<UnitPreviewItem[]>([])
const generatingPreview = ref(false)
const applyingStructure = ref(false)

function reviewUnitsForBuilding(buildingName: string): UnitPreviewItem[] {
  return reviewUnits.value.filter((u) => u.buildingName === buildingName)
}

/** Unidades do bloco agrupadas por andar (andar -> unidades) para exibir em grid. */
function reviewUnitsByFloor(buildingName: string): Record<number, UnitPreviewItem[]> {
  const units = reviewUnitsForBuilding(buildingName)
  const byFloor: Record<number, UnitPreviewItem[]> = {}
  for (const u of units) {
    if (!byFloor[u.floor]) byFloor[u.floor] = []
    byFloor[u.floor].push(u)
  }
  return byFloor
}

function removeReviewBuilding(buildingName: string) {
  reviewBuildings.value = reviewBuildings.value.filter((b) => b.name !== buildingName)
  reviewUnits.value = reviewUnits.value.filter((u) => u.buildingName !== buildingName)
}

function removeReviewUnit(idx: number) {
  reviewUnits.value = reviewUnits.value.filter((_, i) => i !== idx)
}

function confirmAndApply() {
  applyingStructure.value = true
  error.value = ''
  success.value = ''
  condominiumApi
    .applyStructure(condoId.value, {
      buildings: [...reviewBuildings.value],
      units: [...reviewUnits.value],
    })
    .then(() => {
      success.value = 'Estrutura salva no banco com sucesso.'
      reviewBuildings.value = []
      reviewUnits.value = []
      showStructureGenerator.value = true
      return loadData()
    })
    .catch((err: any) => {
      error.value = err.response?.data?.message || 'Erro ao salvar estrutura.'
    })
    .finally(() => {
      applyingStructure.value = false
    })
}

async function loadPreview() {
  generatingPreview.value = true
  error.value = ''
  try {
    const res = await condominiumApi.previewStructure(condoId.value, {
      blocksCount: structurePreview.value.blocks,
      unitsPerFloor: structurePreview.value.perFloor,
      floorsPerBlock: structurePreview.value.floors,
      identifierFormat: structureIdentifierFormat.value,
      identifierStart: Math.max(0, Math.min(999, structureIdentifierStart.value ?? 1)),
    })
    reviewBuildings.value = res.data?.buildings ?? []
    reviewUnits.value = res.data?.units ?? []
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao gerar preview.'
  } finally {
    generatingPreview.value = false
  }
}

// Building form
const showBuildingForm = ref(false)
const newBuildingName = ref('')
const newBuildingFloors = ref<string>('')

// Unit form
const showUnitForm = ref(false)
const newUnitBuildingId = ref<string>('')
const newUnitIdentifier = ref('')
const newUnitFloor = ref<string>('')

onMounted(async () => {
  await loadData()
})

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const condoResponse = await condominiumApi.getCondominium(condoId.value)
    condominium.value = condoResponse.data
    try {
      const [buildingsResponse, unitsResponse, usersResponse] = await Promise.all([
        condominiumApi.listBuildings(condoId.value),
        condominiumApi.listUnits(condoId.value),
        userApi.listUsers(condoId.value, { page: 0, size: 1 }).catch(() => ({ data: { totalElements: 0 } })),
      ])
      buildings.value = buildingsResponse.data
      units.value = unitsResponse.data
      userCount.value = usersResponse.data?.totalElements ?? 0
    } catch (errInner: any) {
      buildings.value = []
      units.value = []
      userCount.value = 0
      error.value = errInner.response?.data?.message || 'Blocos/unidades nao carregados. Voce ainda pode acessar a gestao de moradores para criar o sindico.'
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar condominio.'
    condominium.value = null
  } finally {
    loading.value = false
  }
}

function statusBadgeVariant(status: string): 'green' | 'yellow' | 'red' | 'gray' {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'PENDING':
      return 'yellow'
    case 'SUSPENDED':
      return 'red'
    default:
      return 'gray'
  }
}

function statusLabel(status: string): string {
  switch (status) {
    case 'ACTIVE':
      return 'Ativo'
    case 'PENDING':
      return 'Pendente'
    case 'SUSPENDED':
      return 'Suspenso'
    case 'DEACTIVATED':
      return 'Desativado'
    default:
      return status
  }
}

async function changeStatus(newStatus: string) {
  saving.value = true
  error.value = ''
  try {
    await condominiumApi.updateCondominiumStatus(condoId.value, newStatus)
    success.value = 'Status atualizado com sucesso!'
    await loadData()
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao atualizar status.'
  } finally {
    saving.value = false
  }
}

async function addBuilding() {
  if (!newBuildingName.value) return

  saving.value = true
  error.value = ''
  try {
    await condominiumApi.createBuilding(condoId.value, {
      name: newBuildingName.value,
      floors: newBuildingFloors.value ? Number(newBuildingFloors.value) : null,
    })
    newBuildingName.value = ''
    newBuildingFloors.value = ''
    showBuildingForm.value = false
    success.value = 'Bloco adicionado com sucesso!'
    const buildingsResponse = await condominiumApi.listBuildings(condoId.value)
    buildings.value = buildingsResponse.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao adicionar bloco.'
  } finally {
    saving.value = false
  }
}

async function addUnit() {
  if (!newUnitIdentifier.value) return

  saving.value = true
  error.value = ''
  try {
    await condominiumApi.createUnit(condoId.value, {
      buildingId: newUnitBuildingId.value ? Number(newUnitBuildingId.value) : null,
      identifier: newUnitIdentifier.value,
      floor: newUnitFloor.value ? Number(newUnitFloor.value) : null,
      type: 'APARTMENT',
      areaSqm: null,
    })
    newUnitBuildingId.value = ''
    newUnitIdentifier.value = ''
    newUnitFloor.value = ''
    showUnitForm.value = false
    success.value = 'Unidade adicionada com sucesso!'
    const unitsResponse = await condominiumApi.listUnits(condoId.value)
    units.value = unitsResponse.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao adicionar unidade.'
  } finally {
    saving.value = false
  }
}

async function removeUnit(unit: Unit) {
  if (!confirm(`Excluir a unidade ${unit.identifier} (Andar ${unit.floor ?? '-'})?`)) return
  saving.value = true
  error.value = ''
  try {
    await condominiumApi.deleteUnit(condoId.value, unit.id)
    success.value = 'Unidade excluida.'
    const unitsResponse = await condominiumApi.listUnits(condoId.value)
    units.value = unitsResponse.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao excluir unidade.'
  } finally {
    saving.value = false
  }
}

async function removeBuilding(building: Building) {
  const count = getUnitsForBuilding(building.id).length
  if (!confirm(`Excluir o bloco ${building.name} e suas ${count} unidade(s)? Nao e possivel se houver moradores vinculados.`)) return
  saving.value = true
  error.value = ''
  try {
    await condominiumApi.deleteBuilding(condoId.value, building.id)
    success.value = 'Bloco excluido.'
    const [buildingsResponse, unitsResponse] = await Promise.all([
      condominiumApi.listBuildings(condoId.value),
      condominiumApi.listUnits(condoId.value),
    ])
    buildings.value = buildingsResponse.data
    units.value = unitsResponse.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao excluir bloco.'
  } finally {
    saving.value = false
  }
}

function getUnitsForBuilding(buildingId: number): Unit[] {
  return units.value.filter((u) => u.buildingId === buildingId)
}

function fullAddress(): string {
  if (!condominium.value) return '-'
  const c = condominium.value
  const parts = [c.addressStreet, c.addressNumber, c.addressComplement, c.addressNeighborhood].filter(Boolean)
  const cityState = [c.addressCity, c.addressState].filter(Boolean).join('/')
  if (cityState) parts.push(cityState)
  if (c.addressZip) parts.push(`CEP: ${c.addressZip}`)
  return parts.join(', ') || '-'
}

function goBack() {
  router.push('/admin/condominiums')
}

async function loadPlans() {
  try {
    const res = await condominiumApi.listPlans()
    plans.value = (res.data || []).map((p) => ({ id: p.id, name: p.name, priceCents: p.priceCents }))
  } catch {
    plans.value = []
  }
}

function onSubscriptionTabEnter() {
  if (plans.value.length === 0) loadPlans()
  selectedPlanId.value = condominium.value?.planId != null ? String(condominium.value.planId) : ''
}

async function savePlan() {
  if (!condominium.value) return
  savingPlan.value = true
  error.value = ''
  const planId = selectedPlanId.value ? Number(selectedPlanId.value) : null
  try {
    const res = await condominiumApi.setCondominiumPlan(condominium.value.id, planId)
    condominium.value = res.data
    success.value = 'Plano atualizado com sucesso.'
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao atualizar plano.'
  } finally {
    savingPlan.value = false
  }
}

const planOptions = computed(() => [
  { value: '', label: 'Nenhum plano' },
  ...plans.value.map((p) => ({ value: String(p.id), label: `${p.name} (R$ ${(p.priceCents / 100).toFixed(2)}/mês)` })),
])
</script>

<template>
  <div class="mx-auto max-w-4xl space-y-6">
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

    <BaseLoadingSpinner v-if="loading" text="Carregando condominio..." />

    <template v-else-if="!condominium && error">
      <BaseAlert type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>
      <BaseCard title="Acao possivel">
        <p class="text-sm text-gray-500">
          Voce pode acessar o painel do condominio para criar o primeiro usuario (sindico) mesmo assim.
        </p>
        <div class="mt-4">
          <BaseButton variant="primary" @click="router.push(`/c/${condoId}/users/new`)">
            Criar usuario do condominio (Sindico)
          </BaseButton>
        </div>
      </BaseCard>
    </template>

    <template v-else-if="condominium">
      <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
        {{ success }}
      </BaseAlert>
      <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>

      <!-- Info Card -->
      <BaseCard title="Informacoes do Condominio">
        <template #header-actions>
          <BaseBadge :variant="statusBadgeVariant(condominium.status)">
            {{ statusLabel(condominium.status) }}
          </BaseBadge>
        </template>

        <div class="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Nome</p>
            <p class="text-sm text-gray-900">{{ condominium.name }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">CNPJ</p>
            <p class="text-sm text-gray-900">{{ condominium.cnpj ? formatCNPJ(condominium.cnpj) : '-' }}</p>
          </div>
          <div class="sm:col-span-2">
            <p class="text-xs font-medium uppercase text-gray-500">Endereco</p>
            <p class="text-sm text-gray-900">{{ fullAddress() }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Email</p>
            <p class="text-sm text-gray-900">{{ condominium.email || '-' }}</p>
          </div>
          <div>
            <p class="text-xs font-medium uppercase text-gray-500">Telefone</p>
            <p class="text-sm text-gray-900">{{ condominium.phone ? formatPhone(condominium.phone) : '-' }}</p>
          </div>
        </div>

        <!-- Status change buttons -->
        <div class="mt-6 flex flex-wrap gap-2 border-t border-gray-200 pt-4">
          <BaseButton
            v-if="condominium.status !== 'ACTIVE'"
            variant="primary"
            size="sm"
            :loading="saving"
            @click="changeStatus('ACTIVE')"
          >
            Ativar
          </BaseButton>
          <BaseButton
            v-if="condominium.status !== 'SUSPENDED'"
            variant="danger"
            size="sm"
            :loading="saving"
            @click="changeStatus('SUSPENDED')"
          >
            Suspender
          </BaseButton>
          <BaseButton
            v-if="condominium.status !== 'DEACTIVATED'"
            variant="outline"
            size="sm"
            :loading="saving"
            @click="changeStatus('DEACTIVATED')"
          >
            Desativar
          </BaseButton>
        </div>
      </BaseCard>

      <!-- Tabs -->
      <div class="border-b border-gray-200">
        <nav class="-mb-px flex gap-4 overflow-x-auto" aria-label="Tabs">
          <button
            class="whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors"
            :class="
              activeTab === 'buildings'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
            "
            @click="activeTab = 'buildings'"
          >
            Blocos & Unidades
          </button>
          <button
            class="whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors"
            :class="
              activeTab === 'residents'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
            "
            @click="activeTab = 'residents'"
          >
            Moradores
          </button>
          <button
            class="whitespace-nowrap border-b-2 px-1 py-3 text-sm font-medium transition-colors"
            :class="
              activeTab === 'subscription'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
            "
            @click="activeTab = 'subscription'; onSubscriptionTabEnter()"
          >
            Assinatura
          </button>
        </nav>
      </div>

      <!-- Buildings & Units Tab -->
      <template v-if="activeTab === 'buildings'">
        <!-- Gerar estrutura em lote -->
        <BaseCard title="Gerar estrutura em lote">
          <p class="mb-4 text-sm text-gray-500">
            Defina quantos blocos, apartamentos por andar e andares. Clique em &quot;Gerar e salvar&quot; para criar. Blocos que ja existirem sao ignorados. Revise a lista abaixo apos gerar.
          </p>
          <div v-if="!showStructureGenerator" class="flex flex-wrap gap-2">
            <BaseButton variant="primary" @click="showStructureGenerator = true">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
              Abrir parametros para gerar
            </BaseButton>
          </div>
          <div v-else class="rounded-lg border border-gray-200 bg-gray-50 p-4">
            <h4 class="mb-3 text-sm font-medium text-gray-900">Parametros</h4>
            <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <BaseInput
                v-model.number="structureBlocks"
                label="Quantos blocos?"
                type="number"
                min="1"
                max="26"
                placeholder="Ex: 2"
              />
              <BaseInput
                v-model.number="structureUnitsPerFloor"
                label="Apartamentos por andar"
                type="number"
                min="1"
                max="50"
                placeholder="Ex: 7"
              />
              <BaseInput
                v-model.number="structureFloors"
                label="Quantos andares por bloco?"
                type="number"
                min="1"
                max="100"
                placeholder="Ex: 17"
              />
              <div class="sm:col-span-2">
                <label class="mb-1 block text-sm font-medium text-gray-700">Formato do identificador</label>
                <select
                  v-model="structureIdentifierFormat"
                  class="w-full rounded border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
                >
                  <option value="101">101, 102... (andar + numero)</option>
                  <option value="01">01, 02... (sequencial 2 digitos)</option>
                  <option value="1">1, 2, 3... (sequencial simples)</option>
                </select>
              </div>
              <div>
                <label class="mb-1 block text-sm font-medium text-gray-700">Comecar no numero</label>
                <BaseInput
                  v-model.number="structureIdentifierStart"
                  type="number"
                  min="0"
                  max="999"
                  placeholder="Ex: 1 ou 11"
                />
                <p class="mt-0.5 text-xs text-gray-500">Ex.: 1 (1,2,3...), 11 (11,12...) ou 101 no formato andar+numero</p>
              </div>
            </div>
            <div class="mt-4 rounded border border-primary-200 bg-primary-50 p-3 text-sm text-gray-700">
              <strong>Preview:</strong> Serao listados <strong>{{ structurePreview.blocks }} bloco(s)</strong> ({{ structurePreview.blockNames }}), <strong>{{ structurePreview.floors }} andares</strong>, <strong>{{ structurePreview.perFloor }} aptos/andar</strong>. Total: <strong>{{ structurePreview.totalUnits }} unidades</strong>. Clique em &quot;Gerar preview&quot; para ver a lista, revisar, editar ou excluir itens e depois &quot;Confirmar e salvar&quot;.
            </div>
            <div class="mt-4 flex flex-wrap gap-2">
              <BaseButton variant="primary" :loading="generatingPreview" @click="loadPreview">
                Gerar preview (revisar antes de salvar)
              </BaseButton>
              <BaseButton variant="outline" :disabled="generatingPreview" @click="showStructureGenerator = false">
                Cancelar
              </BaseButton>
            </div>
          </div>
        </BaseCard>

        <!-- Revisao: lista gerada para confirmar, editar ou excluir antes de salvar -->
        <BaseCard v-if="reviewBuildings.length > 0" title="Revisar estrutura antes de salvar">
          <p class="mb-4 text-sm text-gray-500">
            Confira a lista abaixo. Edite o identificador da unidade se quiser, exclua itens com a lixeira. Quando estiver tudo certo, clique em <strong>Confirmar e salvar no banco</strong>.
          </p>
          <div class="space-y-4">
            <div
              v-for="b in reviewBuildings"
              :key="b.name"
              class="rounded-lg border border-gray-200"
            >
              <div class="flex items-center justify-between bg-gray-100 px-4 py-2">
                <span class="font-medium text-gray-900">Bloco {{ b.name }}</span>
                <button
                  type="button"
                  class="rounded p-1.5 text-gray-500 hover:bg-red-50 hover:text-red-600"
                  title="Remover bloco da lista"
                  @click="removeReviewBuilding(b.name)"
                >
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
              <div class="space-y-4 p-4">
                <template v-for="(floorNum) in (b.floors || 0)" :key="`${b.name}-floor-${floorNum}`">
                  <div v-if="reviewUnitsByFloor(b.name)[floorNum]?.length" class="space-y-2">
                    <span class="text-sm font-medium text-gray-600">Andar {{ floorNum }}</span>
                    <div class="grid grid-cols-4 gap-2 sm:grid-cols-6 md:grid-cols-8">
                      <div
                        v-for="(unit, ui) in reviewUnitsByFloor(b.name)[floorNum]"
                        :key="`${b.name}-${unit.floor}-${unit.identifier}-${ui}`"
                        class="flex items-center gap-1 rounded border border-gray-100 bg-white p-1.5"
                      >
                        <input
                          v-model="unit.identifier"
                          type="text"
                          class="min-w-0 flex-1 rounded border border-gray-300 px-2 py-1 text-sm"
                          placeholder="101"
                        />
                        <button
                          type="button"
                          class="shrink-0 rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
                          title="Remover unidade da lista"
                          @click="removeReviewUnit(reviewUnits.findIndex((u) => u === unit))"
                        >
                          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                          </svg>
                        </button>
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
          <div class="mt-4 flex flex-wrap gap-2 border-t border-gray-200 pt-4">
            <BaseButton variant="primary" :loading="applyingStructure" @click="confirmAndApply">
              Confirmar e salvar no banco
            </BaseButton>
            <BaseButton variant="outline" :disabled="applyingStructure" @click="reviewBuildings = []; reviewUnits = []">
              Descartar preview
            </BaseButton>
          </div>
        </BaseCard>

        <!-- Revisão: lista de blocos e unidades (editar/excluir aqui) -->
        <BaseCard title="Blocos">
          <p class="mb-3 text-sm text-gray-500">
            Aqui voce revisa, confirma e pode excluir blocos ou unidades. Cada bloco lista suas unidades; use o botao de lixeira para excluir. Unidade em uso por morador nao pode ser excluida.
          </p>
          <template #header-actions>
            <BaseButton
              v-if="!showBuildingForm"
              variant="outline"
              size="sm"
              @click="showBuildingForm = true"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Adicionar Bloco
            </BaseButton>
          </template>

          <!-- Building form -->
          <div v-if="showBuildingForm" class="mb-4 rounded-lg border border-gray-200 bg-gray-50 p-4">
            <h4 class="mb-3 text-sm font-medium text-gray-900">Novo Bloco</h4>
            <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <BaseInput v-model="newBuildingName" label="Nome do Bloco" placeholder="Ex: Bloco C" required />
              <BaseInput v-model="newBuildingFloors" label="Numero de Andares" type="number" placeholder="Ex: 10" />
            </div>
            <div class="mt-3 flex gap-2">
              <BaseButton variant="primary" size="sm" :loading="saving" @click="addBuilding">Salvar</BaseButton>
              <BaseButton variant="outline" size="sm" @click="showBuildingForm = false">Cancelar</BaseButton>
            </div>
          </div>

          <!-- Buildings list -->
          <div class="space-y-4">
            <div
              v-for="building in buildings"
              :key="building.id"
              class="rounded-lg border border-gray-200"
            >
              <div class="flex items-center justify-between bg-gray-50 px-4 py-3">
                <div>
                  <h4 class="text-sm font-medium text-gray-900">{{ building.name }}</h4>
                  <p class="text-xs text-gray-500">
                    {{ building.floors ? `${building.floors} andares` : '' }} - {{ getUnitsForBuilding(building.id).length }} unidades
                  </p>
                </div>
                <button
                  type="button"
                  class="rounded p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-600"
                  title="Excluir bloco"
                  @click="removeBuilding(building)"
                >
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
              <div class="divide-y divide-gray-100">
                <div
                  v-for="unit in getUnitsForBuilding(building.id)"
                  :key="unit.id"
                  class="flex items-center justify-between px-4 py-2"
                >
                  <div class="flex items-center gap-3">
                    <span class="text-sm font-medium text-gray-900">{{ unit.identifier }}</span>
                    <span v-if="unit.floor" class="text-xs text-gray-500">Andar {{ unit.floor }}</span>
                  </div>
                  <div class="flex items-center gap-2">
                    <BaseBadge :variant="unit.isOccupied ? 'green' : 'gray'">
                      {{ unit.isOccupied ? 'Ocupada' : 'Vazia' }}
                    </BaseBadge>
                    <button
                      type="button"
                      class="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
                      title="Excluir unidade"
                      @click="removeUnit(unit)"
                    >
                      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </div>
                <div v-if="getUnitsForBuilding(building.id).length === 0" class="px-4 py-3 text-sm text-gray-500">
                  Nenhuma unidade cadastrada.
                </div>
              </div>
            </div>

            <div v-if="buildings.length === 0" class="py-4 text-center text-sm text-gray-500">
              Nenhum bloco cadastrado.
            </div>
          </div>

          <!-- Add Unit -->
          <div class="mt-4 border-t border-gray-200 pt-4">
            <BaseButton
              v-if="!showUnitForm"
              variant="outline"
              size="sm"
              @click="showUnitForm = true"
            >
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
              </svg>
              Adicionar Unidade
            </BaseButton>

            <div v-if="showUnitForm" class="rounded-lg border border-gray-200 bg-gray-50 p-4">
              <h4 class="mb-3 text-sm font-medium text-gray-900">Nova Unidade</h4>
              <div class="grid grid-cols-1 gap-3 sm:grid-cols-3">
                <div>
                  <label class="label">Bloco</label>
                  <select v-model="newUnitBuildingId" class="input-field">
                    <option value="">Sem bloco</option>
                    <option v-for="b in buildings" :key="b.id" :value="String(b.id)">{{ b.name }}</option>
                  </select>
                </div>
                <BaseInput v-model="newUnitIdentifier" label="Identificador" placeholder="Ex: 301" required />
                <BaseInput v-model="newUnitFloor" label="Andar" type="number" placeholder="Ex: 3" />
              </div>
              <div class="mt-3 flex gap-2">
                <BaseButton variant="primary" size="sm" :loading="saving" @click="addUnit">Salvar</BaseButton>
                <BaseButton variant="outline" size="sm" @click="showUnitForm = false">Cancelar</BaseButton>
              </div>
            </div>
          </div>
        </BaseCard>
      </template>

      <!-- Residents Tab -->
      <template v-if="activeTab === 'residents'">
        <BaseCard :title="userCount === 0 ? 'Moradores e primeiro sindico' : 'Moradores'">
          <p class="text-sm text-gray-500">
            <template v-if="userCount === 0">
              Crie o primeiro usuario (sindico) e outros moradores no painel do condominio. Clique abaixo para abrir a lista de usuarios e adicionar o sindico.
            </template>
            <template v-else>
              Gerencie os moradores e o sindico no painel do condominio. Clique abaixo para ver a lista ou adicionar novos usuarios.
            </template>
          </p>
          <div class="mt-4 flex flex-wrap gap-3">
            <BaseButton variant="primary" @click="router.push(`/c/${condoId}/users/new`)">
              Criar usuario (Sindico / Morador)
            </BaseButton>
            <BaseButton variant="outline" @click="router.push(`/c/${condoId}/users`)">
              Ver lista de moradores
            </BaseButton>
          </div>
        </BaseCard>
      </template>

      <!-- Subscription Tab -->
      <template v-if="activeTab === 'subscription'">
        <BaseCard title="Assinatura e plano">
          <p class="mb-4 text-sm text-gray-500">
            Associe um plano ao condominio. Se o condominio deixar de pagar a fatura, use &quot;Suspender&quot; no card de informacoes acima: o acesso do condominio e de todos os moradores sera bloqueado ate a regularizacao.
          </p>
          <div class="grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
            <div>
              <p class="text-xs font-medium uppercase text-gray-500">Plano atual</p>
              <p class="text-sm font-medium text-gray-900">{{ condominium.planName || 'Nenhum plano associado' }}</p>
            </div>
            <div>
              <p class="text-xs font-medium uppercase text-gray-500">Status</p>
              <BaseBadge :variant="condominium.status === 'ACTIVE' ? 'green' : condominium.status === 'SUSPENDED' ? 'red' : 'gray'">
                {{ condominium.status === 'ACTIVE' ? 'Em dia' : condominium.status === 'SUSPENDED' ? 'Suspenso' : condominium.status }}
              </BaseBadge>
            </div>
          </div>
          <div class="mt-6 flex flex-wrap items-end gap-3 border-t border-gray-200 pt-4">
            <div class="min-w-[200px]">
              <label class="label">Alterar plano</label>
              <select
                v-model="selectedPlanId"
                class="input-field"
              >
                <option v-for="opt in planOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
            </div>
            <BaseButton variant="primary" size="sm" :loading="savingPlan" @click="savePlan">
              Salvar plano
            </BaseButton>
          </div>
        </BaseCard>
      </template>
    </template>
  </div>
</template>
