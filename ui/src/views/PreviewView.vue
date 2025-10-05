<script setup lang="ts">
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import StlPreviewDrawer from '@/components/StlPreviewDrawer.vue'

const { selectedFileId } = storeToRefs(useSelectionStore())
</script>

<template>
  <StlPreviewDrawer :file-id="selectedFileId" />
</template>
