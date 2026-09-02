<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { ArticleDetail } from '@/types/content';
import { formatCount, formatDate } from '@/utils/format';

const props = withDefaults(defineProps<{ article: ArticleDetail; liking?: boolean; likeFeedback?: string; backTo?: string }>(), {
  liking: false,
  likeFeedback: '',
  backTo: '/articles',
});
const emit = defineEmits<{ like: [] }>();
const coverFailed = ref(false);

const showUpdatedDate = computed(() => Boolean(props.article.updatedAt && formatDate(props.article.updatedAt) !== formatDate(props.article.publishedAt)));
watch(() => props.article.coverImage, () => { coverFailed.value = false; });
</script>

<template>
  <header class="article-header">
    <RouterLink class="inline-flex items-center gap-2 font-mono text-[0.68rem] text-text-muted transition hover:text-brand" :to="backTo"><span aria-hidden="true">←</span> 返回文章</RouterLink>

    <div class="mt-10 max-w-4xl">
      <div class="flex flex-wrap items-center gap-3 font-mono text-[0.65rem] uppercase tracking-[0.12em] text-text-muted">
        <span class="text-brand">{{ article.categoryName || '未分类' }}</span>
        <span v-if="article.isTop" class="rounded-full border border-accent/30 px-2 py-0.5 text-accent">精选</span>
      </div>
      <h1 class="mt-5 font-display text-[clamp(2.45rem,6.5vw,5rem)] font-bold leading-[1.03] tracking-[-0.05em] text-text-primary">{{ article.title }}</h1>
      <p v-if="article.summary" class="mt-6 max-w-3xl text-base leading-8 text-text-secondary sm:text-lg">{{ article.summary }}</p>

      <div class="mt-8 flex flex-wrap gap-x-6 gap-y-3 border-y border-border-subtle/62 py-4 font-mono text-[0.65rem] text-text-muted">
        <span>发布于 <strong>{{ formatDate(article.publishedAt) }}</strong></span>
        <span v-if="showUpdatedDate">更新于 <strong>{{ formatDate(article.updatedAt) }}</strong></span>
        <span>预计阅读 <strong>{{ article.readingTime }}</strong> 分钟</span>
        <span><strong>{{ formatCount(article.viewCount) }}</strong> 次阅读</span>
        <span><strong>{{ formatCount(article.commentCount) }}</strong> 条评论</span>
      </div>

      <div class="mt-5 flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between">
        <div class="flex flex-wrap gap-2">
          <span v-for="tag in article.tags" :key="tag.id" class="rounded-full border border-border-subtle/72 px-2.5 py-1 font-mono text-[0.65rem] text-text-secondary">#{{ tag.name }}</span>
        </div>
        <div class="flex items-center gap-3">
          <span v-if="likeFeedback" aria-live="polite" class="font-mono text-[0.65rem] text-brand">{{ likeFeedback }}</span>
          <button class="article-like" :disabled="liking" type="button" @click="emit('like')">
            <span aria-hidden="true">◇</span>
            {{ liking ? '正在同步……' : `喜欢 · ${formatCount(article.likeCount)}` }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="article.coverImage" class="article-cover" :class="{ 'is-failed': coverFailed }">
      <img v-if="!coverFailed" :src="article.coverImage" :alt="article.title" decoding="async" @error="coverFailed = true" />
      <div v-else class="article-cover__fallback" role="img" :aria-label="`${article.title} 封面暂不可用`">
        <span>封面暂不可用 // COVER</span><strong>{{ article.title.slice(0, 2).toUpperCase() }}</strong>
      </div>
    </div>
  </header>
</template>

<style scoped>
.article-header { padding: 2rem 0 3.5rem; }
.article-header strong { font-weight: 500; color: rgb(var(--color-text-primary)); }
.article-like { display: inline-flex; min-height: 2.65rem; align-items: center; gap: .55rem; border: 1px solid rgb(var(--color-border-subtle) / .78); border-radius: .625rem; padding: 0 .9rem; background: rgb(var(--color-surface-elevated) / .45); font-family: 'JetBrains Mono',monospace; font-size: .68rem; color: rgb(var(--color-text-secondary)); transition: border-color 180ms, color 180ms, background-color 180ms; }
.article-like:hover:not(:disabled) { border-color: rgb(var(--color-border-active) / .58); background: rgb(var(--color-brand-primary) / .07); color: rgb(var(--color-brand-primary)); }
.article-like:disabled { cursor: wait; opacity: .55; }
.article-cover { position: relative; overflow: hidden; width: min(100%,70rem); min-height: 18rem; max-height: 34rem; margin-top: 3rem; border: 1px solid rgb(var(--color-border-subtle) / .65); border-radius: 1rem; background: rgb(var(--color-bg-secondary)); box-shadow: var(--shadow-soft); }
.article-cover img { display: block; width: 100%; height: clamp(18rem,42vw,34rem); object-fit: cover; }
.article-cover__fallback { display: grid; min-height: 18rem; place-items: center; background: radial-gradient(circle at 72% 22%, rgb(var(--color-accent-secondary) / .12), transparent 30%), linear-gradient(135deg, rgb(var(--color-surface-elevated)), rgb(var(--color-bg-secondary))); }
.article-cover__fallback::before { position: absolute; inset: 0; content: ''; background-image: linear-gradient(rgb(var(--color-brand-primary) / .05) 1px, transparent 1px), linear-gradient(90deg, rgb(var(--color-brand-primary) / .05) 1px, transparent 1px); background-size: 32px 32px; }
.article-cover__fallback span { position: absolute; left: 1rem; top: 1rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; letter-spacing: .12em; color: rgb(var(--color-text-muted)); }
.article-cover__fallback strong { position: relative; font-family: 'Space Grotesk',sans-serif; font-size: clamp(3rem,10vw,7rem); letter-spacing: -.07em; color: rgb(var(--color-brand-primary) / .7); }
@media (max-width: 639px) { .article-header { padding-top: 1.25rem; padding-bottom: 2.5rem; } .article-cover { min-height: 12rem; margin-top: 2rem; } .article-cover img { height: 14rem; } .article-cover__fallback { min-height: 12rem; } }
</style>
