// Ensure the store exposes a way to access a per-library entry
import {defineStore} from 'pinia'
import type {Ref} from 'vue'
import type {ModelDetails, ModelVariant} from '@/types/external/ModelTypes'
import {getModelsForLibrary} from '@/api/models'
import type {StlFileBaseInfo} from "@/types/external/StlFileTypes.ts";

type CacheEntry = {
  data: Ref<ModelDetails[] | undefined>
  isLoading: Ref<boolean>
  isFinished: Ref<boolean>
  error: Ref<unknown>
  load: () => Promise<void>
  abort: (message?: string) => void
}

export const useLibraryModelsStore = defineStore('libraryModels', () => {
  const cache = new Map<number, CacheEntry>()

  function getEntry(libraryId: number): CacheEntry {
    const existing = cache.get(libraryId)
    if (existing) return existing

    const { data, isLoading, isFinished, error, executeLatest, abort } = getModelsForLibrary(libraryId)
    const entry: CacheEntry = {
      data,
      isLoading,
      isFinished,
      error,
      load: () => executeLatest(),
      abort,
    }
    cache.set(libraryId, entry)
    return entry
  }

  function getModelsByArtistId(libraryId: number, artistId: number): ModelDetails[] | undefined {
    let models = getEntry(libraryId)?.data?.value;
    if (!models) return undefined;
    return models.filter(model => model.artist.id === artistId);
  }

  function getArtistsByLibraryId(libraryId: number) {
    const models = getEntry(libraryId)?.data?.value;
    if (!models) return undefined;

    // Deduplicate by artist.id (Map keeps the last seen artist object per id)
    const byId = new Map<number, ModelDetails['artist']>(
        models.map(m => [m.artist.id, m.artist])
    );

    return [...byId.values()];
  }

  function getVariantsByModelId(libraryId: number, modelId: number): ModelVariant[] | undefined {
    let models = getEntry(libraryId)?.data?.value;
    console.log(models)
    if (!models) return undefined;
    return models.find(x => x.id == modelId)?.variants
  }

  function getFilesByVariantId(libraryId: number, modelId: number, variantId: number): StlFileBaseInfo[] | undefined {
    let variants = getVariantsByModelId(libraryId, modelId);
    if (!variants) return undefined;
    console.log(variants)
    return variants.find(x => x.id == variantId)?.stlFiles;
  }

  // Optional convenience methods
  async function load(libraryId: number) {
    return getEntry(libraryId).load()
  }
  function abort(libraryId: number) {
    return getEntry(libraryId).abort()
  }


  return {
    getEntry,
    getVariantsByModelId,
    getFilesByVariantId,
    getModelsByArtistId,
    getArtistsByLibraryId,
    load,
    abort,
  }
})
