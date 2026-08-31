<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { deleteAdminNote, fetchAdminNotes } from '@/api/adminNotes';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { PageResult } from '@/types/api';
import type { AdminNoteQuery, NoteItem } from '@/types/note';
import { getErrorMessage } from '@/utils/errors';
import { formatBoolean, formatDateTime } from '@/utils/format';

const pageSize = 10;
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const notePage = ref<PageResult<NoteItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminNoteQuery>({
  keyword: '',
  topic: '',
  isPublic: '',
  page: 1,
  size: pageSize,
});

async function loadNotes() {
  loading.value = true;
  errorMessage.value = '';
  try {
    notePage.value = await fetchAdminNotes({
      keyword: filters.keyword?.trim() || undefined,
      topic: filters.topic?.trim() || undefined,
      isPublic: filters.isPublic === '' ? undefined : filters.isPublic,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '笔记列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadNotes();
}

async function changePage(page: number) {
  if (page < 1 || (notePage.value.totalPages > 0 && page > notePage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadNotes();
}

async function removeNote(note: NoteItem) {
  const confirmed = await feedback.confirm({
    title: '删除 Note',
    message: `确定要删除「${note.title}」吗？此操作不可恢复。`,
    confirmLabel: '删除 Note',
    danger: true,
  });
  if (!confirmed) {
    return;
  }

  actionLoadingId.value = note.id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await deleteAdminNote(note.id);
    successMessage.value = '笔记已删除';
    feedback.success('笔记已删除');
    await loadNotes();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '笔记删除失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    actionLoadingId.value = '';
  }
}

onMounted(loadNotes);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="terminal-label text-sm">admin_notes // index</p>
          <h2 class="mt-3 font-display text-3xl font-semibold">笔记管理</h2>
          <p class="mt-2 text-sm text-cyber-muted">管理数字花园笔记，支持主题筛选、公开状态和标签维护。</p>
        </div>

        <RouterLink class="inline-flex items-center justify-center rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan" to="/admin/notes/new">
          新建笔记
        </RouterLink>
      </div>

      <form class="mt-6 grid gap-3 lg:grid-cols-[1.2fr_1fr_1fr_auto]" @submit.prevent="submitFilters">
        <input v-model="filters.keyword" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan" placeholder="搜索标题或内容" type="search" />
        <input v-model="filters.topic" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan" placeholder="主题" type="search" />
        <select v-model="filters.isPublic" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan" @change="submitFilters">
          <option value="">全部状态</option>
          <option :value="true">公开</option>
          <option :value="false">隐藏</option>
        </select>
        <button class="rounded-lg border border-cyber-cyan/60 px-4 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base" type="submit">查询</button>
      </form>
    </div>

    <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
    <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

    <div class="glass-panel overflow-x-auto rounded-glass">
      <table class="min-w-[1080px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">标题</th>
            <th class="px-4 py-3">主题</th>
            <th class="px-4 py-3">标签</th>
            <th class="px-4 py-3">公开</th>
            <th class="px-4 py-3">排序</th>
            <th class="px-4 py-3">更新时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="7">笔记加载中...</td>
          </tr>
          <tr v-else-if="notePage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="7">暂无笔记。</td>
          </tr>
          <tr v-for="note in notePage.list" v-else :key="note.id" class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5">
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ note.title }}</p>
              <p class="mt-1 max-w-xs truncate text-xs text-cyber-muted">{{ note.summary || note.slug }}</p>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ note.topic || '-' }}</td>
            <td class="px-4 py-4">
              <div class="flex max-w-56 flex-wrap gap-1">
                <span v-for="tag in note.tags" :key="tag" class="rounded-full border border-cyber-border px-2 py-0.5 font-mono text-[11px] text-cyber-muted">{{ tag }}</span>
              </div>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ formatBoolean(note.isPublic) }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ note.sortOrder ?? 0 }}</td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(note.updatedAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex flex-wrap gap-2">
                <RouterLink class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" :to="`/admin/notes/${note.id}/edit`">编辑</RouterLink>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeNote(note)">删除</button>
                <span v-if="actionLoadingId === note.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="notePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!notePage.hasPrevious || loading" type="button" @click="changePage(notePage.pageNum - 1)">上一页</button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ notePage.pageNum }} / {{ notePage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!notePage.hasNext || loading" type="button" @click="changePage(notePage.pageNum + 1)">下一页</button>
    </div>
  </section>
</template>
