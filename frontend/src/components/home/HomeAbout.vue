<script setup lang="ts">
import { computed } from 'vue';

import type { AboutData, AboutProfile } from '@/types/site';

const props = withDefaults(defineProps<{
  about?: AboutData | null;
  profile?: AboutProfile;
  learning?: string[];
}>(), {
  about: null,
  profile: () => ({}),
  learning: () => [],
});

const directions = computed(() => Array.isArray(props.profile.careerDirection)
  ? props.profile.careerDirection.filter((item): item is string => typeof item === 'string' && Boolean(item.trim())).slice(0, 4)
  : []);
const role = computed(() => typeof props.profile.role === 'string' ? props.profile.role : '计算机科学与技术学习者');
const description = computed(() => typeof props.profile.description === 'string' && props.profile.description.trim()
  ? props.profile.description
  : '我把课程设计、问题排查与项目实践，整理成可以长期复用的工程经验。');
const location = computed(() => typeof props.profile.location === 'string' ? props.profile.location.trim() : '');
</script>

<template>
  <section class="mx-auto grid max-w-6xl gap-8 py-16 sm:py-20 lg:grid-cols-[0.72fr_1.28fr] lg:gap-16 lg:py-28" aria-labelledby="about-snapshot-title">
    <div class="lg:sticky lg:top-28 lg:self-start">
      <p class="terminal-label text-xs uppercase tracking-[0.14em]">01 // IDENTITY</p>
      <h2 id="about-snapshot-title" class="mt-3 font-display text-h2">系统背后，<br />是一个仍在学习的人。</h2>
      <RouterLink class="mt-5 inline-flex items-center gap-2 font-mono text-xs text-brand transition hover:text-brand-strong" to="/about">完整了解我 <span aria-hidden="true">→</span></RouterLink>
    </div>

    <div>
      <p class="max-w-3xl font-display text-[clamp(1.45rem,3vw,2.35rem)] font-medium leading-[1.35] tracking-[-0.025em] text-text-primary">
        {{ description }}
      </p>
      <div class="mt-8 grid gap-px overflow-hidden rounded-panel border border-border-subtle/65 bg-border-subtle/65 sm:grid-cols-2">
        <div class="bg-surface-elevated/75 p-5 sm:p-6">
          <p class="font-mono text-[0.625rem] uppercase tracking-[0.15em] text-text-muted">当前身份</p>
          <p class="mt-3 text-base font-medium text-text-primary">{{ role }}</p>
          <p v-if="location" class="mt-1 text-sm text-text-muted">{{ location }}</p>
        </div>
        <div class="bg-surface-elevated/75 p-5 sm:p-6">
          <p class="font-mono text-[0.625rem] uppercase tracking-[0.15em] text-text-muted">学习方式</p>
          <p class="mt-3 text-sm leading-7 text-text-secondary">{{ about?.learningPhilosophy || '通过实践学习，把真正可复用的部分留下来。' }}</p>
        </div>
      </div>

      <div v-if="directions.length" class="mt-8 grid gap-3 sm:grid-cols-2">
        <div v-for="(direction, index) in directions" :key="direction" class="group flex items-center gap-4 border-b border-border-subtle/65 py-4">
          <span class="font-mono text-[0.625rem] text-brand">0{{ index + 1 }}</span>
          <span class="text-sm font-medium text-text-primary transition group-hover:translate-x-1 group-hover:text-brand">{{ direction }}</span>
        </div>
      </div>

      <div v-if="learning.length" class="mt-8 flex flex-wrap items-center gap-2">
            <span class="mr-2 font-mono text-[0.625rem] uppercase tracking-[0.14em] text-text-muted">最近在看</span>
        <span v-for="item in learning.slice(0, 6)" :key="item" class="rounded-full border border-border-subtle/75 px-2.5 py-1 font-mono text-[0.65rem] text-text-secondary">{{ item }}</span>
      </div>
    </div>
  </section>
</template>
