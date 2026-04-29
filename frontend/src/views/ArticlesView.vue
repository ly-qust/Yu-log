<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { fetchArticles } from '@/api/articles';
import { fetchCategories } from '@/api/categories';
import { fetchTags } from '@/api/tags';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { ArticleListItem, PublicCategory, PublicTag } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';
import { formatCount, formatDate } from '@/utils/format';

const pageSize = 6;
const loading = ref(false);
const errorMessage = ref('');
const categories = ref<PublicCategory[]>([]);
const tags = ref<PublicTag[]>([]);
const articlePage = ref<PageResult<ArticleListItem>>({
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
  categoryId: '',
  tagId: '',
  page: 1,
});

async function loadOptions() {
  const [categoryData, tagData] = await Promise.all([fetchCategories(), fetchTags()]);
  categories.value = categoryData;
  tags.value = tagData;
}

async function loadArticles() {
  loading.value = true;
  errorMessage.value = '';

  try {
    articlePage.value = await fetchArticles({
      keyword: filters.keyword.trim() || undefined,
      categoryId: filters.categoryId || undefined,
      tagId: filters.tagId || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitSearch() {
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

async function clearFilters() {
  filters.keyword = '';
  filters.categoryId = '';
  filters.tagId = '';
  filters.page = 1;
  await loadArticles();
}

onMounted(async () => {
  loading.value = true;
  try {
    await loadOptions();
    await loadArticles();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <GlassPanel>
        <div class="flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="terminal-label text-sm">archive // live_query</p>
            <h1 class="mt-4 font-display text-3xl font-semibold text-cyber-text">技术文章</h1>
            <p class="mt-3 max-w-2xl text-cyber-muted">
              这里记录我的技术学习、项目实践和问题复盘，支持按关键词、分类和标签筛选。
            </p>
          </div>

          <form class="grid gap-3 lg:min-w-[640px] lg:grid-cols-[1.2fr_1fr_1fr_auto]" @submit.prevent="submitSearch">
            <input
              v-model="filters.keyword"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
              placeholder="搜索标题或摘要"
              type="search"
            />

            <select
              v-model="filters.categoryId"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan"
              @change="submitSearch"
            >
              <option value="">全部分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }} ({{ category.articleCount }})
              </option>
            </select>

            <select
              v-model="filters.tagId"
              class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan"
              @change="submitSearch"
            >
              <option value="">全部标签</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">
                {{ tag.name }} ({{ tag.articleCount }})
              </option>
            </select>

            <button
              class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50"
              :disabled="loading"
              type="submit"
            >
              查询
            </button>
          </form>
        </div>

        <button
          class="mt-4 font-mono text-xs text-cyber-cyan transition hover:text-cyber-cyanBright"
          type="button"
          @click="clearFilters"
        >
          清空筛选
        </button>
      </GlassPanel>

      <div v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </div>

      <GlassPanel v-if="loading">
        <p class="font-mono text-sm text-cyber-cyan">文章加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="articlePage.list.length === 0">
        <p class="terminal-label text-sm">archive // empty</p>
        <h2 class="mt-3 font-display text-2xl font-semibold">暂无文章</h2>
        <p class="mt-2 text-cyber-muted">换个关键词或清空筛选试试。</p>
      </GlassPanel>

      <div v-else class="grid gap-4">
        <RouterLink
          v-for="article in articlePage.list"
          :key="article.id"
          class="glass-panel block rounded-glass p-5 transition hover:border-cyber-cyan/70 hover:shadow-glow"
          :to="`/articles/${article.id}`"
        >
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2">
                <span v-if="article.isTop" class="rounded-full border border-cyber-purple/60 px-2 py-0.5 font-mono text-[11px] text-cyber-purple">
                  置顶
                </span>
                <span class="font-mono text-xs text-cyber-cyan">{{ article.categoryName || '未分类' }}</span>
                <span class="font-mono text-xs text-cyber-muted">{{ formatDate(article.publishedAt) }}</span>
              </div>

              <h2 class="mt-3 font-display text-2xl font-semibold text-cyber-text">
                {{ article.title }}
              </h2>
              <p class="mt-2 text-sm text-cyber-muted">
                {{ article.summary || '暂无摘要。' }}
              </p>

              <div class="mt-4 flex flex-wrap gap-2">
                <span
                  v-for="tag in article.tags"
                  :key="tag.id"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
                >
                  #{{ tag.name }}
                </span>
              </div>
            </div>

            <div class="grid grid-cols-3 gap-3 text-right md:min-w-56">
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">阅读</p>
                <p class="text-sm text-cyber-text">{{ article.readingTime }} 分钟</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">浏览</p>
                <p class="text-sm text-cyber-text">{{ formatCount(article.viewCount) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">点赞</p>
                <p class="text-sm text-cyber-text">{{ formatCount(article.likeCount) }}</p>
              </div>
            </div>
          </div>
        </RouterLink>
      </div>

      <div v-if="articlePage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
        <button
          class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan disabled:cursor-not-allowed disabled:opacity-40"
          :disabled="!articlePage.hasPrevious || loading"
          type="button"
          @click="changePage(articlePage.pageNum - 1)"
        >
          上一页
        </button>

        <p class="font-mono text-xs text-cyber-muted">
          第 {{ articlePage.pageNum }} / {{ articlePage.totalPages }} 页 · 共 {{ articlePage.total }} 篇
        </p>

        <button
          class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan disabled:cursor-not-allowed disabled:opacity-40"
          :disabled="!articlePage.hasNext || loading"
          type="button"
          @click="changePage(articlePage.pageNum + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </PublicLayout>
</template>
