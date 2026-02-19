<script setup lang="ts">
import { ref, watch } from 'vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import { authApi } from '@/api'
import type { CondominiumOption, UnitOption } from '@/api/modules/auth.api'

const form = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  cpf: '',
  phone: '',
  condominiumId: '' as string | number,
  unitId: '' as string | number,
})
const condominiums = ref<CondominiumOption[]>([])
const units = ref<UnitOption[]>([])
const loadingCondominiums = ref(false)
const loadingUnits = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref(false)

const condoOptions = ref<{ value: number; label: string }[]>([])
const unitOptions = ref<{ value: number; label: string }[]>([])

async function loadCondominiums() {
  loadingCondominiums.value = true
  try {
    const res = await authApi.listCondominiumsForRegistration()
    const list = res.data ?? []
    condominiums.value = list
    condoOptions.value = list.map((c) => ({ value: c.id, label: c.name }))
  } catch {
    condoOptions.value = []
  } finally {
    loadingCondominiums.value = false
  }
}

async function loadUnits(condominiumId: number) {
  if (!condominiumId) {
    units.value = []
    unitOptions.value = []
    form.value.unitId = ''
    return
  }
  loadingUnits.value = true
  try {
    const res = await authApi.listUnitsForRegistration(condominiumId)
    const list = res.data ?? []
    units.value = list
    unitOptions.value = list.map((u) => ({
      value: u.id,
      label: u.buildingName ? `${u.buildingName} - ${u.identifier}` : u.identifier,
    }))
  } catch {
    unitOptions.value = []
  } finally {
    form.value.unitId = ''
    loadingUnits.value = false
  }
}

loadCondominiums()

watch(
  () => form.value.condominiumId,
  (id) => {
    const num = id === '' || id === null ? 0 : Number(id)
    if (num) loadUnits(num)
    else {
      units.value = []
      unitOptions.value = []
      form.value.unitId = ''
    }
  },
)

async function handleRegister() {
  error.value = ''
  if (form.value.password !== form.value.confirmPassword) {
    error.value = 'As senhas nao conferem.'
    return
  }
  const condoId = Number(form.value.condominiumId)
  const unitId = Number(form.value.unitId)
  if (!condoId || !unitId) {
    error.value = 'Selecione o condominio e a unidade.'
    return
  }
  loading.value = true
  try {
    await authApi.register({
      name: form.value.name,
      email: form.value.email,
      password: form.value.password,
      cpf: form.value.cpf,
      phone: form.value.phone,
      condominiumId: condoId,
      unitId,
    })
    success.value = true
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao registrar. Tente novamente.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900">Criar conta</h2>
    <p class="mt-1 text-sm text-gray-500">
      Cadastre-se como morador. Quem cria o condominio e o administrador (sindico); moradores entram por aqui ou sao cadastrados pelo sindico.
    </p>

    <BaseAlert v-if="success" type="success" class="mt-4">
      Cadastro realizado com sucesso! Aguarde a aprovacao do sindico para acessar o sistema.
      <router-link to="/login" class="font-medium underline">Voltar ao login</router-link>
    </BaseAlert>

    <BaseAlert v-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <form v-if="!success" class="mt-6 space-y-4" @submit.prevent="handleRegister">
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <BaseInput v-model="form.name" label="Nome completo" placeholder="Seu nome" required />
        <BaseInput v-model="form.email" label="Email" type="email" placeholder="seu@email.com" required />
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <BaseInput v-model="form.cpf" label="CPF" placeholder="000.000.000-00" />
        <BaseInput v-model="form.phone" label="Telefone" placeholder="(11) 99999-9999" />
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <BaseInput v-model="form.password" label="Senha" type="password" placeholder="Minimo 6 caracteres" required />
        <BaseInput v-model="form.confirmPassword" label="Confirmar senha" type="password" placeholder="Repita a senha" required />
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <BaseSelect
          v-model="form.condominiumId"
          label="Condominio"
          :options="condoOptions"
          placeholder="Selecione o condominio"
          required
          :disabled="loadingCondominiums"
        />
        <BaseSelect
          v-model="form.unitId"
          label="Unidade"
          :options="unitOptions"
          placeholder="Selecione a unidade"
          required
          :disabled="loadingUnits || !form.condominiumId"
        />
      </div>
      <p v-if="!loadingCondominiums && condoOptions.length === 0" class="text-sm text-amber-600">
        Nenhum condominio ativo disponivel para cadastro. O condominio e criado pelo administrador da plataforma.
      </p>
      <p v-else-if="form.condominiumId && !loadingUnits && unitOptions.length === 0" class="text-sm text-amber-600">
        Nenhuma unidade cadastrada neste condominio. O administrador deve cadastrar blocos e unidades no painel.
      </p>

      <BaseButton type="submit" variant="primary" class="w-full" :loading="loading">
        Cadastrar
      </BaseButton>
    </form>

    <p class="mt-6 text-center text-sm text-gray-500">
      Ja tem conta?
      <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-500">
        Fazer login
      </router-link>
    </p>
  </div>
</template>
