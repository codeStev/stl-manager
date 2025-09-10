<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from "vue";

type TestResponse = {
  status: string;
  timestamp: string;
};

const loading = ref(false);
const error = ref<string | null>(null);
const data = ref<TestResponse | null>(null);

let abortController: AbortController | null = null;

async function callTest() {
  // Cancel any in-flight request
  if (abortController) abortController.abort();
  abortController = new AbortController();

  loading.value = true;
  error.value = null;

  try {
    const res = await fetch("/api/test", {
      method: "GET",
      headers: { Accept: "application/json" },
      signal: abortController.signal,
    });

    if (!res.ok) {
      throw new Error(`HTTP ${res.status} ${res.statusText}`);
    }

    const json = (await res.json()) as TestResponse;
    data.value = json;
  } catch (e: any) {
    if (e?.name !== "AbortError") {
      error.value = e?.message ?? String(e);
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => callTest());
onBeforeUnmount(() => abortController?.abort());
</script>

<template>
  <h1>You did it!</h1>

  <div style="margin: 0.5rem 0;">
    <button @click="callTest" :disabled="loading">Call /api/test</button>
  </div>

  <div v-if="loading">Loading…</div>
  <div v-else-if="error" style="color: #c00">Error: {{ error }}</div>
  <pre v-else-if="data">{{ JSON.stringify(data, null, 2) }}</pre>

  <p>
    Visit <a href="https://vuejs.org/" target="_blank" rel="noopener">vuejs.org</a> to read the
    documentation
  </p>
</template>

<style scoped></style>
