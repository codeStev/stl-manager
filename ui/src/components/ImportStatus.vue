<script setup lang="ts">
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'
import axios from 'axios'

const isReimporting = ref(false)

type ImportState = 'IDLE' | 'RUNNING' | 'SUCCESS' | 'FAILED'

// Payload coming from the API (server)
interface ApiImportStatusDto {
  state: ImportState
  startedAt: string | null
  finishedAt: string | null
  totalLibraries: number
  totalModels: number
  processedLibraries: number
  processedModels: number
  lastError: string | null
}

// Internal shape used by the component (keeps existing totalItems/processedItems for rendering logic)
interface ImportStatusDto {
  state: ImportState
  startedAt: string | null
  finishedAt: string | null
  totalItems: number            // derived from totalModels (keeps existing rendering logic unchanged)
  processedItems: number        // derived from processedModels (keeps existing rendering logic unchanged)
  lastError: string | null

  // Detailed fields for the snackbar details view
  totalLibraries: number
  processedLibraries: number
  totalModels: number
  processedModels: number
}

const emit = defineEmits<{
  (e: 'import-finished', status: ImportStatusDto): void
}>()

const importStatus = ref<ImportStatusDto | null>(null)
const pollerId = ref<number | null>(null)
const isPolling = ref(false)

// Toggle to switch the snackbar between summary and details
const showSnackDetails = ref(false)

const snackbar = ref<{
  open: boolean
  message: string
  color: 'success' | 'warning' | 'error' | 'info' | 'primary'
  timeout: number
}>({
  open: false,
  message: '',
  color: 'info',
  timeout: 3000
})

function openSnack(
  message: string,
  color: 'success' | 'warning' | 'error' | 'info' | 'primary' = 'info',
  timeout = 3000
) {
  snackbar.value = { open: true, message, color, timeout }
}

// Keep existing rendering logic
const hasTotals = computed(() => (importStatus.value?.totalItems ?? 0) > 0)
const progressPercent = computed(() => {
  const s = importStatus.value
  if (!s) return 0
  if (!hasTotals.value) return 0
  const pct = Math.floor((s.processedItems / Math.max(1, s.totalItems)) * 100)
  return Math.max(0, Math.min(100, pct))
})
const showProgress = computed(() => {
  const s = importStatus.value
  return !!s && (s.state === 'RUNNING' || s.state === 'SUCCESS' || s.state === 'FAILED')
})
const statusText = computed(() => {
  const s = importStatus.value
  if (!s) return ''
  switch (s.state) {
    case 'RUNNING':
      return hasTotals.value
        ? `Processing ${s.processedItems}/${s.totalItems} (${progressPercent.value}%)`
        : 'Processing...'
    case 'SUCCESS':
      return hasTotals.value
        ? `Completed ${s.processedItems}/${s.totalItems}`
        : 'Completed'
    case 'FAILED':
      return s.lastError ? `Failed: ${s.lastError}` : 'Failed'
    case 'IDLE':
    default:
      return ''
  }
})

// New: computed text for the snackbar that switches between summary and details
const snackText = computed(() => {
  const s = importStatus.value
  if (!s) return snackbar.value.message

  if (showSnackDetails.value && (s.state === 'RUNNING' || s.state === 'SUCCESS' || s.state === 'FAILED')) {
    // Focus on processedModels in real time; also show libraries for extra context
    if (s.state === 'RUNNING') {
      return `Models ${s.processedModels}/${s.totalModels} • Libraries ${s.processedLibraries}/${s.totalLibraries}`
    }
    if (s.state === 'SUCCESS') {
      return `Completed models ${s.processedModels}/${s.totalModels} • libraries ${s.processedLibraries}/${s.totalLibraries}`
    }
    // FAILED
    return s.lastError
      ? `Failed: ${s.lastError} • Models ${s.processedModels}/${s.totalModels} • Libraries ${s.processedLibraries}/${s.totalLibraries}`
      : `Failed • Models ${s.processedModels}/${s.totalModels} • Libraries ${s.processedLibraries}/${s.totalLibraries}`
  }

  // Default to the existing summary message
  return snackbar.value.message
})

function updateSnackForStatus(s: ImportStatusDto) {
  if (s.state === 'RUNNING') {
    snackbar.value.open = true
    snackbar.value.color = 'primary'
    snackbar.value.message = 'Import in progress'
    snackbar.value.timeout = -1 // keep open while running
  } else if (s.state === 'SUCCESS') {
    snackbar.value.open = true
    snackbar.value.color = 'success'
    snackbar.value.message = 'Import completed'
    snackbar.value.timeout = 3000
  } else if (s.state === 'FAILED') {
    snackbar.value.open = true
    snackbar.value.color = 'error'
    snackbar.value.message = 'Import failed'
    snackbar.value.timeout = 8000
  }
}

// Map API payload into internal shape (preserves existing totalItems/processedItems fields)
function mapStatus(api: ApiImportStatusDto): ImportStatusDto {
  return {
    state: api.state,
    startedAt: api.startedAt,
    finishedAt: api.finishedAt,
    // Keep existing rendering logic based on "items" by deriving them from models
    totalItems: api.totalModels ?? 0,
    processedItems: api.processedModels ?? 0,
    lastError: api.lastError ?? null,
    // Detailed fields
    totalLibraries: api.totalLibraries ?? 0,
    processedLibraries: api.processedLibraries ?? 0,
    totalModels: api.totalModels ?? 0,
    processedModels: api.processedModels ?? 0
  }
}

async function fetchStatusOnce(): Promise<ImportStatusDto | null> {
  try {
    const { data } = await axios.get<ApiImportStatusDto>('/api/import/status', { withCredentials: true })
    const mapped = mapStatus(data)

    const prevState = importStatus.value?.state
    importStatus.value = mapped

    if (mapped.state === 'RUNNING') {
      updateSnackForStatus(mapped) // keep the “in progress” snackbar open
      return mapped
    }

    // Terminal state reached (SUCCESS/FAILED)
    stopPolling()

    const transitionedFromRunning =
      prevState === 'RUNNING' && (mapped.state === 'SUCCESS' || mapped.state === 'FAILED')

    if (transitionedFromRunning) {
      // Only show terminal snackbar when we actually finished this run in-session
      updateSnackForStatus(mapped)
      emit('import-finished', mapped)
    } else {
      // On initial load or refresh with an already-terminal state, don’t show the snackbar again
      if (snackbar.value.open) {
        snackbar.value.open = false
      }
      // Also avoid emitting import-finished here to prevent surprise refreshes
    }

    return mapped
  } catch (e) {
    openSnack('Unable to fetch import status', 'error', 4000)
    return null
  }
}

async function onReimport() {
  if (isReimporting.value) return
  isReimporting.value = true
  try {
    const res = await axios.post('/api/import/')
    if (res.status === 202 || res.status === 200) {
      openSnack('Reimport started.', 'success', 1500)
      startPolling()
    } else if (res.status === 409) {
      openSnack('A reimport is already running.', 'warning', 3000)
      startPolling() // begin polling to show live status
    } else {
      openSnack(`Unexpected response: ${res.status}`, 'info')
    }
  } catch (err: any) {
    const status = err?.response?.status
    if (status === 409) {
      openSnack('A reimport is already running.', 'warning', 3000)
      startPolling()
    } else {
      openSnack('Failed to start reimport. Please try again.', 'error')
    }
  } finally {
    isReimporting.value = false
  }
}

function startPolling(intervalMs = 1000) {
  if (isPolling.value) return
  isPolling.value = true
  // Initial fetch
  fetchStatusOnce()
  pollerId.value = window.setInterval(fetchStatusOnce, intervalMs)
}

function stopPolling() {
  isPolling.value = false
  if (pollerId.value !== null) {
    clearInterval(pollerId.value)
    pollerId.value = null
  }
}

watch(
  () => showSnackDetails.value,
  () => {
    if (snackbar.value.open && importStatus.value) {
      updateSnackForStatus(importStatus.value)
    }
  }
)


onMounted(() => {
  startPolling(1000)
})

onUnmounted(() => {
  stopPolling()
})
</script>


<template>
  <v-container>
    <v-btn
      color="primary"
      :loading="isReimporting"
      :disabled="isReimporting"
      prepend-icon="mdi-refresh"
      @click="onReimport"
    >
      Reimport
    </v-btn>
  </v-container>
  <v-snackbar
    v-model="snackbar.open"
    :timeout="snackbar.timeout"
    :color="snackbar.color"
    location="bottom right"
    multi-line
  >
    <div>{{ snackText }}</div>

    <template #actions>
      <v-btn
        variant="text"
        density="comfortable"
        @click="showSnackDetails = !showSnackDetails"
      >
        {{ showSnackDetails ? 'Summary' : 'Details' }}
      </v-btn>
      <v-btn
        variant="text"
        density="comfortable"
        @click="snackbar.open = false"
      >
        Close
      </v-btn>
    </template>
  </v-snackbar>

</template>

<style scoped>

</style>
