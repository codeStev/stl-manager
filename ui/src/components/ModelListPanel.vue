// src/views/panels/ModelListPanel.vue
<script setup lang="ts">
import {computed, onBeforeUnmount, watch} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from '@/stores/models' // adjust path/name to your models store

const selection = useSelectionStore()
const { selectedLibraryId, selectedModelId } = storeToRefs(selection)

const modelsStore = useLibraryModelsStore()

const entry = computed(() =>
  selectedLibraryId.value != null
    ? modelsStore.getEntry(selectedLibraryId.value)
    : null
)

const data = computed(() => entry.value?.data.value ?? [])
const isLoading = computed(() => entry.value?.isLoading.value ?? false)
const isFinished = computed(() => entry.value?.isFinished.value ?? false)
const error = computed(() => (entry.value?.error.value as string | null) ?? null)

watch(
  () => selectedLibraryId.value,
  (id) => {
    if (id != null) entry.value?.load()
  },
  { immediate: true }
)

onBeforeUnmount(() => entry.value?.abort?.())

function pickModel(modelId: number) {
  selection.selectModel(modelId)
}
</script>

<template>
  <v-card>
    <v-card-title>Models</v-card-title>
    <v-divider />
    <v-card-text class="panel-scroll">
      <div v-if="selectedLibraryId == null" class="text-medium-emphasis">
        Select a library to see models.
      </div>

      <v-skeleton-loader v-else-if="isLoading" type="list-item-three-line" />

      <v-alert v-else-if="error" type="error" variant="tonal">Error: {{ error }}</v-alert>

      <template v-else>
        <v-list v-if="data.length">
          <v-list-item
            v-for="model in data"
            :key="(model as any)?.id ?? (model as any)?.model_id"
            :active="selectedModelId === ((model as any)?.id ?? (model as any)?.model_id)"
            @click="pickModel((model as any)?.id ?? (model as any)?.model_id)"
            density="comfortable"
            clickable
          >
            <v-list-item-title>{{ (model as any)?.name ?? (model as any)?.model_name ?? 'Model' }}</v-list-item-title>
            <v-list-item-subtitle>{{ (model as any)?.description ?? '' }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>

        <div v-else-if="isFinished" class="text-medium-emphasis">
          No models found.
        </div>
      </template>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
