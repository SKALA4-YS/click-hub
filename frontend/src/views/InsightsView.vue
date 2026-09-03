<script setup>
import { computed, ref } from 'vue'
import TrendFilterTabs from '@/components/pages/insights/TrendFilterTabs.vue'
import TrendRankingCard from '@/components/pages/insights/TrendRankingCard.vue'
import {
  hotKeywords,
  insightFilters,
  makerOpportunities,
  rankedTrends,
  emergingStacks,
} from '@/data/mockWeeklyInsights'

const selectedFilter = ref('all')
const visibleTrends = computed(() =>
  selectedFilter.value === 'all'
    ? rankedTrends
    : rankedTrends.filter((trend) => trend.category === selectedFilter.value),
)
</script>

<template>
  <div class="mx-auto max-w-[1120px] pb-10 pt-3 sm:pt-7">
    <section
      class="rounded-2xl border border-primary-100 bg-gradient-to-br from-white via-primary-50/60 to-blue-50 p-6 dark:border-primary-800 dark:from-surface-dark-1 dark:via-primary-950 dark:to-surface-dark-2 sm:p-8"
    >
      <p class="text-xs font-bold tracking-[0.14em] text-primary-600 dark:text-primary-200">
        WEEKLY MAKER INSIGHTS
      </p>
      <div class="mt-3 flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
        <div class="max-w-2xl">
          <h1
            class="font-headline text-3xl font-bold tracking-tight text-heading-light dark:text-heading-dark sm:text-4xl"
          >
            지금 메이커들이 가장 주목하는 AI 개발 트렌드 &amp; 키워드
          </h1>
          <p class="mt-3 text-sm leading-6 text-body-light dark:text-body-dark">
            이번 주 새롭게 떠오른 개발 테마와 실제로 런칭된 프로젝트를 한눈에 살펴보세요.
          </p>
        </div>
        <RouterLink
          to="/projects/new"
          class="inline-flex shrink-0 items-center justify-center rounded-lg bg-primary-600 px-4 py-2.5 text-sm font-bold text-white hover:bg-primary-700"
          >+ 지금 내 프로젝트 등록하기</RouterLink
        >
      </div>
      <div class="mt-6 border-t border-primary-100 pt-4 dark:border-primary-800">
        <TrendFilterTabs v-model:selected="selectedFilter" :filters="insightFilters" />
      </div>
    </section>

    <div class="mt-8 grid gap-6 lg:grid-cols-[minmax(0,1fr)_300px]">
      <main>
        <div class="mb-4 flex items-center justify-between gap-3">
          <h2
            class="border-l-4 border-primary-500 pl-3 font-headline text-lg font-bold text-heading-light dark:text-heading-dark"
          >
            이번 주 실시간 급상승 키워드 TOP 10
            <span class="font-sans text-xs font-normal text-body-light dark:text-body-dark"
              >(Search &amp; Interest Trends)</span
            >
          </h2>
          <span class="text-xs text-body-light dark:text-body-dark"
            >실시간 12시간 주기 업데이트</span
          >
        </div>
        <div class="space-y-4">
          <TrendRankingCard v-for="trend in visibleTrends" :key="trend.id" :trend="trend" />
        </div>
      </main>

      <aside class="space-y-4">
        <section
          class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <div class="flex items-center justify-between">
            <h2 class="font-headline font-bold text-heading-light dark:text-heading-dark">
              이번 주 핫검색어 랭킹
            </h2>
            <span class="text-xs text-primary-600">1–8위</span>
          </div>
          <ol class="mt-4 space-y-3">
            <li
              v-for="(keyword, index) in hotKeywords"
              :key="keyword"
              class="flex items-center gap-3 text-sm"
            >
              <span class="w-4 text-center text-xs font-bold text-primary-600">{{ index + 1 }}</span
              ><span class="truncate text-body-light dark:text-body-dark">{{ keyword }}</span>
            </li>
          </ol>
        </section>
        <section
          class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline font-bold text-heading-light dark:text-heading-dark">
            메이커를 위한 주간 런칭 액션 플랜
          </h2>
          <div class="mt-4 rounded-lg bg-base-light p-4 dark:bg-base-dark">
            <p class="text-xs font-bold text-primary-600">지금 시작하기 좋은 아이디어</p>
            <p class="mt-2 text-sm font-bold text-heading-light dark:text-heading-dark">
              슬랙 연동 팀 노하우 Q&amp;A 봇
            </p>
            <p class="mt-1 text-xs leading-5 text-body-light dark:text-body-dark">
              사내 문서 10개만 임베딩해 슬랙 채널에서 즉시 대답하는 봇을 3일 만에 만들어 보세요.
            </p>
          </div>
          <RouterLink
            to="/projects/new"
            class="mt-4 flex justify-center rounded-lg bg-primary-600 px-3 py-2.5 text-sm font-bold text-white hover:bg-primary-700"
            >추천 스타터 템플릿 열기</RouterLink
          >
        </section>
        <section
          class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <h2 class="font-headline font-bold text-heading-light dark:text-heading-dark">
            매주 월요일 아침 트렌드 리포트 구독
          </h2>
          <p class="mt-3 text-xs leading-5 text-body-light dark:text-body-dark">
            가장 먼저 뜨는 오픈소스와 실전 수익화 모델을 메일로 받아보세요.
          </p>
          <input
            aria-label="리포트 구독 이메일"
            class="mt-4 w-full rounded-lg border border-divider/20 bg-base-light px-3 py-2 text-sm dark:bg-base-dark"
            placeholder="이메일 주소를 입력하세요"
            type="email"
          /><button
            type="button"
            class="mt-2 w-full rounded-lg bg-secondary px-3 py-2.5 text-sm font-bold text-white"
          >
            무료 구독하기 (무료 100%)
          </button>
        </section>
      </aside>
    </div>

    <section id="maker-opportunities" data-testid="maker-opportunities" class="mt-10">
      <div class="flex items-end justify-between gap-4">
        <h2
          class="border-l-4 border-blue-500 pl-3 font-headline text-xl font-bold text-heading-light dark:text-heading-dark"
        >
          이번 주 주목해야 할 AI 프로덕트 기회
          <span class="font-sans text-sm font-normal text-body-light dark:text-body-dark"
            >(Maker Insights &amp; Opportunities)</span
          >
        </h2>
        <span class="text-xs text-body-light dark:text-body-dark">수익 전환 높은 3대 영역</span>
      </div>
      <div class="mt-5 space-y-3">
        <article
          v-for="opportunity in makerOpportunities"
          :key="opportunity.id"
          class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <div class="flex items-center justify-between gap-3">
            <h3 class="font-headline font-bold text-heading-light dark:text-heading-dark">
              <span
                class="mr-2 inline-flex h-6 w-6 items-center justify-center rounded-md bg-primary-50 text-xs text-primary-600 dark:bg-primary-900"
                >{{ opportunity.marker }}</span
              >{{ opportunity.title }}
            </h3>
            <span
              class="shrink-0 rounded-full bg-success/10 px-2 py-1 text-xs font-bold text-success"
              >{{ opportunity.metric }}</span
            >
          </div>
          <p class="mt-3 text-sm leading-6 text-body-light dark:text-body-dark">
            {{ opportunity.summary }}
          </p>
        </article>
      </div>
    </section>

    <section class="mt-10">
      <div class="flex items-end justify-between gap-4">
        <h2
          class="border-l-4 border-success pl-3 font-headline text-xl font-bold text-heading-light dark:text-heading-dark"
        >
          개발자들이 실제로 가장 많이 쓰는 신규 기술 스택 TOP 4
        </h2>
        <RouterLink
          to="/projects/new"
          class="text-sm font-bold text-primary-600 hover:text-primary-700"
          >기술 스택 분석 더 보기 →</RouterLink
        >
      </div>
      <div class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <article
          v-for="stack in emergingStacks"
          :key="stack.id"
          class="rounded-xl border border-divider/15 bg-surface-light-1 p-5 dark:border-divider/25 dark:bg-surface-dark-1"
        >
          <div class="flex items-start justify-between gap-2">
            <h3 class="font-headline font-bold text-heading-light dark:text-heading-dark">
              {{ stack.title }}
            </h3>
            <span class="shrink-0 text-xs font-bold text-primary-600">{{ stack.metric }}</span>
          </div>
          <p class="mt-3 text-sm leading-6 text-body-light dark:text-body-dark">
            {{ stack.summary }}
          </p>
          <div class="mt-4 flex flex-wrap gap-1.5">
            <span
              v-for="tag in stack.tags"
              :key="tag"
              class="rounded bg-primary-50 px-2 py-1 text-[11px] font-semibold text-primary-600 dark:bg-primary-900 dark:text-primary-100"
              >{{ tag }}</span
            >
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
