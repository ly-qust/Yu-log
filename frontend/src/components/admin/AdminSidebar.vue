<script setup lang="ts">
import { watch } from 'vue';
import { useRoute } from 'vue-router';

import { useAuthStore } from '@/stores/auth';

defineProps<{ open: boolean }>();
const emit = defineEmits<{ close: [] }>();
const route = useRoute();
const authStore = useAuthStore();

const groups = [
  {
    label: '内容',
    items: [
      { label: '文章', to: '/admin/articles', icon: 'W' },
      { label: '草稿', to: '/admin/articles?status=DRAFT', icon: 'D' },
      { label: '分类', to: '/admin/categories', icon: 'C' },
      { label: '标签', to: '/admin/tags', icon: '#' },
    ],
  },
  {
    label: '花园',
    items: [
      { label: '项目', to: '/admin/projects', icon: 'P' },
      { label: 'Notes', to: '/admin/notes', icon: 'N' },
      { label: 'Timeline', to: '/admin/timeline', icon: 'T' },
    ],
  },
  {
    label: '互动',
    items: [
      { label: '评论', to: '/admin/comments', icon: 'R' },
      { label: '留言', to: '/admin/messages', icon: 'M' },
    ],
  },
  {
    label: '系统',
    items: [{ label: '站点配置', to: '/admin/site-settings', icon: 'S' }],
  },
];

function isItemActive(to: string) {
  const path = to.split('?')[0];
  if (to === '/admin/articles?status=DRAFT') {
    return route.path === path && route.query.status === 'DRAFT';
  }
  return route.path === path || route.path.startsWith(`${path}/`);
}

watch(() => route.fullPath, () => emit('close'));
</script>

<template>
  <div v-if="open" class="admin-sidebar__backdrop" aria-hidden="true" @click="emit('close')"></div>
  <aside class="admin-sidebar" :class="{ 'admin-sidebar--open': open }" aria-label="Admin 主导航">
    <div class="admin-sidebar__brand">
      <RouterLink to="/admin" class="admin-brand-mark" aria-label="返回 Admin 控制台">
        <span>Y</span>
      </RouterLink>
      <div>
        <p class="admin-sidebar__kicker">YU.LOG / ADMIN</p>
        <p class="admin-sidebar__title">内容工作台</p>
      </div>
      <button class="admin-sidebar__close" type="button" aria-label="关闭导航" @click="emit('close')">×</button>
    </div>

    <div class="admin-sidebar__identity">
      <span class="admin-avatar">{{ (authStore.user?.nickname || authStore.user?.username || 'A').slice(0, 1).toUpperCase() }}</span>
      <span>
        <strong>{{ authStore.user?.nickname || authStore.user?.username || 'Admin' }}</strong>
        <small>管理员</small>
      </span>
    </div>

    <nav class="admin-sidebar__nav">
      <RouterLink to="/admin" class="admin-nav-item" :class="{ 'is-active': route.path === '/admin' }" exact-active-class="is-active">
        <span class="admin-nav-item__icon" aria-hidden="true">⌂</span>
        <span>Dashboard</span>
      </RouterLink>

      <section v-for="group in groups" :key="group.label" class="admin-nav-group">
        <p class="admin-nav-group__label">{{ group.label }}</p>
        <RouterLink
          v-for="item in group.items"
          :key="item.to"
          :to="item.to"
          class="admin-nav-item"
          :class="{ 'is-active': isItemActive(item.to) }"
          :aria-current="isItemActive(item.to) ? 'page' : undefined"
        >
          <span class="admin-nav-item__icon" aria-hidden="true">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </RouterLink>
      </section>
    </nav>

    <div class="admin-sidebar__footer">
      <RouterLink to="/" class="admin-sidebar__footer-link">↗ 查看公开站点</RouterLink>
      <p>Writing is maintenance.</p>
    </div>
  </aside>
</template>
