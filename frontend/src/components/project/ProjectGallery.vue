<script setup lang="ts">
import { nextTick, onUnmounted, ref, watch } from 'vue';

import type { ProjectGalleryImage } from '@/types/project';

const props = defineProps<{ images: ProjectGalleryImage[] }>();
const activeIndex = ref<number | null>(null);
const closeButton = ref<HTMLButtonElement | null>(null);
let returnTarget: HTMLElement | null = null;

function openImage(index: number, event: MouseEvent) {
  returnTarget = event.currentTarget instanceof HTMLElement ? event.currentTarget : null;
  activeIndex.value = index;
}

function closeImage() {
  activeIndex.value = null;
  void nextTick(() => returnTarget?.focus());
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') closeImage();
  if (event.key === 'ArrowRight' && activeIndex.value !== null) activeIndex.value = (activeIndex.value + 1) % props.images.length;
  if (event.key === 'ArrowLeft' && activeIndex.value !== null) activeIndex.value = (activeIndex.value - 1 + props.images.length) % props.images.length;
}

watch(activeIndex, (value) => {
  if (value !== null) {
    window.addEventListener('keydown', handleKeydown);
    void nextTick(() => closeButton.value?.focus());
  } else {
    window.removeEventListener('keydown', handleKeydown);
  }
});

onUnmounted(() => window.removeEventListener('keydown', handleKeydown));
</script>

<template>
  <section v-if="images.length" class="project-gallery" aria-labelledby="project-gallery-title">
    <div class="project-section-heading"><p class="font-mono text-[0.63rem] uppercase tracking-[0.15em] text-brand">Selected screens</p><h2 id="project-gallery-title">A closer look</h2></div>
    <div class="project-gallery__grid">
      <button v-for="(image, index) in images" :key="image.src" class="project-gallery__item" type="button" @click="openImage(index, $event)">
        <img :src="image.src" :alt="image.alt" loading="lazy" decoding="async" />
        <span v-if="image.caption">{{ image.caption }}</span>
      </button>
    </div>
    <div v-if="activeIndex !== null" class="project-lightbox" role="presentation" @click.self="closeImage">
      <div class="project-lightbox__dialog" role="dialog" aria-modal="true" :aria-label="images[activeIndex]?.alt || '项目截图预览'">
        <button ref="closeButton" class="project-lightbox__close" type="button" aria-label="关闭图片预览" @click="closeImage">×</button>
        <img :src="images[activeIndex]?.src" :alt="images[activeIndex]?.alt" />
        <p v-if="images[activeIndex]?.caption">{{ images[activeIndex]?.caption }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.project-gallery { border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 3.5rem; }
.project-gallery__grid { display: grid; grid-template-columns: repeat(2,minmax(0,1fr)); gap: 1rem; margin-top: 2rem; }
.project-gallery__item { position: relative; overflow: hidden; min-height: 12rem; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .8rem; padding: 0; background: rgb(var(--color-surface-elevated) / .55); text-align: left; cursor: zoom-in; }
.project-gallery__item img { display: block; width: 100%; height: 100%; min-height: 12rem; object-fit: cover; transition: transform 400ms; }
.project-gallery__item:hover img { transform: scale(1.025); }
.project-gallery__item span { position: absolute; right: .8rem; bottom: .7rem; border-radius: .3rem; padding: .25rem .4rem; background: rgb(var(--color-overlay) / .7); font-family: 'JetBrains Mono',monospace; font-size: .58rem; color: rgb(var(--color-text-primary)); }
.project-lightbox { position: fixed; inset: 0; z-index: 80; display: grid; place-items: center; padding: 1.25rem; background: rgb(var(--color-overlay) / .82); backdrop-filter: blur(10px); }
.project-lightbox__dialog { position: relative; max-width: min(92vw,70rem); max-height: 92vh; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: .85rem; padding: .65rem; background: rgb(var(--color-surface-elevated)); box-shadow: var(--shadow-elevated); }
.project-lightbox__dialog img { display: block; max-width: 100%; max-height: calc(92vh - 3rem); object-fit: contain; }
.project-lightbox__dialog p { padding: .6rem .35rem .15rem; font-size: .75rem; color: rgb(var(--color-text-secondary)); }
.project-lightbox__close { position: absolute; right: .5rem; top: .5rem; z-index: 1; width: 2rem; height: 2rem; border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: .4rem; background: rgb(var(--color-overlay) / .65); font-size: 1.3rem; line-height: 1; color: rgb(var(--color-text-primary)); }
@media (max-width: 639px) { .project-gallery__grid { grid-template-columns: 1fr; } .project-gallery__item { min-height: 10rem; } .project-gallery__item img { min-height: 10rem; } }
@media (prefers-reduced-motion: reduce) { .project-gallery__item:hover img { transform: none; } }
</style>
