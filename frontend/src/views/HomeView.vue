<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue';

import { fetchHomeOverview } from '@/api/home';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import FeaturedProjects from '@/components/home/FeaturedProjects.vue';
import GardenNotes from '@/components/home/GardenNotes.vue';
import HomeAbout from '@/components/home/HomeAbout.vue';
import HomeClosing from '@/components/home/HomeClosing.vue';
import HomeHero from '@/components/home/HomeHero.vue';
import LatestWriting from '@/components/home/LatestWriting.vue';
import TechLandscape from '@/components/home/TechLandscape.vue';
import TimelinePreview from '@/components/home/TimelinePreview.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import { useSiteStore } from '@/stores/site';
import type { HomeOverview } from '@/types/site';

const siteStore = useSiteStore();
const homeRoot = ref<HTMLElement | null>(null);
const loading = ref(true);
const overview = ref<HomeOverview | null>(null);
let revealObserver: IntersectionObserver | null = null;
let previousTitle = '';
let previousDescription = '';

const profile = computed(() => siteStore.profile || {});
const about = computed(() => siteStore.about);
const hasProjects = computed(() => Boolean(overview.value?.featuredProjects?.length));
const hasArticles = computed(() => Boolean(overview.value?.latestArticles?.length));
const hasTimeline = computed(() => Boolean(overview.value?.timelinePreview?.length));
const hasNotes = computed(() => Boolean(overview.value?.latestNotes?.length));
const techItems = computed(() => {
  const items = [...(about.value?.skills || []), ...(overview.value?.currentlyLearning || [])];
  return [...new Set(items.map((item) => item.trim()).filter(Boolean))];
});

function updateHomepageMeta() {
  const nickname = String(profile.value.nickname || 'Yu');
  const description = overview.value?.hero.description?.trim()
    || String(profile.value.description || '').trim()
    || 'Yu 的个人技术主页：记录后端系统、AI 应用、项目实践与持续生长的数字花园。';
  document.title = `${nickname} — Backend Developer & Digital Gardener | YU.LOG`;
  const meta = document.querySelector<HTMLMetaElement>('meta[name="description"]');
  if (meta) {
    meta.content = description;
  }
}

function setupRevealObserver() {
  revealObserver?.disconnect();
  const sections = homeRoot.value?.querySelectorAll<HTMLElement>('.home-reveal') || [];
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches || !('IntersectionObserver' in window)) {
    sections.forEach((section) => section.classList.add('is-visible'));
    return;
  }

  revealObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    });
  }, { rootMargin: '0px 0px -10% 0px', threshold: 0.08 });
  sections.forEach((section) => revealObserver?.observe(section));
}

async function loadHomepage() {
  loading.value = true;
  const [overviewResult] = await Promise.allSettled([
    fetchHomeOverview(),
    siteStore.loadPublicProfile(),
  ]);
  overview.value = overviewResult.status === 'fulfilled' ? overviewResult.value : null;
  loading.value = false;
  updateHomepageMeta();
  await nextTick();
  setupRevealObserver();
}

onMounted(() => {
  previousTitle = document.title;
  previousDescription = document.querySelector<HTMLMetaElement>('meta[name="description"]')?.content || '';
  void loadHomepage();
});

onUnmounted(() => {
  revealObserver?.disconnect();
  document.title = previousTitle || 'YU.LOG';
  const meta = document.querySelector<HTMLMetaElement>('meta[name="description"]');
  if (meta && previousDescription) {
    meta.content = previousDescription;
  }
});
</script>

<template>
  <PublicLayout>
    <div ref="homeRoot" class="home-page -mt-2 sm:-mt-4 lg:-mt-6">
      <HomeHero
        class="home-reveal"
        :github-url="siteStore.githubUrl"
        :hero="overview?.hero"
        :learning="overview?.currentlyLearning || []"
        :profile="profile"
        :stats="overview?.stats"
      />

      <div v-if="loading" class="mx-auto grid min-h-[40rem] max-w-6xl gap-6 py-20 md:grid-cols-2" aria-label="首页内容加载中">
        <div v-for="item in 4" :key="item" class="glass-panel min-h-52 rounded-panel p-6">
          <LoadingSkeleton :lines="4" />
        </div>
      </div>

      <template v-else>
        <HomeAbout class="home-reveal" :about="about" :learning="overview?.currentlyLearning || []" :profile="profile" />
        <FeaturedProjects v-if="hasProjects" class="home-reveal" :projects="overview!.featuredProjects" />
        <LatestWriting v-if="hasArticles" class="home-reveal" :articles="overview!.latestArticles" />
        <TechLandscape v-if="techItems.length" class="home-reveal" :items="techItems" />
        <TimelinePreview v-if="hasTimeline" class="home-reveal" :events="overview!.timelinePreview" />
        <GardenNotes v-if="hasNotes" class="home-reveal" :notes="overview!.latestNotes" />
        <HomeClosing class="home-reveal" :email="siteStore.email" :github-url="siteStore.githubUrl" />
      </template>
    </div>
  </PublicLayout>
</template>
