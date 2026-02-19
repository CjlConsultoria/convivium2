<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import BaseCard from '@/components/base/BaseCard.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseAlert from '@/components/base/BaseAlert.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useTenantStore } from '@/stores/tenant.store'

const router = useRouter()
const authStore = useAuthStore()
const tenantStore = useTenantStore()

const user = computed(() => authStore.user)
const condominiumName = computed(() => tenantStore.currentCondominium?.name ?? '-')
const condoId = computed(
  () =>
    tenantStore.currentCondominiumId ?? authStore.user?.condominiumRoles?.[0]?.condominiumId,
)

const name = ref('')
const phone = ref('')
const loading = ref(false)
const success = ref('')
const error = ref('')

watch(
  user,
  (u) => {
    if (u) {
      name.value = u.name ?? ''
      phone.value = u.phone ?? ''
    }
  },
  { immediate: true },
)

async function submit() {
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    await authStore.updateMyProfile({ name: name.value.trim() || undefined, phone: phone.value.trim() || undefined })
    success.value = 'Perfil atualizado com sucesso.'
  } catch (err: any) {
    error.value = err.response?.data?.message ?? 'Erro ao atualizar perfil.'
  } finally {
    loading.value = false
  }
}

function goBack() {
  if (authStore.isPlatformAdmin) {
    router.push('/admin')
  } else if (condoId.value) {
    router.push(`/c/${condoId.value}`)
  } else {
    router.push('/')
  }
}
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 sm:text-3xl">Meu Perfil</h1>
        <p class="mt-1 text-sm text-gray-500">Edite seu nome e telefone. E-mail e CPF nao podem ser alterados.</p>
      </div>
      <BaseButton variant="outline" @click="goBack">
        Voltar
      </BaseButton>
    </div>

    <BaseAlert v-if="success" type="success" class="mt-4" dismissible @dismiss="success = ''">
      {{ success }}
    </BaseAlert>
    <BaseAlert v-if="error" type="error" class="mt-4" dismissible @dismiss="error = ''">
      {{ error }}
    </BaseAlert>

    <BaseCard title="Dados pessoais">
      <form class="space-y-4" @submit.prevent="submit">
        <BaseInput
          v-model="name"
          label="Nome"
          type="text"
          placeholder="Seu nome"
          required
        />
        <div>
          <label class="mb-1 block text-sm font-medium text-gray-700">E-mail</label>
          <div class="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-600">
            {{ user?.email ?? '-' }}
          </div>
          <p class="mt-1 text-xs text-gray-500">E-mail nao pode ser alterado.</p>
        </div>
        <BaseInput
          v-model="phone"
          label="Telefone"
          type="tel"
          placeholder="(00) 00000-0000"
        />
        <div v-if="condominiumName && condominiumName !== '-'">
          <label class="mb-1 block text-sm font-medium text-gray-700">Condominio</label>
          <div class="text-sm text-gray-900">{{ condominiumName }}</div>
        </div>
        <div class="pt-2">
          <BaseButton type="submit" variant="primary" :loading="loading">
            Salvar alteracoes
          </BaseButton>
        </div>
      </form>
    </BaseCard>
  </div>
</template>
