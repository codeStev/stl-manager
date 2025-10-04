<script setup lang="ts">
import {computed, onBeforeUnmount, watch} from 'vue'
import {useLibraryModelsStore} from '@/stores/models'

const props = defineProps<{ libraryId: number }>()
const modelsStore = useLibraryModelsStore()

// Entry for the current library; expose convenient computed props
const entry = computed(() => modelsStore.getEntry(props.libraryId))
const data = computed(() => entry.value.data.value ?? [])
const isLoading = computed(() => entry.value.isLoading.value)
const isFinished = computed(() => entry.value.isFinished.value)
const error = computed(() => (entry.value.error.value as string | null) ?? null)

// Load on mount and when libraryId changes
watch(
  () => props.libraryId,
  () => entry.value.load(),
  { immediate: true }
)

onBeforeUnmount(() => {
  entry.value.abort?.()
})
</script>

<template>
  <v-container class="pa-4" fluid>
    <v-skeleton-loader v-if="isLoading" type="list-item-three-line" />
    <v-alert v-else-if="error" type="error" variant="tonal">Error: {{ error }}</v-alert>

    <template v-else>
      <v-list v-if="data.length">
        <v-list-item v-for="model in data" :key="(model as any)?.id ?? (model as any)?.model_id">
          <v-list-item-title>{{ (model as any)?.name ?? (model as any)?.model_name ?? 'Model' }}</v-list-item-title>
          <v-list-item-subtitle>{{ (model as any)?.id ?? (model as any)?.model_id ?? '' }}</v-list-item-subtitle>
        </v-list-item>
      </v-list>
      <v-container v-else-if="isFinished">No models found.</v-container>
    </template>
  </v-container>
</template>

<style scoped>
</style>
