// src/views/panels/FileDetailsPanel.vue
<script setup lang="ts">
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useFileStore} from "@/stores/files.ts";
import {computed, onBeforeUnmount, watch} from "vue";
import type {StlFileDetails} from "@/types/external/StlFileTypes.ts";

const selection = useSelectionStore()
const { selectedFileId } = storeToRefs(selection)

const fileStore = useFileStore()

const entry = computed(() =>
  selectedFileId.value != null
    ? fileStore.getEntry(selectedFileId.value)
    : null
)

const details = computed(() => entry.value?.data.value ?? null)
const isLoading = computed(() => entry.value?.isLoading.value ?? false)
const error = computed(() => (entry.value?.error.value as string | null) ?? null)

watch(
  () => selectedFileId.value,
  (id) => {
    if (id != null) entry.value?.load()
  },
  { immediate: true }
)

onBeforeUnmount(() => entry.value?.abort?.())
</script>

<template>
  <v-card>
    <v-card-title>Details</v-card-title>
    <v-divider />
    <v-card-text class="panel-scroll">
      <div v-if="selectedFileId == null" class="text-medium-emphasis">
        Select a file to see details.
      </div>

      <v-skeleton-loader v-else-if="isLoading" type="paragraph" />

      <v-alert v-else-if="error" type="error" variant="tonal">Error: {{ error }}</v-alert>

      <div v-else-if="details">
        <v-list density="compact">
          <v-list-item>
            <v-list-item-title>Name</v-list-item-title>
            <v-list-item-subtitle>{{ (details as StlFileDetails)?.fileName}}</v-list-item-subtitle>
            <v-tooltip
              activator="parent"
              :text="details?.fileName"
            />
          </v-list-item>
          <v-list-item v-if="(details as StlFileDetails)?.sizeBytes">
            <v-list-item-title>Size</v-list-item-title>
            <v-list-item-subtitle>{{ (details as StlFileDetails)?.sizeBytes }} bytes</v-list-item-subtitle>
            <v-tooltip
              activator="parent"
              :text="String(details?.sizeBytes) + ' bytes'"
            />
          </v-list-item>
          <v-list-item v-if="(details as StlFileDetails)?.storagePath">
            <v-list-item-title>Path</v-list-item-title>
            <v-list-item-subtitle>{{ (details as StlFileDetails)?.storagePath }}</v-list-item-subtitle>
            <v-tooltip
              activator="parent"
              :text="details?.storagePath"
            />
          </v-list-item>
          <v-list-item v-if="(details as StlFileDetails)?.createdAt">
            <v-list-item-title>Created</v-list-item-title>
            <v-list-item-subtitle>{{ (details as StlFileDetails)?.createdAt }}</v-list-item-subtitle>
            <v-tooltip
              activator="parent"
              :text="String(details?.createdAt)"
            />
          </v-list-item>
        </v-list>
      </div>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
