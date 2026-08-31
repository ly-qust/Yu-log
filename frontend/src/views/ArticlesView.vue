<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchArticles } from '@/api/articles';
import { fetchCategories } from '@/api/categories';
import { fetchTags } from '@/api/tags';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { ArticleListItem, PublicCategory, PublicTag } from '@/types/content';
import { formatCount, formatDate } from '@/utils/format';
import { applySeo } from '@/utils/seo';

type ArticleSort = 'latest' | 'oldest' | 'views' | 'likes';

const route = useRoute();
const router = useRouter();
const pageSize = 8;
const loading = ref(false);
const optionsLoading = ref(false);
const errorMessage = ref('');
const categories = ref<PublicCategory[]>([]);
const tags = ref<PublicTag[]>([]);
const coverFailedIds = ref(new Set<string>());
const articlePage = ref<PageResult<ArticleListItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});
const filters = reactive({ keyword: '', categoryId: '', tagId: '', sort: 'latest' as ArticleSort });
let requestSequence = 0;
let cleanupSeo = () => {};

const hasActiveFilters = computed(() => Boolean(filters.keyword || filters.categoryId || filters.tagId));
const resultDescription = computed(() => {
  if (loading.value) return '正在检索文章…';
  if (hasActiveFilters.value) return `找到 ${articlePage.value.total} 篇匹配内容`;
  return `共 ${articlePage.value.total} 篇公开文章`;
});

function queryValue(value: unknown): string {
  return Array.isArray(value) ? String(value[0] || '') : typeof value === 'string' ? value : '';
}

function normalizedPage(value: unknown): number {
  const page = Number.parseInt(queryValue(value), 10);
  return Number.isFinite(page) && page > 0 ? page : 1;
}

function normalizedSort(value: unknown): ArticleSort {
  const sort = queryValue(value);
  return ['oldest', 'views', 'likes'].includes(sort) ? sort as ArticleSort : 'latest';
}

function markCoverFailed(id: string) {
  coverFailedIds.value = new Set(coverFailedIds.value).add(id);
}

function buildQuery(page = 1) {
  return {
    ...(filters.keyword.trim() ? { q: filters.keyword.trim() } : {}),
    ...(filters.categoryId ? { category: filters.categoryId } : {}),
    ...(filters.tagId ? { tag: filters.tagId } : {}),
    ...(filters.sort !== 'latest' ? { sort: filters.sort } : {}),
    ...(page > 1 ? { page: String(page) } : {}),
  };
}

async function loadOptions() {
  optionsLoading.value = true;
  const [categoryResult, tagResult] = await Promise.allSettled([fetchCategories(), fetchTags()]);
  if (categoryResult.status === 'fulfilled') categories.value = categoryResult.value;
  if (tagResult.status === 'fulfilled') tags.value = tagResult.value;
  optionsLoading.value = false;
}

async function loadArticles(page: number) {
  const requestId = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchArticles({
      keyword: filters.keyword.trim() || undefined,
      categoryId: filters.categoryId || undefined,
      tagId: filters.tagId || undefined,
      sort: filters.sort === 'latest' ? undefined : filters.sort,
      page,
      size: pageSize,
    });
    if (requestId === requestSequence) articlePage.value = result;
  } catch {
    if (requestId === requestSequence) errorMessage.value = '文章暂时无法加载，请稍后再试。';
  } finally {
    if (requestId === requestSequence) loading.value = false;
  }
}

function syncFromRoute() {
  filters.keyword = queryValue(route.query.q);
  filters.categoryId = queryValue(route.query.category);
  filters.tagId = queryValue(route.query.tag);
  filters.sort = normalizedSort(route.query.sort);
  void loadArticles(normalizedPage(route.query.page));
}

async function updateRoute(page = 1) {
  const target = { name: 'articles' as const, query: buildQuery(page) };
  const targetPath = router.resolve(target).fullPath;
  if (targetPath === route.fullPath) {
    await loadArticles(page);
    return;
  }
  await router.push(target);
}

async function clearFilters() {
  filters.keyword = '';
  filters.categoryId = '';
  filters.tagId = '';
  filters.sort = 'latest';
  await updateRoute(1);
}

watch(() => route.fullPath, syncFromRoute, { immediate: true });

onMounted(() => {
  void loadOptions();
  cleanupSeo = applySeo({
    title: 'Writing — 技术文章 | YU.LOG',
    description: '关于软件工程、项目实践与持续学习的技术文章归档。',
    canonicalPath: '/articles',
  });
});

onUnmounted(() => {
  cleanupSeo();
});
</script>

<template>
  <PublicLayout>
    <div class="articles-page">
      <header class="articles-intro">
        <p class="font-mono text-[0.68rem] uppercase tracking-[0.16em] text-brand">Writing archive</p>
        <div class="mt-5 grid gap-5 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
          <div>
            <h1 class="font-display text-[clamp(2.8rem,8vw,6.5rem)] font-bold leading-[0.95] tracking-[-0.065em] text-text-primary">Writing</h1>
            <p class="mt-5 max-w-2xl text-base leading-8 text-text-secondary sm:text-lg">记录那些值得被反复查阅的工程经验、技术判断和项目复盘。</p>
          </div>
          <p class="font-mono text-[0.68rem] text-text-muted" aria-live="polite">{{ resultDescription }}</p>
        </div>
      </header>

      <section class="articles-controls" aria-label="文章筛选">
        <form class="grid gap-3 lg:grid-cols-[minmax(13rem,1.6fr)_repeat(3,minmax(9rem,1fr))_auto]" @submit.prevent="updateRoute(1)">
          <label class="articles-field">
            <span>Search</span>
            <input v-model="filters.keyword" type="search" placeholder="标题、摘要或关键词" />
          </label>
          <label class="articles-field">
            <span>Category</span>
            <select v-model="filters.categoryId" :disabled="optionsLoading" @change="updateRoute(1)">
              <option value="">全部分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }} · {{ category.articleCount }}</option>
            </select>
          </label>
          <label class="articles-field">
            <span>Tag</span>
            <select v-model="filters.tagId" :disabled="optionsLoading" @change="updateRoute(1)">
              <option value="">全部标签</option>
              <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }} · {{ tag.articleCount }}</option>
            </select>
          </label>
          <label class="articles-field">
            <span>Sort by</span>
            <select v-model="filters.sort" @change="updateRoute(1)">
              <option value="latest">最新发布</option>
              <option value="oldest">最早发布</option>
              <option value="views">浏览最多</option>
              <option value="likes">点赞最多</option>
            </select>
          </label>
          <button class="articles-submit" :disabled="loading" type="submit">Search</button>
        </form>
        <button v-if="hasActiveFilters || filters.sort !== 'latest'" class="articles-clear" type="button" @click="clearFilters">Reset all filters</button>
      </section>

      <p v-if="errorMessage" class="articles-error" role="alert">{{ errorMessage }}</p>

      <section class="articles-results" aria-label="文章列表">
        <template v-if="loading">
          <div v-for="index in 4" :key="index" class="articles-skeleton"><LoadingSkeleton :lines="3" /></div>
        </template>

        <EmptyState
          v-else-if="articlePage.list.length === 0"
          :title="hasActiveFilters ? '没有匹配的文章' : '文章正在生长'"
          :description="hasActiveFilters ? '试试减少筛选条件，或换一个关键词。' : '新的长期内容准备好后会出现在这里。'"
        >
          <template v-if="hasActiveFilters" #action><button class="articles-clear is-prominent" type="button" @click="clearFilters">清空筛选</button></template>
        </EmptyState>

        <article v-for="(item, index) in articlePage.list" v-else :key="item.id" class="article-row">
          <RouterLink class="article-row__link" :to="`/articles/${item.id}`">
            <div class="article-row__index" aria-hidden="true">{{ String((articlePage.pageNum - 1) * pageSize + index + 1).padStart(2, '0') }}</div>
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-x-3 gap-y-2 font-mono text-[0.64rem] uppercase tracking-[0.1em] text-text-muted">
                <span class="text-brand">{{ item.categoryName || 'Uncategorized' }}</span>
                <span v-if="item.isTop" class="text-accent">Featured</span>
                <time>{{ formatDate(item.publishedAt) }}</time>
              </div>
              <h2>{{ item.title }}</h2>
              <p class="article-row__summary">{{ item.summary || '这篇文章暂时没有摘要，进入正文继续阅读。' }}</p>
              <div v-if="item.tags.length" class="article-row__tags"><span v-for="tag in item.tags" :key="tag.id">#{{ tag.name }}</span></div>
            </div>
            <div v-if="item.coverImage" class="article-row__cover" :class="{ 'is-failed': coverFailedIds.has(item.id) }">
              <img v-if="!coverFailedIds.has(item.id)" :src="item.coverImage" :alt="`${item.title} 封面`" loading="lazy" decoding="async" @error="markCoverFailed(item.id)" />
              <span v-else aria-hidden="true">YU.LOG</span>
            </div>
            <div class="article-row__meta">
              <span>{{ item.readingTime }} min</span>
              <span>{{ formatCount(item.viewCount) }} views</span>
              <span>{{ formatCount(item.likeCount) }} likes</span>
              <strong>Read article <span aria-hidden="true">↗</span></strong>
            </div>
          </RouterLink>
        </article>
      </section>

      <nav v-if="!loading && articlePage.totalPages > 1" class="articles-pagination" aria-label="文章分页">
        <button :disabled="!articlePage.hasPrevious" type="button" @click="updateRoute(articlePage.pageNum - 1)">← Previous</button>
        <span>Page {{ articlePage.pageNum }} / {{ articlePage.totalPages }}</span>
        <button :disabled="!articlePage.hasNext" type="button" @click="updateRoute(articlePage.pageNum + 1)">Next →</button>
      </nav>
    </div>
  </PublicLayout>
</template>

<style scoped>
.articles-page { width: min(100%, 76rem); margin: 0 auto; padding: clamp(2rem,5vw,5rem) 0 5rem; }
.articles-intro { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding-bottom: clamp(2rem,5vw,4rem); }
.articles-controls { position: relative; z-index: 2; padding: 1.25rem 0; border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); }
.articles-field { display: grid; gap: .42rem; }
.articles-field > span { font-family: 'JetBrains Mono',monospace; font-size: .58rem; text-transform: uppercase; letter-spacing: .12em; color: rgb(var(--color-text-muted)); }
.articles-field input,.articles-field select { width: 100%; min-height: 2.8rem; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .55rem; padding: 0 .75rem; background: rgb(var(--color-surface-elevated) / .58); font-size: .78rem; color: rgb(var(--color-text-primary)); outline: none; transition: border-color 180ms,box-shadow 180ms; }
.articles-field input:focus,.articles-field select:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .1); }
.articles-submit { align-self: end; min-height: 2.8rem; border-radius: .55rem; padding: 0 1.15rem; background: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono',monospace; font-size: .65rem; font-weight: 700; color: rgb(var(--color-brand-contrast)); transition: filter 180ms,transform 180ms; }
.articles-submit:hover:not(:disabled) { filter: brightness(1.08); transform: translateY(-1px); }
.articles-submit:disabled { cursor: wait; opacity: .55; }
.articles-clear { margin-top: .85rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); transition: color 180ms; }
.articles-clear:hover { color: rgb(var(--color-brand-primary)); }
.articles-clear.is-prominent { margin-top: 0; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: .6rem .8rem; }
.articles-error { margin-top: 1rem; border-left: 2px solid rgb(var(--color-danger)); padding: .7rem 1rem; background: rgb(var(--color-danger) / .06); font-size: .8rem; color: rgb(var(--color-danger)); }
.articles-results { min-height: 20rem; }
.articles-skeleton { padding: 2rem 0; border-bottom: 1px solid rgb(var(--color-border-subtle) / .58); }
.article-row { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); }
.article-row__link { display: grid; grid-template-columns: 2.5rem minmax(0,1fr) 8rem 9rem; gap: clamp(1rem,3vw,2rem); padding: clamp(1.6rem,4vw,2.6rem) 0; transition: background-color 180ms; }
.article-row__link:hover h2 { color: rgb(var(--color-brand-primary)); }
.article-row__index { padding-top: .1rem; font-family: 'JetBrains Mono',monospace; font-size: .66rem; color: rgb(var(--color-text-muted)); }
.article-row h2 { margin-top: .65rem; font-family: 'Space Grotesk',sans-serif; font-size: clamp(1.35rem,3vw,2rem); font-weight: 650; line-height: 1.2; letter-spacing: -.025em; color: rgb(var(--color-text-primary)); transition: color 180ms; }
.article-row__summary { max-width: 48rem; margin-top: .75rem; font-size: .88rem; line-height: 1.75; color: rgb(var(--color-text-secondary)); }
.article-row__tags { display: flex; flex-wrap: wrap; gap: .75rem; margin-top: .9rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.article-row__cover { width: 8rem; height: 6rem; align-self: center; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .7rem; background: radial-gradient(circle at 70% 20%, rgb(var(--color-accent-secondary) / .17), transparent 38%), rgb(var(--color-bg-secondary)); }
.article-row__cover img { display: block; width: 100%; height: 100%; object-fit: cover; }
.article-row__cover.is-failed { display: grid; place-items: center; background-image: linear-gradient(rgb(var(--color-brand-primary) / .06) 1px, transparent 1px), linear-gradient(90deg, rgb(var(--color-brand-primary) / .06) 1px, transparent 1px); background-size: 1rem 1rem; }
.article-row__cover span { font-family: 'JetBrains Mono',monospace; font-size: .52rem; letter-spacing: .1em; color: rgb(var(--color-brand-primary) / .72); }
.article-row__meta { display: flex; flex-direction: column; align-items: flex-end; gap: .35rem; padding-top: .1rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.article-row__meta strong { margin-top: auto; color: rgb(var(--color-brand-primary)); }
.articles-pagination { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 1rem; padding-top: 2rem; font-family: 'JetBrains Mono',monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); }
.articles-pagination button { justify-self: start; min-height: 2.5rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: 0 .8rem; color: rgb(var(--color-text-secondary)); }
.articles-pagination button:last-child { justify-self: end; }
.articles-pagination button:hover:not(:disabled) { border-color: rgb(var(--color-brand-primary) / .55); color: rgb(var(--color-brand-primary)); }
.articles-pagination button:disabled { cursor: not-allowed; opacity: .35; }
@media (max-width: 767px) {
  .article-row__link { grid-template-columns: 1.8rem minmax(0,1fr); }
  .article-row__cover { display: none; }
  .article-row__meta { grid-column: 2; flex-direction: row; flex-wrap: wrap; align-items: center; gap: .45rem .9rem; }
  .article-row__meta strong { width: 100%; margin-top: .45rem; }
}
@media (max-width: 479px) {
  .article-row__index { display: none; }
  .article-row__link { grid-template-columns: 1fr; }
  .article-row__cover { display: none; }
  .article-row__meta { grid-column: 1; }
  .articles-pagination span { text-align: center; }
  .articles-pagination button { padding: 0 .6rem; font-size: .58rem; }
}
@media (prefers-reduced-motion: reduce) { .articles-submit:hover:not(:disabled) { transform: none; } }
</style>
