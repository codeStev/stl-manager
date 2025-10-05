import {defineStore} from 'pinia'
import {computed} from 'vue'
import type {Library} from '@/types/external/LibraryTypes'
import {getLibraries} from '@/api/libraries'

export const useLibrariesStore = defineStore('libraries', () => {
  const { data, isLoading, isFinished, error, executeLatest, abort } = getLibraries()

  const items = computed<Library[]>(() => data.value ?? [])

  async function loadAll(options?: { force?: boolean }) {
    if (!options?.force && (data.value?.length ?? 0) > 0) {
      return
    }
    await executeLatest().catch(() => {
      // Swallow; error is already reflected in `error`
    })
  }

  function reset() {
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
