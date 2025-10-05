// src/stores/selection.ts
import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useSelectionStore = defineStore('selection', () => {
  const selectedLibraryId = ref<number | null>(null)
  const selectedArtistId = ref<number | null>(null)
  const selectedModelId = ref<number | null>(null)
  const selectedVariantId = ref<number | null>(null)
  const selectedFileId = ref<number | null>(null)

  function selectLibrary(id: number | null) {
    selectedLibraryId.value = id
    // Cascade reset
    selectedArtistId.value = null
    selectedModelId.value = null
    selectedVariantId.value = null
    selectedFileId.value = null
  }
  function selectArtist(id: number | null) {
    selectedArtistId.value = id
    // Cascade reset
    selectedModelId.value = null
    selectedVariantId.value = null
    selectedFileId.value = null
  }

  function selectModel(id: number | null) {
    selectedModelId.value = id
    // Cascade reset
    selectedVariantId.value = null
    selectedFileId.value = null
  }

  function selectVariant(id: number | null) {
    selectedVariantId.value = id
    // Cascade reset
    selectedFileId.value = null
  }

  function selectFile(id: number | null) {
    selectedFileId.value = id
  }

  function resetAll() {
    selectLibrary(null)
  }

  return {
    selectedLibraryId,
    selectedArtistId,
    selectedModelId,
    selectedVariantId,
    selectedFileId,
    selectLibrary,
    selectArtist,
    selectModel,
    selectVariant,
    selectFile,
    resetAll,
  }
})
