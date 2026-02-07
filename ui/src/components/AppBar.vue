<script setup lang="ts">
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {useLibrariesStore} from '@/stores/libraries.ts'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import ImportStatus from "@/components/ImportStatus.vue"
import CreateLibraryDialog from "@/components/CreateLibraryDialog.vue"

const store = useLibrariesStore()
const { items, isLoading, isFinished, error } = storeToRefs(store)

const showCreateDialog = ref(false)

function refresh() {
  store.loadAll()
}

const selection = useSelectionStore()

function onLibraryClick(libraryId: number) {
  selection.selectLibrary(libraryId)
}

function onImportFinished(status: { state: 'IDLE' | 'RUNNING' | 'SUCCESS' | 'FAILED' }) {
  if (status.state === 'SUCCESS') {
    refresh()
  }
}

function onLibraryCreated() {
  refresh()
}

onMounted(() => {
  refresh()
})

onBeforeUnmount(() => {
  store.abort()
})
</script>

<script lang="ts">
export default {}
</script>

<template>
  <v-navigation-drawer permanent>
    <v-container>
      <!-- Create Library Button -->
      <v-btn
        color="primary"
        block
        class="mb-4"
        prepend-icon="mdi-plus"
        @click="showCreateDialog = true"
      >
        Create New Library
      </v-btn>

      <v-skeleton-loader v-if="isLoading" type="list-item-avatar" />
      <v-container v-else-if="error" style="color:#c00">Error: {{ error }}</v-container>

      <v-list v-else-if="items.length">
        <v-list-item
          v-for="item in items"
          :key="item.library_id"
          @click="onLibraryClick(item?.library_id)"
          link
        >
          <v-list-item-title>{{ item?.library_name }}</v-list-item-title>
          <v-list-item-subtitle>{{ item?.library_path }}</v-list-item-subtitle>
          <v-list-item-action />
          <v-tooltip
            activator="parent"
            :text="item?.library_path"
          />
        </v-list-item>
      </v-list>

      <v-container v-else-if="isFinished">No libraries found.</v-container>

      <ImportStatus @import-finished="onImportFinished" />
    </v-container>
  </v-navigation-drawer>

  <v-app-bar :elevation="2" color="primary" dark>
    <v-app-bar-title>STL-Manager</v-app-bar-title>
  </v-app-bar>

  <!-- Create Library Dialog -->
  <CreateLibraryDialog
    v-model="showCreateDialog"
    @library-created="onLibraryCreated"
  />
</template>

<style scoped>
</style>
