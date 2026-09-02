<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  deleteAdminMessage,
  fetchAdminMessages,
  replyAdminMessage,
  updateAdminMessageStatus,
} from '@/api/adminMessages';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { PageResult } from '@/types/api';
import type { AdminMessage, AdminMessageQuery, InteractionStatus } from '@/types/interaction';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime, formatInteractionStatus } from '@/utils/format';

const pageSize = 10;
const route = useRoute();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const messagePage = ref<PageResult<AdminMessage>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminMessageQuery>({
  status: route.query.status === 'PENDING' || route.query.status === 'APPROVED' || route.query.status === 'REJECTED' ? route.query.status : '',
  keyword: '',
  page: 1,
  size: pageSize,
});

async function loadMessages() {
  loading.value = true;
  errorMessage.value = '';
  try {
    messagePage.value = await fetchAdminMessages({
      status: filters.status || undefined,
      keyword: filters.keyword?.trim() || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '留言列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadMessages();
}

async function changePage(page: number) {
  if (page < 1 || (messagePage.value.totalPages > 0 && page > messagePage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadMessages();
}

async function runMessageAction(id: string, action: () => Promise<unknown>, message: string) {
  actionLoadingId.value = id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await action();
    successMessage.value = message;
    feedback.success(message);
    await loadMessages();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '操作失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    actionLoadingId.value = '';
  }
}

async function changeStatus(message: AdminMessage, status: InteractionStatus) {
  await runMessageAction(
    message.id,
    () => updateAdminMessageStatus(message.id, status),
    `留言状态已更新为${formatInteractionStatus(status)}`,
  );
}

async function replyMessage(message: AdminMessage) {
  const content = await feedback.prompt({ title: '回复留言', label: '管理员回复', initialValue: message.adminReply || '' });
  if (content === null) {
    return;
  }
  if (!content.trim()) {
    errorMessage.value = '回复内容不能为空';
    return;
  }

  await runMessageAction(message.id, () => replyAdminMessage(message.id, content.trim()), '留言回复已保存');
}

async function removeMessage(message: AdminMessage) {
  const confirmed = await feedback.confirm({
    title: '删除留言',
    message: `确定要删除「${message.nickname}」的留言吗？此操作不可恢复。`,
    confirmLabel: '删除留言',
    danger: true,
  });
  if (!confirmed) {
    return;
  }

  await runMessageAction(message.id, () => deleteAdminMessage(message.id), '留言已删除');
}

onMounted(loadMessages);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div>
        <p class="terminal-label text-sm">留言 // 审核</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">留言审核</h2>
        <p class="mt-2 text-sm text-cyber-muted">审核留言板内容，支持通过、拒绝、改回待审核、回复和删除。</p>
      </div>

      <form class="mt-6 grid gap-3 lg:grid-cols-[1fr_1.6fr_auto]" @submit.prevent="submitFilters">
        <select
          v-model="filters.status"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan"
          @change="submitFilters"
        >
          <option value="">全部状态</option>
          <option value="PENDING">待审核</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已拒绝</option>
        </select>

        <input
          v-model="filters.keyword"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
          placeholder="搜索昵称、邮箱或留言内容"
          type="search"
        />

        <button
          class="rounded-lg border border-cyber-cyan/60 px-4 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base"
          type="submit"
        >
          查询
        </button>
      </form>
    </div>

    <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
      {{ errorMessage }}
    </p>

    <div class="glass-panel overflow-x-auto rounded-glass">
      <table class="min-w-[1040px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">昵称</th>
            <th class="px-4 py-3">邮箱</th>
            <th class="px-4 py-3">留言内容</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">管理员回复</th>
            <th class="px-4 py-3">提交时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="7">留言加载中...</td>
          </tr>
          <tr v-else-if="messagePage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="7">暂无留言。</td>
          </tr>
          <tr
            v-for="message in messagePage.list"
            v-else
            :key="message.id"
            class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5"
          >
            <td class="px-4 py-4 text-cyber-text">{{ message.nickname }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ message.email || '-' }}</td>
            <td class="px-4 py-4">
              <p class="max-w-sm whitespace-pre-wrap break-words leading-6 text-cyber-muted">{{ message.content }}</p>
            </td>
            <td class="px-4 py-4">
              <span class="rounded-full border border-cyber-cyan/40 px-2 py-1 font-mono text-[11px] text-cyber-cyan">
                {{ formatInteractionStatus(message.status) }}
              </span>
            </td>
            <td class="px-4 py-4">
              <p class="max-w-xs whitespace-pre-wrap break-words text-cyber-muted">{{ message.adminReply || '-' }}</p>
            </td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(message.createdAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex max-w-56 flex-wrap gap-2">
                <button v-if="message.status !== 'APPROVED'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(message, 'APPROVED')">
                  通过
                </button>
                <button v-if="message.status !== 'REJECTED'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(message, 'REJECTED')">
                  拒绝
                </button>
                <button v-if="message.status !== 'PENDING'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(message, 'PENDING')">
                  待审核
                </button>
                <button class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="replyMessage(message)">
                  回复
                </button>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeMessage(message)">
                  删除
                </button>
                <span v-if="actionLoadingId === message.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="messagePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!messagePage.hasPrevious || loading" type="button" @click="changePage(messagePage.pageNum - 1)">
        上一页
      </button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ messagePage.pageNum }} / {{ messagePage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!messagePage.hasNext || loading" type="button" @click="changePage(messagePage.pageNum + 1)">
        下一页
      </button>
    </div>
  </section>
</template>
