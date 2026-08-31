<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { fetchNoteDetail } from '@/api/notes';
import ArticleToc from '@/components/article/ArticleToc.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { ArticleHeading } from '@/types/markdown';
import type { NoteItem } from '@/types/note';
import { formatDateTime } from '@/utils/format';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const note = ref<NoteItem | null>(null);
const loading = ref(false);
const errorMessage = ref('');
const headings = ref<ArticleHeading[]>([]);
const activeHeadingId = ref('');
const noteContent = ref<HTMLElement | null>(null);
let requestSequence = 0;
let headingObserver: IntersectionObserver | null = null;
let cleanupSeo = () => {};

const noteId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const backTo = computed(() => {
  const back = window.history.state?.back;
  return typeof back === 'string' && /^\/notes(?:\?|$)/.test(back) ? back : '/notes';
});
const content = computed(() => note.value?.content?.trim() || '');
const tocHeadings = computed(() => headings.value.length > 1 ? headings.value : []);

function plainDescription(value: NoteItem): string {
  const source = value.summary?.trim() || value.content?.trim() || '';
  return source
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~\-|]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 180) || 'YU.LOG 数字花园知识笔记。';
}

function applyNoteSeo(value: NoteItem) {
  cleanupSeo();
  applySeo({
    title: `${value.title} | Notes | YU.LOG`,
    description: plainDescription(value),
    canonicalPath: route.path,
    type: 'website',
  });
}

function setupHeadingObserver() {
  headingObserver?.disconnect();
  if (!headings.value.length || !noteContent.value) return;
  const elements = headings.value
    .map((heading) => document.getElementById(heading.id))
    .filter((element): element is HTMLElement => Boolean(element));
  if (!elements.length) return;

  activeHeadingId.value ||= elements[0].id;
  headingObserver = new IntersectionObserver(() => {
    const passed = elements.filter((element) => element.getBoundingClientRect().top <= 132);
    activeHeadingId.value = (passed[passed.length - 1] || elements[0]).id;
  }, { rootMargin: '-96px 0px -72% 0px', threshold: [0, 1] });
  elements.forEach((element) => headingObserver?.observe(element));
}

async function updateToc(value: ArticleHeading[]) {
  headings.value = value;
  await nextTick();
  setupHeadingObserver();
}

function navigateHeading(id: string) {
  const target = document.getElementById(id);
  if (!target) return;
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  target.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' });
  activeHeadingId.value = id;
  const url = new URL(window.location.href);
  url.hash = id;
  window.history.replaceState(window.history.state, '', url);
}

async function loadNote() {
  const id = noteId.value;
  if (!id) {
    note.value = null;
    errorMessage.value = '这条笔记不存在或已下线。';
    return;
  }

  const requestId = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';
  note.value = null;
  headings.value = [];
  activeHeadingId.value = '';
  headingObserver?.disconnect();
  try {
    const result = await fetchNoteDetail(id);
    if (requestId !== requestSequence) return;
    note.value = result;
    applyNoteSeo(result);
    loading.value = false;
    await nextTick();
    if (route.hash) {
      const target = document.getElementById(decodeURIComponent(route.hash.slice(1)));
      target?.scrollIntoView({ behavior: 'auto', block: 'start' });
    } else {
      window.scrollTo({ top: 0, behavior: 'auto' });
    }
  } catch {
    if (requestId !== requestSequence) return;
    note.value = null;
    errorMessage.value = '这条笔记不存在、已下线，或暂时无法读取。';
    loading.value = false;
  }
}

watch(() => noteId.value, () => { void loadNote(); }, { immediate: true });

onUnmounted(() => {
  requestSequence += 1;
  headingObserver?.disconnect();
  cleanupSeo();
});
</script>

<template>
  <PublicLayout>
    <div class="note-detail-page">
      <div v-if="loading" class="note-detail-loading" aria-label="笔记加载中"><LoadingSkeleton :lines="4" /><div class="mt-10"><LoadingSkeleton :lines="8" /></div></div>

      <section v-else-if="errorMessage" class="note-unavailable">
        <p class="note-kicker">Knowledge record unavailable</p>
        <h1>这条笔记暂时读不到</h1>
        <p>{{ errorMessage }}</p>
        <div class="note-unavailable__actions"><button type="button" @click="loadNote">重新加载</button><RouterLink to="/notes">返回数字花园</RouterLink></div>
      </section>

      <EmptyState v-else-if="!note" title="数字花园正在生长" description="新的知识节点准备好公开后会出现在这里。" />

      <article v-else class="note-record">
        <header class="note-record__header">
          <RouterLink class="note-back" :to="backTo">← Back to the garden</RouterLink>
          <div class="note-record__eyebrow"><span class="note-kicker">NOTE // {{ note.topic || 'KNOWLEDGE' }}</span><span class="note-record__state">Knowledge record</span></div>
          <h1>{{ note.title }}</h1>
          <p v-if="note.summary" class="note-record__summary">{{ note.summary }}</p>
          <div class="note-record__meta">
            <span v-if="note.updatedAt">Updated <strong>{{ formatDateTime(note.updatedAt) }}</strong></span>
            <span v-if="note.createdAt">Created <strong>{{ formatDateTime(note.createdAt) }}</strong></span>
            <span v-if="note.slug">/{{ note.slug }}</span>
          </div>
          <div v-if="note.tags.length" class="note-record__tags"><span v-for="tag in note.tags" :key="tag">#{{ tag }}</span></div>
        </header>

        <ArticleToc class="note-record__toc-mobile" :headings="tocHeadings" :active-id="activeHeadingId" @navigate="navigateHeading" />
        <div class="note-reading-grid">
          <div ref="noteContent" class="note-content">
            <MarkdownRenderer v-if="content" :content="content" @toc="updateToc" />
            <p v-else class="note-empty-body">这条笔记暂时没有正文内容。</p>
            <footer class="note-record__end"><span aria-hidden="true">◆</span><p>Knowledge stays in motion.</p></footer>
          </div>
          <aside class="note-toc-rail"><ArticleToc :headings="tocHeadings" :active-id="activeHeadingId" @navigate="navigateHeading" /></aside>
        </div>
      </article>
    </div>
  </PublicLayout>
</template>

<style scoped>
.note-detail-page { width: min(100%, 76rem); margin: 0 auto; padding-bottom: 6rem; }
.note-detail-loading { width: min(100%, 50rem); margin: 0 auto; padding: clamp(3rem, 8vw, 7rem) 0; }
.note-kicker { font-family: 'JetBrains Mono', monospace; font-size: .64rem; text-transform: uppercase; letter-spacing: .15em; color: rgb(var(--color-brand-primary)); }
.note-unavailable { width: min(100%, 48rem); margin: 0 auto; padding: clamp(4rem, 10vw, 8rem) 0; }
.note-unavailable h1 { margin-top: 1rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.4rem, 7vw, 4.5rem); font-weight: 700; line-height: 1; letter-spacing: -.05em; }
.note-unavailable > p:last-of-type { max-width: 34rem; margin-top: 1.25rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.note-unavailable__actions { display: flex; flex-wrap: wrap; gap: .75rem; margin-top: 1.75rem; }
.note-unavailable__actions button, .note-unavailable__actions a { display: inline-flex; min-height: 2.7rem; align-items: center; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .55rem; padding: 0 .9rem; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-secondary)); }
.note-unavailable__actions button:hover, .note-unavailable__actions a:hover { border-color: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-primary)); }
.note-record__header { width: min(100%, 58rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 clamp(2.5rem, 6vw, 5rem); }
.note-back { display: inline-flex; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); transition: color 180ms; }
.note-back:hover { color: rgb(var(--color-brand-primary)); }
.note-record__eyebrow { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: .8rem; margin-top: clamp(3rem, 7vw, 5.5rem); }
.note-record__state { border: 1px solid rgb(var(--color-accent-secondary) / .35); border-radius: 999px; padding: .35rem .6rem; font-family: 'JetBrains Mono', monospace; font-size: .58rem; color: rgb(var(--color-accent-secondary)); }
.note-record h1 { max-width: 54rem; margin-top: 1.1rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.35rem, 6vw, 5rem); font-weight: 700; line-height: 1.03; letter-spacing: -.06em; }
.note-record__summary { max-width: 44rem; margin-top: 1.4rem; font-size: 1rem; line-height: 1.85; color: rgb(var(--color-text-secondary)); }
.note-record__meta { display: flex; flex-wrap: wrap; gap: .6rem 1.5rem; margin-top: 1.5rem; border-top: 1px solid rgb(var(--color-border-subtle) / .62); border-bottom: 1px solid rgb(var(--color-border-subtle) / .62); padding: .85rem 0; font-family: 'JetBrains Mono', monospace; font-size: .61rem; color: rgb(var(--color-text-muted)); }
.note-record__meta strong { font-weight: 500; color: rgb(var(--color-text-primary)); }
.note-record__tags { display: flex; flex-wrap: wrap; gap: .55rem .9rem; margin-top: 1.1rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.note-reading-grid { display: grid; grid-template-columns: minmax(0, 50rem) 13rem; justify-content: center; gap: clamp(2.5rem, 5vw, 4.5rem); }
.note-content { min-width: 0; }
.note-toc-rail { position: sticky; top: 7rem; align-self: start; }
.note-record__toc-mobile { display: none; }
.note-empty-body { border-top: 1px solid rgb(var(--color-border-subtle) / .68); border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding: 2rem 0; font-size: 1rem; color: rgb(var(--color-text-muted)); }
.note-record__end { display: grid; justify-items: center; gap: .75rem; margin-top: 4rem; border-top: 1px solid rgb(var(--color-border-subtle) / .62); padding-top: 2.5rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; text-transform: uppercase; letter-spacing: .1em; color: rgb(var(--color-text-muted)); }
.note-record__end span { color: rgb(var(--color-brand-primary)); }
@media (max-width: 1023px) { .note-reading-grid { grid-template-columns: minmax(0, 50rem); } .note-toc-rail { display: none; } .note-record__toc-mobile { display: block; margin-bottom: 2rem; } }
@media (max-width: 639px) { .note-detail-page { padding-bottom: 4rem; } .note-record__eyebrow { align-items: flex-start; flex-direction: column; } .note-record h1 { font-size: clamp(2.25rem, 11vw, 4rem); } }
</style>
