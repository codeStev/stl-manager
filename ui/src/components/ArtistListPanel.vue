<script setup lang="ts">
import {computed, onBeforeUnmount, watch} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from "@/stores/models.ts";
// If you already have an artists store, import it. Otherwise, adapt to your data source.

const selection = useSelectionStore()
const { selectedLibraryId, selectedArtistId } = storeToRefs(selection)

const modelsStore = useLibraryModelsStore()

// Follow the same entry/data pattern used by your other panels
const entry = computed(() => {
    console.log(selectedLibraryId.value)
    return selectedLibraryId.value != null
      ? modelsStore.getArtistsByLibraryId(selectedLibraryId.value)
      : null
  }
)
const models = computed(() =>
  selectedLibraryId.value != null
    ? modelsStore.getEntry(selectedLibraryId.value)
    : null
)

const data = computed(() => entry.value ?? [])
const isLoading = computed(() => models.value?.isLoading.value ?? false)
const isFinished = computed(() => models.value?.isFinished.value ?? false)
const error = computed(() => (models.value?.error.value as string | null) ?? null)

watch(
  () => selectedLibraryId.value,
  (id) => {
    if (id != null) models.value?.load()
  },
  { immediate: true }
)

onBeforeUnmount(() => models.value?.abort?.())
function selectArtist(id: number) {
  selection.selectArtist(id)
}
</script>

<template>

  <div v-if="selectedLibraryId == null" class="text-medium-emphasis">
    Select a library to see artists.
  </div>

  <v-skeleton-loader v-else-if="isLoading" type="list-item-three-line" />

  <v-alert v-else-if="error" type="error" variant="tonal">Error: {{ error }}</v-alert>

    <v-divider />
      <v-list v-if="data.length">
        <v-list-item
          v-for="artist in data"
          :key="artist.id"
          :title="artist.name"
          :active="artist.id === selectedArtistId"
          @click="selectArtist(artist.id)"
        />
      </v-list>
  <div v-else-if="isFinished" class="text-medium-emphasis">
    No models found.
  </div>
</template>
