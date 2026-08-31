<script setup lang="ts">
import { computed } from 'vue';

import BaseBadge from '@/components/common/BaseBadge.vue';
import type { ProjectItem } from '@/types/project';
import { formatDate, formatProjectStatus } from '@/utils/format';
import { safeExternalUrl } from '@/utils/links';

const props = defineProps<{ project: ProjectItem }>();
const githubUrl = computed(() => safeExternalUrl(props.project.githubUrl));
const demoUrl = computed(() => safeExternalUrl(props.project.demoUrl));
const statusVariant = computed(() => props.project.status === 'COMPLETED' ? 'success' : props.project.status === 'DEVELOPING' ? 'brand' : 'accent');
const visibleTech = computed(() => props.project.techStack.slice(0, 5));
const extraTechCount = computed(() => Math.max(props.project.techStack.length - visibleTech.value.length, 0));
</script>

<template>
  <section class="project-snapshot" aria-labelledby="project-snapshot-title">
    <div class="project-snapshot__heading">
      <p class="font-mono text-[0.63rem] uppercase tracking-[0.15em] text-brand">Project snapshot</p>
      <h2 id="project-snapshot-title">The working set</h2>
    </div>
    <dl class="project-snapshot__grid">
      <div><dt>Stack</dt><dd><span v-for="tech in visibleTech" :key="tech">{{ tech }}</span><span v-if="extraTechCount">+{{ extraTechCount }}</span></dd></div>
      <div><dt>Status</dt><dd><BaseBadge :variant="statusVariant" dot>{{ formatProjectStatus(project.status) }}</BaseBadge></dd></div>
      <div><dt>Updated</dt><dd>{{ formatDate(project.updatedAt || project.createdAt) }}</dd></div>
      <div v-if="githubUrl || demoUrl"><dt>Links</dt><dd class="project-snapshot__links"><a v-if="githubUrl" :href="githubUrl" target="_blank" rel="noreferrer">GitHub ↗</a><a v-if="demoUrl" :href="demoUrl" target="_blank" rel="noreferrer">Demo ↗</a></dd></div>
    </dl>
  </section>
</template>

<style scoped>
.project-snapshot { border-top: 1px solid rgb(var(--color-border-subtle) / .68); border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding: 1.5rem 0; }
.project-snapshot__heading h2 { margin-top: .5rem; font-family: 'Space Grotesk',sans-serif; font-size: 1.25rem; font-weight: 600; letter-spacing: -.02em; }
.project-snapshot__grid { display: grid; grid-template-columns: 1.55fr repeat(3,1fr); gap: 1.5rem; margin-top: 1.5rem; }
.project-snapshot__grid > div { min-width: 0; }
.project-snapshot dt { font-family: 'JetBrains Mono',monospace; font-size: .58rem; text-transform: uppercase; letter-spacing: .12em; color: rgb(var(--color-text-muted)); }
.project-snapshot dd { display: flex; flex-wrap: wrap; align-items: center; gap: .45rem .8rem; margin-top: .7rem; font-size: .8rem; line-height: 1.6; color: rgb(var(--color-text-primary)); }
.project-snapshot dd > span { white-space: nowrap; }
.project-snapshot dd > span:not(:last-child)::after { margin-left: .8rem; color: rgb(var(--color-text-muted)); content: '·'; }
.project-snapshot__links a { color: rgb(var(--color-brand-primary)); transition: color 180ms; }
.project-snapshot__links a:hover { color: rgb(var(--color-brand-strong)); }
@media (max-width: 767px) { .project-snapshot__grid { grid-template-columns: repeat(2,minmax(0,1fr)); gap: 1.25rem 1rem; } .project-snapshot__grid > div:first-child { grid-column: 1 / -1; } }
</style>
