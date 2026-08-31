<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchNotes } from '@/api/notes';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import NoteNode from '@/components/notes/NoteNode.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { PageResult } from '@/types/api';
import type { NoteItem } from '@/types/note';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const router = useRouter();
const pageSize = 8;
const loading = ref(false);
const optionsLoading = ref(false);
const errorMessage = ref('');
const allNotes = ref<NoteItem[]>([]);
const notePage = ref<PageResult<NoteItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});
const filters = reactive({ keyword: '', topic: '' });
let requestSequence = 0;
let cleanupSeo = () => {};

const hasActiveFilters = computed(() => Boolean(filters.keyword || filters.topic));
const topicOptions = computed(() => [...new Set(allNotes.value.map((note) => note.topic?.trim()).filter((topic): topic is string => Boolean(topic)))].sort((a, b) => a.localeCompare(b)));
const resultDescription = computed(() => loading.value ? '正在读取知识节点…' : hasActiveFilters.value ? `找到 ${notePage.value.total} 条匹配笔记` : `数字花园中有 ${notePage.value.total} 条公开笔记`);

function queryValue(value: unknown): string {
  return Array.isArray(value) ? String(value[0] || '') : typeof value === 'string' ? value : '';
}

function normalizedPage(value: unknown): number {
  const page = Number.parseInt(queryValue(value), 10);
  return Number.isFinite(page) && page > 0 ? page : 1;
}

function buildQuery(page = 1) {
  return {
    ...(filters.keyword.trim() ? { q: filters.keyword.trim() } : {}),
    ...(filters.topic ? { topic: filters.topic } : {}),
    ...(page > 1 ? { page: String(page) } : {}),
  };
}

async function loadAllNotes() {
  optionsLoading.value = true;
  try {
    const result = await fetchNotes({ page: 1, size: 100 });
    allNotes.value = result.list;
  } catch {
    allNotes.value = [];
  } finally {
    optionsLoading.value = false;
  }
}

async function loadNotes(page: number) {
  const requestId = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchNotes({ keyword: filters.keyword.trim() || undefined, topic: filters.topic || undefined, page, size: pageSize });
    if (requestId === requestSequence) notePage.value = result;
  } catch {
    if (requestId === requestSequence) errorMessage.value = '笔记暂时无法加载，请稍后再试。';
  } finally {
    if (requestId === requestSequence) loading.value = false;
  }
}

function syncFromRoute() {
  filters.keyword = queryValue(route.query.q);
  filters.topic = queryValue(route.query.topic);
  void loadNotes(normalizedPage(route.query.page));
}

async function updateRoute(page = 1) {
  const target = { name: 'notes' as const, query: buildQuery(page) };
  const targetPath = router.resolve(target).fullPath;
  if (targetPath === route.fullPath) {
    await loadNotes(page);
    return;
  }
  await router.push(target);
}

async function clearFilters() {
  filters.keyword = '';
  filters.topic = '';
  await updateRoute(1);
}

onMounted(() => {
  void loadAllNotes();
  cleanupSeo = applySeo({
    title: 'Notes — Digital Garden | YU.LOG',
    description: 'YU.LOG 的数字花园：记录仍在生长的学习碎片、工程命令和知识节点。',
    canonicalPath: '/notes',
  });
});

watch(() => route.fullPath, syncFromRoute, { immediate: true });

onUnmounted(() => cleanupSeo());
</script>

<template>
  <PublicLayout>
    <div class="notes-page">
      <header class="notes-intro">
        <p class="notes-kicker">Notes / Digital garden</p>
        <div class="notes-intro__row">
          <div><h1>Small pieces of knowledge,<br /><span>still growing.</span></h1><p>文章保存阶段性结论，笔记保留知识仍在形成时的样子。</p></div>
          <p class="notes-result" aria-live="polite">{{ resultDescription }}</p>
        </div>
        <div class="notes-stats" aria-label="笔记统计"><span><strong>{{ allNotes.length }}</strong> public nodes</span><span><strong>{{ topicOptions.length }}</strong> topics</span><span>Updated by learning</span></div>
      </header>

      <section class="notes-controls" aria-label="笔记筛选">
        <form class="grid gap-3 lg:grid-cols-[minmax(13rem,1.5fr)_minmax(11rem,1fr)_auto]" @submit.prevent="updateRoute(1)">
          <label class="notes-field"><span>Search</span><input v-model="filters.keyword" type="search" placeholder="标题、摘要或内容" /></label>
          <label class="notes-field"><span>Topic</span><select v-model="filters.topic" :disabled="optionsLoading" @change="updateRoute(1)"><option value="">All topics</option><option v-for="topic in topicOptions" :key="topic" :value="topic">{{ topic }}</option></select></label>
          <button class="notes-submit" :disabled="loading" type="submit">Search</button>
        </form>
        <button v-if="hasActiveFilters" class="notes-clear" type="button" @click="clearFilters">Reset all filters</button>
      </section>

      <p v-if="errorMessage" class="notes-error" role="alert">{{ errorMessage }}</p>

      <section class="notes-results" aria-label="数字花园笔记列表">
        <div v-if="loading" class="notes-loading"><div v-for="index in 3" :key="index"><LoadingSkeleton :lines="4" /></div></div>
        <EmptyState v-else-if="notePage.list.length === 0" :title="hasActiveFilters ? '没有匹配的笔记' : '数字花园正在生长'" :description="hasActiveFilters ? '试试减少筛选条件，或换一个关键词。' : '新的知识节点准备好公开后会出现在这里。'"><template v-if="hasActiveFilters" #action><button class="notes-empty-action" type="button" @click="clearFilters">清空筛选</button></template></EmptyState>
        <div v-else class="notes-list"><NoteNode v-for="(note, index) in notePage.list" :key="note.id" :note="note" :index="(notePage.pageNum - 1) * pageSize + index" /></div>
      </section>

      <nav v-if="!loading && notePage.totalPages > 1" class="notes-pagination" aria-label="笔记分页"><button :disabled="!notePage.hasPrevious" type="button" @click="updateRoute(notePage.pageNum - 1)">← Previous</button><span>Page {{ notePage.pageNum }} / {{ notePage.totalPages }}</span><button :disabled="!notePage.hasNext" type="button" @click="updateRoute(notePage.pageNum + 1)">Next →</button></nav>
    </div>
  </PublicLayout>
</template>

<style scoped>
.notes-page { width: min(100%, 76rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 5rem; }
.notes-kicker, .notes-field > span { font-family: 'JetBrains Mono', monospace; font-size: .64rem; text-transform: uppercase; letter-spacing: .15em; color: rgb(var(--color-brand-primary)); }
.notes-intro { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding-bottom: clamp(2.5rem, 6vw, 5rem); }
.notes-intro__row { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 2rem; align-items: end; margin-top: 1.15rem; }
.notes-intro h1 { font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.8rem, 8vw, 6.5rem); font-weight: 700; line-height: .96; letter-spacing: -.07em; color: rgb(var(--color-text-primary)); }
.notes-intro h1 span { color: rgb(var(--color-text-secondary)); }
.notes-intro__row > div > p { max-width: 38rem; margin-top: 1.5rem; font-size: 1rem; line-height: 1.85; color: rgb(var(--color-text-secondary)); }
.notes-result { padding-bottom: .3rem; font-family: 'JetBrains Mono', monospace; font-size: .66rem; color: rgb(var(--color-text-muted)); }
.notes-stats { display: flex; flex-wrap: wrap; gap: .75rem 1.5rem; margin-top: 2.5rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.notes-stats strong { font-size: 1.15rem; color: rgb(var(--color-brand-primary)); }
.notes-controls { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding: 1.25rem 0; }
.notes-field { display: grid; gap: .42rem; }
.notes-field input, .notes-field select { width: 100%; min-height: 2.8rem; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .55rem; padding: 0 .75rem; background: rgb(var(--color-surface-elevated) / .58); font-size: .78rem; color: rgb(var(--color-text-primary)); outline: none; }
.notes-field input:focus, .notes-field select:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .1); }
.notes-submit { align-self: end; min-height: 2.8rem; border-radius: .55rem; padding: 0 1.15rem; background: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; font-weight: 700; color: rgb(var(--color-brand-contrast)); }
.notes-submit:hover:not(:disabled) { filter: brightness(1.08); }
.notes-submit:disabled { cursor: wait; opacity: .55; }
.notes-clear { margin-top: .85rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.notes-clear:hover { color: rgb(var(--color-brand-primary)); }
.notes-error { margin-top: 1rem; border-left: 2px solid rgb(var(--color-danger)); padding: .7rem 1rem; background: rgb(var(--color-danger) / .06); font-size: .8rem; color: rgb(var(--color-danger)); }
.notes-results { min-height: 20rem; }
.notes-loading { display: grid; gap: 1rem; max-width: 52rem; margin: 0 auto; padding-top: 2rem; }
.notes-loading > div { border-bottom: 1px solid rgb(var(--color-border-subtle) / .58); padding: 1.5rem 0; }
.notes-list { max-width: 58rem; margin: 1rem auto 0; }
.notes-pagination { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 1rem; padding-top: 2rem; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); }
.notes-pagination button { justify-self: start; min-height: 2.5rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: 0 .8rem; color: rgb(var(--color-text-secondary)); }
.notes-pagination button:last-child { justify-self: end; }
.notes-pagination button:hover:not(:disabled) { border-color: rgb(var(--color-brand-primary) / .55); color: rgb(var(--color-brand-primary)); }
.notes-pagination button:disabled { cursor: not-allowed; opacity: .35; }
.notes-empty-action { border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: .6rem .8rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-secondary)); }
@media (max-width: 767px) { .notes-intro__row { grid-template-columns: 1fr; gap: 1rem; } .notes-result { padding-bottom: 0; } }
@media (max-width: 479px) { .notes-stats { gap: .6rem 1rem; } .notes-pagination span { text-align: center; } .notes-pagination button { padding: 0 .6rem; font-size: .58rem; } }
</style>
