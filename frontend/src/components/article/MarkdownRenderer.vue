<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';

import type { ArticleHeading } from '@/types/markdown';
import { uiCopy } from '@/config/ui-copy';
import { renderMarkdown } from '@/utils/markdown';

const props = defineProps<{ content: string }>();
const emit = defineEmits<{ toc: [headings: ArticleHeading[]] }>();
const rendered = computed(() => renderMarkdown(props.content));
const feedbackTimers = new Set<number>();
const previewImage = ref<{ src: string; alt: string } | null>(null);
const previewRef = ref<HTMLElement | null>(null);
const previewTrigger = ref<HTMLElement | null>(null);
let previousBodyOverflow = '';

watch(rendered, (value) => {
  emit('toc', value.headings);
  void nextTick(decorateImages);
}, { immediate: true });

function decorateImages() {
  document.querySelectorAll<HTMLImageElement>('[data-article-prose] img').forEach((image) => {
    image.dataset.previewImage = 'true';
    image.tabIndex = 0;
    image.setAttribute('role', 'button');
    image.setAttribute('aria-label', image.alt ? `预览图片：${image.alt}` : '预览图片');
  });
}

function openPreview(image: HTMLImageElement) {
  previewTrigger.value = image;
  previewImage.value = { src: image.currentSrc || image.src, alt: image.alt };
  previousBodyOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  void nextTick(() => previewRef.value?.focus());
}

function closePreview() {
  previewImage.value = null;
  document.body.style.overflow = previousBodyOverflow;
  previousBodyOverflow = '';
  void nextTick(() => previewTrigger.value?.focus());
  previewTrigger.value = null;
}

async function copyText(value: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(value);
    return true;
  } catch {
    const textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    const copied = document.execCommand('copy');
    textarea.remove();
    return copied;
  }
}

function setTemporaryLabel(button: HTMLButtonElement, label: string, resetLabel: string) {
  button.textContent = label;
  const timer = window.setTimeout(() => {
    button.textContent = resetLabel;
    feedbackTimers.delete(timer);
  }, 1600);
  feedbackTimers.add(timer);
}

async function handleContentClick(event: MouseEvent) {
  const target = event.target as HTMLElement;
  const image = target.closest<HTMLImageElement>('img[data-preview-image]');
  if (image) {
    openPreview(image);
    return;
  }
  const copyButton = target.closest<HTMLButtonElement>('[data-copy-code]');
  if (copyButton) {
    const code = copyButton.closest('[data-code-block]')?.querySelector('code')?.textContent || '';
    const copied = await copyText(code);
    setTemporaryLabel(copyButton, copied ? uiCopy.copied : uiCopy.copyFailed, uiCopy.copy);
    return;
  }

  const anchorButton = target.closest<HTMLButtonElement>('[data-heading-anchor]');
  if (anchorButton) {
    const id = anchorButton.dataset.headingAnchor;
    if (!id) return;
    const url = new URL(window.location.href);
    url.hash = id;
    window.history.replaceState(window.history.state, '', url);
    const copied = await copyText(url.toString());
    anchorButton.dataset.feedback = copied ? 'copied' : 'failed';
    setTemporaryLabel(anchorButton, copied ? '✓' : '!', '#');
  }
}

function handleContentKeydown(event: KeyboardEvent) {
  const target = event.target as HTMLElement;
  const image = target.closest<HTMLImageElement>('img[data-preview-image]');
  if (!image || !['Enter', ' '].includes(event.key)) return;
  event.preventDefault();
  openPreview(image);
}

function handleImageError(event: Event) {
  const image = event.target;
  if (!(image instanceof HTMLImageElement) || image.dataset.failed === 'true') return;
  image.dataset.failed = 'true';
  image.hidden = true;
  const fallback = document.createElement('span');
  fallback.className = 'article-image-fallback';
  fallback.textContent = image.alt ? `图片暂不可用：${image.alt}` : '图片暂不可用';
  image.insertAdjacentElement('afterend', fallback);
}

onUnmounted(() => feedbackTimers.forEach((timer) => window.clearTimeout(timer)));
onMounted(() => void nextTick(decorateImages));
onUnmounted(() => {
  if (previewImage.value) document.body.style.overflow = previousBodyOverflow;
});
</script>

<template>
  <div
    class="article-prose"
    data-article-prose
    @click="handleContentClick"
    @keydown="handleContentKeydown"
    @error.capture="handleImageError"
    v-html="rendered.html"
  ></div>
  <Teleport to="body">
    <Transition name="image-preview">
      <div v-if="previewImage" ref="previewRef" class="article-image-preview" role="dialog" aria-modal="true" aria-label="图片预览" tabindex="-1" @click.self="closePreview" @keydown.esc="closePreview">
        <button type="button" class="article-image-preview__close" aria-label="关闭图片预览" @click="closePreview">ESC</button>
        <figure class="article-image-preview__figure">
          <img :src="previewImage.src" :alt="previewImage.alt" />
          <figcaption v-if="previewImage.alt">{{ previewImage.alt }}</figcaption>
        </figure>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.article-prose { color: rgb(var(--color-text-primary)); font-size: 1.0625rem; line-height: 1.95; overflow-wrap: anywhere; }
.article-prose :deep(p) { margin: 1.35em 0; color: rgb(var(--color-text-secondary)); }
.article-prose :deep(.article-heading) { position: relative; scroll-margin-top: 6.5rem; color: rgb(var(--color-text-primary)); }
.article-prose :deep(h2) { margin: 2.5em 0 .8em; font-size: clamp(1.65rem,4vw,2.15rem); line-height: 1.3; letter-spacing: -.025em; }
.article-prose :deep(h3) { margin: 2.2em 0 .72em; font-size: clamp(1.35rem,3vw,1.65rem); line-height: 1.38; letter-spacing: -.018em; }
.article-prose :deep(h4) { margin: 2em 0 .65em; font-size: 1.2rem; line-height: 1.45; }
.article-prose :deep(h5), .article-prose :deep(h6) { margin: 1.8em 0 .6em; font-size: 1.05rem; line-height: 1.5; }
.article-prose :deep(.article-source-title) { padding-bottom: .65rem; border-bottom: 1px solid rgb(var(--color-border-subtle) / .62); }
.article-prose :deep(.article-heading-anchor) { position: absolute; left: -1.35em; top: .12em; width: 1.2em; border: 0; padding: 0; background: transparent; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .72em; color: rgb(var(--color-text-muted) / .45); cursor: pointer; opacity: 0; transition: color 180ms, opacity 180ms; }
.article-prose :deep(.article-heading:hover .article-heading-anchor), .article-prose :deep(.article-heading-anchor:focus-visible) { color: rgb(var(--color-brand-primary)); opacity: 1; }
.article-prose :deep(strong) { color: rgb(var(--color-text-primary)); font-weight: 650; }
.article-prose :deep(em) { color: rgb(var(--color-text-primary)); }
.article-prose :deep(del) { color: rgb(var(--color-text-muted)); text-decoration-color: rgb(var(--color-text-muted) / .65); }
.article-prose :deep(a) { color: rgb(var(--color-brand-primary)); text-decoration: underline; text-decoration-color: rgb(var(--color-brand-primary) / .35); text-decoration-thickness: 1px; text-underline-offset: .2em; transition: color 180ms, text-decoration-color 180ms; }
.article-prose :deep(a:hover) { color: rgb(var(--color-brand-strong)); text-decoration-color: currentColor; }
.article-prose :deep(a[target='_blank']::after) { content: ' ↗'; font-family: 'JetBrains Mono',monospace; font-size: .72em; }
.article-prose :deep(code:not(.hljs)) { border: 1px solid rgb(var(--color-border-subtle) / .65); border-radius: .35rem; padding: .12em .38em; background: rgb(var(--color-surface-hover) / .74); font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .88em; color: rgb(var(--color-accent-secondary)); }
.article-prose :deep(ul), .article-prose :deep(ol) { margin: 1.25em 0; padding-left: 1.55em; color: rgb(var(--color-text-secondary)); }
.article-prose :deep(ul) { list-style: disc; }
.article-prose :deep(ol) { list-style: decimal; }
.article-prose :deep(li) { margin: .48em 0; padding-left: .25em; }
.article-prose :deep(li::marker) { color: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono',monospace; }
.article-prose :deep(.task-list-item) { list-style: none; margin-left: -1.4em; }
.article-prose :deep(.task-list-checkbox) { width: 1rem; height: 1rem; margin: 0 .55rem 0 0; accent-color: rgb(var(--color-brand-primary)); vertical-align: -.12rem; }
.article-prose :deep(blockquote) { margin: 1.8em 0; border-left: 3px solid rgb(var(--color-brand-primary) / .72); border-radius: 0 .65rem .65rem 0; padding: .85rem 1.15rem; background: rgb(var(--color-surface-elevated) / .58); color: rgb(var(--color-text-secondary)); }
.article-prose :deep(blockquote p) { margin: .35em 0; }
.article-prose :deep(hr) { height: 1px; margin: 3rem 0; border: 0; background: linear-gradient(90deg, transparent, rgb(var(--color-border-subtle)), transparent); }
.article-prose :deep(img) { display: block; max-width: 100%; height: auto; max-height: 42rem; margin: 2rem auto; border: 1px solid rgb(var(--color-border-subtle) / .62); border-radius: .9rem; object-fit: contain; box-shadow: var(--shadow-soft); }
.article-prose :deep(img[data-preview-image]) { cursor: zoom-in; transition: border-color var(--motion-fast) var(--ease-standard), box-shadow var(--motion-normal) var(--ease-standard), transform var(--motion-normal) var(--ease-emphasized); }
.article-prose :deep(img[data-preview-image]:hover), .article-prose :deep(img[data-preview-image]:focus-visible) { border-color: rgb(var(--color-border-active) / .72); box-shadow: var(--shadow-glow); outline: none; transform: translateY(-2px); }
.article-image-preview { position: fixed; z-index: 80; inset: 0; display: grid; place-items: center; padding: clamp(1rem, 4vw, 4rem); background: rgb(var(--color-bg-primary) / .92); backdrop-filter: blur(16px); }
.article-image-preview__close { position: absolute; top: 1rem; right: 1rem; min-height: 2.4rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: 0 .7rem; background: rgb(var(--color-surface-elevated) / .72); font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-secondary)); cursor: pointer; transition: border-color var(--motion-fast) var(--ease-standard), color var(--motion-fast) var(--ease-standard); }
.article-image-preview__close:hover, .article-image-preview__close:focus-visible { border-color: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-primary)); outline: none; }
.article-image-preview__figure { display: grid; max-width: min(92vw, 78rem); max-height: 90vh; justify-items: center; gap: .85rem; }
.article-image-preview__figure img { max-width: 100%; max-height: 78vh; border: 1px solid rgb(var(--color-border-subtle) / .78); border-radius: .85rem; object-fit: contain; box-shadow: var(--shadow-elevated); }
.article-image-preview__figure figcaption { max-width: 60rem; font-size: .78rem; line-height: 1.6; text-align: center; color: rgb(var(--color-text-secondary)); }
.image-preview-enter-active, .image-preview-leave-active { transition: opacity var(--motion-normal) var(--ease-standard); }
.image-preview-enter-from, .image-preview-leave-to { opacity: 0; }
.article-prose :deep(.article-image-fallback) { display: grid; min-height: 8rem; margin: 1.75rem 0; place-items: center; border: 1px dashed rgb(var(--color-border-subtle)); border-radius: .9rem; padding: 1rem; background: rgb(var(--color-bg-secondary) / .5); font-family: 'JetBrains Mono',monospace; font-size: .72rem; color: rgb(var(--color-text-muted)); }
.article-prose :deep(.article-table-scroll) { max-width: 100%; margin: 1.8rem 0; overflow-x: auto; border: 1px solid rgb(var(--color-border-subtle) / .68); border-radius: .75rem; background: rgb(var(--color-surface-elevated) / .52); }
.article-prose :deep(table) { width: 100%; min-width: 36rem; border-collapse: collapse; font-size: .9rem; line-height: 1.6; }
.article-prose :deep(th), .article-prose :deep(td) { border-bottom: 1px solid rgb(var(--color-border-subtle) / .65); padding: .78rem .9rem; text-align: left; vertical-align: top; }
.article-prose :deep(th) { background: rgb(var(--color-surface-hover) / .72); font-family: 'JetBrains Mono',monospace; font-size: .68rem; letter-spacing: .04em; color: rgb(var(--color-text-primary)); }
.article-prose :deep(tr:last-child td) { border-bottom: 0; }
.article-prose :deep(.article-code-frame) { margin: 2rem 0; overflow: hidden; border: 1px solid rgb(var(--color-border-subtle) / .72); border-radius: .85rem; background: rgb(var(--color-bg-secondary) / .94); box-shadow: var(--shadow-soft); }
.article-prose :deep(.article-code-toolbar) { display: flex; min-height: 2.65rem; align-items: center; justify-content: space-between; gap: 1rem; border-bottom: 1px solid rgb(var(--color-border-subtle) / .58); padding: 0 .9rem; background: rgb(var(--color-surface-elevated) / .7); }
.article-prose :deep(.article-code-language) { font-family: 'JetBrains Mono',monospace; font-size: .62rem; letter-spacing: .12em; text-transform: uppercase; color: rgb(var(--color-text-muted)); }
.article-prose :deep(.article-code-copy) { min-height: 1.85rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .35rem; padding: 0 .6rem; background: rgb(var(--color-bg-primary) / .65); font-family: 'JetBrains Mono',monospace; font-size: .62rem; color: rgb(var(--color-text-secondary)); cursor: pointer; transition: border-color 180ms, color 180ms, background-color 180ms; }
.article-prose :deep(.article-code-copy:hover) { border-color: rgb(var(--color-border-active) / .62); color: rgb(var(--color-brand-primary)); }
.article-prose :deep(.article-code-block) { max-width: 100%; margin: 0; overflow-x: auto; padding: 1.15rem 1.25rem 1.35rem; background: transparent; }
.article-prose :deep(.article-code-block code) { display: block; width: max-content; min-width: 100%; font-family: 'JetBrains Mono','Cascadia Code',monospace; font-size: .82rem; line-height: 1.75; tab-size: 2; color: rgb(var(--color-text-primary)); }
.article-prose :deep(.hljs-comment), .article-prose :deep(.hljs-quote) { color: rgb(var(--color-text-muted)); font-style: italic; }
.article-prose :deep(.hljs-keyword), .article-prose :deep(.hljs-selector-tag), .article-prose :deep(.hljs-literal), .article-prose :deep(.hljs-doctag) { color: rgb(var(--color-accent-secondary)); }
.article-prose :deep(.hljs-string), .article-prose :deep(.hljs-regexp), .article-prose :deep(.hljs-attribute) { color: rgb(var(--color-success)); }
.article-prose :deep(.hljs-number), .article-prose :deep(.hljs-symbol), .article-prose :deep(.hljs-bullet), .article-prose :deep(.hljs-variable) { color: rgb(var(--color-warning)); }
.article-prose :deep(.hljs-title), .article-prose :deep(.hljs-section), .article-prose :deep(.hljs-function), .article-prose :deep(.hljs-selector-id), .article-prose :deep(.hljs-selector-class) { color: rgb(var(--color-brand-primary)); }
.article-prose :deep(.hljs-type), .article-prose :deep(.hljs-built_in), .article-prose :deep(.hljs-class) { color: rgb(var(--color-info)); }
.article-prose :deep(.hljs-meta), .article-prose :deep(.hljs-tag), .article-prose :deep(.hljs-name) { color: rgb(var(--color-danger)); }
.article-prose :deep(.hljs-params), .article-prose :deep(.hljs-property) { color: rgb(var(--color-text-secondary)); }
@media (max-width: 639px) { .article-prose { font-size: 1rem; line-height: 1.9; } .article-prose :deep(p) { margin: 1.2em 0; } .article-prose :deep(.article-heading-anchor) { position: static; display: inline-flex; width: 1.4rem; min-height: 1.4rem; align-items: center; justify-content: center; margin-left: .25rem; opacity: 0; vertical-align: middle; } .article-prose :deep(.article-heading-anchor:focus-visible) { opacity: 1; } .article-prose :deep(.article-code-frame) { margin-left: -.25rem; margin-right: -.25rem; } .article-prose :deep(.article-code-block) { padding: 1rem; } .article-prose :deep(.article-code-block code) { font-size: .76rem; } }
@media (prefers-reduced-motion: reduce) { .article-prose :deep(*) { scroll-behavior: auto; } .article-prose :deep(img[data-preview-image]) { transition: none; } .article-prose :deep(img[data-preview-image]:hover), .article-prose :deep(img[data-preview-image]:focus-visible) { transform: none; } .image-preview-enter-active, .image-preview-leave-active { transition: none; } }
</style>
