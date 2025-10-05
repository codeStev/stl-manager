// Ensure the store exposes a way to access a per-library entry
import {defineStore} from 'pinia'
import type {Ref} from 'vue'
import type {StlFileDetails} from "@/types/external/StlFileTypes.ts";
import {getFileDetails} from "@/api/files.ts";

type CacheEntry = {
  data: Ref<StlFileDetails | undefined>
  isLoading: Ref<boolean>
  isFinished: Ref<boolean>
  error: Ref<unknown>
  load: () => Promise<void>
  abort: (message?: string) => void
}

export const useFileStore = defineStore('files', () => {
  const cache = new Map<number, CacheEntry>()

  function getEntry(fileId: number): CacheEntry {
    const existing = cache.get(fileId)
    if (existing) return existing

    const { data, isLoading, isFinished, error, executeLatest, abort } = getFileDetails(fileId)
    const entry: CacheEntry = {
      data,
      isLoading,
      isFinished,
      error,
      load: () => executeLatest(),
      abort,
    }
    cache.set(fileId, entry)
    return entry
  }

  // Optional convenience methods
  async function load(fileId: number) {
    return getEntry(fileId).load()
  }
  function abort(fileId: number) {
    return getEntry(fileId).abort()
  }

  return {
    getEntry,
    load,
    abort,
  }
})
