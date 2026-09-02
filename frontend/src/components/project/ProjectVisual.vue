<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import type { ProjectItem } from '@/types/project';

const props = defineProps<{ project: ProjectItem }>();
const coverFailed = ref(false);
const initials = computed(() => props.project.name.trim().slice(0, 2).toUpperCase() || 'PR');
const stackSignal = computed(() => props.project.techStack.slice(0, 4));
watch(() => props.project.coverImage, () => { coverFailed.value = false; });
</script>

<template>
  <figure class="project-visual" :class="{ 'is-failed': coverFailed || !project.coverImage }">
    <img v-if="project.coverImage && !coverFailed" :src="project.coverImage" :alt="`${project.name} 项目封面`" decoding="async" loading="eager" @error="coverFailed = true" />
    <div v-else class="project-visual__fallback" role="img" :aria-label="`${project.name} 项目视觉概览`">
      <div class="project-visual__signal" aria-hidden="true">
        <span v-for="(signal, index) in stackSignal" :key="signal" :style="{ '--signal-index': index }">{{ signal }}</span>
      </div>
      <span class="project-visual__label">项目 // SYSTEM MAP</span>
      <strong>{{ initials }}</strong>
      <i aria-hidden="true"></i>
    </div>
    <figcaption v-if="project.coverImage && !coverFailed" class="sr-only">{{ project.name }} 项目封面</figcaption>
  </figure>
</template>

<style scoped>
.project-visual { position: relative; overflow: hidden; min-height: clamp(17rem,32vw,28rem); margin: 0; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: 1rem; background: rgb(var(--color-bg-secondary)); box-shadow: var(--shadow-soft); }
.project-visual > img { display: block; width: 100%; height: clamp(17rem,32vw,28rem); object-fit: cover; }
.project-visual__fallback { position: relative; display: grid; min-height: clamp(17rem,32vw,28rem); place-items: center; overflow: hidden; background: radial-gradient(circle at 78% 18%, rgb(var(--color-accent-secondary) / .14), transparent 30%), linear-gradient(135deg, rgb(var(--color-surface-elevated) / .88), rgb(var(--color-bg-secondary))); }
.project-visual__fallback::before { position: absolute; inset: 0; content: ''; background-image: linear-gradient(rgb(var(--color-brand-primary) / .075) 1px, transparent 1px), linear-gradient(90deg, rgb(var(--color-brand-primary) / .075) 1px, transparent 1px); background-size: 32px 32px; mask-image: linear-gradient(135deg, black, transparent 82%); }
.project-visual__fallback strong { position: relative; z-index: 1; font-family: 'Space Grotesk',sans-serif; font-size: clamp(4rem,12vw,9rem); font-weight: 700; letter-spacing: -.09em; color: rgb(var(--color-brand-primary) / .7); text-shadow: 0 0 44px rgb(var(--color-brand-primary) / .18); }
.project-visual__label { position: absolute; left: 1.25rem; top: 1.15rem; z-index: 2; font-family: 'JetBrains Mono',monospace; font-size: .6rem; letter-spacing: .14em; color: rgb(var(--color-text-muted)); }
.project-visual__fallback i { position: absolute; right: 13%; bottom: 20%; width: 28%; height: 1px; background: rgb(var(--color-brand-primary) / .5); transform: rotate(-24deg); transform-origin: right; box-shadow: 0 0 12px rgb(var(--color-brand-primary) / .42); }
.project-visual__fallback i::after { position: absolute; right: 0; top: -3px; width: 7px; height: 7px; border-radius: 50%; background: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 5px rgb(var(--color-brand-primary) / .12), 0 0 18px rgb(var(--color-brand-primary) / .55); content: ''; }
.project-visual__signal { position: absolute; right: 1.25rem; top: 1rem; display: grid; gap: .45rem; width: 9rem; }
.project-visual__signal span { justify-self: end; border-bottom: 1px solid rgb(var(--color-border-subtle) / .55); padding-bottom: .2rem; font-family: 'JetBrains Mono',monospace; font-size: .55rem; color: rgb(var(--color-text-muted)); opacity: calc(.78 - (var(--signal-index) * .12)); }
@media (max-width: 639px) { .project-visual,.project-visual__fallback,.project-visual > img { min-height: 13rem; height: 13rem; } .project-visual__signal { right: .8rem; width: 7rem; } .project-visual__label { left: .8rem; top: .8rem; font-size: .52rem; } }
</style>
