<script setup lang="ts">
import type { NoteItem } from '@/types/note';
import { formatDate } from '@/utils/format';

defineProps<{ notes: NoteItem[] }>();
</script>

<template>
  <section class="relative left-1/2 w-[min(100vw-2rem,72rem)] -translate-x-1/2 py-16 sm:py-20 lg:py-28" aria-labelledby="garden-notes-title">
    <div class="garden-heading text-center">
      <p class="terminal-label text-xs uppercase tracking-[0.14em]">06 // DIGITAL GARDEN</p>
      <h2 id="garden-notes-title" class="mt-3 font-display text-h2">随手记</h2>
      <p class="mx-auto mt-4 max-w-xl text-sm leading-7 text-text-secondary">文章是阶段性结论，笔记则保留知识仍在生长时的样子。</p>
    </div>

    <div class="note-cluster">
      <div class="note-cluster__line" aria-hidden="true"></div>
      <RouterLink v-for="(note, index) in notes.slice(0, 4)" :key="note.id" :to="`/notes/${note.id}`" class="note-node group" :class="`note-node--${index + 1}`" :aria-label="`阅读笔记：${note.title}`">
        <div class="flex items-center justify-between gap-3">
          <span class="font-mono text-[0.6rem] uppercase tracking-[0.12em] text-brand">{{ note.topic || '笔记' }}</span>
          <span class="font-mono text-[0.58rem] text-text-muted">{{ formatDate(note.updatedAt || note.createdAt) }}</span>
        </div>
        <h3 class="mt-3 font-display text-lg font-semibold leading-snug text-text-primary transition group-hover:text-brand">{{ note.title }}</h3>
        <p v-if="note.summary" class="mt-2 line-clamp-2 text-sm leading-6 text-text-secondary">{{ note.summary }}</p>
        <div v-if="note.tags.length" class="mt-4 flex flex-wrap gap-2"><span v-for="tag in note.tags.slice(0, 3)" :key="tag" class="font-mono text-[0.58rem] text-text-muted">#{{ tag }}</span></div>
      </RouterLink>
    </div>

    <div class="mt-8 text-center"><RouterLink class="font-mono text-xs text-brand transition hover:text-brand-strong" to="/notes">进入数字花园 →</RouterLink></div>
  </section>
</template>

<style scoped>
.note-cluster { position: relative; display: grid; gap: 1rem; margin-top: 2.75rem; grid-template-columns: repeat(12,minmax(0,1fr)); }
.note-cluster__line { position: absolute; left: 18%; right: 18%; top: 50%; height: 1px; background: linear-gradient(90deg, transparent, rgb(var(--color-brand-primary) / .25), transparent); transform: rotate(-4deg); }
.note-node { position: relative; z-index: 1; min-height: 11rem; border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: 1rem; padding: 1.2rem; background: rgb(var(--color-surface-elevated) / .72); box-shadow: var(--shadow-soft); transition: border-color 240ms, transform 240ms, box-shadow 240ms; }
.note-node::before { position: absolute; left: 1rem; top: -.25rem; width: .48rem; height: .48rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); box-shadow: 0 0 13px rgb(var(--color-brand-primary) / .55); content: ''; }
.note-node:hover { z-index: 2; border-color: rgb(var(--color-border-active) / .55); box-shadow: var(--shadow-glow); transform: translateY(-4px) rotate(0); }
.note-node:focus-visible { z-index: 2; border-color: rgb(var(--color-brand-primary)); box-shadow: var(--shadow-glow); outline: 2px solid rgb(var(--color-brand-primary) / .4); outline-offset: 3px; transform: translateY(-3px) rotate(0); }
.note-node--1 { grid-column: 1 / span 5; transform: rotate(-.8deg); }
.note-node--2 { grid-column: 6 / span 7; margin-top: 2.5rem; transform: rotate(.6deg); }
.note-node--3 { grid-column: 2 / span 6; transform: rotate(.5deg); }
.note-node--4 { grid-column: 8 / span 5; margin-top: 1rem; transform: rotate(-.7deg); }
@media (max-width: 767px) { .note-cluster { display: grid; grid-template-columns: 1fr; } .note-cluster__line { left: 1rem; right: auto; top: 0; bottom: 0; width: 1px; height: auto; transform: none; } .note-node, .note-node--1, .note-node--2, .note-node--3, .note-node--4 { grid-column: auto; min-height: auto; margin: 0 0 0 1.5rem; transform: none; } }
@media (prefers-reduced-motion: reduce) { .note-node:hover, .note-node:focus-visible { transform: none; } }
</style>
