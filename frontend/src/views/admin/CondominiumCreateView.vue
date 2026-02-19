<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { condominiumApi } from '@/api'
import { useViaCep } from '@/composables/useViaCep'

const router = useRouter()
const { fetchByCep } = useViaCep()

const saving = ref(false)
const error = ref('')
const cepError = ref('')

const form = ref({
  name: '',
  slug: '',
  cnpj: '',
  email: '',
  phone: '',
  addressStreet: '',
  addressNumber: '',
  addressComplement: '',
  addressNeighborhood: '',
  addressCity: '',
  addressState: '',
  addressZip: '',
})

function generateSlug(name: string): string {
  return name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}

function onNameInput() {
  form.value.slug = generateSlug(form.value.name)
}

async function onCepBlur() {
  cepError.value = ''
  const digits = (form.value.addressZip || '').replace(/\D/g, '')
  if (digits.length !== 8) return
  try {
    const addr = await fetchByCep(form.value.addressZip)
    if (!addr) {
      cepError.value = 'CEP nao encontrado.'
      return
    }
    form.value.addressStreet = addr.logradouro || form.value.addressStreet
    form.value.addressNeighborhood = addr.bairro || form.value.addressNeighborhood
    form.value.addressCity = addr.localidade || form.value.addressCity
    form.value.addressState = addr.uf || form.value.addressState
    if (addr.cep) form.value.addressZip = addr.cep.replace(/^(\d{5})(\d{3})$/, '$1-$2')
  } catch {
    cepError.value = 'Erro ao buscar CEP.'
  }
}

async function handleSubmit() {
  if (!form.value.name) {
    error.value = 'Nome do condominio e obrigatorio.'
    return
  }

  saving.value = true
  error.value = ''
  try {
    await condominiumApi.createCondominium(form.value as any)
    router.push('/admin/condominiums')
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao criar condominio.'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-3xl space-y-6">
    <!-- Back -->
    <button
      class="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700"
      @click="router.push('/admin/condominiums')"
    >
      <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
      Voltar para lista
    </button>

    <div>
      <h1 class="text-2xl font-bold text-gray-900">Novo Condominio</h1>
      <p class="mt-1 text-sm text-gray-500">Cadastre um novo condominio na plataforma</p>
    </div>

    <BaseAlert v-if="error" type="error" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <form @submit.prevent="handleSubmit">
      <!-- Basic Info -->
      <BaseCard title="Informacoes Basicas" class="mb-6">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput
            v-model="form.name"
            label="Nome do Condominio"
            placeholder="Ex: Residencial Solar"
            required
            class="sm:col-span-2"
            @input="onNameInput"
          />
          <BaseInput
            v-model="form.slug"
            label="Slug (URL)"
            placeholder="residencial-solar"
          />
          <BaseInput
            v-model="form.cnpj"
            label="CNPJ"
            placeholder="00.000.000/0001-00"
          />
          <BaseInput
            v-model="form.email"
            label="Email"
            type="email"
            placeholder="contato@condominio.com"
          />
          <BaseInput
            v-model="form.phone"
            label="Telefone"
            placeholder="(11) 3333-4444"
          />
        </div>
      </BaseCard>

      <!-- Address -->
      <BaseCard title="Endereco" class="mb-6">
        <p v-if="cepError" class="mb-2 text-sm text-amber-600">{{ cepError }}</p>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput
            v-model="form.addressZip"
            label="CEP"
            placeholder="01234-000"
            maxlength="9"
            @blur="onCepBlur"
          />
          <BaseInput
            v-model="form.addressStreet"
            label="Rua"
            placeholder="Rua das Flores"
            class="sm:col-span-2"
          />
          <BaseInput
            v-model="form.addressNumber"
            label="Numero"
            placeholder="100"
          />
          <BaseInput
            v-model="form.addressComplement"
            label="Complemento"
            placeholder="Bloco A"
          />
          <BaseInput
            v-model="form.addressNeighborhood"
            label="Bairro"
            placeholder="Jardim Primavera"
          />
          <BaseInput
            v-model="form.addressCity"
            label="Cidade"
            placeholder="Sao Paulo"
          />
          <BaseInput
            v-model="form.addressState"
            label="Estado"
            placeholder="SP"
          />
        </div>
      </BaseCard>

      <!-- Actions -->
      <div class="flex justify-end gap-3">
        <BaseButton variant="outline" @click="router.push('/admin/condominiums')">
          Cancelar
        </BaseButton>
        <BaseButton type="submit" variant="primary" :loading="saving">
          Criar Condominio
        </BaseButton>
      </div>
    </form>
  </div>
</template>
