<script setup>
import { RouterLink } from 'vue-router'

import DeveloperDetailView from '@/views/DeveloperDetailView.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light dark:text-body-dark">
      마이페이지를 확인하려면 로그인이 필요합니다.
    </p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>
  <DeveloperDetailView v-else />
</template>
