<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as faqApi from '@/api/modules/faq.api'

const loading = ref(true)
const categories = ref<faqApi.FaqCategory[]>([])
const expandedItems = ref<Set<number>>(new Set())

function toggleItem(id: number) {
  if (expandedItems.value.has(id)) {
    expandedItems.value.delete(id)
  } else {
    expandedItems.value.add(id)
  }
}

async function loadFaq() {
  loading.value = true
  try {
    const res = await faqApi.listFaq()
    if (res.success && res.data) {
      categories.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadFaq)
</script>

<template>
  <div class="space-y-6">
    <div>
      <h1 class="text-2xl font-bold text-gray-900">Perguntas Frequentes</h1>
      <p class="text-sm text-gray-500">Encontre respostas para as duvidas mais comuns.</p>
    </div>

    <div v-if="loading" class="text-center text-gray-500 py-12">Carregando...</div>

    <div v-else-if="categories.length === 0" class="text-center text-gray-500 py-12">
      Nenhuma pergunta cadastrada ainda.
    </div>

    <div v-else class="space-y-8">
      <div v-for="category in categories" :key="category.id">
        <h2 class="mb-4 text-lg font-semibold text-gray-800">{{ category.name }}</h2>
        <div class="space-y-2">
          <div
            v-for="item in category.items"
            :key="item.id"
            class="rounded-lg border border-gray-200 bg-white overflow-hidden"
          >
            <button
              class="flex w-full items-center justify-between px-5 py-4 text-left text-sm font-medium text-gray-900 hover:bg-gray-50"
              @click="toggleItem(item.id)"
            >
              <span>{{ item.question }}</span>
              <svg
                class="h-5 w-5 shrink-0 text-gray-400 transition-transform"
                :class="{ 'rotate-180': expandedItems.has(item.id) }"
                fill="none" stroke="currentColor" viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
              </svg>
            </button>
            <div
              v-if="expandedItems.has(item.id)"
              class="border-t border-gray-100 px-5 py-4 text-sm text-gray-600"
            >
              {{ item.answer }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
