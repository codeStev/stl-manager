<script setup lang="ts">
import {storeToRefs} from 'pinia'
import {useUiStore} from '@/stores/ui.ts'
import ModelListView from './ModelListView.vue'

const ui = useUiStore()
const { activeView, selectedLibraryId } = storeToRefs(ui)
</script>

<template>
  <v-container class="pa-4">
    <ModelListView
      v-if="activeView === 'models' && selectedLibraryId != null"
      :library-id="selectedLibraryId!"
    />
    <div v-else class="text-medium-emphasis">
      Select a library to see its models.
    </div>
  </v-container>
</template>

<style scoped>
</style>
