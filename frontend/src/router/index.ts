import { createRouter, createWebHistory } from 'vue-router';

import AboutView from '@/views/AboutView.vue';
import ArticleDetailView from '@/views/ArticleDetailView.vue';
import ArticlesView from '@/views/ArticlesView.vue';
import HomeView from '@/views/HomeView.vue';
import MessageBoardView from '@/views/MessageBoardView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import NotesView from '@/views/NotesView.vue';
import ProjectsView from '@/views/ProjectsView.vue';
import TimelineView from '@/views/TimelineView.vue';
import AdminArticleFormView from '@/views/admin/AdminArticleFormView.vue';
import AdminArticlesView from '@/views/admin/AdminArticlesView.vue';
import AdminCategoriesView from '@/views/admin/AdminCategoriesView.vue';
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue';
import AdminLoginView from '@/views/admin/AdminLoginView.vue';
import AdminShellView from '@/views/admin/AdminShellView.vue';
import AdminTagsView from '@/views/admin/AdminTagsView.vue';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/articles', name: 'articles', component: ArticlesView },
    { path: '/articles/:id', name: 'article-detail', component: ArticleDetailView },
    { path: '/projects', name: 'projects', component: ProjectsView },
    { path: '/notes', name: 'notes', component: NotesView },
    { path: '/timeline', name: 'timeline', component: TimelineView },
    { path: '/about', name: 'about', component: AboutView },
    { path: '/messages', name: 'messages', component: MessageBoardView },
    { path: '/admin/login', name: 'admin-login', component: AdminLoginView },
    {
      path: '/admin',
      component: AdminShellView,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: AdminDashboardView },
        { path: 'articles', name: 'admin-articles', component: AdminArticlesView },
        { path: 'articles/new', name: 'admin-article-new', component: AdminArticleFormView },
        { path: 'articles/:id/edit', name: 'admin-article-edit', component: AdminArticleFormView },
        { path: 'categories', name: 'admin-categories', component: AdminCategoriesView },
        { path: 'tags', name: 'admin-tags', component: AdminTagsView },
      ],
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
  ],
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  const requiresAuth = Boolean(to.meta.requiresAuth);

  if (to.name === 'admin-login' && authStore.isAuthenticated) {
    try {
      if (!authStore.user) {
        await authStore.fetchMe();
      }
      if (authStore.isAdmin) {
        return { name: 'admin-dashboard' };
      }
    } catch {
      authStore.clear();
    }
  }

  if (!requiresAuth) {
    return true;
  }

  if (!authStore.isAuthenticated) {
    return { name: 'admin-login', query: { redirect: to.fullPath } };
  }

  try {
    if (!authStore.user) {
      await authStore.fetchMe();
    }
  } catch {
    authStore.clear();
    return { name: 'admin-login', query: { redirect: to.fullPath } };
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    authStore.clear();
    return { name: 'admin-login' };
  }

  return true;
});

export default router;
