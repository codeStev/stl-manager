<script setup lang="ts">
import {onBeforeUnmount, ref, watch} from 'vue'
import {useLibraryModelsStore} from '@/stores/models'

// libraryId is provided by the route using props: true in the router
const props = defineProps<{ libraryId: number }>()
const modelsStore = useLibraryModelsStore()

// Keep the currently active cache entry for the route's libraryId
const entry = ref(modelsStore.getEntry(props.libraryId))

// Load whenever the libraryId changes (and immediately on mount)
watch(
  () => props.libraryId,
  (id) => {
    entry.value = modelsStore.getEntry(id)
    entry.value.load()
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  entry.value?.abort?.()
})
</script>

<template>
  <v-container class="pa-4">
    <v-skeleton-loader v-if="entry.isLoading" type="list-item-three-line"></v-skeleton-loader>
    <v-alert v-else-if="entry.error" type="error" variant="tonal">Error: {{ entry.error }}</v-alert>

    <template v-else>
      <v-list v-if="entry.data?.length">
        <v-list-item v-for="(model, i) in entry.data" :key="i">
          <v-list-item-title>{{ (model as any)?.name ?? (model as any)?.model_name ?? 'Model' }}</v-list-item-title>
          <v-list-item-subtitle>{{ (model as any)?.id ?? '' }}</v-list-item-subtitle>
        </v-list-item>
      </v-list>
      <v-container v-else-if="entry.isFinished">No models found.</v-container>
    </template>
  </v-container>
</template>

<style scoped>
</style>
