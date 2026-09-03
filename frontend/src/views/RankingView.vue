<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import ProjectCard from '@/components/project/ProjectCard.vue'
import { categories } from '@/data/mockProjects'
import { fetchTop100 } from '@/api/projects'

const projects = ref([])
const isLoading = ref(true)
const activeCategory = ref('all')

const filteredProjects = computed(() =>
  activeCategory.value === 'all'
    ? projects.value
    : projects.value.filter((project) => project.category === activeCategory.value),
)

onMounted(async () => {
  projects.value = await fetchTop100()
  isLoading.value = false
})
</script>

<template>
  <section class="flex flex-col gap-6">
    <nav class="text-sm text-neutral-500">
      <RouterLink to="/" class="hover:text-primary-600">홈</RouterLink>
      <span class="mx-1">&gt;</span>
      <span>Top 100</span>
    </nav>

    <div>
      <h1 class="font-headline text-2xl font-extrabold">Top 100 사이트 전체보기</h1>
      <p class="mt-1 text-sm text-neutral-500">
        인디 메이커와 개발자들의 인기 프로덕트를 한눈에 탐색해보세요. (총 {{ projects.length }}개
        프로젝트 등록)
      </p>
    </div>

    <div class="flex flex-wrap items-center justify-between gap-3">
      <div class="flex flex-wrap gap-2">
        <button
          v-for="category in categories"
          :key="category.slug"
          type="button"
          class="rounded-full border px-4 py-1.5 text-sm font-medium transition"
          :class="
            activeCategory === category.slug
              ? 'border-secondary bg-secondary text-white'
              : 'border-neutral-200 text-neutral-600 hover:bg-neutral-50 dark:border-white/10 dark:text-neutral-300 dark:hover:bg-white/5'
          "
          @click="activeCategory = category.slug"
        >
          {{ category.name }}
        </button>
      </div>
      <span class="text-sm text-neutral-500"
        >{{ filteredProjects.length }}개 프로젝트 · 인기순 (Top 100)</span
      >
    </div>

    <p v-if="isLoading" class="text-sm text-neutral-500">불러오는 중...</p>
    <p v-else-if="filteredProjects.length === 0" class="text-sm text-neutral-500">
      이 카테고리에는 아직 등록된 프로젝트가 없습니다.
    </p>
    <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      <ProjectCard
        v-for="project in filteredProjects"
        :key="project.id"
        :project="project"
        :rank="projects.indexOf(project) + 1"
      />
    </div>
  </section>
</template>
