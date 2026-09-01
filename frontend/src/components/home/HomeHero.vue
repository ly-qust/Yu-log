<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue';

import { homepageIdentity } from '@/config/homepage';
import type { AboutProfile, HomeHero, HomeStats } from '@/types/site';
import { formatCount } from '@/utils/format';

const props = withDefaults(defineProps<{
  hero?: HomeHero;
  stats?: HomeStats;
  profile?: AboutProfile;
  learning?: string[];
  githubUrl?: string;
}>(), {
  hero: undefined,
  stats: undefined,
  profile: () => ({}),
  learning: () => [],
  githubUrl: '',
});

const visual = ref<HTMLElement | null>(null);
const activeSignal = ref<number | null>(null);
let pointerFrame = 0;
let pendingPointer: { clientX: number; clientY: number } | null = null;
const nickname = computed(() => String(props.profile.nickname || 'Yu'));
const eyebrow = computed(() => {
  const role = typeof props.profile.role === 'string' ? props.profile.role.trim() : '';
  return role ? `${role} · Digital Gardener` : homepageIdentity.eyebrowFallback;
});
const title = computed(() => props.hero?.title?.trim() || `Hi, I’m ${nickname.value}.` || homepageIdentity.heroTitleFallback);
const description = computed(() => props.hero?.description?.trim() || homepageIdentity.heroDescriptionFallback);
const status = computed(() => props.hero?.statusText?.trim() || homepageIdentity.statusFallback);
const focus = computed(() => props.learning[0] || 'Backend Systems');
const visualLabels = computed(() => {
  const labels = props.learning.slice(0, 4);
  return labels.length ? labels : ['build', 'learn', 'document', 'grow'];
});
const metrics = computed(() => props.stats ? [
  { label: 'Articles', value: props.stats.articleCount },
  { label: 'Projects', value: props.stats.projectCount },
  { label: 'Notes', value: props.stats.noteCount },
] : []);

function trackHeroPointer(event: PointerEvent) {
  if (event.pointerType !== 'mouse' || window.matchMedia('(prefers-reduced-motion: reduce)').matches || !visual.value) {
    return;
  }
  pendingPointer = { clientX: event.clientX, clientY: event.clientY };
  if (pointerFrame) return;
  pointerFrame = window.requestAnimationFrame(() => {
    pointerFrame = 0;
    if (!visual.value || !pendingPointer) return;
    const rect = visual.value.getBoundingClientRect();
    const x = (pendingPointer.clientX - rect.left) / rect.width;
    const y = (pendingPointer.clientY - rect.top) / rect.height;
    const clampedX = Math.max(0, Math.min(1, x));
    const clampedY = Math.max(0, Math.min(1, y));
    visual.value.style.setProperty('--pointer-x', `${pendingPointer.clientX - rect.left}px`);
    visual.value.style.setProperty('--pointer-y', `${pendingPointer.clientY - rect.top}px`);
    visual.value.style.setProperty('--pointer-tilt-x', `${((0.5 - clampedY) * 3.6).toFixed(2)}deg`);
    visual.value.style.setProperty('--pointer-tilt-y', `${((clampedX - 0.5) * 3.6).toFixed(2)}deg`);
  });
}

function resetHeroPointer() {
  pendingPointer = null;
  if (pointerFrame) {
    window.cancelAnimationFrame(pointerFrame);
    pointerFrame = 0;
  }
  visual.value?.style.setProperty('--pointer-x', '50%');
  visual.value?.style.setProperty('--pointer-y', '50%');
  visual.value?.style.setProperty('--pointer-tilt-x', '0deg');
  visual.value?.style.setProperty('--pointer-tilt-y', '0deg');
}

onUnmounted(() => resetHeroPointer());
</script>

<template>
  <section class="hero-shell" aria-labelledby="home-hero-title">
    <div class="hero-ambient hero-ambient--brand" aria-hidden="true"></div>
    <div class="hero-ambient hero-ambient--accent" aria-hidden="true"></div>

    <div class="relative z-10 grid gap-10 lg:grid-cols-[1.08fr_0.92fr] lg:items-center lg:gap-12">
      <div class="hero-copy">
        <div class="inline-flex items-center gap-2 font-mono text-[0.6875rem] uppercase tracking-[0.14em] text-brand">
          <span class="h-1.5 w-1.5 rounded-full bg-success shadow-[0_0_12px_rgb(var(--color-success)/0.8)]" aria-hidden="true"></span>
          {{ eyebrow }}
        </div>

        <h1 id="home-hero-title" class="mt-6 font-display text-[clamp(2.65rem,7vw,5.75rem)] font-bold leading-[0.94] tracking-[-0.055em] text-text-primary">
          <span class="block text-[0.42em] font-medium leading-none tracking-[-0.025em] text-text-secondary">{{ title }}</span>
          <span class="mt-5 block">Build. Learn.</span>
          <span class="block text-brand">Document. Grow.</span>
        </h1>

        <p v-if="hero?.subtitle" class="mt-6 font-mono text-xs uppercase tracking-[0.13em] text-text-muted">
          {{ hero.subtitle }}
        </p>
        <p class="mt-4 max-w-2xl text-base leading-8 text-text-secondary sm:text-lg">
          {{ description }}
        </p>

        <div class="mt-8 flex flex-col gap-3 sm:flex-row sm:items-center">
          <RouterLink class="hero-cta hero-cta--primary" to="/projects">
            Explore my work
            <svg aria-hidden="true" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path d="M5 12h14m-6-6 6 6-6 6" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" /></svg>
          </RouterLink>
          <RouterLink class="hero-cta hero-cta--secondary" to="/articles">Read the writing</RouterLink>
          <a v-if="githubUrl" :href="githubUrl" class="hero-github" target="_blank" rel="noreferrer" aria-label="访问 Yu 的 GitHub">GitHub ↗</a>
        </div>

        <dl v-if="metrics.length" class="mt-10 flex flex-wrap gap-x-9 gap-y-4 border-t border-border-subtle/65 pt-6">
          <div v-for="metric in metrics" :key="metric.label" class="min-w-20">
            <dd class="font-display text-2xl font-semibold tracking-tight text-text-primary">{{ formatCount(metric.value) }}</dd>
            <dt class="mt-0.5 font-mono text-[0.625rem] uppercase tracking-[0.14em] text-text-muted">{{ metric.label }}</dt>
          </div>
        </dl>
      </div>

      <div ref="visual" class="garden-core" @pointermove="trackHeroPointer" @pointerleave="resetHeroPointer">
        <div class="garden-core__light" aria-hidden="true"></div>
        <div class="garden-core__header">
          <div class="flex items-center gap-2" aria-hidden="true"><span></span><span></span><span></span></div>
          <p>~/yu-log/garden.core</p>
          <p class="hidden sm:block">LIVE_SIGNAL</p>
        </div>

        <div class="garden-core__canvas" role="group" aria-label="数字花园信号图">
          <svg class="garden-core__lines" viewBox="0 0 520 430" fill="none" preserveAspectRatio="xMidYMid meet">
            <path d="M260 214 100 112M260 214 420 102M260 214 438 292M260 214 126 332M260 214 258 56" />
            <path v-for="(label, index) in visualLabels" :key="`signal-${label}`" class="garden-core__signal" :class="{ 'is-active': activeSignal === index }" :d="['M260 214 100 112', 'M260 214 420 102', 'M260 214 438 292', 'M260 214 126 332'][index]" />
            <circle cx="260" cy="214" r="88" />
            <circle cx="260" cy="214" r="132" stroke-dasharray="3 13" />
          </svg>
          <div class="garden-core__orbit"></div>
          <div class="garden-core__center">
            <span class="garden-core__pulse"></span>
            <svg viewBox="0 0 64 64" fill="none"><path d="M32 52V24m0 17c-8-7-15-7-21-3 3 10 10 15 21 15m0-9c7-7 14-8 21-4-3 10-10 15-21 15M32 29c-8-7-10-14-6-21 10 3 15 10 14 21" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2.2" /></svg>
          </div>
          <button v-for="(label, index) in visualLabels" :key="label" type="button" class="garden-core__node" :class="[`garden-core__node--${index + 1}`, { 'is-active': activeSignal === index }]" :aria-pressed="activeSignal === index" @mouseenter="activeSignal = index" @mouseleave="activeSignal = null" @focus="activeSignal = index" @blur="activeSignal = null">{{ label }}</button>
        </div>

        <div class="garden-core__status">
          <div><span>Status</span><strong>{{ status }}</strong></div>
          <div><span>Focus</span><strong>{{ focus }}</strong></div>
          <div v-if="stats"><span>Garden</span><strong>{{ formatCount(stats.noteCount) }} notes</strong></div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero-shell { position: relative; min-height: min(760px, calc(100vh - 5rem)); padding: clamp(4rem, 8vw, 7rem) 0; display: grid; align-items: center; overflow: hidden; }
.hero-ambient { position: absolute; pointer-events: none; border-radius: 999px; filter: blur(18px); }
.hero-ambient--brand { left: -12%; top: 8%; width: 28rem; height: 28rem; background: rgb(var(--color-glow-brand) / .09); }
.hero-ambient--accent { right: -8%; bottom: 8%; width: 22rem; height: 22rem; background: rgb(var(--color-glow-accent) / .07); }
.hero-cta { display: inline-flex; min-height: 3rem; align-items: center; justify-content: center; gap: .55rem; border-radius: .625rem; border: 1px solid; padding: 0 1.15rem; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .75rem; font-weight: 700; transition: transform 240ms, border-color 240ms, background-color 240ms, color 240ms; }
.hero-cta:hover { transform: translateY(-2px); }
.hero-cta--primary { border-color: rgb(var(--color-brand-primary)); background: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-contrast)); box-shadow: var(--shadow-glow); }
.hero-cta--primary:hover { background: rgb(var(--color-brand-strong)); border-color: rgb(var(--color-brand-strong)); }
.hero-cta--secondary { border-color: rgb(var(--color-border-subtle) / .8); background: rgb(var(--color-surface-elevated) / .48); color: rgb(var(--color-text-primary)); }
.hero-cta--secondary:hover { border-color: rgb(var(--color-border-active) / .65); color: rgb(var(--color-brand-primary)); }
.hero-github { padding: .65rem .5rem; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .75rem; color: rgb(var(--color-text-muted)); transition: color 200ms; }
.hero-github:hover { color: rgb(var(--color-brand-primary)); }
.garden-core { --pointer-x: 50%; --pointer-y: 50%; --pointer-tilt-x: 0deg; --pointer-tilt-y: 0deg; position: relative; overflow: hidden; min-height: 34rem; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: 1.25rem; background: linear-gradient(145deg, rgb(var(--color-surface-elevated) / .74), rgb(var(--color-bg-secondary) / .56)); box-shadow: var(--shadow-elevated); isolation: isolate; }
.garden-core::before { position: absolute; inset: 0; content: ''; background-image: linear-gradient(rgb(var(--color-brand-primary) / .05) 1px, transparent 1px), linear-gradient(90deg, rgb(var(--color-brand-primary) / .05) 1px, transparent 1px); background-size: 30px 30px; mask-image: radial-gradient(circle at 50% 45%, black, transparent 78%); }
.garden-core__light { position: absolute; inset: 0; z-index: -1; background: radial-gradient(260px circle at var(--pointer-x) var(--pointer-y), rgb(var(--color-brand-primary) / .13), transparent 68%); opacity: .7; }
.garden-core__header { position: relative; z-index: 2; display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; min-height: 3.25rem; border-bottom: 1px solid rgb(var(--color-border-subtle) / .55); padding: 0 1rem; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .58rem; letter-spacing: .1em; text-transform: uppercase; color: rgb(var(--color-text-muted)); }
.garden-core__header > p:last-child { text-align: right; color: rgb(var(--color-success)); }
.garden-core__header div span { width: .48rem; height: .48rem; border-radius: 99px; background: rgb(var(--color-border-subtle)); }
.garden-core__header div span:nth-child(2) { background: rgb(var(--color-warning) / .7); }
.garden-core__header div span:nth-child(3) { background: rgb(var(--color-brand-primary) / .8); }
.garden-core__canvas { position: relative; min-height: 26.5rem; transform: perspective(900px) rotateX(var(--pointer-tilt-x)) rotateY(var(--pointer-tilt-y)); transform-origin: center; transition: transform var(--motion-slow) var(--ease-standard); }
.garden-core__lines { position: absolute; inset: 1rem 0 0; width: 100%; height: calc(100% - 1rem); stroke: rgb(var(--color-border-active) / .38); stroke-width: 1; }
.garden-core__signal { stroke: rgb(var(--color-brand-primary) / .44); stroke-dasharray: 4 18; opacity: .68; animation: signal-flow 9s linear infinite; transition: opacity var(--motion-fast) var(--ease-standard), stroke-width var(--motion-fast) var(--ease-standard), stroke var(--motion-fast) var(--ease-standard); }
.garden-core__signal.is-active { stroke: rgb(var(--color-brand-primary)); stroke-width: 1.8; opacity: 1; }
.garden-core__orbit { position: absolute; left: 50%; top: 50%; width: 15.5rem; height: 15.5rem; border: 1px solid rgb(var(--color-accent-secondary) / .22); border-radius: 50%; transform: translate(-50%, -50%); }
.garden-core__orbit::after { position: absolute; left: 50%; top: -.25rem; width: .5rem; height: .5rem; content: ''; border-radius: 50%; background: rgb(var(--color-accent-secondary)); box-shadow: 0 0 18px rgb(var(--color-accent-secondary) / .7); animation: orbit-spin 12s linear infinite; transform-origin: 0 8rem; }
.garden-core__center { position: absolute; left: 50%; top: 50%; display: grid; width: 6.75rem; height: 6.75rem; place-items: center; border: 1px solid rgb(var(--color-brand-primary) / .7); border-radius: 50%; background: rgb(var(--color-bg-primary) / .86); color: rgb(var(--color-brand-primary)); box-shadow: 0 0 48px rgb(var(--color-brand-primary) / .18); transform: translate(-50%, -50%); }
.garden-core__center svg { width: 3.5rem; height: 3.5rem; }
.garden-core__pulse { position: absolute; inset: -.55rem; border: 1px solid rgb(var(--color-brand-primary) / .32); border-radius: 50%; animation: core-breathe 3.8s ease-in-out infinite; }
.garden-core__node { position: absolute; max-width: 8.5rem; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .8); border-radius: 999px; background: rgb(var(--color-surface-elevated) / .88); padding: .42rem .7rem; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .58rem; text-overflow: ellipsis; white-space: nowrap; color: rgb(var(--color-text-secondary)); box-shadow: var(--shadow-soft); cursor: pointer; text-align: left; transition: border-color var(--motion-fast) var(--ease-standard), background-color var(--motion-fast) var(--ease-standard), color var(--motion-fast) var(--ease-standard), box-shadow var(--motion-fast) var(--ease-standard); }
.garden-core__node::before { display: inline-block; width: .35rem; height: .35rem; margin-right: .4rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); content: ''; box-shadow: 0 0 8px rgb(var(--color-brand-primary) / .65); }
.garden-core__node:hover, .garden-core__node:focus-visible, .garden-core__node.is-active { border-color: rgb(var(--color-brand-primary) / .82); background: rgb(var(--color-brand-primary) / .12); color: rgb(var(--color-brand-primary)); box-shadow: var(--shadow-glow); outline: none; }
.garden-core__node--1 { left: 5%; top: 18%; animation: node-float 5s ease-in-out infinite; }
.garden-core__node--2 { right: 4%; top: 16%; animation: node-float 6s ease-in-out -1.5s infinite; }
.garden-core__node--3 { right: 3%; bottom: 15%; animation: node-float 5.5s ease-in-out -.8s infinite; }
.garden-core__node--4 { left: 6%; bottom: 11%; animation: node-float 6.5s ease-in-out -2s infinite; }
.garden-core__status { position: relative; z-index: 2; display: grid; grid-template-columns: repeat(3, 1fr); gap: .75rem; border-top: 1px solid rgb(var(--color-border-subtle) / .55); padding: .9rem 1rem 1rem; }
.garden-core__status div { min-width: 0; }
.garden-core__status span, .garden-core__status strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-family: 'JetBrains Mono','Cascadia Code',monospace; }
.garden-core__status span { font-size: .55rem; letter-spacing: .13em; text-transform: uppercase; color: rgb(var(--color-text-muted)); }
.garden-core__status strong { margin-top: .2rem; font-size: .68rem; font-weight: 500; color: rgb(var(--color-brand-primary)); }
@keyframes signal-flow { to { stroke-dashoffset: -88; } }
@keyframes orbit-spin { to { transform: rotate(360deg); } }
@keyframes core-breathe { 50% { opacity: .35; transform: scale(1.08); } }
@keyframes node-float { 50% { transform: translateY(-6px); } }
@media (max-width: 1023px) { .hero-shell { min-height: auto; padding-top: 4.5rem; } .garden-core { max-width: 43rem; min-height: 31rem; margin: 0 auto; } .garden-core__canvas { min-height: 23.5rem; } }
@media (max-width: 639px) { .hero-shell { padding: 3.5rem 0 4.5rem; } .garden-core { min-height: 26rem; } .garden-core__canvas { min-height: 19.5rem; } .garden-core__header { grid-template-columns: 1fr auto; } .garden-core__header > p:first-of-type { text-align: right; } .garden-core__lines { inset: .5rem 0 0; height: calc(100% - .5rem); } .garden-core__orbit { width: 11.5rem; height: 11.5rem; } .garden-core__orbit::after { transform-origin: 0 6rem; } .garden-core__center { width: 5.25rem; height: 5.25rem; } .garden-core__center svg { width: 2.75rem; height: 2.75rem; } .garden-core__node { max-width: 6.7rem; font-size: .52rem; padding: .34rem .5rem; } .garden-core__node--1 { left: 2%; top: 18%; } .garden-core__node--2 { right: 1%; top: 17%; } .garden-core__node--3 { right: 1%; bottom: 12%; } .garden-core__node--4 { left: 2%; bottom: 10%; } .garden-core__status { grid-template-columns: repeat(2, 1fr); } .garden-core__status div:nth-child(3) { display: none; } }
@media (hover: none) { .garden-core__light { display: none; } }
@media (prefers-reduced-motion: reduce) { .garden-core__signal, .garden-core__orbit::after, .garden-core__pulse, .garden-core__node { animation: none; } .hero-cta:hover { transform: none; } .garden-core__light { display: none; } .garden-core__canvas { transform: none; transition: none; } .garden-core__signal { transition: none; } }
</style>
