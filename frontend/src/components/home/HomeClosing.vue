<script setup lang="ts">
import { homepageIdentity } from '@/config/homepage';

withDefaults(defineProps<{ githubUrl?: string; email?: string }>(), { githubUrl: '', email: '' });
</script>

<template>
  <section class="closing-section py-20 text-center sm:py-28 lg:py-36" aria-labelledby="home-closing-title">
    <p class="font-mono text-[0.65rem] uppercase tracking-[0.2em] text-brand">这片花园还在开放</p>
    <h2 id="home-closing-title" class="mx-auto mt-6 max-w-4xl font-display text-[clamp(2.5rem,7vw,5.75rem)] font-bold leading-[.98] tracking-[-.055em] text-text-primary">
      <span v-for="(line, index) in homepageIdentity.closingLines" :key="line" class="block" :class="index === 2 ? 'text-brand' : ''">{{ line }}</span>
    </h2>
    <p class="mx-auto mt-7 max-w-xl text-sm leading-7 text-text-secondary sm:text-base">这里记录尚未完成的理解、正在构建的系统，以及每一次向前生长的痕迹。</p>
    <div class="mt-9 flex flex-wrap items-center justify-center gap-3">
      <RouterLink class="closing-link closing-link--primary" to="/articles">继续逛逛</RouterLink>
      <RouterLink class="closing-link" to="/about">了解 Yu</RouterLink>
      <a v-if="githubUrl" :href="githubUrl" class="closing-link" target="_blank" rel="noreferrer">GitHub ↗</a>
      <a v-if="email" :href="`mailto:${email}`" class="closing-link">Email</a>
    </div>
  </section>
</template>

<style scoped>
.closing-section { position: relative; }
.closing-section::before { position: absolute; left: 50%; top: 50%; z-index: -1; width: min(46rem,90vw); height: min(30rem,60vw); border-radius: 50%; background: radial-gradient(ellipse, rgb(var(--color-glow-brand) / .1), transparent 68%); content: ''; transform: translate(-50%,-50%); pointer-events: none; }
.closing-link { display: inline-flex; min-height: 2.75rem; align-items: center; justify-content: center; border: 1px solid rgb(var(--color-border-subtle) / .8); border-radius: .625rem; padding: 0 1rem; font-family: 'JetBrains Mono',monospace; font-size: .7rem; color: rgb(var(--color-text-secondary)); transition: border-color 200ms, color 200ms, background-color 200ms, transform 200ms; }
.closing-link:hover { border-color: rgb(var(--color-border-active) / .62); color: rgb(var(--color-brand-primary)); transform: translateY(-2px); }
.closing-link--primary { border-color: rgb(var(--color-brand-primary)); background: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-contrast)); }
.closing-link--primary:hover { border-color: rgb(var(--color-brand-strong)); background: rgb(var(--color-brand-strong)); color: rgb(var(--color-brand-contrast)); }
@media (prefers-reduced-motion: reduce) { .closing-link:hover { transform: none; } }
</style>
