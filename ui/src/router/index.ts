import { createRouter, createWebHistory } from 'vue-router'
import * as path from "node:path";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/test",
      name: "test",
      component: () => import("../views/TestView.vue"), // lazy-loaded
    },
    {
      path: "/",
      name: "libraries",
      component: () => import("../views/MainView.vue"), // lazy-loaded
    },
    {
      path: "/models",
      name: "models",
      component: () => import("../views/MainView.vue"), // lazy-loaded
    },
  ],
})

export default router
