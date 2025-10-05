import { createApp } from 'vue'
import { createPinia } from 'pinia'

import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import App from './App.vue'
import router from './router'
import 'unfonts.css'
import '@mdi/font/css/materialdesignicons.css' // Ensure you are using css-loader

const app = createApp(App)

const vuetify = createVuetify({
  components,
  directives,
})

app.use(vuetify)
app.use(createPinia())
app.use(router)


app.mount('#app')
