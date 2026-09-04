<script setup>
// 홈 화면의 "최신 프로젝트 / 내가 팔로잉한 개발자" 처럼
// 제목 + (선택) 뱃지 + 더보기 링크 + 카드 그리드로 구성되는 섹션의 공통 틀.
// 카드 자체는 부모가 slot으로 넘겨서, 어떤 카드 컴포넌트를 쓸지는 이 컴포넌트가 몰라도 되게 한다.
defineProps({
  title: {
    type: String,
    required: true,
  },
  badge: {
    type: String,
    default: null,
  },
  // 아직 라우트가 없는 화면(예: 랭킹 전체보기)은 넘기지 않으면 링크를 숨긴다.
  moreTo: {
    type: String,
    default: null,
  },
  items: {
    type: Array,
    required: true,
  },
})
</script>

<template>
  <section class="flex flex-col gap-4">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-2">
        <h2 class="font-headline text-xl font-bold text-heading-light dark:text-heading-dark">
          {{ title }}
        </h2>
        <span
          v-if="badge"
          class="rounded-full bg-status-hot/10 px-2 py-0.5 text-xs font-bold text-status-hot"
        >
          {{ badge }}
        </span>
      </div>

      <RouterLink
        v-if="moreTo"
        :to="moreTo"
        class="text-sm font-medium text-body-light hover:text-primary-600 dark:text-body-dark"
      >
        더보기 >
      </RouterLink>
    </div>

    <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
      <slot v-for="(item, index) in items" :key="item.id" :item="item" :index="index" />
    </div>
  </section>
</template>
