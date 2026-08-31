<script setup lang="ts">
import type { ArticleListItem } from '@/types/content';

withDefaults(defineProps<{ previous?: ArticleListItem | null; next?: ArticleListItem | null }>(), { previous: null, next: null });
</script>

<template>
  <nav v-if="previous || next" class="article-navigation" aria-label="上一篇和下一篇文章">
    <RouterLink v-if="previous" :to="`/articles/${previous.id}`" class="article-navigation__item">
      <span>← Previous article</span><strong>{{ previous.title }}</strong>
    </RouterLink>
    <span v-else></span>
    <RouterLink v-if="next" :to="`/articles/${next.id}`" class="article-navigation__item text-right">
      <span>Next article →</span><strong>{{ next.title }}</strong>
    </RouterLink>
  </nav>
</template>

<style scoped>
.article-navigation { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 1rem; margin-top: 3rem; border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 1.5rem; }
.article-navigation__item { display: flex; min-height: 7rem; flex-direction: column; justify-content: space-between; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .8rem; padding: 1rem; background: rgb(var(--color-surface-elevated) / .45); transition: border-color 180ms, background-color 180ms, transform 180ms; }
.article-navigation__item:hover { border-color: rgb(var(--color-border-active) / .52); background: rgb(var(--color-surface-hover) / .62); transform: translateY(-2px); }
.article-navigation__item span { font-family: 'JetBrains Mono',monospace; font-size: .62rem; text-transform: uppercase; letter-spacing: .1em; color: rgb(var(--color-brand-primary)); }
.article-navigation__item strong { margin-top: 1rem; font-family: 'Space Grotesk',sans-serif; font-size: 1rem; line-height: 1.45; color: rgb(var(--color-text-primary)); }
@media (max-width: 639px) { .article-navigation { grid-template-columns: 1fr; } .article-navigation > span:empty { display: none; } .article-navigation__item { min-height: 6rem; text-align: left; } }
@media (prefers-reduced-motion: reduce) { .article-navigation__item:hover { transform: none; } }
</style>
