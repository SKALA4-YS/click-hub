import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '@/auth/tokenStorage'
import HomeView from '../views/HomeView.vue'

export const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { standalone: true },
  },
  {
    path: '/onboarding',
    name: 'onboarding',
    component: () => import('../views/OnboardingView.vue'),
    meta: { standalone: true, requiresAuth: true },
  },
  {
    path: '/oauth/callback',
    name: 'oauth-callback',
    component: () => import('../views/OAuthCallbackView.vue'),
    meta: { standalone: true },
  },
  {
    path: '/signup',
    name: 'signup',
    redirect: { name: 'login' },
    meta: { standalone: true },
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('../views/MyPageView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: () => import('../views/FavoritesView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/following',
    name: 'following',
    component: () => import('../views/FollowingView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: () => import('../views/NotificationsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/projects/new',
    name: 'project-register',
    component: () => import('../views/ProjectRegisterView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/projects/:id',
    name: 'project-detail',
    component: () => import('../views/ProjectDetailView.vue'),
  },
  {
    path: '/projects/:id/edit',
    name: 'project-edit',
    component: () => import('../views/ProjectEditView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/rankings',
    name: 'rankings',
    component: () => import('../views/ProjectListView.vue'),
  },
  {
    path: '/rankings/developers',
    name: 'developer-rankings',
    component: () => import('../views/DeveloperRankingView.vue'),
  },
  {
    path: '/developers/:id',
    name: 'developer-detail',
    component: () => import('../views/DeveloperDetailView.vue'),
  },
  {
    path: '/community',
    name: 'community',
    component: () => import('../views/CommunityBoardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/community/posts/:id',
    name: 'community-post-detail',
    component: () => import('../views/CommunityPostDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/tutorials',
    name: 'tutorials',
    component: () => import('../views/TutorialsView.vue'),
  },
  {
    path: '/insights',
    name: 'insights',
    component: () => import('../views/InsightsView.vue'),
  },
  {
    path: '/admin/projects',
    name: 'admin-project-approval',
    component: () => import('../views/AdminApprovalView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/admin/projects/:id',
    name: 'admin-project-detail',
    component: () => import('../views/AdminProjectDetailView.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !getAccessToken()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
