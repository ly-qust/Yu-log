<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';

import {
  deleteAdminComment,
  fetchAdminComments,
  replyAdminComment,
  updateAdminCommentStatus,
} from '@/api/adminComments';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { PageResult } from '@/types/api';
import type { AdminComment, AdminCommentQuery, InteractionStatus } from '@/types/interaction';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime, formatInteractionStatus } from '@/utils/format';

const pageSize = 10;
const route = useRoute();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const commentPage = ref<PageResult<AdminComment>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminCommentQuery>({
  status: route.query.status === 'PENDING' || route.query.status === 'APPROVED' || route.query.status === 'REJECTED' ? route.query.status : '',
  keyword: '',
  page: 1,
  size: pageSize,
});

async function loadComments() {
  loading.value = true;
  errorMessage.value = '';
  try {
    commentPage.value = await fetchAdminComments({
      status: filters.status || undefined,
      keyword: filters.keyword?.trim() || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '评论列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadComments();
}

async function changePage(page: number) {
  if (page < 1 || (commentPage.value.totalPages > 0 && page > commentPage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadComments();
}

async function runCommentAction(id: string, action: () => Promise<unknown>, message: string) {
  actionLoadingId.value = id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await action();
    successMessage.value = message;
    feedback.success(message);
    await loadComments();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '操作失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    actionLoadingId.value = '';
  }
}

async function changeStatus(comment: AdminComment, status: InteractionStatus) {
  await runCommentAction(
    comment.id,
    () => updateAdminCommentStatus(comment.id, status),
    `评论状态已更新为${formatInteractionStatus(status)}`,
  );
}

async function replyComment(comment: AdminComment) {
  const content = await feedback.prompt({ title: '回复评论', label: '管理员回复', initialValue: comment.adminReply || '' });
  if (content === null) {
    return;
  }
  if (!content.trim()) {
    errorMessage.value = '回复内容不能为空';
    return;
  }

  await runCommentAction(comment.id, () => replyAdminComment(comment.id, content.trim()), '评论回复已保存');
}

async function removeComment(comment: AdminComment) {
  const confirmed = await feedback.confirm({
    title: '删除评论',
    message: `确定要删除「${comment.nickname}」的评论吗？此操作不可恢复。`,
    confirmLabel: '删除评论',
    danger: true,
  });
  if (!confirmed) {
    return;
  }

  await runCommentAction(comment.id, () => deleteAdminComment(comment.id), '评论已删除');
}

onMounted(loadComments);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div>
        <p class="terminal-label text-sm">admin_comments // moderation</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">评论审核</h2>
        <p class="mt-2 text-sm text-cyber-muted">审核文章评论，支持通过、拒绝、改回待审核、回复和删除。</p>
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
          placeholder="搜索昵称、邮箱或评论内容"
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
      <table class="min-w-[1180px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">所属文章</th>
            <th class="px-4 py-3">昵称</th>
            <th class="px-4 py-3">邮箱</th>
            <th class="px-4 py-3">评论内容</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">管理员回复</th>
            <th class="px-4 py-3">提交时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="8">评论加载中...</td>
          </tr>
          <tr v-else-if="commentPage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="8">暂无评论。</td>
          </tr>
          <tr
            v-for="comment in commentPage.list"
            v-else
            :key="comment.id"
            class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5"
          >
            <td class="px-4 py-4">
              <p class="max-w-56 truncate font-semibold text-cyber-text">{{ comment.articleTitle || '-' }}</p>
              <p class="mt-1 font-mono text-[11px] text-cyber-outline">#{{ comment.articleId }}</p>
            </td>
            <td class="px-4 py-4 text-cyber-text">{{ comment.nickname }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ comment.email || '-' }}</td>
            <td class="px-4 py-4">
              <p class="max-w-xs whitespace-pre-wrap break-words leading-6 text-cyber-muted">{{ comment.content }}</p>
            </td>
            <td class="px-4 py-4">
              <span class="rounded-full border border-cyber-cyan/40 px-2 py-1 font-mono text-[11px] text-cyber-cyan">
                {{ formatInteractionStatus(comment.status) }}
              </span>
            </td>
            <td class="px-4 py-4">
              <p class="max-w-xs whitespace-pre-wrap break-words text-cyber-muted">{{ comment.adminReply || '-' }}</p>
            </td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(comment.createdAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex max-w-56 flex-wrap gap-2">
                <button v-if="comment.status !== 'APPROVED'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(comment, 'APPROVED')">
                  通过
                </button>
                <button v-if="comment.status !== 'REJECTED'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(comment, 'REJECTED')">
                  拒绝
                </button>
                <button v-if="comment.status !== 'PENDING'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(comment, 'PENDING')">
                  待审核
                </button>
                <button class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="replyComment(comment)">
                  回复
                </button>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeComment(comment)">
                  删除
                </button>
                <span v-if="actionLoadingId === comment.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="commentPage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!commentPage.hasPrevious || loading" type="button" @click="changePage(commentPage.pageNum - 1)">
        上一页
      </button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ commentPage.pageNum }} / {{ commentPage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!commentPage.hasNext || loading" type="button" @click="changePage(commentPage.pageNum + 1)">
        下一页
      </button>
    </div>
  </section>
</template>
