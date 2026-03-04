<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BaseCard from '@/components/base/BaseCard.vue'
import * as faqApi from '@/api/modules/faq.api'

const loading = ref(true)
const categories = ref<faqApi.FaqCategory[]>([])
const items = ref<faqApi.FaqItem[]>([])
const tab = ref<'categories' | 'items'>('categories')

// Forms
const showCategoryForm = ref(false)
const showItemForm = ref(false)
const editingCategory = ref<faqApi.FaqCategory | null>(null)
const editingItem = ref<faqApi.FaqItem | null>(null)

const categoryForm = ref({ name: '', sortOrder: 0 })
const itemForm = ref({ categoryId: 0, question: '', answer: '', sortOrder: 0 })
const formLoading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const [catRes, itemRes] = await Promise.all([faqApi.adminListCategories(), faqApi.adminListItems()])
    if (catRes.success && catRes.data) categories.value = catRes.data
    if (itemRes.success && itemRes.data) items.value = itemRes.data
  } finally {
    loading.value = false
  }
}

// ---- Categories ----
function newCategory() {
  editingCategory.value = null
  categoryForm.value = { name: '', sortOrder: 0 }
  showCategoryForm.value = true
}

function editCategory(cat: faqApi.FaqCategory) {
  editingCategory.value = cat
  categoryForm.value = { name: cat.name, sortOrder: cat.sortOrder }
  showCategoryForm.value = true
}

async function saveCategory() {
  formLoading.value = true
  try {
    if (editingCategory.value) {
      await faqApi.adminUpdateCategory(editingCategory.value.id, categoryForm.value)
    } else {
      await faqApi.adminCreateCategory(categoryForm.value)
    }
    showCategoryForm.value = false
    await loadData()
  } finally {
    formLoading.value = false
  }
}

async function deleteCategory(id: number) {
  if (!confirm('Excluir esta categoria? Os itens associados serao desvinculados.')) return
  await faqApi.adminDeleteCategory(id)
  await loadData()
}

// ---- Items ----
function newItem() {
  editingItem.value = null
  itemForm.value = { categoryId: categories.value[0]?.id || 0, question: '', answer: '', sortOrder: 0 }
  showItemForm.value = true
}

function editItem(item: faqApi.FaqItem) {
  editingItem.value = item
  itemForm.value = {
    categoryId: item.categoryId,
    question: item.question,
    answer: item.answer,
    sortOrder: item.sortOrder,
  }
  showItemForm.value = true
}

async function saveItem() {
  formLoading.value = true
  try {
    if (editingItem.value) {
      await faqApi.adminUpdateItem(editingItem.value.id, itemForm.value)
    } else {
      await faqApi.adminCreateItem(itemForm.value)
    }
    showItemForm.value = false
    await loadData()
  } finally {
    formLoading.value = false
  }
}

async function deleteItem(id: number) {
  if (!confirm('Excluir esta pergunta?')) return
  await faqApi.adminDeleteItem(id)
  await loadData()
}

async function togglePublished(item: faqApi.FaqItem) {
  await faqApi.adminUpdateItem(item.id, { published: !item.published })
  await loadData()
}

onMounted(loadData)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">FAQ — Administracao</h1>
        <p class="text-sm text-gray-500">Gerencie categorias e perguntas frequentes.</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-4 border-b border-gray-200">
      <button
        class="px-4 py-2 text-sm font-medium"
        :class="tab === 'categories' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500'"
        @click="tab = 'categories'"
      >
        Categorias
      </button>
      <button
        class="px-4 py-2 text-sm font-medium"
        :class="tab === 'items' ? 'border-b-2 border-primary-600 text-primary-600' : 'text-gray-500'"
        @click="tab = 'items'"
      >
        Perguntas
      </button>
    </div>

    <!-- Categories -->
    <BaseCard v-if="tab === 'categories'">
      <div class="mb-4 flex justify-end">
        <button class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700" @click="newCategory">
          Nova Categoria
        </button>
      </div>
      <div v-if="loading" class="text-center text-gray-500 py-8">Carregando...</div>
      <div v-else class="space-y-2">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="flex items-center justify-between rounded-lg border border-gray-200 p-4"
        >
          <div>
            <p class="font-medium text-gray-900">{{ cat.name }}</p>
            <p class="text-xs text-gray-500">Ordem: {{ cat.sortOrder }} | {{ cat.active ? 'Ativa' : 'Inativa' }}</p>
          </div>
          <div class="flex gap-2">
            <button class="text-xs text-primary-600 hover:underline" @click="editCategory(cat)">Editar</button>
            <button class="text-xs text-red-600 hover:underline" @click="deleteCategory(cat.id)">Excluir</button>
          </div>
        </div>
      </div>
    </BaseCard>

    <!-- Items -->
    <BaseCard v-if="tab === 'items'">
      <div class="mb-4 flex justify-end">
        <button class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700" @click="newItem">
          Nova Pergunta
        </button>
      </div>
      <div v-if="loading" class="text-center text-gray-500 py-8">Carregando...</div>
      <div v-else class="space-y-2">
        <div
          v-for="item in items"
          :key="item.id"
          class="rounded-lg border border-gray-200 p-4"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <p class="font-medium text-gray-900">{{ item.question }}</p>
              <p class="mt-1 text-sm text-gray-600 line-clamp-2">{{ item.answer }}</p>
              <p class="mt-1 text-xs text-gray-400">
                {{ item.categoryName || 'Sem categoria' }} | Ordem: {{ item.sortOrder }}
              </p>
            </div>
            <div class="ml-4 flex flex-col gap-1">
              <button
                class="text-xs"
                :class="item.published ? 'text-green-600' : 'text-gray-400'"
                @click="togglePublished(item)"
              >
                {{ item.published ? 'Publicado' : 'Rascunho' }}
              </button>
              <button class="text-xs text-primary-600 hover:underline" @click="editItem(item)">Editar</button>
              <button class="text-xs text-red-600 hover:underline" @click="deleteItem(item.id)">Excluir</button>
            </div>
          </div>
        </div>
      </div>
    </BaseCard>

    <!-- Category Form Modal -->
    <Teleport to="body">
      <div v-if="showCategoryForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
          <h2 class="mb-4 text-lg font-bold">{{ editingCategory ? 'Editar' : 'Nova' }} Categoria</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Nome</label>
              <input v-model="categoryForm.name" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Ordem</label>
              <input v-model.number="categoryForm.sortOrder" type="number" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
            </div>
            <div class="flex justify-end gap-3">
              <button class="rounded-lg border px-4 py-2 text-sm text-gray-700 hover:bg-gray-50" @click="showCategoryForm = false">Cancelar</button>
              <button class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700 disabled:opacity-50" :disabled="formLoading" @click="saveCategory">
                {{ formLoading ? 'Salvando...' : 'Salvar' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Item Form Modal -->
    <Teleport to="body">
      <div v-if="showItemForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
        <div class="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
          <h2 class="mb-4 text-lg font-bold">{{ editingItem ? 'Editar' : 'Nova' }} Pergunta</h2>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Categoria</label>
              <select v-model="itemForm.categoryId" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm">
                <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Pergunta</label>
              <input v-model="itemForm.question" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Resposta</label>
              <textarea v-model="itemForm.answer" rows="4" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Ordem</label>
              <input v-model.number="itemForm.sortOrder" type="number" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
            </div>
            <div class="flex justify-end gap-3">
              <button class="rounded-lg border px-4 py-2 text-sm text-gray-700 hover:bg-gray-50" @click="showItemForm = false">Cancelar</button>
              <button class="rounded-lg bg-primary-600 px-4 py-2 text-sm text-white hover:bg-primary-700 disabled:opacity-50" :disabled="formLoading" @click="saveItem">
                {{ formLoading ? 'Salvando...' : 'Salvar' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
