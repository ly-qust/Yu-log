<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

import BaseButton from '@/components/common/BaseButton.vue';
import ThemeToggle from '@/components/common/ThemeToggle.vue';

const emit = defineEmits<{ menu: []; logout: [] }>();
const route = useRoute();

const titleMap: Record<string, string> = {
  'admin-dashboard': 'Dashboard',
  'admin-articles': '文章管理',
  'admin-article-new': '新建文章',
  'admin-article-edit': '编辑文章',
  'admin-categories': '分类管理',
  'admin-tags': '标签管理',
  'admin-projects': '项目管理',
  'admin-project-new': '新建项目',
  'admin-project-edit': '编辑项目',
  'admin-notes': 'Notes 管理',
  'admin-note-new': '新建 Note',
  'admin-note-edit': '编辑 Note',
  'admin-timeline': 'Timeline 管理',
  'admin-timeline-new': '新建 Timeline',
  'admin-timeline-edit': '编辑 Timeline',
  'admin-comments': '评论审核',
  'admin-messages': '留言审核',
  'admin-site-settings': '站点配置',
  'admin-account': '账号安全',
};

const title = computed(() => titleMap[String(route.name)] || 'Admin 工作台');
</script>

<template>
<header class="admin-header">
  <div class="admin-header__leading">
    <button class="admin-header__menu" type="button" aria-label="打开导航" @click="emit('menu')">☰</button>
    <div>
      <p class="admin-eyebrow">workspace // {{ route.name || 'admin' }}</p>
      <h1 class="admin-header__title">{{ title }}</h1>
    </div>
  </div>
  <div class="admin-header__actions">
    <span class="admin-header__status"><i aria-hidden="true"></i> API connected</span>
    <ThemeToggle :show-label="false" />
    <BaseButton variant="ghost" size="sm" @click="emit('logout')">退出</BaseButton>
  </div>
</header>
</template>
