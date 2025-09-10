import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    port: 5173,
    // Change this if your backend runs on a different port
    proxy: {
      // Forward API requests to Spring Boot running locally
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
        // If your backend does NOT include the /api prefix,
        // uncomment the next line to strip it:
        // rewrite: (path) => path.replace(/^\/api/, ""),
      },

      // Optional: if you have WebSocket endpoints (e.g., STOMP/SockJS)
      // "/ws": {
      //   target: "ws://localhost:8080",
      //   ws: true,
      //   changeOrigin: true,
      // },
    }
  }
  })
