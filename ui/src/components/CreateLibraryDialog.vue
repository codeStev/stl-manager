
<script setup lang="ts">
import {defineEmits, defineProps, ref} from 'vue'
import FolderPicker from './FolderPicker.vue'
import {useLibrariesStore} from "@/stores/libraries.ts";

interface Props {
  modelValue: boolean
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'library-created'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const libraryStore = useLibrariesStore()

const newLibraryName = ref('')
const selectedPath = ref('')
const isCreating = ref(false)
const errorMessage = ref('')

function closeDialog() {
  emit('update:modelValue', false)
  // Reset form when closing
  newLibraryName.value = ''
  selectedPath.value = ''
  errorMessage.value = ''
}

function onFolderSelected(path: string) {
  selectedPath.value = path
  // Auto-fill library name with folder name if not already set
  if (!newLibraryName.value) {
    newLibraryName.value = path
  }
}

async function createLibrary() {
  if (!newLibraryName.value.trim() || !selectedPath.value.trim()) {
    errorMessage.value = 'Please provide both library name and select a folder'
    return
  }

  isCreating.value = true
  errorMessage.value = ''

  try {
    await libraryStore.createLibrary(newLibraryName.value.trim(), selectedPath.value.trim())

    emit('library-created')
    closeDialog()
  } catch (error: any) {
    console.error('Error creating library:', error)
    errorMessage.value = error.response?.data?.message || 'Failed to create library'
  } finally {
    isCreating.value = false
  }
}

function isFormValid(): boolean {
  return newLibraryName.value.trim().length > 0 && selectedPath.value.trim().length > 0
}
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    max-width="600px"
    persistent
  >
    <v-card>
      <v-card-title>
        <span class="text-h5">Create New Library</span>
      </v-card-title>

      <v-card-text>
        <v-container>
          <v-row>
            <v-col cols="12">
              <v-text-field
                v-model="newLibraryName"
                label="Library Name*"
                placeholder="Enter library name"
                required
                :error-messages="errorMessage && !newLibraryName.trim() ? ['Library name is required'] : []"
              />
            </v-col>

            <v-col cols="12">
              <FolderPicker
                v-model="selectedPath"
                label="Library Folder*"
                placeholder="Click to select a folder"
                @folder-selected="onFolderSelected"
              />
            </v-col>

            <v-col cols="12" v-if="errorMessage">
              <v-alert
                type="error"
                variant="text"
                :text="errorMessage"
              />
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          @click="closeDialog"
          :disabled="isCreating"
        >
          Cancel
        </v-btn>
        <v-btn
          color="primary"
          variant="elevated"
          @click="createLibrary"
          :disabled="!isFormValid() || isCreating"
          :loading="isCreating"
        >
          Create Library
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
