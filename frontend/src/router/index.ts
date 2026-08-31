import { createRouter, createWebHistory } from 'vue-router';

import AboutView from '@/views/AboutView.vue';
import HomeView from '@/views/HomeView.vue';
import MessageBoardView from '@/views/MessageBoardView.vue';
import NoteDetailView from '@/views/NoteDetailView.vue';
import NotFoundView from '@/views/NotFoundView.vue';
import NotesView from '@/views/NotesView.vue';
import TimelineView from '@/views/TimelineView.vue';
import AdminArticleFormView from '@/views/admin/AdminArticleFormView.vue';
import AdminArticlesView from '@/views/admin/AdminArticlesView.vue';
import AdminCategoriesView from '@/views/admin/AdminCategoriesView.vue';
import AdminCommentsView from '@/views/admin/AdminCommentsView.vue';
import AdminDashboardView from '@/views/admin/AdminDashboardView.vue';
import AdminLoginView from '@/views/admin/AdminLoginView.vue';
import AdminMessagesView from '@/views/admin/AdminMessagesView.vue';
import AdminNoteFormView from '@/views/admin/AdminNoteFormView.vue';
import AdminNotesView from '@/views/admin/AdminNotesView.vue';
import AdminProjectFormView from '@/views/admin/AdminProjectFormView.vue';
import AdminProjectsView from '@/views/admin/AdminProjectsView.vue';
import AdminShellView from '@/views/admin/AdminShellView.vue';
import AdminSiteSettingsView from '@/views/admin/AdminSiteSettingsView.vue';
import AdminTagsView from '@/views/admin/AdminTagsView.vue';
import AdminTimelineFormView from '@/views/admin/AdminTimelineFormView.vue';
import AdminTimelineView from '@/views/admin/AdminTimelineView.vue';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/articles', name: 'articles', component: () => import('@/views/ArticlesView.vue') },
    { path: '/articles/:id', name: 'article-detail', component: () => import('@/views/ArticleDetailView.vue') },
    { path: '/projects', name: 'projects', component: () => import('@/views/ProjectsView.vue') },
    { path: '/projects/:id', name: 'project-detail', component: () => import('@/views/ProjectDetailView.vue') },
    { path: '/notes', name: 'notes', component: NotesView },
    { path: '/notes/:id', name: 'note-detail', component: NoteDetailView },
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
        { path: 'comments', name: 'admin-comments', component: AdminCommentsView },
        { path: 'messages', name: 'admin-messages', component: AdminMessagesView },
        { path: 'projects', name: 'admin-projects', component: AdminProjectsView },
        { path: 'projects/new', name: 'admin-project-new', component: AdminProjectFormView },
        { path: 'projects/:id/edit', name: 'admin-project-edit', component: AdminProjectFormView },
        { path: 'notes', name: 'admin-notes', component: AdminNotesView },
        { path: 'notes/new', name: 'admin-note-new', component: AdminNoteFormView },
        { path: 'notes/:id/edit', name: 'admin-note-edit', component: AdminNoteFormView },
        { path: 'timeline', name: 'admin-timeline', component: AdminTimelineView },
        { path: 'timeline/new', name: 'admin-timeline-new', component: AdminTimelineFormView },
        { path: 'timeline/:id/edit', name: 'admin-timeline-edit', component: AdminTimelineFormView },
        { path: 'site-settings', name: 'admin-site-settings', component: AdminSiteSettingsView },
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
