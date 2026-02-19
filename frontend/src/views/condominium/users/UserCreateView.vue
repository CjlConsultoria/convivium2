<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { userApi, condominiumApi } from '@/api'
import type { UserCreateRequest } from '@/types/user.types'
import { ROLE_LABELS } from '@/types/user.types'
import type { Unit } from '@/types/condominium.types'

const route = useRoute()
const router = useRouter()
const condoId = computed(() => Number(route.params.condoId))

const loading = ref(false)
const success = ref('')
const error = ref('')

const form = ref<UserCreateRequest>({
  name: '',
  email: '',
  password: '',
  cpf: null,
  phone: null,
  role: 'MORADOR',
  unitId: null,
})

const cpfInput = ref('')
const phoneInput = ref('')
const units = ref<Unit[]>([])

const roleOptions = computed(() =>
  Object.entries(ROLE_LABELS).map(([value, label]) => ({ value, label })),
)

const unitOptions = computed(() =>
  units.value.map((u) => ({
    value: u.id,
    label: u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier,
  })),
)

onMounted(async () => {
  await loadUnits()
})

async function loadUnits() {
  try {
    const response = await condominiumApi.listUnits(condoId.value)
    units.value = response.data
  } catch {
    units.value = [
      { id: 1, condominiumId: condoId.value, buildingId: 1, buildingName: 'Bloco A', identifier: '101', floor: 1, type: 'APARTMENT', areaSqm: null, isOccupied: false },
      { id: 2, condominiumId: condoId.value, buildingId: 1, buildingName: 'Bloco A', identifier: '201', floor: 2, type: 'APARTMENT', areaSqm: null, isOccupied: false },
      { id: 3, condominiumId: condoId.value, buildingId: 2, buildingName: 'Bloco B', identifier: '101', floor: 1, type: 'APARTMENT', areaSqm: null, isOccupied: false },
    ]
  }
}

async function handleSubmit() {
  error.value = ''
  success.value = ''

  if (!form.value.name || !form.value.email || !form.value.password) {
    error.value = 'Preencha todos os campos obrigatorios.'
    return
  }

  loading.value = true
  try {
    form.value.cpf = cpfInput.value || null
    form.value.phone = phoneInput.value || null

    await userApi.createUser(condoId.value, form.value)
    success.value = 'Usuario cadastrado com sucesso!'

    setTimeout(() => {
      router.push(`/c/${condoId.value}/users`)
    }, 1500)
  } catch (err: any) {
    error.value =
      err.response?.data?.message || 'Erro ao cadastrar usuario. Tente novamente.'
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.push(`/c/${condoId.value}/users`)
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Novo Cadastro</h1>
      <p class="mt-1 text-sm text-gray-500">Cadastre um novo morador ou funcionario</p>
    </div>

    <BaseAlert v-if="success" type="success" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseCard title="Informacoes do Usuario">
      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput
            v-model="form.name"
            label="Nome Completo"
            placeholder="Nome do morador"
            required
          />
          <BaseInput
            v-model="form.email"
            label="Email"
            type="email"
            placeholder="email@exemplo.com"
            required
          />
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput
            v-model="form.password"
            label="Senha"
            type="password"
            placeholder="Senha de acesso"
            required
          />
          <BaseInput
            v-model="cpfInput"
            label="CPF"
            placeholder="000.000.000-00"
          />
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput
            v-model="phoneInput"
            label="Telefone"
            placeholder="(11) 99999-9999"
          />
          <BaseSelect
            v-model="form.role"
            label="Cargo"
            :options="roleOptions"
            placeholder="Selecione o cargo"
            required
          />
        </div>

        <BaseSelect
          v-model="form.unitId as any"
          label="Unidade"
          :options="unitOptions"
          placeholder="Selecione a unidade"
        />

        <!-- Actions -->
        <div class="flex flex-col gap-3 border-t border-gray-200 pt-4 sm:flex-row sm:justify-end">
          <BaseButton variant="outline" type="button" @click="handleCancel">
            Cancelar
          </BaseButton>
          <BaseButton variant="primary" type="submit" :loading="loading">
            Cadastrar Usuario
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>
