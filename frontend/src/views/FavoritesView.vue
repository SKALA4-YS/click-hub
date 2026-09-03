<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { mockFavoriteProjects } from '@/data/mockFavoriteProjects'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const favorites = ref([...mockFavoriteProjects])
const query = ref('')
const viewMode = ref('grid')

const visibleFavorites = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return favorites.value.filter((project) =>
    `${project.title} ${project.category} ${project.owner_name}`.toLowerCase().includes(needle),
  )
})

function remove(id) {
  favorites.value = favorites.value.filter((project) => project.id !== id)
}
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light">즐겨찾기 보관함을 확인하려면 로그인이 필요합니다.</p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>

  <section v-else class="mx-auto max-w-[1120px] pb-14" aria-labelledby="favorites-heading">
    <nav class="mb-5 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-2">›</span> 마이페이지 <span class="px-2">›</span> 즐겨찾기 보관함
    </nav>
    <header class="rounded-2xl border border-divider/20 bg-white p-6 shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-5">
        <div>
          <div class="flex items-center gap-3">
            <h1 id="favorites-heading" class="font-headline text-2xl font-extrabold">
              즐겨찾기 보관함
            </h1>
            <span class="rounded-full bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700"
              >★ 18개 저장됨</span
            >
          </div>
          <p class="mt-3 max-w-2xl text-sm leading-6 text-body-light">
            나중에 다시 보고 싶은 인디 프로젝트와 영감을 주는 혁신적인 웹서비스를 체계적으로
            모아두었습니다.
          </p>
        </div>
        <div class="flex gap-3">
          <div class="rounded-xl border border-divider/20 bg-neutral-50 px-5 py-3">
            <span class="text-xs text-body-light">분류 폴더</span
            ><strong class="mt-1 block text-sm">4개 카테고리</strong>
          </div>
          <div class="rounded-xl border border-divider/20 bg-neutral-50 px-5 py-3">
            <span class="text-xs text-body-light">활성 추적</span
            ><strong class="mt-1 block text-sm text-emerald-600">주간 +3개 추가</strong>
          </div>
        </div>
      </div>
    </header>

    <div
      class="mt-6 flex flex-wrap items-center justify-between gap-4 rounded-xl border border-divider/20 bg-white p-4"
    >
      <label
        class="flex min-w-64 flex-1 items-center gap-2 rounded-lg border border-divider/20 px-3 py-2 text-sm"
        ><span>⌕</span
        ><input
          v-model="query"
          name="favorite-search"
          class="w-full outline-none"
          placeholder="저장한 프로젝트 검색... (이름, 태그, 메모)"
      /></label>
      <div class="flex items-center gap-2">
        <button
          type="button"
          aria-label="그리드 보기"
          :aria-pressed="viewMode === 'grid'"
          class="grid h-9 w-9 place-items-center rounded-lg border border-divider/20"
          @click="viewMode = 'grid'"
        >
          ▦</button
        ><button
          type="button"
          aria-label="목록 보기"
          :aria-pressed="viewMode === 'list'"
          class="grid h-9 w-9 place-items-center rounded-lg border border-divider/20"
          @click="viewMode = 'list'"
        >
          ☰</button
        ><button
          type="button"
          class="rounded-lg border border-divider/20 px-4 py-2 text-xs font-semibold"
        >
          최근 저장순⌄
        </button>
      </div>
    </div>
    <label class="mt-4 flex items-center gap-2 text-xs text-body-light"
      ><input type="checkbox" /> 선택 전체 (0/18)</label
    >

    <div
      v-if="visibleFavorites.length"
      class="mt-5 grid gap-4"
      :class="viewMode === 'grid' ? 'sm:grid-cols-2 lg:grid-cols-3' : 'grid-cols-1'"
    >
      <article
        v-for="project in visibleFavorites"
        :key="project.id"
        data-testid="favorite-card"
        class="group overflow-hidden rounded-2xl border border-divider/20 bg-white shadow-sm"
      >
        <div
          class="relative h-44 overflow-hidden p-5"
          :class="
            project.theme === 'calendar'
              ? 'bg-[#fff5ea]'
              : project.theme === 'type'
                ? 'bg-[#f7f4ef] text-heading-light'
                : project.theme === 'chart'
                  ? 'bg-[#eefbf5] text-heading-light'
                  : 'bg-[#101522] text-white'
          "
        >
          <div v-if="project.theme === 'code'" class="font-mono text-xs leading-6 text-blue-200">
            <span class="text-fuchsia-300">export</span> generateCard = () =&gt; &#123;<br /><span
              class="pl-4"
              >return &lt;CodeSnap /&gt;</span
            ><br />&#125;
          </div>
          <div
            v-else-if="project.theme === 'calendar'"
            class="grid grid-cols-3 gap-2 pt-5 text-[10px]"
          >
            <span
              v-for="item in ['9:00 Sprint Sync', '11:00 PR Review', '15:30 Team Demo']"
              :key="item"
              class="rounded bg-white p-3 shadow-sm"
              >{{ item }}</span
            >
          </div>
          <div v-else-if="project.theme === 'type'" class="pt-5 font-serif text-5xl font-bold">
            Ag<span class="ml-5 text-xs font-sans font-normal">HEADING / BODY</span>
          </div>
          <div
            v-else-if="project.theme === 'chart'"
            class="flex h-full items-end justify-around gap-4"
          >
            <span
              v-for="height in [45, 68, 84, 56]"
              :key="height"
              class="w-8 bg-emerald-500"
              :style="{ height: `${height}%` }"
            ></span>
          </div>
          <div v-else class="space-y-4 pt-4">
            <span class="block h-2 w-2/3 rounded bg-blue-400/30"></span
            ><span class="block h-2 w-full rounded bg-blue-400/20"></span
            ><span class="block h-2 w-1/2 rounded bg-blue-400/20"></span>
          </div>
          <span
            class="absolute right-4 top-4 rounded-full bg-primary-600 px-2 py-1 text-[10px] font-bold text-white"
            >Top {{ project.id === 'taskcraft-ai' ? 5 : 20 }}</span
          >
          <span
            class="absolute bottom-4 right-4 grid h-8 w-8 place-items-center rounded-full bg-white text-primary-700"
            >★</span
          >
        </div>
        <div class="p-5">
          <div class="flex items-center justify-between">
            <span class="rounded bg-primary-50 px-2 py-1 text-[10px] font-bold text-primary-700">{{
              project.category
            }}</span
            ><span class="text-xs text-body-light">9월 {{ project.id.length }}일</span>
          </div>
          <RouterLink
            :to="{ name: 'project-detail', params: { id: project.id } }"
            class="mt-3 block font-headline text-lg font-extrabold group-hover:text-primary-600"
            >{{ project.title }}</RouterLink
          >
          <p class="mt-2 min-h-10 text-xs leading-5 text-body-light">{{ project.description }}</p>
          <div class="mt-4 flex items-center justify-between text-xs text-body-light">
            <span>▱ {{ project.owner_name }}</span
            ><strong class="text-amber-600">☆ {{ project.metric }}</strong>
          </div>
          <div class="mt-5 flex items-center justify-between border-t border-divider/15 pt-4">
            <RouterLink
              :to="{ name: 'project-detail', params: { id: project.id } }"
              class="text-xs font-bold text-primary-700"
              >방문하기 ↗</RouterLink
            >
            <div class="flex gap-3 text-body-light">
              <button type="button" aria-label="폴더로 이동">□</button
              ><button type="button" aria-label="공유">↗</button
              ><button
                type="button"
                :aria-label="`${project.title} 즐겨찾기 해제`"
                @click="remove(project.id)"
              >
                ♜
              </button>
            </div>
          </div>
        </div>
      </article>
    </div>
    <p
      v-else
      class="mt-12 rounded-xl border border-dashed border-divider/30 py-16 text-center text-sm text-body-light"
    >
      검색 조건에 맞는 즐겨찾기 프로젝트가 없습니다.
    </p>
    <footer class="mt-8 flex items-center justify-between text-xs text-body-light">
      <span>표시 중: 1–{{ visibleFavorites.length }} / 총 18개 프로젝트</span>
      <nav class="flex gap-2" aria-label="즐겨찾기 페이지">
        <button class="h-8 w-8 rounded bg-primary-600 text-white">1</button
        ><button class="h-8 w-8 rounded">2</button><button class="h-8 w-8 rounded">3</button>
      </nav>
    </footer>
  </section>
</template>
