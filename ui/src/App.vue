<script setup lang="ts">
import AppBar from "@/components/AppBar.vue";
import Footer from "@/components/Footer.vue";
</script>

<template>
  <v-app>
    <v-layout>
      <AppBar />
      <v-main>
        <v-container>
          <RouterView />
        </v-container>
      </v-main>
    </v-layout>
    <Footer />
  </v-app>
</template>

<style scoped></style>
