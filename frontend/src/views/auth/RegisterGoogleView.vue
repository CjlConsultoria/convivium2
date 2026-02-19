<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import { authApi } from '@/api'
import type { CondominiumOption, UnitOption } from '@/api/modules/auth.api'
import { useAuthStore } from '@/stores/auth.store'
import { useTenantStore } from '@/stores/tenant.store'

const router = useRouter()
const authStore = useAuthStore()
const tenantStore = useTenantStore()

const googleData = ref<{ idToken: string; email: string; name: string; picture?: string } | null>(null)
const form = ref({
  condominiumId: '' as string | number,
  unitId: '' as string | number,
  phone: '',
})
const condominiums = ref<CondominiumOption[]>([])
const units = ref<UnitOption[]>([])
const condoOptions = ref<{ value: number; label: string }[]>([])
const unitOptions = ref<{ value: number; label: string }[]>([])
const loadingCondominiums = ref(false)
const loadingUnits = ref(false)
const loading = ref(false)
const error = ref('')
const success = ref('')
const alreadyPending = ref(false)
const pendingMessage = ref('')
const checkingPending = ref(false)

onMounted(() => {
  try {
    const raw = sessionStorage.getItem('google_signup')
    if (!raw) {
      router.replace('/login')
      return
    }
    const data = JSON.parse(raw)
    if (!data?.idToken || !data?.email) {
      router.replace('/login')
      return
    }
    googleData.value = {
      idToken: data.idToken,
      email: data.email,
      name: data.name ?? data.email,
      picture: data.picture,
    }
  } catch {
    router.replace('/login')
  }
})

async function checkIfAlreadyPending() {
  if (!googleData.value?.idToken || alreadyPending.value || checkingPending.value) return
  checkingPending.value = true
  try {
    const res = await authApi.checkPendingRegistration(googleData.value.idToken)
    const data = res.data
    if (data?.hasPendingApproval && data?.message) {
      alreadyPending.value = true
      pendingMessage.value = data.message
    }
  } catch {
    // em caso de erro, deixa mostrar o formulário normalmente
  } finally {
    checkingPending.value = false
  }
}

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

watch(
  () => googleData.value,
  async (data) => {
    if (data) {
      await checkIfAlreadyPending()
      if (!alreadyPending.value) loadCondominiums()
    }
  },
  { immediate: true },
)

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

async function handleSubmit() {
  error.value = ''
  const condoId = Number(form.value.condominiumId)
  const unitId = Number(form.value.unitId)
  if (!googleData.value?.idToken) {
    error.value = 'Sessão expirada. Faça login com Google novamente.'
    return
  }
  if (!condoId || !unitId) {
    error.value = 'Selecione o condomínio e a unidade.'
    return
  }
  loading.value = true
  try {
    const result = await authStore.completeGoogleRegistration({
      idToken: googleData.value.idToken,
      condominiumId: condoId,
      unitId,
      phone: form.value.phone || undefined,
    })
    sessionStorage.removeItem('google_signup')
    if (result.needsApproval) {
      success.value = result.message ?? 'Cadastro enviado! Aguarde a aprovação do síndico ou administrador para acessar o condomínio.'
      setTimeout(() => router.push('/login'), 3000)
      return
    }
    const condoIdRedirect = authStore.user?.condominiumRoles?.[0]?.condominiumId
    if (condoIdRedirect) {
      await tenantStore.setCondominium(condoIdRedirect)
      router.push(`/c/${condoIdRedirect}`)
    } else {
      router.push('/login')
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Erro ao concluir cadastro. Tente novamente.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div>
    <h2 class="text-2xl font-bold text-gray-900">Completar cadastro</h2>
    <p v-if="!alreadyPending" class="mt-1 text-sm text-gray-500">
      Você entrou com o Google. Informe o condomínio e a unidade onde mora para concluir.
    </p>

    <template v-if="googleData">
      <div class="mt-4 flex items-center gap-4 rounded-lg border border-gray-200 bg-gray-50 p-4">
        <img
          v-if="googleData.picture"
          :src="googleData.picture"
          :alt="googleData.name"
          class="h-12 w-12 rounded-full"
        />
        <div class="min-w-0 flex-1">
          <p class="font-medium text-gray-900">{{ googleData.name }}</p>
          <p class="truncate text-sm text-gray-500">{{ googleData.email }}</p>
        </div>
      </div>

      <BaseAlert v-if="checkingPending" type="info" class="mt-4">
        Verificando...
      </BaseAlert>
      <BaseAlert v-else-if="alreadyPending" type="warning" class="mt-4">
        {{ pendingMessage }}
        <p class="mt-2 text-sm opacity-90">Não é necessário preencher o formulário novamente. Aguarde a aprovação ou entre em contato com o síndico.</p>
      </BaseAlert>
      <BaseAlert v-else-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
        {{ error }}
      </BaseAlert>
      <BaseAlert v-else-if="success" type="success" class="mt-4">
        {{ success }}
        <span class="block text-sm opacity-90">Redirecionando para o login...</span>
      </BaseAlert>

      <form v-if="!success && !alreadyPending && !checkingPending" class="mt-6 space-y-4" @submit.prevent="handleSubmit">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <BaseSelect
            v-model="form.condominiumId"
            label="Condomínio"
            :options="condoOptions"
            placeholder="Selecione o condomínio"
            required
            :disabled="loadingCondominiums"
          />
          <BaseSelect
            v-model="form.unitId"
            label="Unidade / Apartamento"
            :options="unitOptions"
            placeholder="Selecione a unidade"
            required
            :disabled="loadingUnits || !form.condominiumId"
          />
        </div>
        <BaseInput
          v-model="form.phone"
          label="Telefone (opcional)"
          placeholder="(11) 99999-9999"
        />
        <p class="text-sm text-gray-500">
          O telefone é usado para receber avisos de encomendas no WhatsApp.
        </p>
        <BaseButton type="submit" variant="primary" class="w-full" :loading="loading">
          Concluir cadastro
        </BaseButton>
      </form>
    </template>

    <p class="mt-6 text-center text-sm text-gray-500">
      <router-link to="/login" class="font-medium text-primary-600 hover:text-primary-500">
        Voltar ao login
      </router-link>
    </p>
  </div>
</template>
