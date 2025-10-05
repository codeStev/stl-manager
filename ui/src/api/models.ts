import axios, {type AxiosError} from 'axios'
import {useAxios} from '@vueuse/integrations/useAxios'
import {ref, type Ref, type UnwrapRef, watchEffect} from "vue";
import type {ModelDetails} from "@/types/external/ModelTypes.ts";
import {instance} from "@/api/axios.ts";

export function getModelsForLibrary(id: number): {
  data: Ref<ModelDetails[] | undefined>;
  isLoading: Ref<boolean>;
  isFinished: Ref<boolean>;
  error: Ref<UnwrapRef<string | null>, UnwrapRef<string | null> | string | null>;
  executeLatest: () => Promise<void>;
  abort: (message?: (string | undefined)) => void
} {

  const {data,
    isLoading,
    isFinished,
    error: rawError,
    execute,
    abort,
  } = useAxios<ModelDetails[]>(`/libraries/${id}/model`, {method: 'GET'}, instance, {
    immediate: false,
    resetOnExecute: false,
  })

  const error = ref<string | null>(null)

  watchEffect(() => {
    const e = rawError.value as unknown
    if (!e) {
      error.value = null
      return
    }

    // Ignore cancellation errors
    const name = (e as any)?.name ?? ''
    if (name === 'CanceledError' || name === 'AbortError') {
      error.value = null
      return
    }

    if (axios.isAxiosError(e)) {
      const ax = e as AxiosError<any>
      // Prefer server-provided message if available
      const serverMsg = (ax.response?.data as any)?.message
      error.value = serverMsg ?? ax.message ?? 'Request failed'
    } else {
      error.value = e instanceof Error ? e.message : String(e)
    }
  })

  // Only allow the latest call to complete: cancel any in-flight request first.
  async function executeLatest() {
    if (isLoading.value) abort()
    try {
      await execute()
    } catch (e: any) {
      // Swallow cancellation, propagate other errors via `error` ref
      if ((e?.name ?? '') !== 'CanceledError' && (e?.name ?? '') !== 'AbortError') {
        // No-op: `error` ref is already updated by useAxios
      }
    }
  }

  return {
    data,
    isLoading,
    isFinished,
    error,
    executeLatest,
    abort,
  }
}
