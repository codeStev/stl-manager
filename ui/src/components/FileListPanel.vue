// src/views/panels/FileListPanel.vue
<script setup lang="ts">
import {computed} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from "@/stores/models.ts";
import type {StlFileBaseInfo} from "@/types/external/StlFileTypes.ts"; // adjust to your files store

const selection = useSelectionStore()
const {selectedLibraryId, selectedModelId, selectedVariantId, selectedFileId } = storeToRefs(selection)

const modelStore = useLibraryModelsStore()

const entry = computed(() =>
  selectedLibraryId.value != null && selectedModelId.value != null && selectedVariantId.value != null
    ? modelStore.getFilesByVariantId(selectedLibraryId.value, selectedModelId.value, selectedVariantId.value)
    : null
)

const data = computed(() => entry.value ?? null)

function pickFile(fileId: number) {
  selection.selectFile(fileId)
}
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
            :key="(file as StlFileBaseInfo)?.id"
            :active="selectedFileId === ((file as StlFileBaseInfo)?.id)"
            @click="pickFile((file as StlFileBaseInfo)?.id)"
            density="comfortable"
            clickable
          >
            <template #prepend>
              <v-icon icon="mdi-file" />
            </template>
            <v-list-item-title>{{ (file as StlFileBaseInfo)?.fileName }}</v-list-item-title>
          </v-list-item>
        </v-list>

      </template>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
