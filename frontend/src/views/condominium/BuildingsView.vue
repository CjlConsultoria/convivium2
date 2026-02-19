<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseBadge from '@/components/base/BaseBadge.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseLoadingSpinner from '@/components/base/BaseLoadingSpinner.vue'
import { condominiumApi } from '@/api'
import type {
  BuildingPreviewItem,
  UnitPreviewItem,
} from '@/api/modules/condominium.api'
import type { Building, Unit } from '@/types/condominium.types'

const route = useRoute()
const authStore = useAuthStore()
const condoId = computed(() => Number(route.params.condoId))
const isSindico = computed(() => authStore.hasRole('SINDICO'))

const loading = ref(true)
const saving = ref(false)
const success = ref('')
const error = ref('')

const buildings = ref<Building[]>([])
const units = ref<Unit[]>([])

const showStructureGenerator = ref(true)
const structureBlocks = ref(2)
const structureUnitsPerFloor = ref(7)
const structureFloors = ref(17)
const structureIdentifierFormat = ref<'1' | '01' | '101'>('101')
const structureIdentifierStart = ref(1)
const generatingPreview = ref(false)
const applyingStructure = ref(false)

const reviewBuildings = ref<BuildingPreviewItem[]>([])
const reviewUnits = ref<UnitPreviewItem[]>([])

const structurePreview = computed(() => {
  const blocks = Math.max(1, Math.min(26, structureBlocks.value))
  const perFloor = Math.max(1, Math.min(50, structureUnitsPerFloor.value))
  const floors = Math.max(1, Math.min(100, structureFloors.value))
  const totalUnits = blocks * floors * perFloor
  const blockNames = Array.from({ length: blocks }, (_, i) => String.fromCharCode(65 + i)).join(', ')
  return { blocks, perFloor, floors, totalUnits, blockNames }
})

function reviewUnitsForBuilding(buildingName: string) {
  return reviewUnits.value.filter((u) => u.buildingName === buildingName)
}

/** Unidades do bloco agrupadas por andar para exibir em grid. */
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

const showBuildingForm = ref(false)
const newBuildingName = ref('')
const newBuildingFloors = ref<string>('')

const showUnitForm = ref(false)
const newUnitBuildingId = ref<string>('')
const newUnitIdentifier = ref('')
const newUnitFloor = ref<string>('')

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [buildingsResponse, unitsResponse] = await Promise.all([
      condominiumApi.listBuildings(condoId.value),
      condominiumApi.listUnits(condoId.value),
    ])
    buildings.value = buildingsResponse.data
    units.value = unitsResponse.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao carregar blocos e unidades.'
  } finally {
    loading.value = false
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
    const res = await condominiumApi.listBuildings(condoId.value)
    buildings.value = res.data
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
    const res = await condominiumApi.listUnits(condoId.value)
    units.value = res.data
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
    if (selectedUnit.value?.id === unit.id) selectedUnit.value = null
    success.value = 'Unidade excluída.'
    const res = await condominiumApi.listUnits(condoId.value)
    units.value = res.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao excluir unidade.'
  } finally {
    saving.value = false
  }
}

async function removeBuilding(building: Building) {
  const count = getUnitsForBuilding(building.id).length
  if (!confirm(`Excluir o bloco ${building.name} e suas ${count} unidade(s)?`)) return
  saving.value = true
  error.value = ''
  try {
    await condominiumApi.deleteBuilding(condoId.value, building.id)
    success.value = 'Bloco excluido.'
    const [bRes, uRes] = await Promise.all([
      condominiumApi.listBuildings(condoId.value),
      condominiumApi.listUnits(condoId.value),
    ])
    buildings.value = bRes.data
    units.value = uRes.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao excluir bloco.'
  } finally {
    saving.value = false
  }
}

function getUnitsForBuilding(buildingId: number): Unit[] {
  return units.value.filter((u) => u.buildingId === buildingId)
}

const selectedUnit = ref<Unit | null>(null)
function selectUnit(unit: Unit) {
  selectedUnit.value = selectedUnit.value?.id === unit.id ? null : unit
}
</script>

<template>
  <div class="mx-auto max-w-7xl space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">Blocos e Unidades</h1>
        <p class="mt-1 text-sm text-gray-500">
          {{ isSindico ? 'Gere a estrutura em lote ou cadastre blocos e unidades manualmente.' : 'Clique em uma unidade para ver detalhes e moradores.' }}
        </p>
      </div>
    </div>

    <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>
    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseLoadingSpinner v-if="loading" text="Carregando..." />

    <template v-else>
      <BaseCard v-if="isSindico" title="Gerar estrutura em lote">
        <p class="mb-4 text-sm text-gray-500">
          Defina quantos blocos, apartamentos por andar e andares. Clique em &quot;Gerar e salvar&quot; para criar. Blocos que ja existirem sao ignorados.
        </p>
        <div v-if="!showStructureGenerator" class="flex flex-wrap gap-2">
          <BaseButton variant="primary" @click="showStructureGenerator = true">
            Abrir parametros para gerar
          </BaseButton>
        </div>
        <div v-else class="rounded-lg border border-gray-200 bg-gray-50 p-4">
          <h4 class="mb-3 text-sm font-medium text-gray-900">Parametros</h4>
          <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <BaseInput v-model.number="structureBlocks" label="Quantos blocos?" type="number" min="1" max="26" placeholder="Ex: 2" />
            <BaseInput v-model.number="structureUnitsPerFloor" label="Apartamentos por andar" type="number" min="1" max="50" placeholder="Ex: 7" />
            <BaseInput v-model.number="structureFloors" label="Quantos andares por bloco?" type="number" min="1" max="100" placeholder="Ex: 17" />
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
            <strong>Preview:</strong> {{ structurePreview.blocks }} bloco(s), {{ structurePreview.floors }} andares, {{ structurePreview.perFloor }} aptos/andar. Total: <strong>{{ structurePreview.totalUnits }} unidades</strong>. Clique em &quot;Gerar preview&quot;, revise, edite ou exclua e depois &quot;Confirmar e salvar no banco&quot;.
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

      <BaseCard v-if="isSindico && reviewBuildings.length > 0" title="Revisar estrutura antes de salvar">
        <p class="mb-4 text-sm text-gray-500">
          Confira a lista. Edite identificadores, exclua itens com a lixeira. Depois clique em <strong>Confirmar e salvar no banco</strong>.
        </p>
        <div class="space-y-4">
          <div v-for="b in reviewBuildings" :key="b.name" class="rounded-lg border border-gray-200">
            <div class="flex items-center justify-between bg-gray-100 px-4 py-2">
              <span class="font-medium text-gray-900">Bloco {{ b.name }}</span>
              <button type="button" class="rounded p-1.5 text-gray-500 hover:bg-red-50 hover:text-red-600" title="Remover bloco da lista" @click="removeReviewBuilding(b.name)">
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

      <!-- Blocos: layout dois painéis lado a lado -->
      <div class="flex flex-col gap-4 lg:flex-row lg:gap-6">
        <!-- Painel esquerdo: lista de blocos e unidades (grid clicável) -->
        <div class="min-w-0 flex-1 rounded-xl border border-gray-200 bg-white shadow-sm">
          <div class="flex items-center justify-between border-b border-gray-100 px-4 py-3">
            <h2 class="text-sm font-semibold text-gray-800">Blocos e unidades</h2>
            <BaseButton v-if="isSindico && !showBuildingForm" variant="outline" size="sm" @click="showBuildingForm = true">
              + Bloco
            </BaseButton>
          </div>
          <div v-if="isSindico && showBuildingForm" class="border-b border-gray-100 bg-gray-50/80 px-4 py-3">
            <div class="grid grid-cols-2 gap-2">
              <BaseInput v-model="newBuildingName" label="Nome" placeholder="Ex: B" />
              <BaseInput v-model="newBuildingFloors" label="Andares" type="number" placeholder="Ex: 10" />
            </div>
            <div class="mt-2 flex gap-2">
              <BaseButton variant="primary" size="sm" :loading="saving" @click="addBuilding">Salvar</BaseButton>
              <BaseButton variant="outline" size="sm" @click="showBuildingForm = false">Cancelar</BaseButton>
            </div>
          </div>
          <div class="max-h-[calc(100vh-18rem)] overflow-y-auto p-4">
            <div v-if="buildings.length === 0" class="py-8 text-center text-sm text-gray-500">
              Nenhum bloco cadastrado.
            </div>
            <div v-else class="space-y-5">
              <div v-for="building in buildings" :key="building.id" class="rounded-lg border border-gray-100 bg-gray-50/50 p-3">
                <div class="mb-2 flex items-center justify-between">
                  <span class="font-medium text-gray-800">Bloco {{ building.name }}</span>
                  <button v-if="isSindico" type="button" class="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600" title="Excluir bloco" @click="removeBuilding(building)">
                    <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                  </button>
                </div>
                <div class="grid grid-cols-4 gap-1.5 sm:grid-cols-5 md:grid-cols-6">
                  <button
                    v-for="unit in getUnitsForBuilding(building.id)"
                    :key="unit.id"
                    type="button"
                    class="flex items-center justify-center rounded-lg px-2 py-2 text-sm font-medium transition-all focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-1"
                    :class="selectedUnit?.id === unit.id
                      ? 'bg-primary-600 text-white shadow ring-2 ring-primary-500 ring-offset-1'
                      : unit.isOccupied
                        ? 'bg-emerald-50 text-emerald-800 hover:bg-emerald-100'
                        : 'bg-white text-gray-600 shadow-sm hover:bg-gray-100 hover:shadow'"
                    @click="selectUnit(unit)"
                  >
                    {{ unit.identifier }}
                  </button>
                </div>
                <p v-if="getUnitsForBuilding(building.id).length === 0" class="py-2 text-xs text-gray-500">Nenhuma unidade</p>
              </div>
            </div>
          </div>
          <div v-if="isSindico" class="border-t border-gray-100 px-4 py-3">
            <BaseButton v-if="!showUnitForm" variant="outline" size="sm" @click="showUnitForm = true">+ Unidade</BaseButton>
            <div v-else class="rounded-lg border border-gray-200 bg-gray-50 p-3">
              <div class="grid grid-cols-3 gap-2">
                <select v-model="newUnitBuildingId" class="rounded-lg border border-gray-300 px-2 py-1.5 text-sm">
                  <option value="">Bloco</option>
                  <option v-for="b in buildings" :key="b.id" :value="String(b.id)">{{ b.name }}</option>
                </select>
                <input v-model="newUnitIdentifier" type="text" placeholder="Ex: 301" class="rounded-lg border border-gray-300 px-2 py-1.5 text-sm" />
                <input v-model.number="newUnitFloor" type="number" placeholder="Andar" class="rounded-lg border border-gray-300 px-2 py-1.5 text-sm" />
              </div>
              <div class="mt-2 flex gap-2">
                <BaseButton variant="primary" size="sm" :loading="saving" @click="addUnit">Salvar</BaseButton>
                <BaseButton variant="outline" size="sm" @click="showUnitForm = false">Cancelar</BaseButton>
              </div>
            </div>
          </div>
        </div>

        <!-- Painel direito: detalhes da unidade selecionada + moradores -->
        <div class="w-full shrink-0 rounded-xl border border-gray-200 bg-white shadow-sm lg:w-96">
          <div class="border-b border-gray-100 px-4 py-3">
            <h2 class="text-sm font-semibold text-gray-800">Detalhes da unidade</h2>
          </div>
          <div class="max-h-[calc(100vh-18rem)] overflow-y-auto p-4">
            <template v-if="selectedUnit">
              <div class="space-y-4">
                <div class="flex items-center justify-between rounded-lg bg-gray-50 p-4">
                  <div>
                    <p class="text-2xl font-bold text-gray-900">{{ selectedUnit.identifier }}</p>
                    <p class="text-sm text-gray-500">
                      {{ selectedUnit.buildingName ?? 'Sem bloco' }}
                      <span v-if="selectedUnit.floor"> · Andar {{ selectedUnit.floor }}</span>
                    </p>
                  </div>
                  <BaseBadge :variant="selectedUnit.isOccupied ? 'green' : 'gray'">
                    {{ selectedUnit.isOccupied ? 'Ocupada' : 'Vazia' }}
                  </BaseBadge>
                </div>
                <div v-if="isSindico" class="flex justify-end">
                  <BaseButton variant="outline" size="sm" class="text-red-600 hover:bg-red-50" @click="removeUnit(selectedUnit)">
                    Excluir unidade
                  </BaseButton>
                </div>
                <div v-if="selectedUnit.residents?.length" class="rounded-lg border border-gray-200">
                  <h3 class="border-b border-gray-100 px-4 py-2.5 text-xs font-semibold uppercase tracking-wider text-gray-500">Moradores</h3>
                  <ul class="divide-y divide-gray-100">
                    <li v-for="(r, i) in selectedUnit.residents" :key="i" class="flex items-center gap-3 px-4 py-3">
                      <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-100 text-sm font-semibold text-primary-700">
                        {{ (r.name || '?')[0].toUpperCase() }}
                      </div>
                      <div class="min-w-0 flex-1">
                        <p class="truncate font-medium text-gray-900">{{ r.name }}</p>
                        <a :href="`mailto:${r.email}`" class="truncate text-sm text-primary-600 hover:underline">{{ r.email }}</a>
                      </div>
                    </li>
                  </ul>
                </div>
                <div v-else class="rounded-lg border border-dashed border-gray-200 py-8 text-center text-sm text-gray-500">
                  Nenhum morador vinculado a esta unidade.
                </div>
              </div>
            </template>
            <div v-else class="flex flex-col items-center justify-center py-12 text-center">
              <div class="mb-3 flex h-14 w-14 items-center justify-center rounded-full bg-gray-100 text-gray-400">
                <svg class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
              </div>
              <p class="text-sm font-medium text-gray-600">Clique em uma unidade</p>
              <p class="mt-0.5 text-xs text-gray-500">para ver detalhes e moradores</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
