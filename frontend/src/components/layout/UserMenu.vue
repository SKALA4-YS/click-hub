<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const isOpen = ref(false)
const rootEl = ref(null)

function handleOutsideClick(event) {
  if (rootEl.value && !rootEl.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleOutsideClick))
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))

function handleLogout() {
  auth.logout()
  isOpen.value = false
  router.push('/')
}
</script>

<template>
  <div ref="rootEl" class="relative">
    <button
      type="button"
      class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-100 text-sm font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
      @click="isOpen = !isOpen"
    >
      {{ auth.user?.avatar_initial }}
    </button>

    <div
      v-if="isOpen"
      class="absolute right-0 z-20 mt-2 w-48 rounded-xl border border-neutral-200 bg-white py-1 shadow-lg dark:border-neutral-800 dark:bg-neutral-900"
    >
      <RouterLink
        to="/mypage"
        class="block px-4 py-2 text-sm hover:bg-neutral-100 dark:hover:bg-neutral-800"
        @click="isOpen = false"
      >
        마이페이지
      </RouterLink>
      <RouterLink
        to="/favorites"
        class="block px-4 py-2 text-sm hover:bg-neutral-100 dark:hover:bg-neutral-800"
        @click="isOpen = false"
      >
        즐겨찾기
      </RouterLink>
      <RouterLink
        to="/following"
        class="block px-4 py-2 text-sm hover:bg-neutral-100 dark:hover:bg-neutral-800"
        @click="isOpen = false"
      >
        팔로잉 관리
      </RouterLink>
      <hr class="my-1 border-neutral-200 dark:border-neutral-800" />
      <button
        type="button"
        class="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-neutral-100 dark:hover:bg-neutral-800"
        @click="handleLogout"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>
