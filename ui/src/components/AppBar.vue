<script setup lang="ts">
import {onBeforeUnmount, onMounted} from 'vue'
import {useLibrariesStore} from '@/stores/libraries.ts'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'

const store = useLibrariesStore()
const { items, isLoading, isFinished, error } = storeToRefs(store)


function refresh() {
  store.loadAll()
}

const selection = useSelectionStore()

function onLibraryClick(libraryId: number) {
  selection.selectLibrary(libraryId)
}
onMounted(refresh)
onBeforeUnmount(() => store.abort())
</script>

<script lang="ts">
export default {}
</script>

<template>
  <v-navigation-drawer permanent>
    <v-container>
      <v-skeleton-loader v-if="isLoading" type="list-item-avatar" />
      <v-container v-else-if="error" style="color:#c00">Error: {{ error }}</v-container>

      <v-list v-else-if="items.length">
        <v-list-item
          v-for="item in items"
          :key="item.library_id"
          @click="onLibraryClick(item.library_id)"
          link
        >
          <v-list-item-title>{{ item.library_name }}</v-list-item-title>
          <v-list-item-subtitle>{{ item.library_path }}</v-list-item-subtitle>
          <v-list-item-action />
        </v-list-item>
      </v-list>

      <v-container v-else-if="isFinished">No libraries found.</v-container>
    </v-container>
  </v-navigation-drawer>

  <v-app-bar :elevation="2" color="primary" dark>
    <template #prepend>
      <v-input width="20" />
    </template>
    <v-app-bar-title>STL-Manager</v-app-bar-title>
  </v-app-bar>
</template>

<style scoped>
</style>
