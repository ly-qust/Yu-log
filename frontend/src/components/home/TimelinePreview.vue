<script setup lang="ts">
import type { TimelineEventItem } from '@/types/timeline';
import { formatDate } from '@/utils/format';
import { formatTimelineType } from '@/utils/format';

defineProps<{ events: TimelineEventItem[] }>();
</script>

<template>
  <section class="mx-auto max-w-6xl py-16 sm:py-20 lg:py-28" aria-labelledby="timeline-preview-title">
    <div class="mb-10 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <p class="terminal-label text-xs uppercase tracking-[0.14em]">GROWTH LOG</p>
        <h2 id="timeline-preview-title" class="mt-3 font-display text-h2">成长轨迹</h2>
      </div>
      <RouterLink class="font-mono text-xs text-brand transition hover:text-brand-strong" to="/timeline">查看完整轨迹 →</RouterLink>
    </div>

    <div class="timeline-signal">
      <article v-for="(event, index) in events.slice(0, 5)" :key="event.id" class="timeline-event">
        <div class="timeline-event__node" aria-hidden="true"><span></span></div>
        <p class="font-mono text-[0.625rem] uppercase tracking-[0.12em] text-brand">{{ formatDate(event.eventDate) }}</p>
        <h3 class="mt-3 font-display text-xl font-semibold leading-snug text-text-primary">{{ event.title }}</h3>
        <p v-if="event.description" class="mt-2 line-clamp-3 text-sm leading-6 text-text-secondary">{{ event.description }}</p>
        <div class="mt-4 flex flex-wrap gap-2">
          <span v-if="event.type" class="rounded-full border border-brand/25 px-2 py-0.5 font-mono text-[0.58rem] text-brand">{{ formatTimelineType(event.type) }}</span>
          <span v-for="tag in event.tags.slice(0, 2)" :key="tag" class="font-mono text-[0.6rem] text-text-muted">#{{ tag }}</span>
        </div>
        <span class="absolute right-4 top-4 font-mono text-[0.58rem] text-text-muted">0{{ index + 1 }}</span>
      </article>
    </div>
  </section>
</template>

<style scoped>
.timeline-signal { position: relative; display: grid; gap: 1.25rem; grid-template-columns: repeat(4,minmax(0,1fr)); }
.timeline-signal::before { position: absolute; left: 1rem; right: 1rem; top: .42rem; height: 1px; content: ''; background: linear-gradient(90deg, rgb(var(--color-brand-primary) / .15), rgb(var(--color-brand-primary) / .72), rgb(var(--color-brand-primary) / .08)); }
.timeline-event { position: relative; min-height: 15rem; padding: 2.2rem 1rem 1rem; border-left: 1px solid rgb(var(--color-border-subtle) / .6); }
.timeline-event__node { position: absolute; left: -.34rem; top: 0; display: grid; width: .72rem; height: .72rem; place-items: center; border: 1px solid rgb(var(--color-brand-primary)); border-radius: 50%; background: rgb(var(--color-bg-primary)); box-shadow: 0 0 16px rgb(var(--color-brand-primary) / .3); }
.timeline-event__node span { width: .25rem; height: .25rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); animation: timeline-pulse 3s ease-in-out infinite; }
@keyframes timeline-pulse { 50% { opacity: .28; transform: scale(.6); } }
@media (max-width: 1023px) { .timeline-signal { grid-template-columns: repeat(2,minmax(0,1fr)); } .timeline-signal::before { display: none; } .timeline-event { border-top: 1px solid rgb(var(--color-border-subtle) / .6); border-left: 0; } .timeline-event__node { left: 0; } }
@media (max-width: 639px) { .timeline-signal { grid-template-columns: 1fr; gap: 0; } .timeline-event { min-height: auto; padding: 1.5rem 0 1.75rem 1.5rem; border-top: 0; border-left: 1px solid rgb(var(--color-border-subtle) / .65); } .timeline-event__node { left: -.36rem; top: 1.75rem; } }
@media (prefers-reduced-motion: reduce) { .timeline-event__node span { animation: none; } }
</style>
