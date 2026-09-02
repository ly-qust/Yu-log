<script setup lang="ts">
import type { NoteItem } from '@/types/note';
import { formatDate } from '@/utils/format';

defineProps<{ note: NoteItem; index: number }>();
</script>

<template>
  <article class="note-node">
    <RouterLink :to="`/notes/${note.id}`" class="note-node__link">
      <div class="note-node__head"><span class="note-node__signal" aria-hidden="true"></span><span class="note-node__topic">{{ note.topic || '笔记' }}</span><time :datetime="note.updatedAt || note.createdAt || undefined">更新于 {{ formatDate(note.updatedAt || note.createdAt) }}</time></div>
      <p class="note-node__index">NODE / {{ String(index + 1).padStart(2, '0') }}</p>
      <h2>{{ note.title }}</h2>
      <p v-if="note.summary" class="note-node__summary">{{ note.summary }}</p>
      <div v-if="note.tags.length" class="note-node__tags"><span v-for="tag in note.tags.slice(0, 5)" :key="tag">#{{ tag }}</span><span v-if="note.tags.length > 5">还有 {{ note.tags.length - 5 }} 项</span></div>
      <span class="note-node__open">打开笔记 <span aria-hidden="true">↗</span></span>
    </RouterLink>
  </article>
</template>

<style scoped>
.note-node { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); }
.note-node__link { position: relative; display: block; min-height: 14rem; padding: 1.5rem 0 1.7rem; transition: padding-left 180ms, background-color 180ms; }
.note-node__link:hover { padding-left: .8rem; }
.note-node__link::before { position: absolute; left: 0; top: 0; bottom: 0; width: 1px; background: transparent; content: ''; transition: background-color 180ms, box-shadow 180ms; }
.note-node__link:hover::before { background: rgb(var(--color-brand-primary)); box-shadow: 0 0 13px rgb(var(--color-brand-primary) / .35); }
.note-node__head { display: flex; flex-wrap: wrap; align-items: center; gap: .55rem .8rem; font-family: 'JetBrains Mono', monospace; font-size: .6rem; text-transform: uppercase; letter-spacing: .1em; color: rgb(var(--color-text-muted)); }
.note-node__signal { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); box-shadow: 0 0 10px rgb(var(--color-brand-primary) / .55); }
.note-node__topic { color: rgb(var(--color-brand-primary)); }
.note-node__head time { margin-left: auto; }
.note-node__index { margin-top: 1.6rem; font-family: 'JetBrains Mono', monospace; font-size: .58rem; color: rgb(var(--color-text-muted)); }
.note-node h2 { max-width: 48rem; margin-top: .55rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.35rem, 3vw, 2rem); font-weight: 650; line-height: 1.2; letter-spacing: -.03em; transition: color 180ms; }
.note-node__link:hover h2 { color: rgb(var(--color-brand-primary)); }
.note-node__summary { max-width: 45rem; margin-top: .75rem; font-size: .88rem; line-height: 1.75; color: rgb(var(--color-text-secondary)); }
.note-node__tags { display: flex; flex-wrap: wrap; gap: .6rem 1rem; margin-top: 1rem; font-family: 'JetBrains Mono', monospace; font-size: .61rem; color: rgb(var(--color-text-muted)); }
.note-node__open { display: block; margin-top: 1.2rem; font-family: 'JetBrains Mono', monospace; font-size: .63rem; color: rgb(var(--color-brand-primary)); }
@media (max-width: 639px) { .note-node__link:hover { padding-left: .5rem; } .note-node__head time { width: 100%; margin-left: 1.2rem; } }
@media (prefers-reduced-motion: reduce) { .note-node__link:hover { padding-left: 0; } }
</style>
