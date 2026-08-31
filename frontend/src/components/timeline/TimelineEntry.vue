<script setup lang="ts">
import { computed } from 'vue';

import BaseBadge from '@/components/common/BaseBadge.vue';
import type { TimelineEventItem } from '@/types/timeline';
import { formatDate } from '@/utils/format';

const props = withDefaults(defineProps<{
  event: TimelineEventItem;
  index: number;
  side?: 'left' | 'right';
}>(), {
  side: 'left',
});

const type = computed(() => props.event.type?.trim() || 'MILESTONE');
const variant = computed(() => type.value === 'PROJECT' ? 'brand' : type.value === 'DATABASE' ? 'accent' : type.value === 'AWARD' ? 'warning' : 'success');
const entryId = computed(() => `timeline-entry-${props.event.id}`);
</script>

<template>
  <li class="timeline-entry" :class="`is-${side}`">
    <span class="timeline-entry__line-node" aria-hidden="true"><span></span></span>
    <article :aria-labelledby="entryId" class="timeline-entry__card">
      <div class="timeline-entry__meta">
        <time :datetime="event.eventDate">{{ formatDate(event.eventDate) }}</time>
        <BaseBadge :variant="variant" dot>{{ type }}</BaseBadge>
      </div>
      <p class="timeline-entry__index">{{ String(index + 1).padStart(2, '0') }}</p>
      <h2 :id="entryId">{{ event.title }}</h2>
      <p v-if="event.description" class="timeline-entry__description">{{ event.description }}</p>
      <div v-if="event.tags.length" class="timeline-entry__tags"><span v-for="tag in event.tags" :key="tag">#{{ tag }}</span></div>
    </article>
  </li>
</template>

<style scoped>
.timeline-entry { position: relative; width: 50%; padding: 0 2.5rem 2.5rem; }
.timeline-entry.is-left { padding-left: 0; padding-right: 2.5rem; }
.timeline-entry.is-right { margin-left: 50%; padding-left: 2.5rem; padding-right: 0; }
.timeline-entry__line-node { position: absolute; top: 1.6rem; right: -.42rem; display: grid; width: .84rem; height: .84rem; place-items: center; border: 1px solid rgb(var(--color-brand-primary)); border-radius: 50%; background: rgb(var(--color-bg-primary)); box-shadow: 0 0 0 5px rgb(var(--color-brand-primary) / .08), 0 0 16px rgb(var(--color-brand-primary) / .35); }
.timeline-entry__line-node span { width: .28rem; height: .28rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); }
.timeline-entry.is-right .timeline-entry__line-node { right: auto; left: -.42rem; }
.timeline-entry__card { position: relative; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: .85rem; padding: 1.3rem; background: rgb(var(--color-surface-elevated) / .6); box-shadow: var(--shadow-soft); transition: border-color 180ms, background-color 180ms, transform 180ms; }
.timeline-entry__card:hover { border-color: rgb(var(--color-border-active) / .5); background: rgb(var(--color-surface-hover) / .5); transform: translateY(-2px); }
.timeline-entry__card::before { position: absolute; left: 0; top: 1rem; bottom: 1rem; width: 2px; background: rgb(var(--color-brand-primary) / .7); content: ''; }
.timeline-entry.is-right .timeline-entry__card::before { background: rgb(var(--color-accent-secondary) / .8); }
.timeline-entry__meta { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: .6rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.timeline-entry__index { margin-top: 1.4rem; font-family: 'JetBrains Mono', monospace; font-size: .58rem; color: rgb(var(--color-brand-primary)); }
.timeline-entry h2 { margin-top: .55rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.2rem, 2.4vw, 1.65rem); font-weight: 650; line-height: 1.25; letter-spacing: -.025em; }
.timeline-entry__description { margin-top: .7rem; font-size: .84rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.timeline-entry__tags { display: flex; flex-wrap: wrap; gap: .55rem .9rem; margin-top: 1rem; font-family: 'JetBrains Mono', monospace; font-size: .6rem; color: rgb(var(--color-text-muted)); }
@media (max-width: 767px) { .timeline-entry, .timeline-entry.is-left, .timeline-entry.is-right { width: 100%; margin-left: 0; padding: 0 0 1.5rem 1.5rem; } .timeline-entry__line-node, .timeline-entry.is-right .timeline-entry__line-node { top: 1.55rem; right: auto; left: -.42rem; } .timeline-entry__card:hover { transform: none; } }
@media (prefers-reduced-motion: reduce) { .timeline-entry__card:hover { transform: none; } }
</style>
