<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchArticleDetail, likeArticle } from '@/api/articles';
import { fetchArticleComments, submitArticleComment } from '@/api/comments';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { ArticleDetail } from '@/types/content';
import type { PublicComment } from '@/types/interaction';
import { getErrorMessage } from '@/utils/errors';
import { formatCount, formatDateTime } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const article = ref<ArticleDetail | null>(null);
const comments = ref<PublicComment[]>([]);
const loading = ref(false);
const commentsLoading = ref(false);
const submittingComment = ref(false);
const isCommentComposerOpen = ref(false);
const liking = ref(false);
const errorMessage = ref('');
const commentErrorMessage = ref('');
const commentSuccessMessage = ref('');

const commentForm = reactive({
  nickname: '',
  email: '',
  content: '',
});

const articleId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});

function resetCommentForm() {
  commentForm.nickname = '';
  commentForm.email = '';
  commentForm.content = '';
}

function openCommentComposer() {
  commentErrorMessage.value = '';
  commentSuccessMessage.value = '';
  isCommentComposerOpen.value = true;
}

function cancelCommentComposer() {
  resetCommentForm();
  commentErrorMessage.value = '';
  isCommentComposerOpen.value = false;
}

async function loadArticle() {
  if (!articleId.value) {
    errorMessage.value = '文章不存在或已下线';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    article.value = await fetchArticleDetail(articleId.value);
    await loadComments();
  } catch (error) {
    article.value = null;
    errorMessage.value = getErrorMessage(error, '加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function loadComments() {
  if (!articleId.value) {
    return;
  }

  commentsLoading.value = true;
  commentErrorMessage.value = '';
  try {
    comments.value = await fetchArticleComments(articleId.value);
  } catch (error) {
    commentErrorMessage.value = getErrorMessage(error, '评论加载失败，请稍后重试');
  } finally {
    commentsLoading.value = false;
  }
}

async function submitComment() {
  if (!articleId.value || submittingComment.value) {
    return;
  }

  commentErrorMessage.value = '';
  commentSuccessMessage.value = '';
  if (!commentForm.nickname.trim() || !commentForm.content.trim()) {
    commentErrorMessage.value = '请填写昵称和评论内容';
    return;
  }

  submittingComment.value = true;
  try {
    const message = await submitArticleComment(articleId.value, {
      nickname: commentForm.nickname.trim(),
      email: commentForm.email.trim() || undefined,
      content: commentForm.content.trim(),
    });
    commentSuccessMessage.value = message || '评论已提交，审核通过后展示';
    resetCommentForm();
    isCommentComposerOpen.value = false;
    await loadComments();
  } catch (error) {
    commentErrorMessage.value = getErrorMessage(error, '评论提交失败，请稍后重试');
  } finally {
    submittingComment.value = false;
  }
}

async function likeCurrentArticle() {
  if (!article.value || liking.value) {
    return;
  }

  liking.value = true;
  errorMessage.value = '';
  try {
    const likeCount = await likeArticle(article.value.id);
    article.value = {
      ...article.value,
      likeCount,
    };
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '点赞失败，请稍后重试');
  } finally {
    liking.value = false;
  }
}

onMounted(loadArticle);
watch(articleId, loadArticle);
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <button
        class="font-mono text-xs text-cyber-cyan transition hover:text-cyber-cyanBright"
        type="button"
        @click="router.push('/articles')"
      >
        返回文章列表
      </button>

      <GlassPanel v-if="loading">
        <p class="font-mono text-sm text-cyber-cyan">文章加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="errorMessage">
        <p class="terminal-label text-sm">article // detail</p>
        <h1 class="mt-4 font-display text-3xl font-semibold">文章不存在或已下线</h1>
        <p class="mt-3 text-cyber-danger">{{ errorMessage }}</p>
      </GlassPanel>

      <article v-else-if="article" class="space-y-6">
        <GlassPanel>
          <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
            <div class="max-w-3xl">
              <p class="terminal-label text-sm">
                article // {{ article.categoryName || '未分类' }}
              </p>
              <h1 class="mt-4 font-display text-4xl font-semibold leading-tight text-cyber-text">
                {{ article.title }}
              </h1>
              <p class="mt-4 text-cyber-muted">{{ article.summary || '暂无摘要。' }}</p>

              <div class="mt-5 flex flex-wrap gap-2">
                <span
                  v-for="tag in article.tags"
                  :key="tag.id"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
                >
                  #{{ tag.name }}
                </span>
              </div>
            </div>

            <div class="grid min-w-60 grid-cols-2 gap-3 rounded-lg border border-cyber-border bg-cyber-base/50 p-4">
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">发布时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(article.publishedAt) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">更新时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(article.updatedAt) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">阅读时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ article.readingTime }} 分钟</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">浏览量</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatCount(article.viewCount) }}</p>
              </div>
            </div>
          </div>

          <button
            class="mt-6 rounded-lg border border-cyber-cyan/60 px-4 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="liking"
            type="button"
            @click="likeCurrentArticle"
          >
            {{ liking ? '同步中...' : `点赞 / ${formatCount(article.likeCount)}` }}
          </button>
        </GlassPanel>

        <GlassPanel>
          <p class="terminal-label text-sm">markdown_view // 正文内容</p>
          <pre class="mt-5 whitespace-pre-wrap break-words font-sans text-base leading-8 text-cyber-text">{{ article.content }}</pre>
        </GlassPanel>

        <GlassPanel>
          <div class="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <p class="terminal-label text-sm">comment_stream // 审核后展示</p>
              <h2 class="mt-3 font-display text-2xl font-semibold">评论区</h2>
              <p class="mt-2 text-sm text-cyber-muted">欢迎留下问题、补充或复盘想法，评论会在审核通过后公开显示。</p>
            </div>
            <button
              class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan"
              type="button"
              @click="loadComments"
            >
              刷新评论
            </button>
          </div>

          <button
            v-if="!isCommentComposerOpen"
            class="mt-6 flex w-full items-center justify-between gap-3 rounded-lg border border-cyber-border bg-cyber-base/55 px-4 py-3 text-left transition hover:border-cyber-cyan/60 hover:bg-cyber-cyan/5 focus:border-cyber-cyan focus:outline-none"
            type="button"
            @click="openCommentComposer"
          >
            <span class="flex min-w-0 items-center gap-3">
              <span class="h-2.5 w-2.5 shrink-0 rounded-full bg-cyber-cyan shadow-[0_0_16px_rgba(86,246,255,0.55)]"></span>
              <span class="truncate text-sm text-cyber-muted">写下你的评论…</span>
            </span>
            <span class="shrink-0 rounded-md border border-cyber-cyan/50 px-3 py-1.5 font-mono text-xs text-cyber-cyan">
              评论
            </span>
          </button>

          <form v-else class="mt-6 grid gap-3 rounded-lg border border-cyber-border bg-cyber-base/35 p-4" @submit.prevent="submitComment">
            <div class="grid gap-3 md:grid-cols-2">
              <input
                v-model="commentForm.nickname"
                class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
                maxlength="30"
                placeholder="昵称"
                type="text"
              />
              <input
                v-model="commentForm.email"
                class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
                maxlength="128"
                placeholder="邮箱，可选"
                type="email"
              />
            </div>
            <textarea
              v-model="commentForm.content"
              class="min-h-32 rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm leading-6 text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
              maxlength="1000"
              placeholder="评论内容"
            ></textarea>
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <p class="font-mono text-xs text-cyber-outline">{{ commentForm.content.length }} / 1000</p>
              <div class="flex flex-col gap-2 sm:flex-row">
                <button
                  class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="submittingComment"
                  type="button"
                  @click="cancelCommentComposer"
                >
                  取消
                </button>
                <button
                  class="rounded-lg border border-cyber-cyan/60 px-5 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:cursor-not-allowed disabled:opacity-50"
                  :disabled="submittingComment"
                  type="submit"
                >
                  {{ submittingComment ? '提交中...' : '提交评论' }}
                </button>
              </div>
            </div>
          </form>

          <p v-if="commentSuccessMessage" class="mt-4 rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
            {{ commentSuccessMessage }}
          </p>
          <p v-if="commentErrorMessage" class="mt-4 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
            {{ commentErrorMessage }}
          </p>

          <div class="mt-6 space-y-3">
            <p v-if="commentsLoading" class="font-mono text-sm text-cyber-cyan">评论加载中...</p>
            <div v-else-if="comments.length === 0" class="rounded-lg border border-dashed border-cyber-border px-4 py-6 text-sm text-cyber-muted">
              暂无评论，欢迎留下第一条讨论
            </div>
            <div
              v-for="comment in comments"
              v-else
              :key="comment.id"
              class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4"
            >
              <div class="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                <p class="font-semibold text-cyber-text">{{ comment.nickname }}</p>
                <p class="font-mono text-xs text-cyber-outline">{{ formatDateTime(comment.createdAt) }}</p>
              </div>
              <p class="mt-3 whitespace-pre-wrap break-words text-sm leading-7 text-cyber-muted">{{ comment.content }}</p>
              <div v-if="comment.adminReply" class="mt-4 rounded-lg border border-cyber-cyan/30 bg-cyber-cyan/10 p-3">
                <p class="font-mono text-[11px] text-cyber-cyan">管理员回复 // {{ formatDateTime(comment.repliedAt) }}</p>
                <p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6 text-cyber-text">{{ comment.adminReply }}</p>
              </div>
            </div>
          </div>
        </GlassPanel>
      </article>
    </div>
  </PublicLayout>
</template>
