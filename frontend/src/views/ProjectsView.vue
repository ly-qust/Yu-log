<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchProjects } from '@/api/projects';
import BaseBadge from '@/components/common/BaseBadge.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import ProjectVisual from '@/components/project/ProjectVisual.vue';
import type { PageResult } from '@/types/api';
import type { ProjectItem } from '@/types/project';
import { formatDate, formatProjectStatus } from '@/utils/format';
import { safeExternalUrl } from '@/utils/links';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const router = useRouter();
const pageSize = 6;
const loading = ref(false);
const optionsLoading = ref(false);
const errorMessage = ref('');
const allProjects = ref<ProjectItem[]>([]);
const projectPage = ref<PageResult<ProjectItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});
const filters = reactive({ keyword: '', techStack: '', status: '' });
let requestSequence = 0;
let cleanupSeo = () => {};

const hasActiveFilters = computed(() => Boolean(filters.keyword || filters.techStack || filters.status));
const resultDescription = computed(() => {
  if (loading.value) return '正在读取项目索引…';
  if (hasActiveFilters.value) return `找到 ${projectPage.value.total} 个匹配项目`;
  return `索引中有 ${projectPage.value.total} 个公开项目`;
});
const statusCounts = computed(() => ({
  planning: allProjects.value.filter((project) => project.status === 'PLANNING').length,
  developing: allProjects.value.filter((project) => project.status === 'DEVELOPING').length,
  completed: allProjects.value.filter((project) => project.status === 'COMPLETED').length,
}));
const techOptions = computed(() => [...new Set(allProjects.value.flatMap((project) => project.techStack).map((item) => item.trim()).filter(Boolean))].sort((a, b) => a.localeCompare(b)));

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
    ...(filters.techStack ? { tech: filters.techStack } : {}),
    ...(filters.status ? { status: filters.status } : {}),
    ...(page > 1 ? { page: String(page) } : {}),
  };
}

function statusVariant(status?: string) {
  return status === 'COMPLETED' ? 'success' : status === 'DEVELOPING' ? 'brand' : 'accent';
}

async function loadAllProjects() {
  optionsLoading.value = true;
  try {
    const result = await fetchProjects({ page: 1, size: 100 });
    allProjects.value = result.list;
  } catch {
    allProjects.value = [];
  } finally {
    optionsLoading.value = false;
  }
}

async function loadProjects(page: number) {
  const requestId = ++requestSequence;
  loading.value = true;
  errorMessage.value = '';
  try {
    const result = await fetchProjects({
      keyword: filters.keyword.trim() || undefined,
      techStack: filters.techStack || undefined,
      status: filters.status || undefined,
      page,
      size: pageSize,
    });
    if (requestId === requestSequence) projectPage.value = result;
  } catch {
    if (requestId === requestSequence) errorMessage.value = '项目暂时无法加载，请稍后再试。';
  } finally {
    if (requestId === requestSequence) loading.value = false;
  }
}

function syncFromRoute() {
  filters.keyword = queryValue(route.query.q);
  filters.techStack = queryValue(route.query.tech);
  filters.status = queryValue(route.query.status);
  void loadProjects(normalizedPage(route.query.page));
}

async function updateRoute(page = 1) {
  const target = { name: 'projects' as const, query: buildQuery(page) };
  const targetPath = router.resolve(target).fullPath;
  if (targetPath === route.fullPath) {
    await loadProjects(page);
    return;
  }
  await router.push(target);
}

async function clearFilters() {
  filters.keyword = '';
  filters.techStack = '';
  filters.status = '';
  await updateRoute(1);
}

onMounted(() => {
  void loadAllProjects();
  cleanupSeo = applySeo({
    title: '项目｜YU.LOG · Yu 的工程实践',
    description: 'YU.LOG 的公开项目索引：记录真实项目、技术栈、状态与工程上下文。',
    canonicalPath: '/projects',
  });
});

watch(() => route.fullPath, syncFromRoute, { immediate: true });

onUnmounted(() => cleanupSeo());
</script>

<template>
  <PublicLayout>
    <div class="projects-page">
      <header class="projects-intro">
        <p class="font-mono text-[0.68rem] uppercase tracking-[0.16em] text-brand">精选项目 // SELECTED WORK</p>
        <div class="mt-5 grid gap-8 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
          <div>
            <h1>项目</h1>
            <p class="projects-lede">把正在构建、已经完成和仍在学习中的系统，放回它们真实的上下文里。</p>
          </div>
          <p class="projects-result" aria-live="polite">{{ resultDescription }}</p>
        </div>
        <dl class="projects-stats" aria-label="项目统计">
          <div><dt>全部项目</dt><dd>{{ allProjects.length }}</dd></div>
          <div><dt>规划中</dt><dd>{{ statusCounts.planning }}</dd></div>
          <div><dt>开发中</dt><dd>{{ statusCounts.developing }}</dd></div>
          <div><dt>已完成</dt><dd>{{ statusCounts.completed }}</dd></div>
        </dl>
      </header>

      <section class="projects-controls" aria-label="项目筛选">
        <form class="grid gap-3 lg:grid-cols-[minmax(13rem,1.6fr)_repeat(2,minmax(10rem,1fr))_auto]" @submit.prevent="updateRoute(1)">
          <label class="projects-field">
            <span>搜索</span>
            <input v-model="filters.keyword" type="search" placeholder="项目名称或描述" />
          </label>
          <label class="projects-field">
            <span>技术栈</span>
            <select v-model="filters.techStack" :disabled="optionsLoading" @change="updateRoute(1)">
              <option value="">全部技术栈</option>
              <option v-for="tech in techOptions" :key="tech" :value="tech">{{ tech }}</option>
            </select>
          </label>
          <label class="projects-field">
            <span>状态</span>
            <select v-model="filters.status" @change="updateRoute(1)">
              <option value="">全部状态</option>
              <option value="PLANNING">规划中</option>
              <option value="DEVELOPING">开发中</option>
              <option value="COMPLETED">已完成</option>
            </select>
          </label>
          <button class="projects-submit" :disabled="loading" type="submit">搜索</button>
        </form>
        <button v-if="hasActiveFilters" class="projects-clear" type="button" @click="clearFilters">重置筛选</button>
      </section>

      <p v-if="errorMessage" class="projects-error" role="alert">{{ errorMessage }}</p>

      <section class="projects-results" aria-label="项目列表">
        <template v-if="loading">
          <div v-for="index in 3" :key="index" class="projects-skeleton"><LoadingSkeleton :lines="4" /></div>
        </template>

        <EmptyState
          v-else-if="projectPage.list.length === 0"
          :title="hasActiveFilters ? '没有匹配的项目' : '项目正在生长'"
          :description="hasActiveFilters ? '试试减少筛选条件，或换一个关键词。' : '新的项目准备好公开后会出现在这里。'"
        >
          <template v-if="hasActiveFilters" #action><button class="projects-clear is-prominent" type="button" @click="clearFilters">清空筛选</button></template>
        </EmptyState>

        <article v-for="(project, index) in projectPage.list" v-else :key="project.id" class="project-row">
          <RouterLink class="project-row__link" :to="`/projects/${project.id}`">
            <div class="project-row__index" aria-hidden="true">{{ String((projectPage.pageNum - 1) * pageSize + index + 1).padStart(2, '0') }}</div>
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-x-3 gap-y-2 font-mono text-[0.64rem] uppercase tracking-[0.1em] text-text-muted">
                <BaseBadge :variant="statusVariant(project.status)" dot>{{ formatProjectStatus(project.status) }}</BaseBadge>
                <span>项目 // {{ String((projectPage.pageNum - 1) * pageSize + index + 1).padStart(2, '0') }}</span>
              </div>
              <h2>{{ project.name }}</h2>
              <p class="project-row__summary">{{ project.description || '项目说明正在整理中。' }}</p>
              <div v-if="project.techStack.length" class="project-row__tags"><span v-for="tech in project.techStack.slice(0, 6)" :key="tech">+ {{ tech }}</span><span v-if="project.techStack.length > 6">还有 {{ project.techStack.length - 6 }} 项</span></div>
            </div>
            <ProjectVisual v-if="index === 0 && !hasActiveFilters" class="project-row__visual" :project="project" />
            <div class="project-row__meta">
              <time>更新于 {{ formatDate(project.updatedAt || project.createdAt) }}</time>
              <span v-if="safeExternalUrl(project.githubUrl) || safeExternalUrl(project.demoUrl)" class="project-row__external">有外部链接</span>
              <strong>查看项目 <span aria-hidden="true">↗</span></strong>
            </div>
          </RouterLink>
        </article>
      </section>

      <nav v-if="!loading && projectPage.totalPages > 1" class="projects-pagination" aria-label="项目分页">
        <button :disabled="!projectPage.hasPrevious" type="button" @click="updateRoute(projectPage.pageNum - 1)">← 上一页</button>
        <span>第 {{ projectPage.pageNum }} / {{ projectPage.totalPages }} 页</span>
        <button :disabled="!projectPage.hasNext" type="button" @click="updateRoute(projectPage.pageNum + 1)">下一页 →</button>
      </nav>
    </div>
  </PublicLayout>
</template>

<style scoped>
.projects-page { width: min(100%, 76rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 5rem; }
.projects-intro { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding-bottom: clamp(2rem, 5vw, 4rem); }
.projects-intro h1 { font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.8rem, 8vw, 6.5rem); font-weight: 700; line-height: .95; letter-spacing: -.065em; color: rgb(var(--color-text-primary)); }
.projects-lede { max-width: 42rem; margin-top: 1.25rem; font-size: clamp(1rem, 2vw, 1.15rem); line-height: 1.9; color: rgb(var(--color-text-secondary)); }
.projects-result { padding-bottom: .25rem; font-family: 'JetBrains Mono', monospace; font-size: .68rem; color: rgb(var(--color-text-muted)); }
.projects-stats { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 1rem; margin-top: 3rem; }
.projects-stats > div { border-left: 1px solid rgb(var(--color-border-subtle) / .68); padding-left: .85rem; }
.projects-stats dt { font-family: 'JetBrains Mono', monospace; font-size: .58rem; text-transform: uppercase; letter-spacing: .12em; color: rgb(var(--color-text-muted)); }
.projects-stats dd { margin-top: .45rem; font-family: 'Space Grotesk', sans-serif; font-size: 1.7rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
.projects-controls { position: relative; z-index: 2; border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding: 1.25rem 0; }
.projects-field { display: grid; gap: .42rem; }
.projects-field > span { font-family: 'JetBrains Mono', monospace; font-size: .58rem; text-transform: uppercase; letter-spacing: .12em; color: rgb(var(--color-text-muted)); }
.projects-field input, .projects-field select { width: 100%; min-height: 2.8rem; border: 1px solid rgb(var(--color-border-subtle) / .82); border-radius: .55rem; padding: 0 .75rem; background: rgb(var(--color-surface-elevated) / .58); font-size: .78rem; color: rgb(var(--color-text-primary)); outline: none; transition: border-color 180ms, box-shadow 180ms; }
.projects-field input:focus, .projects-field select:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .1); }
.projects-submit { align-self: end; min-height: 2.8rem; border-radius: .55rem; padding: 0 1.15rem; background: rgb(var(--color-brand-primary)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; font-weight: 700; color: rgb(var(--color-brand-contrast)); transition: filter 180ms, transform 180ms; }
.projects-submit:hover:not(:disabled) { filter: brightness(1.08); transform: translateY(-1px); }
.projects-submit:disabled { cursor: wait; opacity: .55; }
.projects-clear { margin-top: .85rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); transition: color 180ms; }
.projects-clear:hover { color: rgb(var(--color-brand-primary)); }
.projects-clear.is-prominent { margin-top: 0; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: .6rem .8rem; }
.projects-error { margin-top: 1rem; border-left: 2px solid rgb(var(--color-danger)); padding: .7rem 1rem; background: rgb(var(--color-danger) / .06); font-size: .8rem; color: rgb(var(--color-danger)); }
.projects-results { min-height: 20rem; }
.projects-skeleton { border-bottom: 1px solid rgb(var(--color-border-subtle) / .58); padding: 2rem 0; }
.project-row { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); }
.project-row__link { display: grid; grid-template-columns: 2.5rem minmax(0, 1fr) 15rem 10rem; gap: clamp(1rem, 3vw, 2rem); padding: clamp(1.6rem, 4vw, 2.6rem) 0; }
.project-row__link:hover h2 { color: rgb(var(--color-brand-primary)); }
.project-row__index { padding-top: .15rem; font-family: 'JetBrains Mono', monospace; font-size: .66rem; color: rgb(var(--color-text-muted)); }
.project-row h2 { margin-top: .85rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.45rem, 3vw, 2.3rem); font-weight: 650; line-height: 1.16; letter-spacing: -.03em; color: rgb(var(--color-text-primary)); transition: color 180ms; }
.project-row__summary { max-width: 42rem; margin-top: .8rem; font-size: .9rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.project-row__tags { display: flex; flex-wrap: wrap; gap: .55rem 1rem; margin-top: 1rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.project-row__visual { width: 15rem; min-height: 10rem; align-self: center; }
.project-row__visual :deep(.project-visual__fallback), .project-row__visual :deep(img) { min-height: 10rem; height: 10rem; }
.project-row__visual :deep(.project-visual__label) { font-size: .48rem; }
.project-row__visual :deep(.project-visual__signal) { width: 6rem; }
.project-row__meta { display: flex; flex-direction: column; align-items: flex-end; gap: .45rem; padding-top: .2rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); text-align: right; }
.project-row__external { color: rgb(var(--color-text-muted)); }
.project-row__meta strong { margin-top: auto; color: rgb(var(--color-brand-primary)); }
.projects-pagination { display: grid; grid-template-columns: 1fr auto 1fr; align-items: center; gap: 1rem; padding-top: 2rem; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); }
.projects-pagination button { justify-self: start; min-height: 2.5rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .5rem; padding: 0 .8rem; color: rgb(var(--color-text-secondary)); }
.projects-pagination button:last-child { justify-self: end; }
.projects-pagination button:hover:not(:disabled) { border-color: rgb(var(--color-brand-primary) / .55); color: rgb(var(--color-brand-primary)); }
.projects-pagination button:disabled { cursor: not-allowed; opacity: .35; }
@media (max-width: 1023px) { .project-row__link { grid-template-columns: 2.5rem minmax(0, 1fr) 11rem; } .project-row__visual { display: none; } }
@media (max-width: 767px) { .projects-stats { gap: .75rem; } .projects-stats dd { font-size: 1.4rem; } .project-row__link { grid-template-columns: 1.8rem minmax(0, 1fr); } .project-row__meta { grid-column: 2; flex-direction: row; flex-wrap: wrap; align-items: center; gap: .45rem .9rem; text-align: left; } .project-row__meta strong { width: 100%; margin-top: .45rem; } }
@media (max-width: 479px) { .projects-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 1rem .7rem; } .project-row__index { display: none; } .project-row__link { grid-template-columns: 1fr; } .project-row__meta { grid-column: 1; } .projects-pagination span { text-align: center; } .projects-pagination button { padding: 0 .6rem; font-size: .58rem; } }
@media (prefers-reduced-motion: reduce) { .projects-submit:hover:not(:disabled) { transform: none; } }
</style>
