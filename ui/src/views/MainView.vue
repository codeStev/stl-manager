<script setup lang="ts">
import ModelListPanel from "@/components/ModelListPanel.vue";
import VariantListPanel from "@/components/VariantListPanel.vue";
import FileListPanel from "@/components/FileListPanel.vue";
import ArtistListPanel from "@/components/ArtistListPanel.vue";
import PreviewView from "@/views/PreviewView.vue";
</script>

<template>
  <v-container class="pa-2" fluid>
    <v-row dense>
      <!-- Library panel presumably sits elsewhere in your layout -->
      <v-col cols="12" md="3">
        <ArtistListPanel />
      </v-col>

      <v-col cols="12" md="3">
        <ModelListPanel />
      </v-col>

      <v-col cols="12" md="3">
        <VariantListPanel />
      </v-col>

      <v-col cols="12" md="3">
        <FileListPanel />
      </v-col>
    </v-row>
    <PreviewView></PreviewView>
  </v-container>
</template>

<style scoped>
/* Optional: make lists scroll independently and fill height */
:deep(.panel-scroll) {
  max-height: calc(100vh - 96px); /* adjust if you have app-bar, etc. */
  overflow: auto;
}
</style>
