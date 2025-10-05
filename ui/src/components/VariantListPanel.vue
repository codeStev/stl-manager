// src/views/panels/VariantListPanel.vue
<script setup lang="ts">
import {computed} from 'vue'
import {storeToRefs} from 'pinia'
import {useSelectionStore} from '@/stores/selection'
import {useLibraryModelsStore} from "@/stores/models.ts";
import type {ModelVariant} from "@/types/external/ModelTypes.ts";

const selection = useSelectionStore()
const { selectedLibraryId, selectedModelId, selectedVariantId } = storeToRefs(selection)

const modelStore = useLibraryModelsStore()

const entry = computed(() =>
  selectedLibraryId.value != null && selectedModelId.value != null
    ? modelStore.getVariantsByModelId(selectedLibraryId.value,selectedModelId.value)
    : null
)

const data = computed(() => entry.value ?? [])

function pickVariant(variantId: number) {
  selection.selectVariant(variantId)
}
</script>

<template>
  <v-card>
    <v-card-title>Variants</v-card-title>
    <v-divider />
    <v-card-text class="panel-scroll">
      <div v-if="selectedModelId == null" class="text-medium-emphasis">
        Select a model to see variants.
      </div>

      <template v-else>
        <v-list v-if="data">
          <v-list-item
            v-for="variant in data"
            :key="(variant as any)?.id ?? (variant as any)?.variant_id"
            :active="selectedVariantId === ((variant as any)?.id ?? (variant as any)?.variant_id)"
            @click="pickVariant((variant as any)?.id ?? (variant as any)?.variant_id)"
            density="comfortable"
            clickable
          >
            <v-list-item-title>{{ (variant as ModelVariant)?.name ?? (variant as any)?.variant_name ?? 'Variant' }}</v-list-item-title>
            <v-list-item-subtitle>{{ (variant as any)?.description ?? '' }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>

      </template>
    </v-card-text>
  </v-card>
</template>

<style scoped>
</style>
