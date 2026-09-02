<script setup lang="ts">
import type { ProjectItem } from '@/types/project';

withDefaults(defineProps<{
  previous?: ProjectItem | null;
  next?: ProjectItem | null;
}>(), {
  previous: null,
  next: null,
});
</script>

<template>
  <nav v-if="previous || next" class="project-navigation" aria-label="项目导航">
    <RouterLink v-if="previous" class="project-navigation__item is-previous" :to="`/projects/${previous.id}`">
      <span>← 上一个项目</span>
      <strong>{{ previous.name }}</strong>
    </RouterLink>
    <span v-else class="project-navigation__empty" aria-hidden="true"></span>
    <RouterLink v-if="next" class="project-navigation__item is-next" :to="`/projects/${next.id}`">
      <span>下一个项目 →</span>
      <strong>{{ next.name }}</strong>
    </RouterLink>
  </nav>
</template>

<style scoped>
.project-navigation { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem; margin-top: 4rem; border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 1rem; }
.project-navigation__item { display: grid; gap: .55rem; min-height: 6.5rem; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .75rem; padding: 1rem; background: rgb(var(--color-surface-elevated) / .42); transition: border-color 180ms, background-color 180ms, transform 180ms; }
.project-navigation__item:hover { border-color: rgb(var(--color-brand-primary) / .55); background: rgb(var(--color-surface-hover) / .55); transform: translateY(-2px); }
.project-navigation__item.is-next { text-align: right; }
.project-navigation__item span { font-family: 'JetBrains Mono', monospace; font-size: .6rem; text-transform: uppercase; letter-spacing: .1em; color: rgb(var(--color-text-muted)); }
.project-navigation__item strong { align-self: end; font-family: 'Space Grotesk', sans-serif; font-size: 1rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
@media (max-width: 479px) { .project-navigation { grid-template-columns: 1fr; } .project-navigation__item.is-next { text-align: left; } .project-navigation__empty { display: none; } }
@media (prefers-reduced-motion: reduce) { .project-navigation__item:hover { transform: none; } }
</style>
