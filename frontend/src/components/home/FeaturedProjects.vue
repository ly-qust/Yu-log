<script setup lang="ts">
import { computed } from 'vue';

import BaseBadge from '@/components/common/BaseBadge.vue';
import type { ProjectItem } from '@/types/project';
import { safeExternalUrl } from '@/utils/links';
import { formatProjectStatus } from '@/utils/format';

const props = defineProps<{ projects: ProjectItem[] }>();
const visibleProjects = computed(() => props.projects.slice(0, 3));

</script>

<template>
  <section class="relative left-1/2 w-[min(100vw-2rem,80rem)] -translate-x-1/2 py-16 sm:py-20 lg:py-28" aria-labelledby="featured-projects-title">
    <div class="mb-9 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="terminal-label text-xs uppercase tracking-[0.14em]">SELECTED WORK</p>
        <h2 id="featured-projects-title" class="mt-3 font-display text-h2">精选项目</h2>
        <p class="mt-3 max-w-2xl text-sm leading-7 text-text-secondary">把想法变成可运行的系统，也把过程中遇到的问题沉淀下来。</p>
      </div>
      <RouterLink class="font-mono text-xs text-brand transition hover:text-brand-strong" to="/projects">进入项目实验室 →</RouterLink>
    </div>

    <div class="grid gap-4 lg:grid-cols-12 lg:grid-rows-2">
      <article v-for="(project, index) in visibleProjects" :key="project.id" class="project-card group" :class="index === 0 ? 'lg:col-span-8 lg:row-span-2' : 'lg:col-span-4'">
        <div v-if="project.coverImage" class="project-card__cover" :class="index === 0 ? 'min-h-64' : 'min-h-32'">
          <img :src="project.coverImage" :alt="`${project.name} 项目封面`" loading="lazy" @error="($event.currentTarget as HTMLImageElement).style.display = 'none'" />
        </div>
        <div v-else class="project-card__fallback" :class="index === 0 ? 'min-h-64' : 'min-h-32'" aria-hidden="true">
          <span class="project-card__index">0{{ index + 1 }}</span>
          <div class="project-card__branch"><i></i><i></i><i></i></div>
          <span class="project-card__monogram">{{ project.name.slice(0, 2).toUpperCase() }}</span>
        </div>

        <div class="project-card__body" :class="index === 0 ? 'sm:p-7' : ''">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <BaseBadge :variant="project.status === 'COMPLETED' ? 'success' : 'brand'" dot>{{ formatProjectStatus(project.status) }}</BaseBadge>
            <span class="font-mono text-[0.625rem] uppercase tracking-[0.13em] text-text-muted">项目 // 0{{ index + 1 }}</span>
          </div>
          <h3 class="mt-5 font-display font-semibold tracking-tight text-text-primary" :class="index === 0 ? 'text-3xl sm:text-4xl' : 'text-xl'">{{ project.name }}</h3>
          <p class="mt-3 text-sm leading-7 text-text-secondary" :class="index === 0 ? 'max-w-2xl sm:text-base' : 'line-clamp-3'">{{ project.description || '项目说明正在整理中。' }}</p>

          <div v-if="project.techStack.length" class="mt-5 flex flex-wrap gap-2">
            <span v-for="tech in project.techStack.slice(0, index === 0 ? 8 : 4)" :key="tech" class="font-mono text-[0.65rem] text-text-muted"><span class="text-brand">+</span> {{ tech }}</span>
          </div>

          <div class="mt-6 flex flex-wrap items-center gap-4">
            <RouterLink class="inline-flex items-center gap-2 font-mono text-xs font-semibold text-brand transition group-hover:gap-3" :to="`/projects/${project.id}`">查看详情 <span aria-hidden="true">→</span></RouterLink>
            <a v-if="safeExternalUrl(project.githubUrl)" :href="safeExternalUrl(project.githubUrl)" class="font-mono text-xs text-text-muted transition hover:text-text-primary" target="_blank" rel="noreferrer">GitHub ↗</a>
            <a v-if="safeExternalUrl(project.demoUrl)" :href="safeExternalUrl(project.demoUrl)" class="font-mono text-xs text-text-muted transition hover:text-text-primary" target="_blank" rel="noreferrer">Demo ↗</a>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.project-card { overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: 1.25rem; background: rgb(var(--color-surface-elevated) / .7); box-shadow: var(--shadow-soft); transition: border-color 240ms, transform 240ms, box-shadow 240ms; }
.project-card:hover { border-color: rgb(var(--color-border-active) / .55); box-shadow: var(--shadow-glow); transform: translateY(-3px); }
.project-card__cover, .project-card__fallback { position: relative; overflow: hidden; border-bottom: 1px solid rgb(var(--color-border-subtle) / .55); }
.project-card__cover img { width: 100%; height: 100%; position: absolute; inset: 0; object-fit: cover; transition: transform 500ms cubic-bezier(.2,.8,.2,1); }
.project-card:hover .project-card__cover img { transform: scale(1.025); }
.project-card__fallback { display: grid; place-items: center; background: radial-gradient(circle at 72% 24%, rgb(var(--color-accent-secondary) / .13), transparent 32%), linear-gradient(135deg, rgb(var(--color-bg-secondary) / .9), rgb(var(--color-surface) / .65)); }
.project-card__fallback::before { position: absolute; inset: 0; content: ''; opacity: .7; background-image: linear-gradient(rgb(var(--color-brand-primary) / .055) 1px, transparent 1px), linear-gradient(90deg, rgb(var(--color-brand-primary) / .055) 1px, transparent 1px); background-size: 28px 28px; }
.project-card__index { position: absolute; left: 1rem; top: .8rem; font-family: 'JetBrains Mono',monospace; font-size: .6rem; color: rgb(var(--color-text-muted)); }
.project-card__monogram { position: relative; z-index: 1; font-family: 'Space Grotesk',sans-serif; font-size: clamp(2.5rem,6vw,5rem); font-weight: 700; letter-spacing: -.08em; color: rgb(var(--color-brand-primary) / .72); text-shadow: 0 0 40px rgb(var(--color-brand-primary) / .18); }
.project-card__branch { position: absolute; inset: 0; }
.project-card__branch::before, .project-card__branch i { position: absolute; height: 1px; background: rgb(var(--color-brand-primary) / .2); content: ''; }
.project-card__branch::before { left: 8%; top: 50%; width: 84%; }
.project-card__branch i { left: 48%; top: 50%; width: 28%; transform-origin: left; }
.project-card__branch i:nth-child(1) { transform: rotate(-32deg); }
.project-card__branch i:nth-child(2) { transform: rotate(32deg); }
.project-card__branch i:nth-child(3) { left: 22%; width: 25%; transform: rotate(-42deg); }
.project-card__body { padding: 1.25rem; }
@media (prefers-reduced-motion: reduce) { .project-card:hover { transform: none; } .project-card:hover .project-card__cover img { transform: none; } }
</style>
