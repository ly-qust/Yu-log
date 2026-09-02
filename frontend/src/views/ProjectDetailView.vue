<script setup lang="ts">
import { computed, nextTick, onUnmounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { fetchProjectDetail, fetchProjects } from '@/api/projects';
import BaseBadge from '@/components/common/BaseBadge.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue';
import ProjectArchitecture from '@/components/project/ProjectArchitecture.vue';
import ProjectGallery from '@/components/project/ProjectGallery.vue';
import ProjectNavigation from '@/components/project/ProjectNavigation.vue';
import ProjectSnapshot from '@/components/project/ProjectSnapshot.vue';
import ProjectVisual from '@/components/project/ProjectVisual.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import { useSiteStore } from '@/stores/site';
import type { ProjectArchitectureNode, ProjectDetail, ProjectGalleryImage, ProjectItem } from '@/types/project';
import { formatDate, formatProjectStatus } from '@/utils/format';
import { safeExternalUrl } from '@/utils/links';
import { applySeo } from '@/utils/seo';

const route = useRoute();
const siteStore = useSiteStore();
const project = ref<ProjectDetail | null>(null);
const projectIndex = ref<ProjectItem[]>([]);
const loading = ref(false);
const fatalError = ref('');
const activeArchitectureNode = ref('');
let requestSequence = 0;
let cleanupSeo = () => {};

const projectId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const backTo = computed(() => {
  const back = window.history.state?.back;
  return typeof back === 'string' && /^\/projects(?:\?|$)/.test(back) ? back : '/projects';
});
const projectNumber = computed(() => {
  const index = projectIndex.value.findIndex((item) => item.id === project.value?.id);
  return String(index >= 0 ? index + 1 : 1).padStart(2, '0');
});
const previousProject = computed(() => {
  const index = projectIndex.value.findIndex((item) => item.id === project.value?.id);
  return index > 0 ? projectIndex.value[index - 1] : null;
});
const nextProject = computed(() => {
  const index = projectIndex.value.findIndex((item) => item.id === project.value?.id);
  return index >= 0 && index < projectIndex.value.length - 1 ? projectIndex.value[index + 1] : null;
});
const githubUrl = computed(() => safeExternalUrl(project.value?.githubUrl));
const demoUrl = computed(() => safeExternalUrl(project.value?.demoUrl));
const detailText = computed(() => project.value?.detailContent?.trim() || '');
const architectureAvailable = computed(() => {
  if (!project.value || !project.value.name.toLowerCase().includes('yu.log')) return false;
  const stack = project.value.techStack.join(' ').toLowerCase();
  return ['vue3', 'spring boot', 'mysql', 'redis'].every((term) => stack.includes(term));
});
const architectureNodes = computed<ProjectArchitectureNode[]>(() => architectureAvailable.value ? [
  { id: 'client', label: 'Vue3 / Tailwind CSS', caption: '公开界面', tone: 'brand' },
  { id: 'api', label: 'Spring Boot 3', caption: 'HTTP 接口', tone: 'accent' },
  { id: 'database', label: 'MySQL 8', caption: '持久化数据', tone: 'neutral' },
  { id: 'cache', label: 'Redis', caption: '缓存与计数', tone: 'brand' },
] : []);
const workflowSteps = computed(() => architectureAvailable.value ? [
  { number: '01', nodeId: 'client', label: '公开界面', text: 'Vue3 与 Tailwind CSS 负责渲染公开阅读与项目页面。' },
  { number: '02', nodeId: 'api', label: 'HTTP 接口', text: 'Spring Boot 提供公开内容与项目接口。' },
  { number: '03', nodeId: 'database', label: '持久化数据', text: 'MySQL 保存网站使用的项目与内容记录。' },
  { number: '04', nodeId: 'cache', label: '缓存路径', text: 'Redis 支持仓库中已经存在的缓存与计数相关工作。' },
] : []);
const galleryImages = computed<ProjectGalleryImage[]>(() => []);

function plainDescription(value: ProjectDetail): string {
  const source = value.description?.trim() || value.detailContent?.trim() || '';
  return source
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[#>*_`~\-|]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .slice(0, 180) || 'YU.LOG 公开项目案例。';
}

function applyProjectSeo(value: ProjectDetail) {
  cleanupSeo();
  const author = siteStore.profile?.nickname || 'Yu';
  const description = plainDescription(value);
  const canonicalUrl = new URL(route.path, window.location.origin).toString();
  cleanupSeo = applySeo({
    title: `${value.name}｜项目｜YU.LOG`,
    description,
    canonicalPath: route.path,
    image: value.coverImage || undefined,
    author,
    structuredData: {
      '@context': 'https://schema.org',
      '@type': 'CreativeWork',
      name: value.name,
      description,
      dateCreated: value.createdAt || undefined,
      dateModified: value.updatedAt || value.createdAt || undefined,
      keywords: value.techStack.join(', ') || undefined,
      creator: { '@type': 'Person', name: author },
      image: value.coverImage ? new URL(value.coverImage, window.location.origin).toString() : undefined,
      url: canonicalUrl,
    },
  });
}

async function loadEntry() {
  const id = projectId.value;
  if (!id) {
    fatalError.value = '这个项目不存在，或暂时没有公开。';
    return;
  }

  const requestId = ++requestSequence;
  loading.value = true;
  fatalError.value = '';
  project.value = null;
  projectIndex.value = [];
  const [detailResult, indexResult] = await Promise.allSettled([
    fetchProjectDetail(id),
    fetchProjects({ page: 1, size: 100 }),
    siteStore.loadPublicProfile(),
  ]);

  if (requestId !== requestSequence) return;
  if (detailResult.status === 'rejected') {
    fatalError.value = '这个项目不存在、已下线，或暂时无法读取。';
    loading.value = false;
    return;
  }

  project.value = detailResult.value;
  if (indexResult.status === 'fulfilled') projectIndex.value = indexResult.value.list;
  applyProjectSeo(detailResult.value);
  loading.value = false;
  await nextTick();
  window.scrollTo({ top: 0, behavior: 'auto' });
}

watch(() => projectId.value, () => { void loadEntry(); }, { immediate: true });

onUnmounted(() => {
  requestSequence += 1;
  cleanupSeo();
});
</script>

<template>
  <PublicLayout>
    <div class="project-detail-page">
      <div v-if="loading" class="project-detail-loading" aria-label="项目详情加载中">
        <LoadingSkeleton :lines="3" />
        <div class="mt-10"><LoadingSkeleton :lines="7" /></div>
        <div class="mt-10"><LoadingSkeleton :lines="5" /></div>
      </div>

      <section v-else-if="fatalError" class="project-unavailable">
        <p class="font-mono text-[0.65rem] uppercase tracking-[0.15em] text-brand">项目暂不可用 // PROJECT</p>
        <h1>这一页暂时读不到</h1>
        <p>{{ fatalError }}</p>
        <div class="project-unavailable__actions">
          <button type="button" @click="loadEntry">重新加载</button>
          <RouterLink to="/projects">返回项目列表</RouterLink>
        </div>
      </section>

      <article v-else-if="project">
        <header class="project-hero">
          <RouterLink class="project-back" :to="backTo">← 返回项目</RouterLink>
          <div class="project-hero__meta">
            <p class="font-mono text-[0.65rem] uppercase tracking-[0.15em] text-brand">项目 // {{ projectNumber }} · CASE STUDY</p>
            <BaseBadge :variant="project.status === 'COMPLETED' ? 'success' : project.status === 'DEVELOPING' ? 'brand' : 'accent'" dot>{{ formatProjectStatus(project.status) }}</BaseBadge>
          </div>
          <h1>{{ project.name }}</h1>
          <p class="project-hero__description">{{ project.description || '这个项目的公开说明正在整理中。' }}</p>
          <div class="project-hero__facts">
            <span v-if="project.createdAt">创建于 {{ formatDate(project.createdAt) }}</span>
            <span v-if="project.updatedAt">更新于 {{ formatDate(project.updatedAt) }}</span>
            <span v-if="project.slug">/{{ project.slug }}</span>
          </div>
          <div v-if="githubUrl || demoUrl" class="project-hero__links">
            <a v-if="githubUrl" :href="githubUrl" target="_blank" rel="noreferrer">GitHub <span aria-hidden="true">↗</span></a>
            <a v-if="demoUrl" :href="demoUrl" target="_blank" rel="noreferrer">在线演示 <span aria-hidden="true">↗</span></a>
          </div>
        </header>

        <ProjectVisual :project="project" />
        <ProjectSnapshot :project="project" />

        <div class="project-reading-grid">
          <div class="project-reading-content">
            <section v-if="project.description" class="project-section" aria-labelledby="project-context-title">
              <p class="project-kicker">背景与目的 // CONTEXT</p>
              <h2 id="project-context-title">这个项目要解决什么</h2>
              <p>{{ project.description }}</p>
            </section>

            <ProjectArchitecture :active-node="activeArchitectureNode" :nodes="architectureNodes" @select="activeArchitectureNode = $event" />

            <section v-if="workflowSteps.length" class="project-section project-workflow" aria-labelledby="project-workflow-title">
              <p class="project-kicker">核心流程 // WORKFLOW</p>
              <h2 id="project-workflow-title">从界面走到基础设施</h2>
              <ol class="project-workflow__list">
                <li v-for="step in workflowSteps" :key="step.number" :class="{ 'is-active': activeArchitectureNode === step.nodeId }" tabindex="0" @click="activeArchitectureNode = activeArchitectureNode === step.nodeId ? '' : step.nodeId" @mouseenter="activeArchitectureNode = step.nodeId" @mouseleave="activeArchitectureNode = ''" @focus="activeArchitectureNode = step.nodeId" @blur="activeArchitectureNode = ''">
                  <span>{{ step.number }}</span>
                  <div><h3>{{ step.label }}</h3><p>{{ step.text }}</p></div>
                </li>
              </ol>
            </section>

            <section v-if="detailText" class="project-section project-notes" aria-labelledby="project-notes-title">
              <p class="project-kicker">项目记录 // NOTES</p>
              <h2 id="project-notes-title">我会把什么带到下一次实践</h2>
              <MarkdownRenderer :content="detailText" />
            </section>

            <ProjectGallery :images="galleryImages" />
          </div>
        </div>

        <ProjectNavigation :previous="previousProject" :next="nextProject" />
      </article>

      <EmptyState v-else title="项目正在生长" description="新的项目准备好公开后会出现在这里。" />
    </div>
  </PublicLayout>
</template>

<style scoped>
.project-detail-page { width: min(100%, 76rem); margin: 0 auto; padding-bottom: 6rem; }
.project-detail-loading { width: min(100%, 50rem); margin: 0 auto; padding: clamp(3rem, 8vw, 7rem) 0; }
.project-unavailable { width: min(100%, 48rem); margin: 0 auto; padding: clamp(4rem, 10vw, 8rem) 0; }
.project-unavailable h1 { margin-top: 1rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.4rem, 7vw, 4.5rem); font-weight: 700; line-height: 1; letter-spacing: -.05em; color: rgb(var(--color-text-primary)); }
.project-unavailable > p:last-of-type { max-width: 34rem; margin-top: 1.25rem; line-height: 1.8; color: rgb(var(--color-text-secondary)); }
.project-unavailable__actions { display: flex; flex-wrap: wrap; gap: .75rem; margin-top: 1.75rem; }
.project-unavailable__actions button, .project-unavailable__actions a { display: inline-flex; min-height: 2.7rem; align-items: center; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .55rem; padding: 0 .9rem; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-secondary)); }
.project-unavailable__actions button:hover, .project-unavailable__actions a:hover { border-color: rgb(var(--color-brand-primary)); color: rgb(var(--color-brand-primary)); }
.project-hero { width: min(100%, 58rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 clamp(2.5rem, 6vw, 5rem); }
.project-back { display: inline-flex; font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: rgb(var(--color-text-muted)); transition: color 180ms; }
.project-back:hover { color: rgb(var(--color-brand-primary)); }
.project-hero__meta { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 1rem; margin-top: clamp(3rem, 7vw, 5.5rem); }
.project-hero h1 { max-width: 58rem; margin-top: 1.1rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.8rem, 8vw, 6.6rem); font-weight: 700; line-height: .96; letter-spacing: -.07em; color: rgb(var(--color-text-primary)); }
.project-hero__description { max-width: 46rem; margin-top: 1.5rem; font-size: clamp(1.05rem, 2vw, 1.3rem); line-height: 1.85; color: rgb(var(--color-text-secondary)); }
.project-hero__facts { display: flex; flex-wrap: wrap; gap: .6rem 1.4rem; margin-top: 1.25rem; font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-text-muted)); }
.project-hero__links { display: flex; flex-wrap: wrap; gap: .75rem 1.1rem; margin-top: 1.6rem; }
.project-hero__links a { font-family: 'JetBrains Mono', monospace; font-size: .67rem; color: rgb(var(--color-brand-primary)); transition: color 180ms; }
.project-hero__links a:hover { color: rgb(var(--color-brand-strong)); }
.project-detail-page > article > .project-visual { width: min(100%, 76rem); margin: 0 auto; }
.project-detail-page > article > .project-visual :deep(.project-visual__fallback), .project-detail-page > article > .project-visual :deep(img) { min-height: clamp(20rem, 42vw, 38rem); height: clamp(20rem, 42vw, 38rem); }
.project-reading-grid { display: grid; grid-template-columns: minmax(0, 50rem); justify-content: center; margin-top: 4.5rem; }
.project-reading-content { min-width: 0; }
.project-section { border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 3.5rem; }
.project-section + .project-section { margin-top: 4.5rem; }
.project-kicker { font-family: 'JetBrains Mono', monospace; font-size: .63rem; text-transform: uppercase; letter-spacing: .15em; color: rgb(var(--color-brand-primary)); }
.project-section h2 { margin-top: .65rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.65rem, 4vw, 2.4rem); font-weight: 650; line-height: 1.15; letter-spacing: -.035em; color: rgb(var(--color-text-primary)); }
.project-section > p:not(.project-kicker) { max-width: 42rem; margin-top: 1.2rem; font-size: 1rem; line-height: 1.9; color: rgb(var(--color-text-secondary)); }
.project-workflow__list { display: grid; gap: 0; margin-top: 2rem; }
.project-workflow__list li { display: grid; grid-template-columns: 3rem minmax(0, 1fr); gap: 1.25rem; border-top: 1px solid rgb(var(--color-border-subtle) / .52); padding: 1.3rem .75rem; cursor: pointer; transition: border-color var(--motion-fast) var(--ease-standard), background-color var(--motion-fast) var(--ease-standard), transform var(--motion-normal) var(--ease-emphasized); }
.project-workflow__list li:hover, .project-workflow__list li:focus-visible, .project-workflow__list li.is-active { border-top-color: rgb(var(--color-brand-primary) / .7); background: rgb(var(--color-brand-primary) / .06); outline: none; transform: translateX(.25rem); }
.project-workflow__list li > span { font-family: 'JetBrains Mono', monospace; font-size: .62rem; color: rgb(var(--color-brand-primary)); }
.project-workflow__list h3 { font-family: 'Space Grotesk', sans-serif; font-size: 1.05rem; font-weight: 600; color: rgb(var(--color-text-primary)); }
.project-workflow__list p { margin-top: .38rem; font-size: .86rem; line-height: 1.75; color: rgb(var(--color-text-secondary)); }
.project-notes :deep(.article-prose) { margin-top: 1.4rem; font-size: 1rem; }
@media (prefers-reduced-motion: reduce) { .project-workflow__list li { transition: none; } .project-workflow__list li:hover, .project-workflow__list li:focus-visible, .project-workflow__list li.is-active { transform: none; } }
@media (max-width: 639px) { .project-detail-page { padding-bottom: 4rem; } .project-hero__meta { align-items: flex-start; flex-direction: column; } .project-hero h1 { font-size: clamp(2.7rem, 14vw, 4.5rem); } .project-reading-grid { margin-top: 3rem; } }
</style>
