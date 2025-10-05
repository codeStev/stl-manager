// src/views/panels/ModelListPanel.vue
<script setup lang="ts">
import {computed} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from '@/stores/models'

const selection = useSelectionStore()
const { selectedLibraryId, selectedArtistId, selectedModelId } = storeToRefs(selection)

const modelsStore = useLibraryModelsStore()

const entry = computed(() =>
  selectedLibraryId.value != null && selectedArtistId.value != null
    ? modelsStore.getModelsByArtistId(selectedLibraryId.value, selectedArtistId.value)
    : null
)
const data = computed(() => entry.value ?? null)

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

      <template v-else>
        <v-list v-if="data?.values">
          <v-list-item
            v-for="model in data"
            :key="model?.id"
            :active="selectedModelId === (model?.id)"
            @click="pickModel(model?.id)"
            density="comfortable"
            clickable
          >
            <v-list-item-title>{{ model?.name ?? 'Model' }}</v-list-item-title>
            <v-tooltip
              activator="parent"
              :text="model?.name"
              location="top"
            />
          </v-list-item>
        </v-list>

      </template>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
