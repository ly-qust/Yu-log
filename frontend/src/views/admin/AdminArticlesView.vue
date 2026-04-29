<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import {
  deleteAdminArticle,
  fetchAdminArticles,
  updateAdminArticleStatus,
  updateAdminArticleTop,
} from '@/api/adminArticles';
import { fetchAdminCategories } from '@/api/adminCategories';
import type { PageResult } from '@/types/api';
import type { AdminArticleListItem, AdminArticleQuery, AdminCategory, ArticleStatus } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';
import { formatArticleStatus, formatCount, formatDateTime } from '@/utils/format';

const pageSize = 10;
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const categories = ref<AdminCategory[]>([]);
const articlePage = ref<PageResult<AdminArticleListItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminArticleQuery>({
  keyword: '',
  categoryId: '',
  status: '',
  page: 1,
  size: pageSize,
});

async function loadCategories() {
  categories.value = await fetchAdminCategories();
}

async function loadArticles() {
  loading.value = true;
  errorMessage.value = '';
  try {
    articlePage.value = await fetchAdminArticles({
      keyword: filters.keyword?.trim() || undefined,
      categoryId: filters.categoryId || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadArticles();
}

async function changePage(page: number) {
  if (page < 1 || (articlePage.value.totalPages > 0 && page > articlePage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadArticles();
}

async function runArticleAction(id: string, action: () => Promise<unknown>, message: string) {
  actionLoadingId.value = id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await action();
    successMessage.value = message;
    await loadArticles();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '操作失败，请稍后重试');
  } finally {
    actionLoadingId.value = '';
  }
}

async function changeStatus(article: AdminArticleListItem, status: ArticleStatus) {
  await runArticleAction(article.id, () => updateAdminArticleStatus(article.id, status), `状态已更新为${formatArticleStatus(status)}`);
}

async function toggleTop(article: AdminArticleListItem) {
  await runArticleAction(article.id, () => updateAdminArticleTop(article.id, !article.isTop), article.isTop ? '已取消置顶' : '已置顶');
}

async function removeArticle(article: AdminArticleListItem) {
  if (!window.confirm(`确认删除文章「${article.title}」吗？`)) {
    return;
  }

  await runArticleAction(article.id, () => deleteAdminArticle(article.id), '文章已删除');
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadCategories();
    await loadArticles();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章管理初始化失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="terminal-label text-sm">admin_articles // index</p>
          <h2 class="mt-3 font-display text-3xl font-semibold">文章管理</h2>
          <p class="mt-2 text-sm text-cyber-muted">管理博客文章，支持草稿、发布、隐藏和置顶。</p>
        </div>

        <RouterLink
          class="inline-flex items-center justify-center rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan"
          to="/admin/articles/new"
        >
          新建文章
        </RouterLink>
      </div>

      <form class="mt-6 grid gap-3 lg:grid-cols-[1.2fr_1fr_1fr_auto]" @submit.prevent="submitFilters">
        <input
          v-model="filters.keyword"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
          placeholder="搜索标题或摘要"
          type="search"
        />

        <select
          v-model="filters.categoryId"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan"
          @change="submitFilters"
        >
          <option value="">全部分类</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>

        <select
          v-model="filters.status"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan"
          @change="submitFilters"
        >
          <option value="">全部状态</option>
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">已发布</option>
          <option value="HIDDEN">已隐藏</option>
        </select>

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
      <table class="min-w-[1100px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">标题</th>
            <th class="px-4 py-3">分类</th>
            <th class="px-4 py-3">标签</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">置顶</th>
            <th class="px-4 py-3">数据</th>
            <th class="px-4 py-3">发布时间</th>
            <th class="px-4 py-3">更新时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="9">文章加载中...</td>
          </tr>
          <tr v-else-if="articlePage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="9">暂无文章。</td>
          </tr>
          <tr
            v-for="article in articlePage.list"
            v-else
            :key="article.id"
            class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5"
          >
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ article.title }}</p>
              <p class="mt-1 max-w-xs truncate text-xs text-cyber-muted">{{ article.summary || article.slug }}</p>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ article.categoryName || '-' }}</td>
            <td class="px-4 py-4">
              <div class="flex max-w-48 flex-wrap gap-1">
                <span v-for="tag in article.tags" :key="tag.id" class="rounded-full border border-cyber-border px-2 py-0.5 font-mono text-[11px] text-cyber-muted">
                  {{ tag.name }}
                </span>
              </div>
            </td>
            <td class="px-4 py-4">
              <span class="rounded-full border border-cyber-cyan/40 px-2 py-1 font-mono text-[11px] text-cyber-cyan">
                {{ formatArticleStatus(article.status) }}
              </span>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ article.isTop ? '是' : '否' }}</td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-muted">
              {{ formatCount(article.viewCount) }} 浏览<br />
              {{ formatCount(article.likeCount) }} 点赞
            </td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(article.publishedAt) }}</td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(article.updatedAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex flex-wrap gap-2">
                <RouterLink class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" :to="`/admin/articles/${article.id}/edit`">
                  编辑
                </RouterLink>
                <button v-if="article.status !== 'PUBLISHED'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(article, 'PUBLISHED')">
                  发布
                </button>
                <button v-if="article.status !== 'DRAFT'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(article, 'DRAFT')">
                  设为草稿
                </button>
                <button v-if="article.status !== 'HIDDEN'" class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="changeStatus(article, 'HIDDEN')">
                  隐藏
                </button>
                <button class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="toggleTop(article)">
                  {{ article.isTop ? '取消置顶' : '置顶' }}
                </button>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeArticle(article)">
                  删除
                </button>
                <span v-if="actionLoadingId === article.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="articlePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!articlePage.hasPrevious || loading" type="button" @click="changePage(articlePage.pageNum - 1)">
        上一页
      </button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ articlePage.pageNum }} / {{ articlePage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!articlePage.hasNext || loading" type="button" @click="changePage(articlePage.pageNum + 1)">
        下一页
      </button>
    </div>
  </section>
</template>
