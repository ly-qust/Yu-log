<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { deleteAdminTimelineEvent, fetchAdminTimeline } from '@/api/adminTimeline';
import type { PageResult } from '@/types/api';
import type { AdminTimelineQuery, TimelineEventItem } from '@/types/timeline';
import { getErrorMessage } from '@/utils/errors';
import { formatBoolean, formatDate, formatDateTime } from '@/utils/format';

const pageSize = 10;
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const timelinePage = ref<PageResult<TimelineEventItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminTimelineQuery>({
  keyword: '',
  type: '',
  page: 1,
  size: pageSize,
});

async function loadTimeline() {
  loading.value = true;
  errorMessage.value = '';
  try {
    timelinePage.value = await fetchAdminTimeline({
      keyword: filters.keyword?.trim() || undefined,
      type: filters.type?.trim() || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadTimeline();
}

async function changePage(page: number) {
  if (page < 1 || (timelinePage.value.totalPages > 0 && page > timelinePage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadTimeline();
}

async function removeEvent(event: TimelineEventItem) {
  if (!window.confirm(`确认删除时间线「${event.title}」吗？`)) {
    return;
  }

  actionLoadingId.value = event.id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await deleteAdminTimelineEvent(event.id);
    successMessage.value = '时间线已删除';
    await loadTimeline();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线删除失败，请稍后重试');
  } finally {
    actionLoadingId.value = '';
  }
}

onMounted(loadTimeline);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="terminal-label text-sm">admin_timeline // index</p>
          <h2 class="mt-3 font-display text-3xl font-semibold">时间线管理</h2>
          <p class="mt-2 text-sm text-cyber-muted">管理学习轨迹、项目节点和阶段复盘。</p>
        </div>

        <RouterLink class="inline-flex items-center justify-center rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan" to="/admin/timeline/new">
          新建时间线
        </RouterLink>
      </div>

      <form class="mt-6 grid gap-3 lg:grid-cols-[1.4fr_1fr_auto]" @submit.prevent="submitFilters">
        <input v-model="filters.keyword" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan" placeholder="搜索标题或描述" type="search" />
        <input v-model="filters.type" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan" placeholder="类型" type="search" />
        <button class="rounded-lg border border-cyber-cyan/60 px-4 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base" type="submit">查询</button>
      </form>
    </div>

    <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
    <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

    <div class="glass-panel overflow-x-auto rounded-glass">
      <table class="min-w-[1040px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">标题</th>
            <th class="px-4 py-3">日期</th>
            <th class="px-4 py-3">类型</th>
            <th class="px-4 py-3">标签</th>
            <th class="px-4 py-3">可见</th>
            <th class="px-4 py-3">创建时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="7">时间线加载中...</td>
          </tr>
          <tr v-else-if="timelinePage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="7">暂无时间线。</td>
          </tr>
          <tr v-for="event in timelinePage.list" v-else :key="event.id" class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5">
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ event.title }}</p>
              <p class="mt-1 max-w-xs truncate text-xs text-cyber-muted">{{ event.description || '-' }}</p>
            </td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-cyan">{{ formatDate(event.eventDate) }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ event.type || '-' }}</td>
            <td class="px-4 py-4">
              <div class="flex max-w-56 flex-wrap gap-1">
                <span v-for="tag in event.tags" :key="tag" class="rounded-full border border-cyber-border px-2 py-0.5 font-mono text-[11px] text-cyber-muted">{{ tag }}</span>
              </div>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ formatBoolean(event.visible) }}</td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(event.createdAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex flex-wrap gap-2">
                <RouterLink class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" :to="`/admin/timeline/${event.id}/edit`">编辑</RouterLink>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeEvent(event)">删除</button>
                <span v-if="actionLoadingId === event.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="timelinePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!timelinePage.hasPrevious || loading" type="button" @click="changePage(timelinePage.pageNum - 1)">上一页</button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ timelinePage.pageNum }} / {{ timelinePage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!timelinePage.hasNext || loading" type="button" @click="changePage(timelinePage.pageNum + 1)">下一页</button>
    </div>
  </section>
</template>
