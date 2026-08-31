<script setup lang="ts">
import { computed } from 'vue';

import type { AboutProfile } from '@/types/site';
import { safeExternalUrl } from '@/utils/links';

const props = withDefaults(defineProps<{
  profile: AboutProfile;
  careerDirection?: string[];
  githubUrl?: string;
  email?: string;
}>(), {
  careerDirection: () => [],
  githubUrl: '',
  email: '',
});

const initials = computed(() => String(props.profile.nickname || 'Yu').trim().slice(0, 2).toUpperCase() || 'YU');
const github = computed(() => safeExternalUrl(props.githubUrl || props.profile.githubUrl));
</script>

<template>
  <section class="about-identity" aria-labelledby="about-identity-title">
    <div class="about-identity__topline">
      <span>PROFILE NODE</span>
      <span class="about-identity__pulse" aria-hidden="true"></span>
    </div>
    <div class="about-identity__monogram" aria-hidden="true">{{ initials }}</div>
    <p class="about-kicker">Current identity</p>
    <h2 id="about-identity-title">{{ profile.nickname || 'Yu' }}</h2>
    <p class="about-identity__role">{{ profile.role || '计算机科学与技术学习者' }}</p>
    <p v-if="profile.location" class="about-identity__location">{{ profile.location }}</p>

    <div v-if="careerDirection.length" class="about-identity__focus">
      <p class="about-kicker">Current focus</p>
      <ul>
        <li v-for="direction in careerDirection" :key="direction">{{ direction }}</li>
      </ul>
    </div>

    <div v-if="github || email" class="about-identity__contact">
      <p class="about-kicker">Open channels</p>
      <a v-if="github" :href="github" target="_blank" rel="noreferrer">GitHub <span aria-hidden="true">↗</span></a>
      <a v-if="email" :href="`mailto:${email}`">{{ email }}</a>
    </div>
  </section>
</template>

<style scoped>
.about-identity { position: relative; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: 1rem; padding: clamp(1.25rem, 4vw, 2rem); background: rgb(var(--color-surface-elevated) / .66); box-shadow: var(--shadow-soft); }
.about-identity::after { position: absolute; right: -4rem; top: 5rem; width: 12rem; height: 12rem; border: 1px solid rgb(var(--color-brand-primary) / .15); border-radius: 50%; content: ''; }
.about-identity__topline { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; font-family: 'JetBrains Mono', monospace; font-size: .58rem; letter-spacing: .14em; color: rgb(var(--color-text-muted)); }
.about-identity__pulse { width: .45rem; height: .45rem; border-radius: 50%; background: rgb(var(--color-success)); box-shadow: 0 0 12px rgb(var(--color-success) / .65); }
.about-identity__monogram { position: relative; z-index: 1; display: grid; width: clamp(5rem, 16vw, 8rem); height: clamp(5rem, 16vw, 8rem); margin: clamp(2rem, 6vw, 4rem) 0 2rem; place-items: center; border: 1px solid rgb(var(--color-brand-primary) / .55); border-radius: 1.15rem; background: radial-gradient(circle at 68% 25%, rgb(var(--color-accent-secondary) / .2), transparent 40%), rgb(var(--color-bg-secondary) / .75); font-family: 'Space Grotesk', sans-serif; font-size: clamp(2rem, 7vw, 3.8rem); font-weight: 700; letter-spacing: -.08em; color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 8px rgb(var(--color-brand-primary) / .045), var(--shadow-glow); }
.about-kicker { font-family: 'JetBrains Mono', monospace; font-size: .6rem; text-transform: uppercase; letter-spacing: .14em; color: rgb(var(--color-brand-primary)); }
.about-identity h2 { position: relative; z-index: 1; margin-top: .6rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.8rem, 4vw, 2.7rem); font-weight: 650; letter-spacing: -.04em; }
.about-identity__role { position: relative; z-index: 1; margin-top: .4rem; font-size: .9rem; color: rgb(var(--color-text-secondary)); }
.about-identity__location { position: relative; z-index: 1; margin-top: .35rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.about-identity__focus, .about-identity__contact { position: relative; z-index: 1; margin-top: 2rem; border-top: 1px solid rgb(var(--color-border-subtle) / .6); padding-top: 1.2rem; }
.about-identity__focus ul { display: grid; gap: .6rem; margin-top: .75rem; }
.about-identity__focus li { display: flex; gap: .55rem; font-size: .82rem; color: rgb(var(--color-text-primary)); }
.about-identity__focus li::before { content: '↳'; color: rgb(var(--color-brand-primary)); }
.about-identity__contact { display: flex; flex-wrap: wrap; gap: .7rem 1rem; }
.about-identity__contact .about-kicker { width: 100%; }
.about-identity__contact a { font-family: 'JetBrains Mono', monospace; font-size: .64rem; color: rgb(var(--color-text-secondary)); transition: color 180ms; }
.about-identity__contact a:hover { color: rgb(var(--color-brand-primary)); }
</style>
