<script setup lang="ts">
import { computed, ref } from 'vue';

import { techGroupDefinitions } from '@/config/homepage';

const props = defineProps<{ items: string[] }>();
const activeTech = ref('');
const activeGroup = ref('');

const groups = computed(() => {
  const remaining = new Set(props.items);
  const matched = techGroupDefinitions.map((definition) => {
    const technologies = props.items.filter((item) => definition.patterns.some((pattern) => pattern.test(item)));
    technologies.forEach((item) => remaining.delete(item));
    return { ...definition, technologies };
  }).filter((group) => group.technologies.length);

  if (remaining.size) {
    matched.push({ key: 'other', label: 'Toolkit', hint: 'learning / practice', patterns: [], technologies: [...remaining] });
  }
  return matched;
});
</script>

<template>
  <section class="relative left-1/2 w-[min(100vw-2rem,76rem)] -translate-x-1/2 py-16 sm:py-20 lg:py-28" aria-labelledby="tech-landscape-title">
    <div class="tech-shell">
      <div class="tech-shell__intro">
        <p class="terminal-label text-xs uppercase tracking-[0.14em]">04 // working landscape</p>
        <h2 id="tech-landscape-title" class="mt-3 font-display text-h2">Technologies in practice</h2>
        <p class="mt-4 max-w-xl text-sm leading-7 text-text-secondary">不是 Logo 墙，而是当前学习与项目中实际出现的技术坐标。</p>
      </div>

      <div class="tech-matrix">
        <article v-for="(group, index) in groups" :key="group.key" class="tech-group" :class="{ 'is-active': activeGroup === group.key }" @mouseenter="activeGroup = group.key" @mouseleave="activeGroup = ''">
          <div class="flex items-start justify-between gap-4">
            <div><p class="font-display text-lg font-semibold text-text-primary">{{ group.label }}</p><p class="mt-1 font-mono text-[0.58rem] uppercase tracking-[0.13em] text-text-muted">{{ group.hint }}</p></div>
            <span class="font-mono text-[0.6rem] text-brand">{{ String(index + 1).padStart(2, '0') }}</span>
          </div>
          <div class="mt-5 flex flex-wrap gap-2">
            <button v-for="tech in group.technologies" :key="tech" type="button" class="tech-chip" :class="{ 'is-active': activeTech === tech }" :aria-pressed="activeTech === tech" @mouseenter="activeTech = tech" @mouseleave="activeTech = ''" @focus="activeTech = tech" @blur="activeTech = ''">{{ tech }}</button>
          </div>
        </article>
      </div>
      <p v-if="activeTech" class="tech-signal" aria-live="polite">Working signal // {{ activeTech }} is part of the current garden stack.</p>
    </div>
  </section>
</template>

<style scoped>
.tech-shell { position: relative; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: 1.25rem; padding: clamp(1.4rem,4vw,3rem); background: linear-gradient(135deg, rgb(var(--color-surface-elevated) / .76), rgb(var(--color-bg-secondary) / .64)); box-shadow: var(--shadow-soft); }
.tech-shell::after { position: absolute; right: -7rem; top: -7rem; width: 20rem; height: 20rem; border: 1px solid rgb(var(--color-brand-primary) / .12); border-radius: 50%; content: ''; box-shadow: 0 0 0 3rem rgb(var(--color-brand-primary) / .025), 0 0 0 6rem rgb(var(--color-brand-primary) / .02); pointer-events: none; }
.tech-shell__intro { position: relative; z-index: 1; }
.tech-matrix { position: relative; z-index: 1; display: grid; gap: 1px; margin-top: 2.25rem; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .64); border-radius: 1rem; background: rgb(var(--color-border-subtle) / .64); grid-template-columns: repeat(3,minmax(0,1fr)); }
.tech-group { min-height: 10.5rem; padding: 1.25rem; background: rgb(var(--color-bg-primary) / .78); transition: background-color var(--motion-normal) var(--ease-standard), box-shadow var(--motion-normal) var(--ease-standard); }
.tech-group:hover { background: rgb(var(--color-surface-hover) / .82); }
.tech-group.is-active { background: rgb(var(--color-surface-hover) / .82); box-shadow: inset 0 2px 0 rgb(var(--color-brand-primary) / .6); }
.tech-chip { border: 1px solid rgb(var(--color-border-subtle) / .76); border-radius: .5rem; padding: .38rem .58rem; background: rgb(var(--color-surface-elevated) / .55); font-family: 'JetBrains Mono',monospace; font-size: .65rem; color: rgb(var(--color-text-secondary)); cursor: pointer; transition: border-color var(--motion-fast) var(--ease-standard), color var(--motion-fast) var(--ease-standard), background-color var(--motion-fast) var(--ease-standard), transform var(--motion-fast) var(--ease-emphasized); }
.tech-chip:hover, .tech-chip:focus-visible, .tech-chip.is-active { border-color: rgb(var(--color-border-active) / .75); background: rgb(var(--color-brand-primary) / .1); color: rgb(var(--color-brand-primary)); transform: translateY(-2px); outline: none; }
.tech-signal { margin-top: 1rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; letter-spacing: .05em; color: rgb(var(--color-brand-primary)); }
@media (max-width: 767px) { .tech-matrix { grid-template-columns: 1fr; } .tech-group { min-height: auto; } }
@media (min-width: 768px) and (max-width: 1023px) { .tech-matrix { grid-template-columns: repeat(2,minmax(0,1fr)); } }
@media (prefers-reduced-motion: reduce) { .tech-chip, .tech-group { transition: none; } .tech-chip:hover, .tech-chip:focus-visible, .tech-chip.is-active { transform: none; } }
</style>
