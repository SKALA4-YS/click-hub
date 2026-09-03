<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { waitForBackend } from '@/services/api'

const phase = ref('connecting')
const service = ref('')
const errorMessage = ref('')

let activeController
let activeTimeout
let isUnmounted = false

async function checkConnection() {
  activeController?.abort()
  clearTimeout(activeTimeout)

  const controller = new AbortController()
  activeController = controller
  phase.value = 'connecting'
  service.value = ''
  errorMessage.value = ''
  activeTimeout = setTimeout(() => controller.abort(), 90000)

  try {
    const payload = await waitForBackend({ signal: controller.signal })

    if (!isUnmounted && activeController === controller) {
      service.value = payload.service
      phase.value = 'connected'
    }
  } catch (error) {
    if (!isUnmounted && activeController === controller) {
      errorMessage.value = error instanceof Error ? error.message : 'Unknown connection error'
      phase.value = 'error'
    }
  } finally {
    if (activeController === controller) {
      clearTimeout(activeTimeout)
    }
  }
}

onMounted(checkConnection)

onBeforeUnmount(() => {
  isUnmounted = true
  activeController?.abort()
  clearTimeout(activeTimeout)
})
</script>

<template>
  <section class="backend-status" :class="`backend-status--${phase}`" aria-live="polite">
    <h2>Backend connection</h2>

    <p v-if="phase === 'connecting'">
      Connecting to Render… A free instance can take about a minute to wake up.
    </p>
    <p v-else-if="phase === 'connected'">
      Connected to <strong>{{ service }}</strong
      >.
    </p>
    <template v-else>
      <p>Could not connect to the backend.</p>
      <p class="backend-status__detail">{{ errorMessage }}</p>
      <button type="button" @click="checkConnection">Retry</button>
    </template>
  </section>
</template>

<style scoped>
.backend-status {
  margin-bottom: 2rem;
  padding: 1rem 1.25rem;
  border: 1px solid var(--color-border);
  border-radius: 0.75rem;
  background: var(--color-background-soft);
}

.backend-status h2 {
  margin: 0 0 0.5rem;
  font-size: 1rem;
  font-weight: 600;
}

.backend-status p {
  margin: 0;
}

.backend-status--connected {
  border-color: hsla(160, 100%, 37%, 0.6);
}

.backend-status--error {
  border-color: hsla(0, 75%, 55%, 0.6);
}

.backend-status__detail {
  margin-top: 0.25rem !important;
  color: var(--color-text);
  font-size: 0.875rem;
}

button {
  margin-top: 0.75rem;
  padding: 0.4rem 0.8rem;
  border: 1px solid var(--color-border);
  border-radius: 0.4rem;
  cursor: pointer;
}
</style>
