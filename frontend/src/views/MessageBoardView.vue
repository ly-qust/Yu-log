<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { fetchMessages, submitMessage } from '@/api/messages';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { PublicMessage } from '@/types/interaction';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime } from '@/utils/format';

const pageSize = 8;
const loading = ref(false);
const submitting = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const messagePage = ref<PageResult<PublicMessage>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const form = reactive({
  nickname: '',
  email: '',
  content: '',
});

async function loadMessages(page = 1, append = false) {
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchMessages({ page, size: pageSize });
    messagePage.value = append
      ? {
          ...result,
          list: [...messagePage.value.list, ...result.list],
        }
      : result;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '留言加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitCurrentMessage() {
  errorMessage.value = '';
  successMessage.value = '';
  if (!form.nickname.trim() || !form.content.trim()) {
    errorMessage.value = '请填写昵称和留言内容';
    return;
  }

  submitting.value = true;
  try {
    const message = await submitMessage({
      nickname: form.nickname.trim(),
      email: form.email.trim() || undefined,
      content: form.content.trim(),
    });
    successMessage.value = message || '留言已提交，审核通过后展示';
    form.nickname = '';
    form.email = '';
    form.content = '';
    await loadMessages(1);
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '留言提交失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
}

onMounted(() => loadMessages(1));
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <GlassPanel>
        <div class="grid gap-6 lg:grid-cols-[1fr_0.85fr] lg:items-start">
          <div>
            <p class="terminal-label text-sm">访客终端 // 留言板</p>
            <h1 class="mt-4 font-display text-4xl font-semibold">留言板</h1>
            <p class="mt-3 max-w-2xl text-cyber-muted">
              可以在这里留下问题、建议或学习交流。留言默认进入审核队列，通过后会公开展示。
            </p>
            <div class="mt-6 grid gap-3 sm:grid-cols-3">
              <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4">
                <p class="font-mono text-[11px] uppercase text-cyber-outline">审核</p>
                <p class="mt-2 text-sm text-cyber-text">先审核后展示</p>
              </div>
              <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4">
                <p class="font-mono text-[11px] uppercase text-cyber-outline">回复</p>
                <p class="mt-2 text-sm text-cyber-text">支持管理员回复</p>
              </div>
              <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4">
                <p class="font-mono text-[11px] uppercase text-cyber-outline">交流</p>
                <p class="mt-2 text-sm text-cyber-text">友好交流</p>
              </div>
            </div>
          </div>

          <form class="rounded-lg border border-cyber-border bg-cyber-base/50 p-4" @submit.prevent="submitCurrentMessage">
            <p class="font-mono text-xs text-cyber-cyan">写一条留言</p>
            <div class="mt-4 grid gap-3">
              <input
                v-model="form.nickname"
                class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
                maxlength="30"
                placeholder="昵称"
                type="text"
              />
              <input
                v-model="form.email"
                class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
                maxlength="128"
                placeholder="邮箱，可选"
                type="email"
              />
              <textarea
                v-model="form.content"
                class="min-h-36 rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm leading-6 text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
                maxlength="1000"
                placeholder="留言内容"
              ></textarea>
              <div class="flex items-center justify-between">
                <p class="font-mono text-xs text-cyber-outline">{{ form.content.length }} / 1000</p>
                <button
                  class="rounded-lg border border-cyber-cyan/60 px-5 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="submitting"
                  type="submit"
                >
                  {{ submitting ? '提交中...' : '提交留言' }}
                </button>
              </div>
            </div>
          </form>
        </div>

        <p v-if="successMessage" class="mt-5 rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
          {{ successMessage }}
        </p>
        <p v-if="errorMessage" class="mt-5 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
          {{ errorMessage }}
        </p>
      </GlassPanel>

      <GlassPanel>
        <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p class="terminal-label text-sm">公开留言 // 实时</p>
            <h2 class="mt-3 font-display text-2xl font-semibold">公开留言</h2>
          </div>
          <button
            class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan"
            type="button"
            @click="loadMessages(1)"
          >
            刷新留言
          </button>
        </div>

        <div class="mt-6 space-y-3">
          <p v-if="loading && messagePage.list.length === 0" class="font-mono text-sm text-cyber-cyan">留言加载中...</p>
          <div v-else-if="messagePage.list.length === 0" class="rounded-lg border border-dashed border-cyber-border px-4 py-8 text-cyber-muted">
            暂无留言，欢迎留下第一条足迹
          </div>
          <div
            v-for="message in messagePage.list"
            v-else
            :key="message.id"
            class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4"
          >
            <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
              <p class="font-semibold text-cyber-text">{{ message.nickname }}</p>
              <p class="font-mono text-xs text-cyber-outline">{{ formatDateTime(message.createdAt) }}</p>
            </div>
            <p class="mt-3 whitespace-pre-wrap break-words text-sm leading-7 text-cyber-muted">{{ message.content }}</p>
            <div v-if="message.adminReply" class="mt-4 rounded-lg border border-cyber-cyan/30 bg-cyber-cyan/10 p-3">
              <p class="font-mono text-[11px] text-cyber-cyan">管理员回复 // {{ formatDateTime(message.repliedAt) }}</p>
              <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6 text-cyber-text">{{ message.adminReply }}</p>
            </div>
          </div>
        </div>

        <div v-if="messagePage.hasNext" class="mt-6 flex justify-center">
          <button
            class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="loading"
            type="button"
            @click="loadMessages(messagePage.pageNum + 1, true)"
          >
            {{ loading ? '加载中...' : '加载更多' }}
          </button>
        </div>
      </GlassPanel>
    </div>
  </PublicLayout>
</template>
