// src/stores/filesystem.ts
import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import {instance} from '@/api/axios'

export interface DirectoryDto {
  name: string
  absolutePath: string
  parentPath: string | null
  isDirectory: boolean
  canRead: boolean
  canWrite: boolean
  size: number
  lastModified: number
}

export const useFilesystemStore = defineStore('filesystem', () => {
  // State
  const directories = ref<DirectoryDto[]>([])
  const currentPath = ref<string>('')
  const selectedPath = ref<string>('')
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const displayPath = computed(() => {
    return currentPath.value || 'Loading...'
  })

  const hasDirectories = computed(() => {
    return directories.value.length > 0
  })

  // Actions
  async function loadDirectories(path?: string) {
    loading.value = true
    error.value = null

    try {
      const response = await instance.get<DirectoryDto[]>('/filesystem/directories', {
        params: path ? { path } : {}
      })

      directories.value = response.data
      currentPath.value = path || response.data[0]?.parentPath || '/'
    } catch (err: any) {
      error.value = err.response?.data?.message || 'Failed to load directories'
      directories.value = []
    } finally {
      loading.value = false
    }
  }

  async function validatePath(path: string): Promise<boolean> {
    try {
      const response = await instance.get<boolean>('/filesystem/validate-path', {
        params: { path }
      })
      return response.data
    } catch (err) {
      return false
    }
  }

  function selectDirectory(directory: DirectoryDto) {
    selectedPath.value = directory.absolutePath
  }

  function clearSelection() {
    selectedPath.value = ''
  }

  function reset() {
    directories.value = []
    currentPath.value = ''
    selectedPath.value = ''
    loading.value = false
    error.value = null
  }

  return {
    // State
    directories,
    currentPath,
    selectedPath,
    loading,
    error,

    // Getters
    displayPath,
    hasDirectories,

    // Actions
    loadDirectories,
    validatePath,
    selectDirectory,
    clearSelection,
    reset
  }
})
