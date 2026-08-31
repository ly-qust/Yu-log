<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { fetchNotes } from '@/api/notes';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { NoteItem } from '@/types/note';
import { getErrorMessage } from '@/utils/errors';
import { formatDate } from '@/utils/format';

const pageSize = 8;
const loading = ref(false);
const errorMessage = ref('');
const notePage = ref<PageResult<NoteItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive({
  keyword: '',
  topic: '',
  page: 1,
});

async function loadNotes() {
  loading.value = true;
  errorMessage.value = '';
  try {
    notePage.value = await fetchNotes({
      keyword: filters.keyword.trim() || undefined,
      topic: filters.topic.trim() || undefined,
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

async function clearFilters() {
  filters.keyword = '';
  filters.topic = '';
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

onMounted(loadNotes);
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <GlassPanel>
        <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="terminal-label text-sm">digital_garden // live_notes</p>
            <h1 class="mt-4 font-display text-3xl font-semibold text-cyber-text">数字花园笔记</h1>
            <p class="mt-3 max-w-2xl text-cyber-muted">
              这些笔记更像持续生长的节点，用来沉淀课程设计、实习准备、排障记录和工程化想法。
            </p>
          </div>

          <form class="grid gap-3 lg:min-w-[560px] lg:grid-cols-[1.3fr_1fr_auto]" @submit.prevent="submitFilters">
            <input
              v-model="filters.keyword"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
              placeholder="搜索标题或内容"
              type="search"
            />
            <input
              v-model="filters.topic"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
              placeholder="主题，例如 Linux"
              type="search"
            />
            <button
              class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50"
              :disabled="loading"
              type="submit"
            >
              查询
            </button>
          </form>
        </div>

        <button class="mt-4 font-mono text-xs text-cyber-cyan transition hover:text-cyber-cyanBright" type="button" @click="clearFilters">
          清空筛选
        </button>
      </GlassPanel>

      <div v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </div>

      <GlassPanel v-if="loading">
        <p class="font-mono text-sm text-cyber-cyan">笔记加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="notePage.list.length === 0">
        <p class="terminal-label text-sm">notes // empty</p>
        <h2 class="mt-3 font-display text-2xl font-semibold">暂无公开笔记</h2>
        <p class="mt-2 text-cyber-muted">换个关键词、主题或清空筛选试试。</p>
      </GlassPanel>

      <div v-else class="grid gap-4 md:grid-cols-2">
        <article
          v-for="note in notePage.list"
          :key="note.id"
          class="glass-panel rounded-glass p-5 transition hover:border-cyber-cyan/70 hover:shadow-glow"
        >
          <div class="flex flex-wrap items-center gap-2">
            <span class="rounded-full border border-cyber-cyan/40 px-2.5 py-1 font-mono text-[11px] text-cyber-cyan">
              {{ note.topic || '未分类主题' }}
            </span>
            <span class="font-mono text-xs text-cyber-outline">{{ formatDate(note.createdAt) }}</span>
          </div>

          <RouterLink :to="`/notes/${note.id}`" class="mt-4 block font-display text-2xl font-semibold text-cyber-text transition hover:text-cyber-cyan">
            {{ note.title }}
          </RouterLink>
          <p class="mt-2 min-h-14 text-sm leading-7 text-cyber-muted">{{ note.summary || '暂无摘要。' }}</p>

          <div class="mt-4 flex flex-wrap gap-2">
            <span
              v-for="tag in note.tags"
              :key="tag"
              class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
            >
              #{{ tag }}
            </span>
          </div>

          <div class="mt-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p class="font-mono text-xs text-cyber-outline">
              创建 {{ formatDate(note.createdAt) }} · 更新 {{ formatDate(note.updatedAt) }}
            </p>
            <RouterLink
              class="w-fit rounded-lg border border-cyber-cyan/60 px-3 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base"
              :to="`/notes/${note.id}`"
            >
              查看笔记
            </RouterLink>
          </div>
        </article>
      </div>

      <div v-if="notePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
        <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!notePage.hasPrevious || loading" type="button" @click="changePage(notePage.pageNum - 1)">
          上一页
        </button>
        <p class="font-mono text-xs text-cyber-muted">第 {{ notePage.pageNum }} / {{ notePage.totalPages }} 页 · 共 {{ notePage.total }} 条笔记</p>
        <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!notePage.hasNext || loading" type="button" @click="changePage(notePage.pageNum + 1)">
          下一页
        </button>
      </div>
    </div>
  </PublicLayout>
</template>
