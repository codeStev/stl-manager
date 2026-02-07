
<script setup lang="ts">
import {defineEmits, defineProps, ref} from 'vue'
import {storeToRefs} from 'pinia'
import {useFilesystemStore} from '@/stores/fileStructure.ts'

interface Props {
  modelValue: string
  label?: string
  placeholder?: string
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'folder-selected', path: string): void
}

const props = withDefaults(defineProps<Props>(), {
  label: 'Select Folder',
  placeholder: 'No folder selected'
})

const emit = defineEmits<Emits>()

// Store
const filesystemStore = useFilesystemStore()
const {
  directories,
  currentPath,
  selectedPath,
  loading,
  error,
  displayPath,
  hasDirectories
} = storeToRefs(filesystemStore)

// Local state
const showDialog = ref(false)

async function openFolderPicker() {
  if (props.disabled) return

  showDialog.value = true
  filesystemStore.selectedPath = props.modelValue
  await filesystemStore.loadDirectories(props.modelValue || undefined)
}

async function navigateToDirectory(directory: typeof directories.value[0]) {
  if (directory.name === '..') {
    await filesystemStore.loadDirectories(directory.absolutePath)
  } else {
    await filesystemStore.loadDirectories(directory.absolutePath)
  }
}

function selectDirectory(directory: typeof directories.value[0]) {
  filesystemStore.selectDirectory(directory)
}

function confirmSelection() {
  if (selectedPath.value) {
    emit('update:modelValue', selectedPath.value)
    emit('folder-selected', selectedPath.value)
  }
  closeDialog()
}

function closeDialog() {
  showDialog.value = false
  filesystemStore.reset()
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function formatDate(timestamp: number): string {
  return new Date(timestamp).toLocaleDateString()
}
</script>

<template>
  <div class="folder-picker">
    <v-text-field
      :model-value="modelValue"
      :label="label"
      :placeholder="placeholder"
      readonly
      :disabled="disabled"
      @click="openFolderPicker"
    >
      <template #append-inner>
        <v-btn
          icon
          variant="text"
          size="small"
          @click="openFolderPicker"
          :disabled="disabled"
        >
          <v-icon>mdi-folder-open</v-icon>
        </v-btn>
      </template>
    </v-text-field>

    <!-- Folder Selection Dialog -->
    <v-dialog
      v-model="showDialog"
      max-width="800px"
      persistent
    >
      <v-card>
        <v-card-title class="d-flex align-center">
          <v-icon class="me-2">mdi-folder-open</v-icon>
          Select Folder
        </v-card-title>

        <v-card-subtitle>
          Current Path: {{ displayPath }}
        </v-card-subtitle>

        <v-divider />

        <v-card-text style="height: 400px; overflow-y: auto;">
          <v-alert
            v-if="error"
            type="error"
            variant="outlined"
            class="mb-4"
          >
            {{ error }}
          </v-alert>

          <div v-if="loading" class="d-flex justify-center pa-4">
            <v-progress-circular indeterminate />
          </div>

          <v-list v-else density="compact">
            <v-list-item
              v-for="directory in directories"
              :key="directory.absolutePath"
              :class="{ 'bg-primary': selectedPath === directory.absolutePath }"
              @click="selectDirectory(directory)"
              @dblclick="navigateToDirectory(directory)"
            >
              <template #prepend>
                <v-icon>
                  {{ directory.name === '..' ? 'mdi-arrow-up' : 'mdi-folder' }}
                </v-icon>
              </template>

              <v-list-item-title>{{ directory.name }}</v-list-item-title>

              <v-list-item-subtitle v-if="directory.name !== '..'">
                {{ directory.absolutePath }}
              </v-list-item-subtitle>

              <template #append v-if="directory.name !== '..'">
                <div class="text-caption text-medium-emphasis">
                  <div>{{ formatDate(directory.lastModified) }}</div>
                  <div class="d-flex align-center">
                    <v-icon
                      size="12"
                      :color="directory.canRead ? 'success' : 'error'"
                      class="me-1"
                    >
                      mdi-eye
                    </v-icon>
                    <v-icon
                      size="12"
                      :color="directory.canWrite ? 'success' : 'error'"
                    >
                      mdi-pencil
                    </v-icon>
                  </div>
                </div>
              </template>
            </v-list-item>
          </v-list>

          <v-alert
            v-if="!loading && !hasDirectories"
            type="info"
            variant="outlined"
          >
            No directories found or access denied.
          </v-alert>
        </v-card-text>

        <v-divider />

        <v-card-actions>
          <v-text-field
            v-model="selectedPath"
            label="Selected Path"
            readonly
            density="compact"
            variant="outlined"
            class="flex-grow-1 me-2"
          />

          <v-btn
            variant="text"
            @click="closeDialog"
          >
            Cancel
          </v-btn>

          <v-btn
            color="primary"
            variant="elevated"
            :disabled="!selectedPath"
            @click="confirmSelection"
          >
            Select
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<style scoped>
.folder-picker .v-text-field {
  cursor: pointer;
}

.v-list-item {
  cursor: pointer;
}
</style>
