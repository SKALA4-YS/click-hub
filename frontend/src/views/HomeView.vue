<script setup>
import { computed, ref, watchEffect } from 'vue'
import { RouterLink } from 'vue-router'
import ProjectCard from '@/components/project/ProjectCard.vue'
import { categories, mockProjects } from '@/data/mockProjects'
import { mockFollowing } from '@/data/mockFollowing'
import { fetchFeed, fetchTop100 } from '@/api/projects'

const activeCategory = ref('all')
const feedProjects = ref([])
const isLoading = ref(true)
const top100 = ref([])

const followedNames = new Set(mockFollowing.map((creator) => creator.display_name))
const followingProjects = computed(() =>
  mockProjects.filter((project) => followedNames.has(project.owner.display_name)),
)

watchEffect(async () => {
  isLoading.value = true
  feedProjects.value = await fetchFeed({ category: activeCategory.value })
  isLoading.value = false
})

fetchTop100().then((ranked) => {
  top100.value = ranked.slice(0, 3)
})
</script>

<template>
  <section class="flex flex-col gap-10">
    <div class="flex flex-wrap gap-2">
      <button
        v-for="category in categories"
        :key="category.slug"
        type="button"
        class="rounded-full px-4 py-1.5 text-sm font-medium transition"
        :class="
          activeCategory === category.slug
            ? 'bg-secondary text-white'
            : 'bg-neutral-100 text-neutral-600 hover:bg-neutral-200 dark:bg-white/5 dark:text-neutral-300 dark:hover:bg-white/10'
        "
        @click="activeCategory = category.slug"
      >
        {{ category.name }}
      </button>
    </div>

    <div>
      <div class="mb-3 flex items-center gap-2">
        <h2 class="font-headline text-lg font-bold">Top 100</h2>
        <span
          class="rounded bg-primary-100 px-1.5 py-0.5 text-[11px] font-bold text-primary-700 dark:bg-primary-900 dark:text-primary-100"
        >
          HOT
        </span>
        <RouterLink to="/rankings" class="ml-auto text-sm text-neutral-500 hover:text-primary-600">
          더보기 &gt;
        </RouterLink>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ProjectCard
          v-for="(project, index) in top100"
          :key="project.id"
          :project="project"
          :rank="index + 1"
        />
      </div>
    </div>

    <div>
      <div class="mb-3 flex items-center gap-2">
        <h2 class="font-headline text-lg font-bold">맞춤 추천</h2>
        <span class="text-sm text-neutral-500">인기·최신을 함께 보여줍니다</span>
        <RouterLink to="/rankings" class="ml-auto text-sm text-neutral-500 hover:text-primary-600">
          더보기 &gt;
        </RouterLink>
      </div>

      <p v-if="isLoading" class="text-sm text-neutral-500">불러오는 중...</p>
      <p v-else-if="feedProjects.length === 0" class="text-sm text-neutral-500">
        이 카테고리에는 아직 등록된 프로젝트가 없습니다.
      </p>
      <div v-else class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ProjectCard v-for="project in feedProjects" :key="project.id" :project="project" />
      </div>
    </div>

    <div v-if="followingProjects.length > 0">
      <div class="mb-3 flex items-center gap-2">
        <h2 class="font-headline text-lg font-bold">내가 팔로잉한 개발자</h2>
        <RouterLink to="/following" class="ml-auto text-sm text-neutral-500 hover:text-primary-600">
          더보기 &gt;
        </RouterLink>
      </div>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <ProjectCard v-for="project in followingProjects" :key="project.id" :project="project" />
      </div>
    </div>
  </section>
</template>
