<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { rankedDevelopers, rankingFields, rankingPeriods } from '@/data/developerRankingFixture'

const selectedPeriod = ref('주간 랭킹')
const selectedField = ref('전체 분야')
const query = ref('')

const podium = computed(() => rankedDevelopers.slice(0, 3))
const list = computed(() =>
  rankedDevelopers.slice(3).filter((developer) => {
    const matchesField =
      selectedField.value === '전체 분야' || developer.field === selectedField.value
    const needle = query.value.trim().toLowerCase()
    return (
      matchesField &&
      (!needle || `${developer.name} ${developer.project}`.toLowerCase().includes(needle))
    )
  }),
)
</script>

<template>
  <section class="mx-auto max-w-[1120px] pb-10" aria-labelledby="developer-ranking-heading">
    <nav class="mb-4 text-xs text-body-light" aria-label="현재 위치">
      홈 <span class="px-1">›</span> 랭킹 <span class="px-1">›</span> 개발자 랭킹
    </nav>
    <div class="border-b border-divider/20 pb-7">
      <p
        class="mb-2 inline-flex rounded-full border border-primary-200 bg-primary-50 px-3 py-1 text-xs font-bold text-primary-700"
      >
        CLICK HUB MAKER AWARDS 2026
      </p>
      <div class="flex flex-wrap items-end justify-between gap-5">
        <div>
          <h1
            id="developer-ranking-heading"
            class="font-headline text-3xl font-extrabold tracking-tight"
          >
            개발자 랭킹 <span class="text-blue-500">Top Indie Makers</span>
          </h1>
          <p class="mt-2 max-w-2xl text-sm leading-6 text-body-light">
            Click-HUB의 공정한 산정 방식을 바탕으로 어뷰징 필터링을 거친 상위 메이커 명예의
            전당입니다.
          </p>
        </div>
        <button
          type="button"
          class="rounded-lg bg-primary-600 px-5 py-3 text-sm font-bold text-white"
        >
          내 프로젝트 등록하고 랭킹 도전
        </button>
      </div>
    </div>

    <div class="mt-7 flex flex-wrap items-center justify-between gap-3">
      <div
        class="flex flex-wrap gap-1 rounded-xl bg-primary-100/70 p-1"
        role="group"
        aria-label="랭킹 기간"
      >
        <button
          v-for="period in rankingPeriods"
          :key="period"
          type="button"
          :aria-label="period"
          :aria-pressed="selectedPeriod === period"
          class="rounded-lg px-4 py-2 text-sm font-semibold"
          :class="
            selectedPeriod === period
              ? 'bg-primary-600 text-white shadow-sm'
              : 'text-body-light hover:bg-white'
          "
          @click="selectedPeriod = period"
        >
          {{ period }}<span v-if="period === '주간 랭킹'"> (Weekly)</span
          ><span v-else-if="period === '월간 랭킹'"> (Monthly)</span
          ><span v-else-if="period === '역대 누적'"> (All-Time)</span><span v-else> (Rising)</span>
        </button>
      </div>
      <label
        class="flex items-center rounded-lg border border-divider/20 bg-white px-3 py-2 text-sm"
        ><span class="sr-only">메이커 또는 프로젝트 검색</span
        ><input
          v-model="query"
          class="w-48 outline-none"
          placeholder="메이커 또는 프로젝트 검색..."
      /></label>
    </div>
    <div class="mt-3 flex flex-wrap gap-2" role="group" aria-label="랭킹 분야">
      <button
        v-for="field in rankingFields"
        :key="field"
        type="button"
        :aria-label="`${field} 분야`"
        :aria-pressed="selectedField === field"
        class="rounded-lg border px-3 py-2 text-sm"
        :class="
          selectedField === field
            ? 'border-primary-600 bg-primary-600 text-white'
            : 'border-divider/20 bg-white text-body-light'
        "
        @click="selectedField = field"
      >
        {{ field }}<span v-if="field === '전체 분야'"> (All)</span>
      </button>
    </div>

    <div class="mt-8 flex items-center justify-between">
      <h2 class="font-headline text-xl font-extrabold">주간 명예의 전당 (Top 3 Podium)</h2>
      <span class="text-xs text-body-light">Updated 14 mins ago</span>
    </div>
    <div class="mt-4 grid gap-4 md:grid-cols-3">
      <article
        v-for="(developer, index) in podium"
        :key="developer.id"
        class="rounded-2xl border bg-white p-5 shadow-sm"
        :class="
          index === 0
            ? 'border-primary-600 ring-1 ring-primary-600 md:order-2'
            : 'border-divider/20'
        "
      >
        <p class="text-xs font-bold text-primary-700">
          {{ index === 0 ? 'GRAND CHAMPION' : `TOP ${index + 1} MAKER` }}
        </p>
        <RouterLink :to="`/developers/${developer.id}`" class="mt-4 flex items-center gap-3"
          ><span
            class="grid h-11 w-11 place-items-center rounded-full bg-secondary text-xs font-bold text-white"
            >{{ developer.initial }}</span
          ><span
            ><strong class="block">{{ developer.name }}</strong
            ><small class="text-body-light">{{ developer.handle }}</small></span
          ></RouterLink
        >
        <p class="mt-5 font-bold">{{ developer.project }}</p>
        <p class="mt-1 text-xs text-body-light">{{ developer.type }}</p>
        <dl class="mt-5 grid grid-cols-3 border-y border-divider/15 py-4 text-center text-xs">
          <div>
            <dt>획득 북마크</dt>
            <dd class="mt-1 font-bold">{{ developer.bookmarks }}</dd>
          </div>
          <div>
            <dt>누적 조회수</dt>
            <dd class="mt-1 font-bold">{{ developer.clicks }}k</dd>
          </div>
          <div>
            <dt>종합 스코어</dt>
            <dd class="mt-1 font-bold text-primary-700">{{ developer.score }}</dd>
          </div>
        </dl>
        <RouterLink
          :to="`/developers/${developer.id}`"
          class="mt-4 block rounded-lg border border-divider/20 py-2 text-center text-sm font-semibold"
          >프로필 보기</RouterLink
        >
      </article>
    </div>

    <div class="mt-8 grid gap-6 lg:grid-cols-[1fr_320px]">
      <div>
        <div class="mb-3 flex items-center justify-between">
          <h2 class="font-headline text-xl font-extrabold">
            순위 리스트 (4위 ~ 10위)
            <span class="rounded-full bg-primary-100 px-2 py-1 text-xs text-primary-700"
              >총 148명</span
            >
          </h2>
          <span class="text-xs text-body-light">단위 : 7일간의 유효 활동 집계치</span>
        </div>
        <div class="overflow-x-auto rounded-xl border border-divider/20 bg-white">
          <table class="w-full min-w-[690px] text-sm">
            <thead class="border-b border-divider/15 text-left text-xs text-body-light">
              <tr>
                <th class="p-4">순위</th>
                <th>메이커 (MAKER)</th>
                <th>대표 프로젝트 & 분야</th>
                <th>외부클릭 / 북마크</th>
                <th>종합 스코어</th>
                <th class="pr-4">액션</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(developer, index) in list"
                :key="developer.id"
                class="border-b border-divider/10 last:border-0"
              >
                <td class="p-4 font-bold">
                  {{ index + 4
                  }}<small class="mt-1 block text-xs text-body-light">{{ developer.trend }}</small>
                </td>
                <td>
                  <RouterLink :to="`/developers/${developer.id}`" class="flex items-center gap-2"
                    ><span
                      class="grid h-9 w-9 place-items-center rounded-full bg-secondary text-[10px] font-bold text-white"
                      >{{ developer.initial }}</span
                    ><span
                      ><strong class="block">{{ developer.name }}</strong
                      ><small class="text-body-light">{{ developer.handle }}</small></span
                    ></RouterLink
                  >
                </td>
                <td>
                  <strong>{{ developer.project }}</strong
                  ><small class="mt-1 block text-body-light">{{ developer.type }}</small>
                </td>
                <td>
                  <strong>{{ developer.clicks }} clicks</strong
                  ><small class="mt-1 block text-body-light"
                    >{{ developer.bookmarks }} 북마크</small
                  >
                </td>
                <td class="font-bold text-primary-700">{{ developer.score }}</td>
                <td class="pr-4">
                  <RouterLink
                    :to="`/developers/${developer.id}`"
                    class="grid h-8 w-8 place-items-center rounded-lg border border-divider/20 text-lg"
                    >+</RouterLink
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <nav class="mt-5 flex justify-center gap-2" aria-label="랭킹 페이지">
          <button class="h-8 w-8 rounded bg-primary-600 text-sm text-white">1</button
          ><button class="h-8 w-8 rounded text-sm">2</button
          ><button class="h-8 w-8 rounded text-sm">3</button
          ><button class="h-8 w-8 rounded text-sm">›</button>
        </nav>
      </div>
      <aside class="space-y-4">
        <section class="rounded-2xl bg-secondary p-6 text-white">
          <p class="text-xs font-bold tracking-wider text-blue-200">JOIN THE ARENA</p>
          <h2 class="mt-3 text-xl font-bold leading-7">나도 인디 메이커 랭킹에 도전하고 싶다면?</h2>
          <p class="mt-4 text-sm leading-6 text-blue-100">
            방치된 사이드 프로젝트에 새 생명을! GitHub 연동 후 1분 만에 등록하고, 실제 타겟 유저
            피드백과 함께 리더보드에 진입하세요.
          </p>
          <button class="mt-5 w-full rounded-lg bg-blue-500 px-3 py-3 text-sm font-bold">
            내 프로젝트 무료 등록하기
          </button>
        </section>
        <section class="rounded-2xl border border-divider/20 bg-white p-5">
          <h2 class="font-bold">랭킹 산정 기준 안내</h2>
          <p class="mt-3 text-sm leading-6 text-body-light">
            Click HUB 공식 기록식에 정의된 5대 지표 가중치를 실시간 반영하여 단순 조회수 어뷰징을
            엄격 차단합니다.
          </p>
          <ul class="mt-4 space-y-3 text-sm">
            <li>유효 외부 클릭 <strong class="float-right text-primary-700">35%</strong></li>
            <li>순수 좋아요 <strong class="float-right text-primary-700">25%</strong></li>
            <li>고유 피드백 댓글 <strong class="float-right text-primary-700">15%</strong></li>
            <li>메이커 구독자 증가율 <strong class="float-right text-primary-700">15%</strong></li>
          </ul>
        </section>
      </aside>
    </div>
  </section>
</template>
