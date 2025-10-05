// src/views/panels/FileListPanel.vue
<script setup lang="ts">
import {computed} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from "@/stores/models.ts";

const selection = useSelectionStore()
const {selectedLibraryId, selectedArtistId, selectedModelId, selectedVariantId, selectedFileId } = storeToRefs(selection)

const modelStore = useLibraryModelsStore()

const entry = computed(() =>
  selectedLibraryId.value != null && selectedArtistId.value != null && selectedModelId.value != null && selectedVariantId.value != null
    ? modelStore.getFilesByVariantId(selectedLibraryId.value, selectedModelId.value, selectedVariantId.value)
    : null
)
function pickFile(fileId: number) {
  selection.selectFile(fileId)
}
const data = computed(() => entry.value ?? null)
</script>


<template>
  <v-card>
    <v-card-title>Files</v-card-title>
    <v-divider />
    <v-card-text class="panel-scroll">
      <div v-if="selectedVariantId == null" class="text-medium-emphasis">
        Select a variant to see files.
      </div>



      <template v-else>
        <v-list v-if="data?.values">
          <v-list-item
            v-for="file in data"
            :key="file?.id"
            :active="selectedFileId === (file?.id)"
            @click="pickFile(file?.id)"
            density="comfortable"
            clickable
          >
            <template #prepend>
              <v-icon icon="mdi-file" />
            </template>
            <v-list-item-title>{{ file?.fileName }}</v-list-item-title>
            <v-tooltip
              activator="parent"
              :text="file?.fileName"
            />
          </v-list-item>
        </v-list>

      </template>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
