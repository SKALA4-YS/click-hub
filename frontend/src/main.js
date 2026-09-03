import './assets/main.css'

import { createApp, watch } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useNotificationStore } from './stores/notifications'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const auth = useAuthStore(pinia)
const notifications = useNotificationStore(pinia)
window.addEventListener('clickhub:unauthorized', () => auth.clearSession())
watch(
  () => auth.user?.id,
  (userId, previousUserId) => {
    if (!userId || (previousUserId && userId !== previousUserId)) notifications.reset()
  },
)
void auth.restoreSession().catch(() => {})

app.mount('#app')
