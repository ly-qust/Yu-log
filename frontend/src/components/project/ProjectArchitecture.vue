<script setup lang="ts">
import { computed } from 'vue';

import type { ProjectArchitectureNode } from '@/types/project';

const props = withDefaults(defineProps<{ nodes: ProjectArchitectureNode[]; activeNode?: string }>(), { activeNode: '' });
const emit = defineEmits<{ select: [nodeId: string] }>();
const activeNodeLabel = computed(() => props.nodes.find((node) => node.id === props.activeNode)?.label || '');
</script>

<template>
  <section v-if="nodes.length" class="project-architecture" aria-labelledby="project-architecture-title">
    <div class="project-section-heading">
      <p class="font-mono text-[0.63rem] uppercase tracking-[0.15em] text-brand">架构 // ARCHITECTURE</p>
      <h2 id="project-architecture-title">系统如何连接</h2>
      <p>一张只保留项目真实组成部分的系统地图。</p>
    </div>
    <div class="project-architecture__flow" aria-label="项目架构流程">
      <template v-for="(node, index) in nodes" :key="node.id">
        <button type="button" class="project-architecture__node" :class="[`is-${node.tone || 'neutral'}`, { 'is-active': activeNode === node.id }]" :aria-pressed="activeNode === node.id" @mouseenter="emit('select', node.id)" @mouseleave="emit('select', '')" @focus="emit('select', node.id)" @blur="emit('select', '')">
          <span class="project-architecture__index">0{{ index + 1 }}</span>
          <strong>{{ node.label }}</strong>
          <small v-if="node.caption">{{ node.caption }}</small>
        </button>
        <span v-if="index < nodes.length - 1" class="project-architecture__connection" :class="{ 'is-active': activeNode === node.id || activeNode === nodes[index + 1]?.id }" aria-hidden="true">→</span>
      </template>
    </div>
    <p v-if="activeNodeLabel" class="project-architecture__signal" aria-live="polite">当前选择：{{ activeNodeLabel }}</p>
  </section>
</template>

<style scoped>
.project-architecture { border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 3.5rem; }
.project-section-heading h2 { margin-top: .65rem; font-family: 'Space Grotesk',sans-serif; font-size: clamp(1.65rem,4vw,2.4rem); font-weight: 650; letter-spacing: -.035em; }
.project-section-heading > p:last-child { max-width: 38rem; margin-top: .85rem; font-size: .9rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.project-architecture__flow { display: grid; grid-template-columns: minmax(0,1fr) auto minmax(0,1fr) auto minmax(0,1fr) auto minmax(0,1fr); align-items: center; gap: .75rem; margin-top: 2rem; }
.project-architecture__node { position: relative; min-height: 8.5rem; display: flex; flex-direction: column; justify-content: center; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .8rem; padding: 1rem; background: rgb(var(--color-surface-elevated) / .62); box-shadow: var(--shadow-soft); color: inherit; cursor: pointer; text-align: left; transition: border-color var(--motion-fast) var(--ease-standard), background-color var(--motion-fast) var(--ease-standard), box-shadow var(--motion-normal) var(--ease-emphasized), transform var(--motion-normal) var(--ease-emphasized); }
.project-architecture__node:hover, .project-architecture__node:focus-visible, .project-architecture__node.is-active { border-color: rgb(var(--color-brand-primary) / .85); background: rgb(var(--color-brand-primary) / .1); box-shadow: var(--shadow-glow); outline: none; transform: translateY(-2px); }
.project-architecture__node::before { position: absolute; left: 0; top: 1rem; bottom: 1rem; width: 2px; background: rgb(var(--color-text-muted) / .35); content: ''; }
.project-architecture__node.is-brand::before { background: rgb(var(--color-brand-primary)); box-shadow: 0 0 12px rgb(var(--color-brand-primary) / .32); }
.project-architecture__node.is-accent::before { background: rgb(var(--color-accent-secondary)); box-shadow: 0 0 12px rgb(var(--color-accent-secondary) / .28); }
.project-architecture__index { font-family: 'JetBrains Mono',monospace; font-size: .56rem; color: rgb(var(--color-text-muted)); }
.project-architecture__node strong { margin-top: .65rem; font-family: 'Space Grotesk',sans-serif; font-size: 1rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
.project-architecture__node small { margin-top: .35rem; font-family: 'JetBrains Mono',monospace; font-size: .6rem; line-height: 1.5; color: rgb(var(--color-text-muted)); }
.project-architecture__connection { font-family: 'JetBrains Mono',monospace; font-size: 1.15rem; color: rgb(var(--color-brand-primary) / .7); transition: color var(--motion-fast) var(--ease-standard), transform var(--motion-normal) var(--ease-emphasized); }
.project-architecture__connection.is-active { color: rgb(var(--color-brand-primary)); transform: scale(1.12); }
.project-architecture__signal { margin-top: 1rem; font-family: 'JetBrains Mono',monospace; font-size: .62rem; letter-spacing: .08em; color: rgb(var(--color-brand-primary)); }
@media (max-width: 767px) { .project-architecture__flow { grid-template-columns: 1fr; gap: .5rem; } .project-architecture__node { min-height: 6rem; } .project-architecture__connection { justify-self: center; transform: rotate(90deg); } .project-architecture__connection.is-active { transform: rotate(90deg) scale(1.12); } }
@media (prefers-reduced-motion: reduce) { .project-architecture__node, .project-architecture__connection { transition: none; } .project-architecture__node:hover, .project-architecture__node:focus-visible, .project-architecture__node.is-active { transform: none; } }
</style>
