// src/stores/libraries.ts
import { defineStore } from 'pinia'
import {computed, ref} from 'vue'
import type { Library } from '@/types/external/LibraryTypes'
// Adjust this import to where your libraries.ts is located
import { getLibraries } from '@/api/libraries'

export const useLibrariesStore = defineStore('libraries', () => {
  // Reuse your existing axios/useAxios logic
  const { data, isLoading, isFinished, error, executeLatest, abort } = getLibraries()
  const localData = ref<Library[]>([])
  console.log(getLibraries())
  // Provide a stable, typed list for consumers
  const items = computed<Library[]>(() => data.value ?? [])
  // Simple action that triggers the existing request
  async function loadAll() {
    console.log('Loading libraries')
    await executeLatest().catch(() => {
      console.log('Error loading libraries')
    })
  }

  // Optional: clear the cached list if you need to reset the store
  function reset() {
    localData.value = []
    abort()
  }

  return {
    // state
    items,
    isLoading,
    isFinished,
    error,
    // actions
    loadAll,
    abort,
    reset,
  }
})
