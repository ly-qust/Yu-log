<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue';

const props = withDefaults(defineProps<{ target?: HTMLElement | null }>(), { target: null });
const progress = ref(0);
let frameId = 0;

function updateProgress() {
  frameId = 0;
  if (!props.target) {
    progress.value = 0;
    return;
  }
  const rect = props.target.getBoundingClientRect();
  const start = window.scrollY + rect.top - 96;
  const end = start + props.target.offsetHeight - Math.min(window.innerHeight * 0.45, 360);
  const range = Math.max(end - start, 1);
  progress.value = Math.min(100, Math.max(0, ((window.scrollY - start) / range) * 100));
}

function scheduleUpdate() {
  if (!frameId) frameId = window.requestAnimationFrame(updateProgress);
}

watch(() => props.target, () => {
  scheduleUpdate();
}, { immediate: true });

window.addEventListener('scroll', scheduleUpdate, { passive: true });
window.addEventListener('resize', scheduleUpdate, { passive: true });

onUnmounted(() => {
  window.removeEventListener('scroll', scheduleUpdate);
  window.removeEventListener('resize', scheduleUpdate);
  if (frameId) window.cancelAnimationFrame(frameId);
});
</script>

<template>
  <div class="reading-progress" role="progressbar" aria-label="文章阅读进度" aria-valuemin="0" aria-valuemax="100" :aria-valuenow="Math.round(progress)">
    <span :style="{ transform: `scaleX(${progress / 100})` }"></span>
  </div>
</template>

<style scoped>
.reading-progress { position: fixed; left: 0; right: 0; top: 4rem; z-index: 48; height: 2px; pointer-events: none; }
.reading-progress span { display: block; width: 100%; height: 100%; background: linear-gradient(90deg, rgb(var(--color-brand-primary)), rgb(var(--color-accent-secondary))); box-shadow: 0 0 9px rgb(var(--color-brand-primary) / .35); transform-origin: left center; transition: transform 80ms linear; }
@media (prefers-reduced-motion: reduce) { .reading-progress span { transition: none; } }
</style>
