<script setup lang="ts">
import type { ArticleListItem } from '@/types/content';
import { formatDate } from '@/utils/format';

defineProps<{ articles: ArticleListItem[] }>();
</script>

<template>
  <section class="mx-auto max-w-5xl py-16 sm:py-20 lg:py-28" aria-labelledby="latest-writing-title">
    <div class="grid gap-8 lg:grid-cols-[0.42fr_1fr] lg:gap-14">
      <div>
        <p class="terminal-label text-xs uppercase tracking-[0.14em]">03 // TECHNICAL JOURNAL</p>
        <h2 id="latest-writing-title" class="mt-3 font-display text-h2">最近在写</h2>
        <p class="mt-4 max-w-sm text-sm leading-7 text-text-secondary">关于系统、工具与实践的阶段性理解。写下来，是为了让知识可以再次被调用。</p>
        <RouterLink class="mt-6 inline-flex font-mono text-xs text-brand transition hover:text-brand-strong" to="/articles">浏览全部文章 →</RouterLink>
      </div>

      <div class="border-t border-border-subtle/75">
        <RouterLink v-for="(article, index) in articles.slice(0, 5)" :key="article.id" :to="`/articles/${article.id}`" class="writing-row group">
          <div class="writing-row__number">{{ String(index + 1).padStart(2, '0') }}</div>
          <div class="min-w-0">
            <div class="flex flex-wrap items-center gap-x-3 gap-y-1 font-mono text-[0.625rem] uppercase tracking-[0.1em] text-text-muted">
              <span class="text-brand">{{ article.categoryName || '日志' }}</span>
              <span>{{ formatDate(article.publishedAt) }}</span>
              <span>预计阅读 {{ article.readingTime }} 分钟</span>
            </div>
            <h3 class="mt-2 font-display text-xl font-semibold leading-snug tracking-tight text-text-primary transition group-hover:text-brand sm:text-2xl">{{ article.title }}</h3>
            <p v-if="article.summary" class="mt-2 line-clamp-2 text-sm leading-6 text-text-secondary">{{ article.summary }}</p>
            <div v-if="article.tags.length" class="mt-3 flex flex-wrap gap-x-3 gap-y-1">
              <span v-for="tag in article.tags.slice(0, 3)" :key="tag.id" class="font-mono text-[0.625rem] text-text-muted">#{{ tag.name }}</span>
            </div>
          </div>
          <svg aria-hidden="true" class="writing-row__arrow" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M5 12h14m-6-6 6 6-6 6" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" /></svg>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
.writing-row { display: grid; grid-template-columns: 2rem minmax(0,1fr) 1.75rem; gap: 1rem; align-items: start; border-bottom: 1px solid rgb(var(--color-border-subtle) / .75); padding: 1.55rem 0; transition: padding 240ms, background-color 240ms; }
.writing-row:hover { padding-left: .5rem; padding-right: .5rem; background: linear-gradient(90deg, rgb(var(--color-brand-primary) / .04), transparent); }
.writing-row__number { padding-top: .15rem; font-family: 'JetBrains Mono',monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); }
.writing-row__arrow { width: 1.1rem; height: 1.1rem; margin-top: .25rem; color: rgb(var(--color-text-muted)); transition: color 200ms, transform 200ms; }
.writing-row:hover .writing-row__arrow { color: rgb(var(--color-brand-primary)); transform: translateX(4px); }
@media (max-width: 639px) { .writing-row { grid-template-columns: 1.5rem minmax(0,1fr); gap: .6rem; padding: 1.35rem 0; } .writing-row__arrow { display: none; } .writing-row:hover { padding-left: 0; padding-right: 0; } }
@media (prefers-reduced-motion: reduce) { .writing-row:hover { padding-left: 0; padding-right: 0; } .writing-row:hover .writing-row__arrow { transform: none; } }
</style>
