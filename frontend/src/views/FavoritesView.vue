<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { mockFavoriteProjects } from '@/data/mockFavoriteProjects'

const auth = useAuthStore()
const favorites = ref([...mockFavoriteProjects])

function remove(id) {
  favorites.value = favorites.value.filter((item) => item.id !== id)
}
</script>

<template>
  <div v-if="!auth.isLoggedIn" class="py-16 text-center text-sm text-neutral-500">
    로그인이 필요합니다.
    <RouterLink to="/login" class="text-primary-600 hover:underline">로그인하러 가기</RouterLink>
  </div>

  <div v-else class="flex flex-col gap-4">
    <h1 class="text-xl font-bold">즐겨찾기 보관함</h1>

    <p v-if="favorites.length === 0" class="text-sm text-neutral-500">즐겨찾기한 프로젝트가 없습니다.</p>
    <div v-else class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="project in favorites"
        :key="project.id"
        class="flex flex-col gap-2 rounded-xl border border-neutral-200 p-4 dark:border-neutral-800"
      >
        <RouterLink :to="{ name: 'project-detail', params: { id: project.id } }" class="font-semibold hover:underline">
          {{ project.title }}
        </RouterLink>
        <p class="text-xs text-neutral-500">{{ project.category }} · {{ project.owner_name }}</p>
        <button
          type="button"
          class="mt-2 self-start text-xs font-medium text-red-600 hover:underline"
          @click="remove(project.id)"
        >
          즐겨찾기 해제
        </button>
      </div>
    </div>
  </div>
</template>
