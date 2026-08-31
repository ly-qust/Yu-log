<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { fetchAdminComments } from '@/api/adminComments';
import { fetchAdminDashboard, type AdminDashboardStats } from '@/api/adminDashboard';
import { fetchAdminMessages } from '@/api/adminMessages';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import { getErrorMessage } from '@/utils/errors';

interface DashboardViewStats extends AdminDashboardStats {
  pendingCommentCount: number;
  pendingMessageCount: number;
}

const loading = ref(false);
const errorMessage = ref('');
const stats = ref<DashboardViewStats | null>(null);

async function loadStats() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const [base, comments, messages] = await Promise.all([
      fetchAdminDashboard(),
      fetchAdminComments({ status: 'PENDING', page: 1, size: 1 }),
      fetchAdminMessages({ status: 'PENDING', page: 1, size: 1 }),
    ]);
    stats.value = {
      ...base,
      pendingCommentCount: comments.total,
      pendingMessageCount: messages.total,
    };
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '控制台数据加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(loadStats);
</script>

<template>
  <section class="space-y-5">
    <div class="surface-muted rounded-panel p-6 md:p-8">
      <AdminPageHeader
        eyebrow="workspace // overview"
        title="把下一次维护变得更容易。"
        description="这里是内容工作台的入口：查看内容状态，直接进入写作或审核，不做无关的数据可视化。"
      >
        <template #actions>
          <div class="flex flex-wrap gap-2">
            <RouterLink to="/admin/articles/new"><BaseButton size="sm">新建文章</BaseButton></RouterLink>
            <RouterLink to="/admin/notes/new"><BaseButton size="sm" variant="secondary">新建 Note</BaseButton></RouterLink>
          </div>
        </template>
      </AdminPageHeader>
    </div>

    <div v-if="loading" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="item in 4" :key="item" class="surface-muted rounded-panel p-5"><LoadingSkeleton :lines="2" /></div>
    </div>
    <div v-else-if="errorMessage" class="rounded-control border border-danger/40 bg-danger/10 px-4 py-3 text-sm text-danger" role="alert">
      {{ errorMessage }}
    </div>
    <div v-else-if="stats" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <article v-for="item in [
        { label: '文章', value: stats.articleCount, to: '/admin/articles', tone: 'brand' },
        { label: '项目', value: stats.projectCount, to: '/admin/projects', tone: 'accent' },
        { label: 'Notes', value: stats.noteCount, to: '/admin/notes', tone: 'brand' },
        { label: '留言总数', value: stats.messageCount, to: '/admin/messages', tone: 'accent' },
      ]" :key="item.label" class="surface-muted interactive-surface rounded-panel p-5">
        <p class="admin-eyebrow">content // {{ item.label }}</p>
        <p class="mt-4 font-display text-4xl font-semibold" :class="item.tone === 'accent' ? 'text-accent' : 'text-brand'">{{ item.value }}</p>
        <RouterLink class="mt-4 inline-flex text-sm text-text-secondary hover:text-brand" :to="item.to">打开管理 →</RouterLink>
      </article>
    </div>

    <div v-if="stats" class="grid gap-5 lg:grid-cols-[1.1fr_0.9fr]">
      <section class="surface-muted rounded-panel p-6">
        <AdminPageHeader eyebrow="queue // attention" title="待处理事项" description="只显示需要你下一步操作的真实队列。" />
        <div class="mt-6 grid gap-3 sm:grid-cols-2">
          <RouterLink to="/admin/comments?status=PENDING" class="admin-queue-card">
            <span class="admin-queue-card__count">{{ stats.pendingCommentCount }}</span>
            <span><strong>待审核评论</strong><small>检查公开前的内容</small></span>
          </RouterLink>
          <RouterLink to="/admin/messages?status=PENDING" class="admin-queue-card">
            <span class="admin-queue-card__count">{{ stats.pendingMessageCount }}</span>
            <span><strong>待处理留言</strong><small>及时回复访客</small></span>
          </RouterLink>
        </div>
      </section>
      <section class="surface-muted rounded-panel p-6">
        <AdminPageHeader eyebrow="shortcuts // start" title="快速入口" description="把常用动作放在离你最近的地方。" />
        <div class="mt-6 grid gap-2">
          <RouterLink class="admin-shortcut" to="/admin/articles?status=DRAFT">继续编辑草稿 <span>→</span></RouterLink>
          <RouterLink class="admin-shortcut" to="/admin/projects/new">维护一个项目 <span>→</span></RouterLink>
          <RouterLink class="admin-shortcut" to="/admin/timeline/new">记录一个时间线节点 <span>→</span></RouterLink>
          <RouterLink class="admin-shortcut" to="/admin/site-settings">检查站点配置 <span>→</span></RouterLink>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.admin-queue-card,
.admin-shortcut { display: flex; align-items: center; gap: .8rem; border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: .7rem; padding: .75rem; color: rgb(var(--color-text-secondary)); transition: border-color 150ms ease, background-color 150ms ease; }
.admin-queue-card:hover,
.admin-shortcut:hover { border-color: rgb(var(--color-brand-primary) / .55); background: rgb(var(--color-brand-primary) / .06); color: rgb(var(--color-text-primary)); }
.admin-queue-card__count { display: grid; width: 2.5rem; height: 2.5rem; flex: 0 0 auto; place-items: center; border: 1px solid rgb(var(--color-brand-primary) / .35); border-radius: .6rem; color: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono', monospace; font-size: .9rem; }
.admin-queue-card strong, .admin-queue-card small { display: block; }
.admin-queue-card strong { color: rgb(var(--color-text-primary)); font-size: .85rem; }
.admin-queue-card small { margin-top: .15rem; color: rgb(var(--color-text-muted)); font-size: .72rem; }
.admin-shortcut { justify-content: space-between; font-size: .82rem; }
.admin-shortcut span { color: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono', monospace; }
</style>
