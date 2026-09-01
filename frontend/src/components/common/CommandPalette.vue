<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { fetchArticles } from '@/api/articles';
import { fetchNotes } from '@/api/notes';
import { fetchProjects } from '@/api/projects';
import { useThemeStore, type ThemePreference } from '@/stores/theme';
import type { ArticleListItem } from '@/types/content';
import type { NoteItem } from '@/types/note';
import type { ProjectItem } from '@/types/project';

type PaletteItem = {
  id: string;
  group: string;
  label: string;
  description: string;
  to?: string;
  theme?: ThemePreference;
};

const router = useRouter();
const themeStore = useThemeStore();
const isOpen = ref(false);
const query = ref('');
const loading = ref(false);
const selectedIndex = ref(0);
const inputRef = ref<HTMLInputElement | null>(null);
const dialogRef = ref<HTMLElement | null>(null);
const articles = ref<ArticleListItem[]>([]);
const projects = ref<ProjectItem[]>([]);
const notes = ref<NoteItem[]>([]);
let searchTimer = 0;
let searchSequence = 0;
let previousBodyOverflow = '';

const navigationItems: PaletteItem[] = [
  { id: 'nav-home', group: 'Navigate', label: 'Home', description: 'Open the garden entrance', to: '/' },
  { id: 'nav-articles', group: 'Navigate', label: 'Articles', description: 'Browse engineering writing', to: '/articles' },
  { id: 'nav-projects', group: 'Navigate', label: 'Projects', description: 'Explore systems and case studies', to: '/projects' },
  { id: 'nav-notes', group: 'Navigate', label: 'Notes', description: 'Enter the digital garden', to: '/notes' },
  { id: 'nav-timeline', group: 'Navigate', label: 'Timeline', description: 'Follow the growth archive', to: '/timeline' },
  { id: 'nav-about', group: 'Navigate', label: 'About', description: 'Read the personal engineering profile', to: '/about' },
  { id: 'nav-messages', group: 'Navigate', label: 'Messages', description: 'Leave a note for Yu', to: '/messages' },
];

const themeItems: PaletteItem[] = [
  { id: 'theme-dark', group: 'Theme', label: 'Dark mode', description: 'Deep garden signal', theme: 'dark' },
  { id: 'theme-light', group: 'Theme', label: 'Light mode', description: 'Clear daylight surface', theme: 'light' },
  { id: 'theme-system', group: 'Theme', label: 'System mode', description: 'Follow your device preference', theme: 'system' },
];

const staticItems = computed(() => {
  const term = query.value.trim().toLowerCase();
  if (!term) return [...navigationItems, ...themeItems];
  return [...navigationItems, ...themeItems].filter((item) => `${item.label} ${item.description}`.toLowerCase().includes(term));
});

const resultItems = computed<PaletteItem[]>(() => {
  const term = query.value.trim().toLowerCase();
  if (!term) return [];
  return [
    ...articles.value.map((article) => ({
      id: `article-${article.id}`,
      group: 'Articles',
      label: article.title,
      description: article.summary || article.tags.map((tag) => tag.name).join(' · ') || 'Open article',
      to: `/articles/${article.id}`,
    })),
    ...notes.value.map((note) => ({
      id: `note-${note.id}`,
      group: 'Notes',
      label: note.title,
      description: note.summary || note.topic || 'Open garden note',
      to: `/notes/${note.id}`,
    })),
    ...projects.value.map((project) => ({
      id: `project-${project.id}`,
      group: 'Projects',
      label: project.name,
      description: project.description || project.techStack.join(' · ') || 'Open project case study',
      to: `/projects/${project.id}`,
    })),
  ].filter((item) => `${item.label} ${item.description}`.toLowerCase().includes(term));
});

const items = computed(() => [...staticItems.value, ...resultItems.value]);
const groupedItems = computed(() => {
  const groups = new Map<string, PaletteItem[]>();
  items.value.forEach((item) => groups.set(item.group, [...(groups.get(item.group) || []), item]));
  return [...groups.entries()].map(([label, groupItems]) => ({ label, items: groupItems }));
});

function isTextInput(target: EventTarget | null) {
  return target instanceof HTMLElement && (target.matches('input, textarea, select') || target.isContentEditable);
}

function getFocusableElements() {
  if (!dialogRef.value) return [];
  return Array.from(dialogRef.value.querySelectorAll<HTMLElement>('input, button:not([disabled])'));
}

function open() {
  isOpen.value = true;
  query.value = '';
  selectedIndex.value = 0;
  previousBodyOverflow = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  void nextTick(() => inputRef.value?.focus());
}

function close() {
  isOpen.value = false;
  document.body.style.overflow = previousBodyOverflow;
  previousBodyOverflow = '';
}

function moveSelection(delta: number) {
  if (!items.value.length) return;
  selectedIndex.value = (selectedIndex.value + delta + items.value.length) % items.value.length;
  void nextTick(() => document.getElementById(`palette-item-${items.value[selectedIndex.value]?.id}`)?.scrollIntoView({ block: 'nearest' }));
}

async function activate(item: PaletteItem) {
  if (item.theme) {
    await themeStore.setPreference(item.theme);
    close();
    return;
  }
  if (item.to) {
    close();
    await router.push(item.to);
  }
}

async function searchContent(term: string) {
  const requestId = ++searchSequence;
  loading.value = true;
  const [articleResult, projectResult, noteResult] = await Promise.allSettled([
    fetchArticles({ keyword: term, page: 1, size: 6 }),
    fetchProjects({ keyword: term, page: 1, size: 6 }),
    fetchNotes({ keyword: term, page: 1, size: 6 }),
  ]);
  if (requestId !== searchSequence) return;
  articles.value = articleResult.status === 'fulfilled' ? articleResult.value.list : [];
  projects.value = projectResult.status === 'fulfilled' ? projectResult.value.list : [];
  notes.value = noteResult.status === 'fulfilled' ? noteResult.value.list : [];
  loading.value = false;
  selectedIndex.value = 0;
}

function handleKeydown(event: KeyboardEvent) {
  const target = event.target;
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
    event.preventDefault();
    if (isOpen.value) close(); else open();
    return;
  }
  if (event.key === '/' && !isOpen.value && !isTextInput(target)) {
    event.preventDefault();
    open();
    return;
  }
  if (!isOpen.value) return;

  if (event.key === 'Escape') {
    event.preventDefault();
    close();
  } else if (event.key === 'ArrowDown') {
    event.preventDefault();
    moveSelection(1);
  } else if (event.key === 'ArrowUp') {
    event.preventDefault();
    moveSelection(-1);
  } else if (event.key === 'Enter' && items.value[selectedIndex.value]) {
    event.preventDefault();
    void activate(items.value[selectedIndex.value]);
  } else if (event.key === 'Tab') {
    const focusable = getFocusableElements();
    if (!focusable.length) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }
}

watch(query, (value) => {
  window.clearTimeout(searchTimer);
  const term = value.trim();
  if (!term) {
    searchSequence += 1;
    articles.value = [];
    projects.value = [];
    notes.value = [];
    loading.value = false;
    selectedIndex.value = 0;
    return;
  }
  searchTimer = window.setTimeout(() => { void searchContent(term); }, 150);
});

onMounted(() => window.addEventListener('keydown', handleKeydown));
onUnmounted(() => {
  window.removeEventListener('keydown', handleKeydown);
  window.clearTimeout(searchTimer);
  document.body.style.overflow = previousBodyOverflow;
});

defineExpose({ open, close });
</script>

<template>
  <Teleport to="body">
    <Transition name="palette">
      <div v-if="isOpen" class="command-palette" @mousedown.self="close">
        <section ref="dialogRef" aria-labelledby="command-palette-title" aria-modal="true" class="command-palette__dialog" role="dialog">
          <div class="command-palette__topline">
            <p id="command-palette-title">Search the garden</p>
            <button aria-label="关闭搜索面板" class="command-palette__close" type="button" @click="close">ESC</button>
          </div>

          <div class="command-palette__input-wrap">
            <svg aria-hidden="true" class="command-palette__icon" fill="none" viewBox="0 0 24 24" stroke="currentColor"><circle cx="10.8" cy="10.8" r="6.8" stroke-width="1.7" /><path d="m16 16 5 5" stroke-linecap="round" stroke-width="1.7" /></svg>
            <input ref="inputRef" v-model="query" aria-controls="command-palette-results" aria-label="搜索数字花园" :aria-activedescendant="items[selectedIndex] ? `palette-item-${items[selectedIndex].id}` : undefined" autocomplete="off" placeholder="Search articles, notes, projects…" spellcheck="false" type="search" />
            <kbd>⌘ K</kbd>
          </div>

          <p class="command-palette__hint"><span>↑↓ navigate</span><span>↵ open</span><span>ESC close</span><span class="hidden sm:inline">/ quick search</span></p>

          <div v-if="loading" class="command-palette__status" aria-live="polite"><span class="command-palette__pulse"></span> Searching the garden…</div>
          <div v-else-if="query.trim() && !items.length" class="command-palette__status" aria-live="polite">No signal found for “{{ query.trim() }}”.</div>
          <div v-else id="command-palette-results" class="command-palette__results" role="listbox" aria-label="搜索结果">
            <div v-for="group in groupedItems" :key="group.label" class="command-palette__group">
              <p class="command-palette__group-label">{{ group.label }}</p>
              <button v-for="item in group.items" :id="`palette-item-${item.id}`" :key="item.id" :aria-selected="items[selectedIndex]?.id === item.id" class="command-palette__item" role="option" type="button" @click="activate(item)" @mouseenter="selectedIndex = items.findIndex((candidate) => candidate.id === item.id)">
                <span class="command-palette__item-mark" aria-hidden="true">{{ item.theme ? '◌' : item.group === 'Navigate' ? '↗' : '•' }}</span>
                <span class="command-palette__item-copy"><strong>{{ item.label }}</strong><small>{{ item.description }}</small></span>
                <span v-if="item.theme && themeStore.preference === item.theme" class="command-palette__item-state">ACTIVE</span>
                <span v-else-if="item.to" class="command-palette__item-arrow" aria-hidden="true">↗</span>
              </button>
            </div>
          </div>

          <footer class="command-palette__footer"><span>YU.LOG / command palette</span><span>live query · real content</span></footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.command-palette { position: fixed; inset: 0; z-index: 100; display: grid; place-items: start center; padding: clamp(4.5rem, 13vh, 9rem) 1rem 2rem; background: rgb(var(--color-overlay) / .58); backdrop-filter: blur(10px); }
.command-palette__dialog { width: min(100%, 42rem); overflow: hidden; border: 1px solid rgb(var(--color-border-active) / .38); border-radius: 1.1rem; background: rgb(var(--color-surface-elevated) / .96); box-shadow: var(--shadow-elevated), 0 0 0 1px rgb(var(--color-brand-primary) / .06); }
.command-palette__topline, .command-palette__hint, .command-palette__footer { display: flex; align-items: center; justify-content: space-between; gap: 1rem; }
.command-palette__topline { padding: 1rem 1.1rem .75rem; }
.command-palette__topline p, .command-palette__group-label, .command-palette__footer { font-family: 'JetBrains Mono', monospace; font-size: .6rem; letter-spacing: .13em; text-transform: uppercase; color: rgb(var(--color-brand-primary)); }
.command-palette__close { border: 1px solid rgb(var(--color-border-subtle) / .8); border-radius: .35rem; padding: .25rem .4rem; font-family: 'JetBrains Mono', monospace; font-size: .56rem; color: rgb(var(--color-text-muted)); transition: border-color var(--motion-fast) var(--ease-standard), color var(--motion-fast) var(--ease-standard); }
.command-palette__close:hover { border-color: rgb(var(--color-border-active) / .7); color: rgb(var(--color-text-primary)); }
.command-palette__input-wrap { display: flex; align-items: center; gap: .75rem; margin: 0 .75rem; border: 1px solid rgb(var(--color-border-subtle) / .86); border-radius: .75rem; padding: 0 .85rem; background: rgb(var(--color-bg-primary) / .68); transition: border-color var(--motion-fast) var(--ease-standard), box-shadow var(--motion-fast) var(--ease-standard); }
.command-palette__input-wrap:focus-within { border-color: rgb(var(--color-brand-primary) / .8); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .12); }
.command-palette__icon { width: 1.1rem; flex: none; color: rgb(var(--color-brand-primary)); }
.command-palette__input-wrap input { min-width: 0; min-height: 3.25rem; flex: 1; border: 0; outline: 0; background: transparent; font-family: 'JetBrains Mono', monospace; font-size: .8rem; color: rgb(var(--color-text-primary)); }
.command-palette__input-wrap input::placeholder { color: rgb(var(--color-text-muted)); }
.command-palette__input-wrap kbd { border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: .3rem; padding: .25rem .35rem; font-family: 'JetBrains Mono', monospace; font-size: .55rem; color: rgb(var(--color-text-muted)); }
.command-palette__hint { justify-content: flex-start; flex-wrap: wrap; padding: .7rem 1.1rem .85rem; font-family: 'JetBrains Mono', monospace; font-size: .56rem; color: rgb(var(--color-text-muted)); }
.command-palette__results { max-height: min(52vh, 26rem); overflow-y: auto; border-top: 1px solid rgb(var(--color-border-subtle) / .55); padding: .65rem; }
.command-palette__group + .command-palette__group { margin-top: .75rem; }
.command-palette__group-label { padding: .25rem .5rem .4rem; color: rgb(var(--color-text-muted)); }
.command-palette__item { display: flex; width: 100%; align-items: center; gap: .7rem; border: 1px solid transparent; border-radius: .55rem; padding: .65rem .55rem; text-align: left; transition: border-color var(--motion-fast) var(--ease-standard), background-color var(--motion-fast) var(--ease-standard), transform var(--motion-fast) var(--ease-standard); }
.command-palette__item[aria-selected='true'], .command-palette__item:hover { border-color: rgb(var(--color-brand-primary) / .25); background: rgb(var(--color-brand-primary) / .08); transform: translateX(2px); }
.command-palette__item-mark { display: grid; width: 1.35rem; height: 1.35rem; flex: none; place-items: center; border: 1px solid rgb(var(--color-border-subtle) / .75); border-radius: .35rem; color: rgb(var(--color-brand-primary)); }
.command-palette__item-copy { min-width: 0; flex: 1; }
.command-palette__item-copy strong, .command-palette__item-copy small { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.command-palette__item-copy strong { font-size: .78rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
.command-palette__item-copy small { margin-top: .13rem; font-size: .68rem; color: rgb(var(--color-text-muted)); }
.command-palette__item-arrow, .command-palette__item-state { font-family: 'JetBrains Mono', monospace; font-size: .58rem; color: rgb(var(--color-brand-primary)); }
.command-palette__status { border-top: 1px solid rgb(var(--color-border-subtle) / .55); padding: 1.5rem 1.1rem; font-family: 'JetBrains Mono', monospace; font-size: .68rem; color: rgb(var(--color-text-muted)); }
.command-palette__pulse { display: inline-block; width: .4rem; height: .4rem; margin-right: .45rem; border-radius: 50%; background: rgb(var(--color-brand-primary)); box-shadow: 0 0 12px rgb(var(--color-brand-primary) / .75); animation: palette-pulse 1.2s ease-in-out infinite; }
.command-palette__footer { border-top: 1px solid rgb(var(--color-border-subtle) / .55); padding: .7rem 1.1rem .8rem; font-size: .53rem; letter-spacing: .06em; color: rgb(var(--color-text-muted)); }
.palette-enter-active, .palette-leave-active { transition: opacity var(--motion-normal) var(--ease-standard); }
.palette-enter-active .command-palette__dialog, .palette-leave-active .command-palette__dialog { transition: opacity var(--motion-normal) var(--ease-emphasized), transform var(--motion-normal) var(--ease-emphasized); }
.palette-enter-from, .palette-leave-to { opacity: 0; }
.palette-enter-from .command-palette__dialog, .palette-leave-to .command-palette__dialog { opacity: 0; transform: translateY(-10px) scale(.985); }
@keyframes palette-pulse { 50% { opacity: .35; transform: scale(.72); } }
@media (max-width: 639px) { .command-palette { align-items: end; padding: 1rem; } .command-palette__dialog { border-radius: 1rem; } .command-palette__results { max-height: 54vh; } }
@media (prefers-reduced-motion: reduce) { .command-palette__item[aria-selected='true'], .command-palette__item:hover { transform: none; } .command-palette__pulse { animation: none; } }
</style>
