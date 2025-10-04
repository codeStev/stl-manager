// src/stores/ui.ts
import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useUiStore = defineStore('ui', () => {
  const activeView = ref<'none' | 'models'>('none')
  const selectedLibraryId = ref<number | null>(null)

  function openModels(libraryId: number) {
    selectedLibraryId.value = libraryId
    activeView.value = 'models'
  }

  function reset() {
    activeView.value = 'none'
    selectedLibraryId.value = null
  }

  return { activeView, selectedLibraryId, openModels, reset }
})
