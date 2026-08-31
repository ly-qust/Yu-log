<script setup lang="ts">
import type { ProjectArchitectureNode } from '@/types/project';

defineProps<{ nodes: ProjectArchitectureNode[] }>();
</script>

<template>
  <section v-if="nodes.length" class="project-architecture" aria-labelledby="project-architecture-title">
    <div class="project-section-heading">
      <p class="font-mono text-[0.63rem] uppercase tracking-[0.15em] text-brand">Architecture</p>
      <h2 id="project-architecture-title">How the system is shaped</h2>
      <p>一张只保留项目真实组成部分的系统地图。</p>
    </div>
    <div class="project-architecture__flow" aria-label="项目架构流程">
      <template v-for="(node, index) in nodes" :key="node.id">
        <div class="project-architecture__node" :class="`is-${node.tone || 'neutral'}`">
          <span class="project-architecture__index">0{{ index + 1 }}</span>
          <strong>{{ node.label }}</strong>
          <small v-if="node.caption">{{ node.caption }}</small>
        </div>
        <span v-if="index < nodes.length - 1" class="project-architecture__connection" aria-hidden="true">→</span>
      </template>
    </div>
  </section>
</template>

<style scoped>
.project-architecture { border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 3.5rem; }
.project-section-heading h2 { margin-top: .65rem; font-family: 'Space Grotesk',sans-serif; font-size: clamp(1.65rem,4vw,2.4rem); font-weight: 650; letter-spacing: -.035em; }
.project-section-heading > p:last-child { max-width: 38rem; margin-top: .85rem; font-size: .9rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.project-architecture__flow { display: grid; grid-template-columns: minmax(0,1fr) auto minmax(0,1fr) auto minmax(0,1fr) auto minmax(0,1fr); align-items: center; gap: .75rem; margin-top: 2rem; }
.project-architecture__node { position: relative; min-height: 8.5rem; display: flex; flex-direction: column; justify-content: center; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .8rem; padding: 1rem; background: rgb(var(--color-surface-elevated) / .62); box-shadow: var(--shadow-soft); }
.project-architecture__node::before { position: absolute; left: 0; top: 1rem; bottom: 1rem; width: 2px; background: rgb(var(--color-text-muted) / .35); content: ''; }
.project-architecture__node.is-brand::before { background: rgb(var(--color-brand-primary)); box-shadow: 0 0 12px rgb(var(--color-brand-primary) / .32); }
.project-architecture__node.is-accent::before { background: rgb(var(--color-accent-secondary)); box-shadow: 0 0 12px rgb(var(--color-accent-secondary) / .28); }
.project-architecture__index { font-family: 'JetBrains Mono',monospace; font-size: .56rem; color: rgb(var(--color-text-muted)); }
.project-architecture__node strong { margin-top: .65rem; font-family: 'Space Grotesk',sans-serif; font-size: 1rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
.project-architecture__node small { margin-top: .35rem; font-family: 'JetBrains Mono',monospace; font-size: .6rem; line-height: 1.5; color: rgb(var(--color-text-muted)); }
.project-architecture__connection { font-family: 'JetBrains Mono',monospace; font-size: 1.15rem; color: rgb(var(--color-brand-primary) / .7); }
@media (max-width: 767px) { .project-architecture__flow { grid-template-columns: 1fr; gap: .5rem; } .project-architecture__node { min-height: 6rem; } .project-architecture__connection { justify-self: center; transform: rotate(90deg); } }
</style>
