<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { http } from '@/api/http';
import type { Result } from '@/types/api';
import { getErrorMessage } from '@/utils/errors';

interface DashboardStats {
  articleCount: number;
  messageCount: number;
  projectCount: number;
  noteCount: number;
}

const stats = ref<DashboardStats | null>(null);
const loading = ref(false);
const errorMessage = ref('');

async function loadStats() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await http.get<Result<DashboardStats>>('/admin/dashboard');
    stats.value = response.data.data;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '控制台数据加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(loadStats);
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <p class="terminal-label text-sm">dashboard // status</p>
    <h2 class="mt-3 font-display text-3xl font-semibold">系统控制台</h2>
    <p class="mt-2 text-sm text-cyber-muted">当前登录：Yu / 管理员</p>

    <p v-if="loading" class="mt-8 font-mono text-sm text-cyber-cyan">控制台数据加载中...</p>
    <p v-else-if="errorMessage" class="mt-8 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
      {{ errorMessage }}
    </p>

    <div v-else class="mt-8 grid gap-4 md:grid-cols-4">
      <div class="rounded-lg border border-cyber-border bg-cyber-bg/50 p-4">
        <p class="font-mono text-xs uppercase text-cyber-muted">文章数量</p>
        <p class="mt-2 font-display text-3xl font-semibold text-cyber-cyanBright">{{ stats?.articleCount ?? 0 }}</p>
      </div>
      <div class="rounded-lg border border-cyber-border bg-cyber-bg/50 p-4">
        <p class="font-mono text-xs uppercase text-cyber-muted">留言数量</p>
        <p class="mt-2 font-display text-3xl font-semibold text-cyber-cyanBright">{{ stats?.messageCount ?? 0 }}</p>
      </div>
      <div class="rounded-lg border border-cyber-border bg-cyber-bg/50 p-4">
        <p class="font-mono text-xs uppercase text-cyber-muted">项目数量</p>
        <p class="mt-2 font-display text-3xl font-semibold text-cyber-cyanBright">{{ stats?.projectCount ?? 0 }}</p>
      </div>
      <div class="rounded-lg border border-cyber-border bg-cyber-bg/50 p-4">
        <p class="font-mono text-xs uppercase text-cyber-muted">笔记数量</p>
        <p class="mt-2 font-display text-3xl font-semibold text-cyber-cyanBright">{{ stats?.noteCount ?? 0 }}</p>
      </div>
    </div>
  </section>
</template>
