import { createRouter, createWebHistory } from 'vue-router';

import HomeView from '@/views/HomeView.vue';
import MessageBoardView from '@/views/MessageBoardView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/articles', name: 'articles', component: () => import('@/views/ArticlesView.vue') },
    { path: '/articles/:id', name: 'article-detail', component: () => import('@/views/ArticleDetailView.vue') },
    { path: '/projects', name: 'projects', component: () => import('@/views/ProjectsView.vue') },
    { path: '/projects/:id', name: 'project-detail', component: () => import('@/views/ProjectDetailView.vue') },
    { path: '/notes', name: 'notes', component: () => import('@/views/NotesView.vue') },
    { path: '/notes/:id', name: 'note-detail', component: () => import('@/views/NoteDetailView.vue') },
    { path: '/timeline', name: 'timeline', component: () => import('@/views/TimelineView.vue') },
    { path: '/about', name: 'about', component: () => import('@/views/AboutView.vue') },
    { path: '/messages', name: 'messages', component: MessageBoardView },
    { path: '/admin/login', name: 'admin-login', component: () => import('@/views/admin/AdminLoginView.vue') },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminShellView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/AdminDashboardView.vue') },
        { path: 'articles', name: 'admin-articles', component: () => import('@/views/admin/AdminArticlesView.vue') },
        { path: 'articles/new', name: 'admin-article-new', component: () => import('@/views/admin/AdminArticleFormView.vue') },
        { path: 'articles/:id/edit', name: 'admin-article-edit', component: () => import('@/views/admin/AdminArticleFormView.vue') },
        { path: 'categories', name: 'admin-categories', component: () => import('@/views/admin/AdminCategoriesView.vue') },
        { path: 'tags', name: 'admin-tags', component: () => import('@/views/admin/AdminTagsView.vue') },
        { path: 'comments', name: 'admin-comments', component: () => import('@/views/admin/AdminCommentsView.vue') },
        { path: 'messages', name: 'admin-messages', component: () => import('@/views/admin/AdminMessagesView.vue') },
        { path: 'projects', name: 'admin-projects', component: () => import('@/views/admin/AdminProjectsView.vue') },
        { path: 'projects/new', name: 'admin-project-new', component: () => import('@/views/admin/AdminProjectFormView.vue') },
        { path: 'projects/:id/edit', name: 'admin-project-edit', component: () => import('@/views/admin/AdminProjectFormView.vue') },
        { path: 'notes', name: 'admin-notes', component: () => import('@/views/admin/AdminNotesView.vue') },
        { path: 'notes/new', name: 'admin-note-new', component: () => import('@/views/admin/AdminNoteFormView.vue') },
        { path: 'notes/:id/edit', name: 'admin-note-edit', component: () => import('@/views/admin/AdminNoteFormView.vue') },
        { path: 'timeline', name: 'admin-timeline', component: () => import('@/views/admin/AdminTimelineView.vue') },
        { path: 'timeline/new', name: 'admin-timeline-new', component: () => import('@/views/admin/AdminTimelineFormView.vue') },
        { path: 'timeline/:id/edit', name: 'admin-timeline-edit', component: () => import('@/views/admin/AdminTimelineFormView.vue') },
        { path: 'site-settings', name: 'admin-site-settings', component: () => import('@/views/admin/AdminSiteSettingsView.vue') },
        { path: 'account', name: 'admin-account', component: () => import('@/views/admin/AdminAccountView.vue') },
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
