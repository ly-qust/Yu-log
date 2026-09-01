<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchTimeline } from '@/api/timeline';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import TimelineEntry from '@/components/timeline/TimelineEntry.vue';
import type { PageResult } from '@/types/api';
import type { TimelineEventItem } from '@/types/timeline';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const router = useRouter();
const pageSize = 8;
const loading = ref(false);
const optionsLoading = ref(false);
const errorMessage = ref('');
const allEvents = ref<TimelineEventItem[]>([]);
const timelineTrack = ref<HTMLElement | null>(null);
const timelineProgress = ref(0);
const timelinePage = ref<PageResult<TimelineEventItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});
const filters = reactive({ type: '' });
let requestSequence = 0;
let cleanupSeo = () => {};
let timelineFrame = 0;

const typeOptions = computed(() => [...new Set(allEvents.value.map((event) => event.type?.trim()).filter((type): type is string => Boolean(type)))].sort((a, b) => a.localeCompare(b)));
const resultDescription = computed(() => loading.value ? '正在读取成长记录…' : `公开档案中有 ${timelinePage.value.total} 条记录`);

function queryValue(value: unknown): string {
  return Array.isArray(value) ? String(value[0] || '') : typeof value === 'string' ? value : '';
}

function normalizedPage(value: unknown): number {
  const page = Number.parseInt(queryValue(value), 10);
  return Number.isFinite(page) && page > 0 ? page : 1;
}

function buildQuery(page = 1) {
  return {
    ...(filters.type ? { type: filters.type } : {}),
    ...(page > 1 ? { page: String(page) } : {}),
  };
}

async function loadAllEvents() {
  optionsLoading.value = true;
  try {
    const result = await fetchTimeline({ page: 1, size: 100 });
    allEvents.value = result.list;
  } catch {
    allEvents.value = [];
  } finally {
    optionsLoading.value = false;
  }
}

async function loadTimeline(page: number) {
  const requestId = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchTimeline({ type: filters.type || undefined, page, size: pageSize });
    if (requestId === requestSequence) timelinePage.value = result;
  } catch {
    if (requestId === requestSequence) errorMessage.value = '成长记录暂时无法加载，请稍后再试。';
  } finally {
    if (requestId === requestSequence) loading.value = false;
    if (requestId === requestSequence) {
      await nextTick();
      scheduleTimelineProgress();
    }
  }
}

function updateTimelineProgress() {
  timelineFrame = 0;
  const track = timelineTrack.value;
  if (!track) {
    timelineProgress.value = 0;
    return;
  }
  const rect = track.getBoundingClientRect();
  const viewportMarker = window.innerHeight * 0.66;
  const progress = ((viewportMarker - rect.top) / Math.max(rect.height, 1)) * 100;
  timelineProgress.value = Math.max(0, Math.min(100, progress));
}

function scheduleTimelineProgress() {
  if (timelineFrame) return;
  timelineFrame = window.requestAnimationFrame(updateTimelineProgress);
}

function syncFromRoute() {
  filters.type = queryValue(route.query.type);
  void loadTimeline(normalizedPage(route.query.page));
}

async function updateRoute(page = 1) {
  const target = { name: 'timeline' as const, query: buildQuery(page) };
  const targetPath = router.resolve(target).fullPath;
  if (targetPath === route.fullPath) {
    await loadTimeline(page);
    return;
  }
  await router.push(target);
}

async function clearFilters() {
  filters.type = '';
  await updateRoute(1);
}

onMounted(() => {
  void loadAllEvents();
  window.addEventListener('scroll', scheduleTimelineProgress, { passive: true });
  window.addEventListener('resize', scheduleTimelineProgress);
  scheduleTimelineProgress();
  cleanupSeo = applySeo({
    title: 'Timeline — Growth Archive | YU.LOG',
    description: 'Yu 的成长档案：记录真实的项目、学习、数据库和阶段性工程里程碑。',
    canonicalPath: '/timeline',
  });
});

watch(() => route.fullPath, syncFromRoute, { immediate: true });

onUnmounted(() => {
  window.removeEventListener('scroll', scheduleTimelineProgress);
  window.removeEventListener('resize', scheduleTimelineProgress);
  if (timelineFrame) window.cancelAnimationFrame(timelineFrame);
  cleanupSeo();
});
</script>

<template>
  <PublicLayout>
    <div class="timeline-page">
      <header class="timeline-intro">
        <p class="timeline-kicker">Timeline / Growth log</p>
        <div class="timeline-intro__row">
          <div><h1>A record of things<br /><span>built, learned and finished.</span></h1><p>完整保存项目、学习和工程实践留下的成长信号。</p></div>
          <p class="timeline-result" aria-live="polite">{{ resultDescription }}</p>
        </div>
      </header>

      <section class="timeline-controls" aria-label="成长记录筛选">
        <form class="timeline-filter" @submit.prevent="updateRoute(1)">
          <label class="timeline-field"><span>Record type</span><select v-model="filters.type" :disabled="optionsLoading" @change="updateRoute(1)"><option value="">All records</option><option v-for="type in typeOptions" :key="type" :value="type">{{ type }}</option></select></label>
          <button type="submit" :disabled="loading">Filter</button>
          <button v-if="filters.type" class="timeline-reset" type="button" @click="clearFilters">Reset</button>
        </form>
      </section>

      <p v-if="errorMessage" class="timeline-error" role="alert">{{ errorMessage }}</p>

      <section class="timeline-results" aria-label="成长记录列表">
        <div v-if="loading" class="timeline-loading"><div v-for="index in 3" :key="index" class="timeline-loading__item"><LoadingSkeleton :lines="4" /></div></div>
        <EmptyState v-else-if="timelinePage.list.length === 0" :title="filters.type ? '没有匹配的记录' : '成长记录正在生长'" :description="filters.type ? '试试切换记录类型，或重置筛选。' : '新的工程里程碑准备好公开后会出现在这里。'"><template v-if="filters.type" #action><button class="timeline-empty-action" type="button" @click="clearFilters">清空筛选</button></template></EmptyState>
        <ol v-else ref="timelineTrack" class="timeline-track" :style="{ '--timeline-progress': `${timelineProgress}%` }">
          <TimelineEntry v-for="(event, index) in timelinePage.list" :key="event.id" :event="event" :index="(timelinePage.pageNum - 1) * pageSize + index" :side="index % 2 === 0 ? 'left' : 'right'" />
        </ol>
      </section>

      <nav v-if="!loading && timelinePage.totalPages > 1" class="timeline-pagination" aria-label="成长记录分页"><button :disabled="!timelinePage.hasPrevious" type="button" @click="updateRoute(timelinePage.pageNum - 1)">← Previous</button><span>Page {{ timelinePage.pageNum }} / {{ timelinePage.totalPages }}</span><button :disabled="!timelinePage.hasNext" type="button" @click="updateRoute(timelinePage.pageNum + 1)">Next →</button></nav>
    </div>
  </PublicLayout>
</template>

<style scoped>
.timeline-page { width: min(100%, 76rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 5rem; }
.timeline-kicker, .timeline-field > span { font-family: 'JetBrains Mono', monospace; font-size: .64rem; text-transform: uppercase; letter-spacing: .15em; color: rgb(var(--color-brand-primary)); }
.timeline-intro { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding-bottom: clamp(2.5rem, 6vw, 5rem); }
.timeline-intro__row { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 2rem; align-items: end; margin-top: 1.15rem; }
.timeline-intro h1 { font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.8rem, 8vw, 6.5rem); font-weight: 700; line-height: .96; letter-spacing: -.07em; color: rgb(var(--color-text-primary)); }
.timeline-intro h1 span { color: rgb(var(--color-text-secondary)); }
.timeline-intro__row > div > p { max-width: 39rem; margin-top: 1.5rem; font-size: 1rem; line-height: 1.85; color: rgb(var(--color-text-secondary)); }
.timeline-result { padding-bottom: .3rem; font-family: 'JetBrains Mono', monospace; font-size: .66rem; color: rgb(var(--color-text-muted)); }
.timeline-controls { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding: 1.25rem 0; }
.timeline-filter { display: flex; flex-wrap: wrap; align-items: end; gap: .75rem; }
.timeline-field { display: grid; min-width: min(100%, 16rem); gap: .42rem; }
.timeline-field select { min-height: 2.8rem; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .55rem; padding: 0 .75rem; background: rgb(var(--color-surface-elevated) / .58); font-size: .78rem; color: rgb(var(--color-text-primary)); outline: none; }
.timeline-field select:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .1); }
.timeline-filter button { min-height: 2.8rem; border: 1px solid rgb(var(--color-brand-primary) / .65); border-radius: .55rem; padding: 0 1rem; background: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; font-weight: 700; color: rgb(var(--color-brand-contrast)); }
.timeline-filter button:hover:not(:disabled) { filter: brightness(1.08); }
.timeline-filter button:disabled { cursor: wait; opacity: .55; }
.timeline-filter .timeline-reset { border-color: rgb(var(--color-border-subtle)); background: transparent; color: rgb(var(--color-text-secondary)); }
.timeline-error { margin-top: 1rem; border-left: 2px solid rgb(var(--color-danger)); padding: .7rem 1rem; background: rgb(var(--color-danger) / .06); font-size: .8rem; color: rgb(var(--color-danger)); }
.timeline-results { padding-top: clamp(3rem, 6vw, 5rem); }
.timeline-loading { display: grid; gap: 1.25rem; max-width: 50rem; margin: 0 auto; }
.timeline-loading__item { min-height: 9rem; border: 1px solid rgb(var(--color-border-subtle) / .55); border-radius: .8rem; padding: 1.3rem; }
.timeline-track { position: relative; max-width: 68rem; margin: 0 auto; padding: 1rem 0 0; list-style: none; }
.timeline-track::before, .timeline-track::after { position: absolute; top: 0; left: 50%; width: 1px; content: ''; pointer-events: none; }
.timeline-track::before { bottom: 0; background: linear-gradient(rgb(var(--color-brand-primary) / .1), rgb(var(--color-brand-primary) / .7) 20%, rgb(var(--color-accent-secondary) / .5) 80%, transparent); }
.timeline-track::after { height: var(--timeline-progress, 0%); background: linear-gradient(rgb(var(--color-brand-primary)), rgb(var(--color-accent-secondary))); box-shadow: 0 0 12px rgb(var(--color-brand-primary) / .5); transition: height var(--motion-normal) var(--ease-standard); }
.timeline-pagination { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 1rem; padding-top: 2rem; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); }
.timeline-pagination button { justify-self: start; min-height: 2.5rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: 0 .8rem; color: rgb(var(--color-text-secondary)); }
.timeline-pagination button:last-child { justify-self: end; }
.timeline-pagination button:hover:not(:disabled) { border-color: rgb(var(--color-brand-primary) / .55); color: rgb(var(--color-brand-primary)); }
.timeline-pagination button:disabled { cursor: not-allowed; opacity: .35; }
.timeline-empty-action { border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: .6rem .8rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-secondary)); }
@media (max-width: 767px) { .timeline-intro__row { grid-template-columns: 1fr; gap: 1rem; } .timeline-result { padding-bottom: 0; } .timeline-track::before { left: .02rem; } }
@media (max-width: 479px) { .timeline-pagination span { text-align: center; } .timeline-pagination button { padding: 0 .6rem; font-size: .58rem; } }
@media (max-width: 767px) { .timeline-track::after { left: .02rem; } }
@media (prefers-reduced-motion: reduce) { .timeline-track::after { transition: none; } }
</style>
