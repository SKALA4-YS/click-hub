<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'

import { mockFollowing } from '@/data/mockFollowing'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const following = ref([...mockFollowing])
const query = ref('')
const activeFilter = ref('전체')
const notifications = ref({ launch: true, update: true, feedback: false })

const filteredFollowing = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return following.value.filter((creator) =>
    `${creator.display_name} ${creator.handle} ${creator.project}`.toLowerCase().includes(needle),
  )
})

function unfollow(creatorId) {
  following.value = following.value.filter((creator) => creator.creator_id !== creatorId)
}
</script>

<template>
  <section v-if="!auth.isLoggedIn" class="mx-auto max-w-[1120px] py-28 text-center">
    <p class="text-sm text-body-light">팔로잉 목록을 확인하려면 로그인이 필요합니다.</p>
    <RouterLink
      to="/login"
      class="mt-5 inline-flex rounded-lg bg-primary-600 px-6 py-3 text-sm font-bold text-white"
      >로그인하러 가기</RouterLink
    >
  </section>

  <section v-else class="mx-auto max-w-[1120px] pb-14" aria-labelledby="following-heading">
    <nav class="mb-5 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-2">›</span> 마이페이지 <span class="px-2">›</span> 팔로잉 관리
    </nav>
    <header class="flex flex-wrap items-end justify-between gap-5">
      <div>
        <h1 id="following-heading" class="font-headline text-3xl font-extrabold">
          내 팔로잉 관리
          <small class="text-base font-medium text-body-light">Following Creators</small>
        </h1>
        <p class="mt-2 max-w-2xl text-sm leading-6 text-body-light">
          내가 구독하고 응원하는 인디 메이커들의 신규 프로젝트 런칭 소식과 최신 업데이트를
          실시간으로 모아봅니다.
        </p>
      </div>
      <div class="flex gap-3">
        <article
          v-for="metric in [
            ['♙', '팔로잉 메이커', '14명'],
            ['ϟ', '라이브 프로젝트', '38개'],
            ['▣', '신규 업데이트', '3건'],
          ]"
          :key="metric[1]"
          class="min-w-32 rounded-xl border border-divider/20 bg-white p-4"
        >
          <div class="flex items-center gap-2 text-xs text-body-light">
            <span
              class="grid h-7 w-7 place-items-center rounded-lg bg-primary-50 text-primary-700"
              >{{ metric[0] }}</span
            >{{ metric[1] }}
          </div>
          <strong class="mt-2 block text-lg">{{ metric[2] }}</strong>
        </article>
      </div>
    </header>

    <div
      class="mt-7 flex flex-wrap items-center justify-between gap-3 rounded-xl border border-divider/20 bg-white p-3"
    >
      <div class="flex gap-1" role="tablist" aria-label="팔로잉 필터">
        <button
          v-for="filter in ['전체', '최근 업데이트', '신규 런칭', '카테고리']"
          :key="filter"
          type="button"
          role="tab"
          :aria-selected="activeFilter === filter"
          class="rounded-lg px-4 py-2 text-xs font-semibold"
          :class="activeFilter === filter ? 'bg-primary-600 text-white' : 'text-body-light'"
          @click="activeFilter = filter"
        >
          {{ filter }} <span v-if="filter === '전체'">(14)</span
          ><span v-else-if="filter === '최근 업데이트'">(5)</span
          ><span v-else-if="filter === '신규 런칭'">●</span><span v-else>(12)</span>
        </button>
      </div>
      <div class="flex gap-2">
        <label class="flex items-center rounded-lg border border-divider/20 px-3 py-2 text-xs"
          ><span>⌕</span
          ><input
            v-model="query"
            class="ml-2 w-48 outline-none"
            placeholder="메이커 이름, 아이디, 기술 스택..." /></label
        ><button class="rounded-lg border border-divider/20 px-4 py-2 text-xs">최근 활동순⌄</button>
      </div>
    </div>

    <div class="mt-5 grid gap-6 lg:grid-cols-[1fr_320px]">
      <main>
        <div class="space-y-4">
          <article
            v-for="creator in filteredFollowing"
            :key="creator.creator_id"
            data-testid="following-card"
            class="rounded-2xl border border-divider/20 bg-white p-5 shadow-sm"
          >
            <div class="flex flex-wrap items-start gap-4">
              <span
                class="grid h-12 w-12 shrink-0 place-items-center rounded-full bg-primary-50 text-xs font-extrabold text-primary-700"
                >{{ creator.avatar_initial }}</span
              >
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center gap-2">
                  <RouterLink
                    :to="`/developers/${creator.creator_id}`"
                    class="font-headline text-lg font-extrabold hover:text-primary-600"
                    >{{ creator.display_name }}</RouterLink
                  ><span class="text-xs text-body-light">{{ creator.handle }}</span
                  ><span class="rounded-full bg-neutral-100 px-2 py-1 text-[10px]">{{
                    creator.role
                  }}</span>
                </div>
                <p class="mt-2 text-xs leading-5 text-body-light">{{ creator.bio }}</p>
                <div class="mt-3 flex flex-wrap gap-5 text-xs text-body-light">
                  <span>▣ 등록 프로젝트 {{ creator.active_projects }}개</span
                  ><span>↗ 누적 웹마크 {{ creator.clicks }}</span
                  ><a href="#" class="text-primary-600">● 이번 주 활동 1개</a>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  aria-label="새 프로젝트 알림"
                  class="grid h-9 w-9 place-items-center rounded-full bg-primary-50 text-primary-700"
                >
                  ♢</button
                ><button
                  type="button"
                  :aria-label="`${creator.display_name} 팔로우 해제`"
                  class="rounded-lg border border-divider/20 px-3 py-2 text-xs font-semibold"
                  @click="unfollow(creator.creator_id)"
                >
                  ✓ 팔로잉
                </button>
              </div>
            </div>
            <div
              class="mt-4 flex flex-wrap items-center gap-4 rounded-xl border border-divider/15 bg-neutral-50 p-3"
            >
              <span
                class="grid h-10 w-10 place-items-center rounded-lg bg-white font-bold text-primary-700"
                >▣</span
              >
              <div class="min-w-0 flex-1">
                <strong class="text-sm">{{ creator.project }}</strong>
                <div class="mt-1 flex flex-wrap gap-2">
                  <span
                    v-for="tech in creator.tech"
                    :key="tech"
                    class="rounded bg-white px-2 py-0.5 text-[10px] text-body-light"
                    >{{ tech }}</span
                  ><span class="text-[10px] text-blue-600">△ {{ creator.metric }} 북마크</span>
                </div>
              </div>
              <button
                class="rounded-lg border border-divider/20 bg-white px-4 py-2 text-xs font-semibold"
              >
                상세 보기</button
              ><button class="rounded-lg bg-blue-500 px-4 py-2 text-xs font-bold text-white">
                방문 ↗
              </button>
            </div>
          </article>
        </div>
        <nav class="mt-6 flex justify-center gap-2" aria-label="팔로잉 페이지">
          <button class="h-8 w-8 rounded">‹</button
          ><button class="h-8 w-8 rounded bg-primary-600 text-white">1</button
          ><button class="h-8 w-8 rounded">2</button><button class="h-8 w-8 rounded">3</button
          ><button class="h-8 w-8 rounded">›</button>
        </nav>
      </main>

      <aside class="space-y-4">
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <h2 class="font-bold">구독 알림 설정</h2>
          <p class="mt-1 text-xs text-body-light">Notification Preferences</p>
          <div class="mt-5 space-y-4">
            <label
              v-for="setting in [
                ['launch', '새 프로젝트 런칭 알림', '팔로우 메이커의 신규 서비스'],
                ['update', '주간 메이커 다이제스트', '매주 월요일 활동 요약'],
                ['feedback', '댓글 피드백 알림', '관심 프로젝트 새 피드백'],
              ]"
              :key="setting[0]"
              class="flex items-center justify-between gap-3 text-xs"
              ><span
                ><strong class="block">{{ setting[1] }}</strong
                ><small class="mt-1 block text-body-light">{{ setting[2] }}</small></span
              ><button
                type="button"
                role="switch"
                :aria-checked="notifications[setting[0]]"
                class="relative h-6 w-11 rounded-full"
                :class="notifications[setting[0]] ? 'bg-blue-500' : 'bg-neutral-200'"
                @click="notifications[setting[0]] = !notifications[setting[0]]"
              >
                <span
                  class="absolute top-1 h-4 w-4 rounded-full bg-white transition-all"
                  :class="notifications[setting[0]] ? 'left-6' : 'left-1'"
                ></span></button
            ></label>
          </div>
          <button class="mt-5 w-full rounded-lg bg-neutral-100 py-2.5 text-xs font-semibold">
            소식 알림 전체 관리
          </button>
        </section>
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <div class="flex items-center justify-between">
            <h2 class="font-bold">추천 인디 메이커</h2>
            <a href="#" class="text-xs text-primary-600">새로고침</a>
          </div>
          <p class="mt-1 text-xs text-body-light">Next.js, Vue, Supabase 등</p>
          <ul class="mt-4 space-y-4">
            <li
              v-for="maker in [
                ['RL', 'Ryan L.', '@ryan_dev'],
                ['MJ', 'Minjun', '@minjun_git'],
                ['TS', 'TypeSmith', '@typeforge'],
              ]"
              :key="maker[1]"
              class="flex items-center gap-3"
            >
              <span
                class="grid h-9 w-9 place-items-center rounded-full bg-neutral-100 text-[10px] font-bold"
                >{{ maker[0] }}</span
              >
              <p class="min-w-0 flex-1 text-xs">
                <strong class="block">{{ maker[1] }}</strong
                ><span class="text-body-light">{{ maker[2] }}</span>
              </p>
              <button
                class="rounded-lg bg-primary-50 px-3 py-1.5 text-[10px] font-bold text-primary-700"
              >
                + 팔로우
              </button>
            </li>
          </ul>
        </section>
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <h2 class="font-bold">메이커 커뮤니티 팁</h2>
          <p class="mt-3 text-xs leading-5 text-body-light">
            팔로우한 메이커의 런칭과 첫 번째 피드백에 응원 댓글을 남기면 서로의 프로젝트를
            발견시키는 가장 큰 동력이 됩니다.
          </p>
          <a href="#" class="mt-4 inline-block text-xs font-bold text-primary-600"
            >메이커 성장 가이드 보기 →</a
          >
        </section>
      </aside>
    </div>
  </section>
</template>
