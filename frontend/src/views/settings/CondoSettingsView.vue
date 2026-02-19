<script setup lang="ts">
import { ref } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import { useViaCep } from '@/composables/useViaCep'

const { fetchByCep } = useViaCep()

const form = ref({
  name: 'Residencial Convivium',
  cnpj: '12.345.678/0001-90',
  email: 'contato@convivium.com.br',
  phone: '(11) 3456-7890',
  addressStreet: 'Rua das Flores',
  addressNumber: '100',
  addressNeighborhood: 'Jardim Primavera',
  addressCity: 'Sao Paulo',
  addressState: 'SP',
  addressZip: '01234-567',
})

const loading = ref(false)
const cepError = ref('')

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

async function handleSave() {
  loading.value = true
  // TODO: call API
  setTimeout(() => { loading.value = false }, 1000)
}
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Configuracoes</h1>
      <p class="text-sm text-gray-500">Configuracoes do condominio</p>
    </div>

    <BaseCard title="Dados do Condominio">
      <form class="space-y-4" @submit.prevent="handleSave">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput v-model="form.name" label="Nome" />
          <BaseInput v-model="form.cnpj" label="CNPJ" />
        </div>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseInput v-model="form.email" label="Email" type="email" />
          <BaseInput v-model="form.phone" label="Telefone" />
        </div>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <BaseInput v-model="form.addressZip" label="CEP" class="max-w-xs" @blur="onCepBlur" />
          <p v-if="cepError" class="text-sm text-amber-600 sm:col-span-2">{{ cepError }}</p>
        </div>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <BaseInput v-model="form.addressStreet" label="Rua" class="sm:col-span-2" />
          <BaseInput v-model="form.addressNumber" label="Numero" />
        </div>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <BaseInput v-model="form.addressNeighborhood" label="Bairro" />
          <BaseInput v-model="form.addressCity" label="Cidade" />
          <BaseInput v-model="form.addressState" label="UF" />
        </div>
        <div class="flex justify-end pt-4">
          <BaseButton type="submit" :loading="loading">Salvar Alteracoes</BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>
