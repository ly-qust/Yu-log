<script setup lang="ts">
import type { ArticleHeading } from '@/types/markdown';

defineProps<{ headings: ArticleHeading[]; activeId?: string }>();
const emit = defineEmits<{ navigate: [id: string] }>();
</script>

<template>
  <nav v-if="headings.length" class="article-toc" aria-label="文章目录">
    <div class="hidden lg:block">
      <div class="mb-4 flex items-center justify-between gap-3">
        <p class="font-mono text-[0.625rem] uppercase tracking-[0.15em] text-text-muted">目录 // CONTENTS</p>
        <span class="font-mono text-[0.6rem] text-brand">{{ headings.length }} 个章节</span>
      </div>
      <ol class="article-toc__list">
        <li v-for="heading in headings" :key="heading.id" :class="`toc-level-${heading.level}`">
          <a :href="`#${heading.id}`" :aria-current="activeId === heading.id ? 'location' : undefined" :class="{ 'is-active': activeId === heading.id }" @click.prevent="emit('navigate', heading.id)">{{ heading.text }}</a>
        </li>
      </ol>
    </div>

    <details class="article-toc__mobile lg:hidden">
      <summary><span>文章目录</span><span>{{ headings.length }} 个章节</span></summary>
      <ol>
        <li v-for="heading in headings" :key="heading.id" :class="`toc-level-${heading.level}`">
          <a :href="`#${heading.id}`" @click.prevent="emit('navigate', heading.id)">{{ heading.text }}</a>
        </li>
      </ol>
    </details>
  </nav>
</template>

<style scoped>
.article-toc { color: rgb(var(--color-text-secondary)); }
.article-toc__list { max-height: calc(100vh - 12rem); overflow-y: auto; border-left: 1px solid rgb(var(--color-border-subtle) / .68); padding: .2rem 0 .2rem .9rem; scrollbar-width: thin; }
.article-toc__list li { margin: .15rem 0; }
.article-toc__list a { display: block; border-radius: .35rem; padding: .38rem .5rem; font-size: .75rem; line-height: 1.45; color: rgb(var(--color-text-muted)); transition: background-color 180ms, color 180ms, transform 180ms; }
.article-toc__list a:hover { color: rgb(var(--color-text-primary)); }
.article-toc__list a.is-active { background: rgb(var(--color-brand-primary) / .09); color: rgb(var(--color-brand-primary)); transform: translateX(3px); }
.article-toc__list .toc-level-3 a { padding-left: 1rem; font-size: .7rem; }
.article-toc__list .toc-level-4 a { padding-left: 1.5rem; font-size: .67rem; }
.article-toc__mobile { overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .75rem; background: rgb(var(--color-surface-elevated) / .62); }
.article-toc__mobile summary { display: flex; min-height: 3rem; cursor: pointer; list-style: none; align-items: center; justify-content: space-between; gap: 1rem; padding: 0 1rem; font-family: 'JetBrains Mono',monospace; font-size: .68rem; color: rgb(var(--color-text-secondary)); }
.article-toc__mobile summary::-webkit-details-marker { display: none; }
.article-toc__mobile summary span:last-child { color: rgb(var(--color-brand-primary)); }
.article-toc__mobile ol { max-height: 18rem; overflow-y: auto; border-top: 1px solid rgb(var(--color-border-subtle) / .58); padding: .55rem; }
.article-toc__mobile a { display: block; padding: .55rem .6rem; font-size: .8rem; line-height: 1.45; color: rgb(var(--color-text-secondary)); }
.article-toc__mobile .toc-level-3 a { padding-left: 1.25rem; }
.article-toc__mobile .toc-level-4 a { padding-left: 1.9rem; }
@media (prefers-reduced-motion: reduce) { .article-toc__list a.is-active { transform: none; } }
</style>
