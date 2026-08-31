<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { fetchTimeline } from '@/api/timeline';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { TimelineEventItem } from '@/types/timeline';
import { getErrorMessage } from '@/utils/errors';
import { formatDate } from '@/utils/format';

const pageSize = 8;
const loading = ref(false);
const errorMessage = ref('');
const timelinePage = ref<PageResult<TimelineEventItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive({
  type: '',
  page: 1,
});

async function loadTimeline(page = filters.page, append = false) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchTimeline({
      type: filters.type.trim() || undefined,
      page,
      size: pageSize,
    });
    timelinePage.value = append
      ? {
          ...result,
          list: [...timelinePage.value.list, ...result.list],
        }
      : result;
    filters.page = result.pageNum;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadTimeline(1);
}

async function clearFilters() {
  filters.type = '';
  filters.page = 1;
  await loadTimeline(1);
}

onMounted(() => loadTimeline(1));
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <GlassPanel>
        <div class="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
          <div>
            <p class="terminal-label text-sm">timeline // live_events</p>
            <h1 class="mt-4 font-display text-3xl font-semibold text-cyber-text">学习时间线</h1>
            <p class="mt-3 max-w-2xl text-cyber-muted">
              记录课程设计、项目迭代、运维实践、实习准备和阶段性复盘。
            </p>
          </div>

          <form class="grid gap-3 sm:grid-cols-[1fr_auto_auto]" @submit.prevent="submitFilters">
            <input
              v-model="filters.type"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
              placeholder="类型，例如 PROJECT"
              type="search"
            />
            <button class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50" :disabled="loading" type="submit">
              查询
            </button>
            <button class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="clearFilters">
              清空
            </button>
          </form>
        </div>
      </GlassPanel>

      <div v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </div>

      <GlassPanel v-if="loading && timelinePage.list.length === 0">
        <p class="font-mono text-sm text-cyber-cyan">时间线加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="timelinePage.list.length === 0">
        <p class="terminal-label text-sm">timeline // empty</p>
        <h2 class="mt-3 font-display text-2xl font-semibold">暂无时间线记录</h2>
        <p class="mt-2 text-cyber-muted">清空类型筛选后再试试。</p>
      </GlassPanel>

      <div v-else class="relative">
        <div class="absolute bottom-0 left-4 top-0 hidden w-px bg-cyber-border md:block"></div>
        <div class="space-y-4">
          <article
            v-for="event in timelinePage.list"
            :key="event.id"
            class="glass-panel relative rounded-glass p-5 md:ml-10"
          >
            <span class="absolute -left-[2.85rem] top-7 hidden h-4 w-4 rounded-full border border-cyber-cyan bg-cyber-base shadow-glow md:block"></span>
            <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
              <div>
                <p class="font-mono text-xs text-cyber-cyan">{{ formatDate(event.eventDate) }}</p>
                <h2 class="mt-2 font-display text-2xl font-semibold text-cyber-text">{{ event.title }}</h2>
                <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-7 text-cyber-muted">{{ event.description || '暂无描述。' }}</p>
              </div>
              <span class="w-fit rounded-full border border-cyber-border px-2.5 py-1 font-mono text-[11px] text-cyber-muted">
                {{ event.type || 'GENERAL' }}
              </span>
            </div>
            <div class="mt-4 flex flex-wrap gap-2">
              <span
                v-for="tag in event.tags"
                :key="tag"
                class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
              >
                #{{ tag }}
              </span>
            </div>
          </article>
        </div>
      </div>

      <div v-if="timelinePage.hasNext" class="flex justify-center">
        <button
          class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan disabled:opacity-40"
          :disabled="loading"
          type="button"
          @click="loadTimeline(timelinePage.pageNum + 1, true)"
        >
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </PublicLayout>
</template>
