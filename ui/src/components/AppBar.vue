<script setup lang="ts">
import { ref } from 'vue'
import {useLibrariesStore} from "@/stores/libraries.ts";
import {storeToRefs} from "pinia";
import {onMounted, onBeforeUnmount} from "vue";
const drawer = ref(null)
const store = useLibrariesStore()
const { items, isLoading, isFinished, error } = storeToRefs(store)

function refresh() {
  store.loadAll()
}

onMounted(refresh)
onBeforeUnmount(() => store.abort())

</script>

<script lang="ts">
export default {
}
</script>

<template>
    <v-navigation-drawer permanent>
      <v-container>
      <v-skeleton-loader v-if="isLoading" type="list-item-avatar"></v-skeleton-loader>
      <v-container v-else-if="error" style="color:#c00">Error: {{ error }}</v-container>
      <v-list v-else-if="items.length">
        <v-list-item v-for="item in items" v-bind:src="item.library_path" :key="item.library_id">
          <v-list-item-title>{{ item.library_name }}</v-list-item-title>
          <v-list-item-subtitle>{{ item.library_path }}</v-list-item-subtitle>
          <v-list-item-action></v-list-item-action>
        </v-list-item>
      </v-list>
      <v-container v-else-if="isFinished">No libraries found.</v-container>
      </v-container>
    </v-navigation-drawer>
  <v-app-bar :elevation="2" color="primary" dark>
    <template v-slot:prepend>
      <v-input width="20"></v-input>
    </template>
    <v-app-bar-title>STL-Manager</v-app-bar-title>
  </v-app-bar>
</template>
<style scoped>

</style>
