<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';

import { fetchAbout } from '@/api/about';
import AboutIdentity from '@/components/about/AboutIdentity.vue';
import AboutSkillGroups from '@/components/about/AboutSkillGroups.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import LoadingSkeleton from '@/components/common/LoadingSkeleton.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { AboutData } from '@/types/site';
import { applySeo } from '@/utils/seo';
import { safeExternalUrl } from '@/utils/links';

const loading = ref(false);
const errorMessage = ref('');
const about = ref<AboutData | null>(null);
let cleanupSeo = () => {};

function stringList(value: unknown): string[] {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0)
    : [];
}

const profile = computed(() => about.value?.profile || {});
const careerDirection = computed(() => stringList(profile.value.careerDirection));
const githubUrl = computed(() => safeExternalUrl(profile.value.githubUrl));
const email = computed(() => {
  const value = typeof profile.value.email === 'string' ? profile.value.email.trim() : '';
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? value : '';
});

async function loadAbout() {
  loading.value = true;
  errorMessage.value = '';
  try {
    about.value = await fetchAbout();
  } catch {
    errorMessage.value = '个人资料暂时无法加载，请稍后再试。';
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  void loadAbout();
  cleanupSeo = applySeo({
    title: '关于我｜YU.LOG · Yu 的个人工程档案',
    description: 'Yu 的个人工程档案：当前方向、学习方式、公开技能与成长中的系统。',
    canonicalPath: '/about',
  });
});

onUnmounted(() => cleanupSeo());
</script>

<template>
  <PublicLayout>
    <div class="about-page">
      <header class="about-intro">
        <p class="about-kicker">关于我 // ABOUT</p>
        <h1>系统背后，<br /><span>是一个仍在学习的人。</span></h1>
        <p class="about-intro__lede">这不是一份静态简历，而是一张持续更新的个人工程地图：我在什么方向上工作，又如何把学习留下来。</p>
      </header>

      <div v-if="loading" class="about-loading" aria-label="个人资料加载中">
        <LoadingSkeleton :lines="5" />
        <div class="mt-10"><LoadingSkeleton :lines="7" /></div>
      </div>

      <p v-else-if="errorMessage" class="about-error" role="alert">{{ errorMessage }}</p>

      <EmptyState v-else-if="!about" title="个人档案正在生长" description="公开资料准备好后会出现在这里。" />

      <article v-else class="about-content">
        <div class="about-intro-grid">
          <div>
            <p class="about-section-label">01 / 我是谁</p>
            <p class="about-lead">{{ profile.description || '我正在通过课程、项目和问题排查积累可复用的工程经验。' }}</p>
          </div>
          <AboutIdentity :profile="profile" :career-direction="careerDirection" :github-url="githubUrl" :email="email" />
        </div>

        <AboutSkillGroups :skills="stringList(about.skills)" />

        <div class="about-record-grid">
          <section v-if="about.learningPhilosophy" class="about-record" aria-labelledby="about-learning-title">
            <p class="about-section-label">03 / 我如何学习</p>
            <h2 id="about-learning-title">做出来，讲清楚，留下可复用的部分。</h2>
            <p>{{ about.learningPhilosophy }}</p>
          </section>
          <section v-if="about.education.length" class="about-record" aria-labelledby="about-education-title">
            <p class="about-section-label">04 / 当前章节</p>
            <h2 id="about-education-title">学习与准备</h2>
            <ul><li v-for="item in about.education" :key="item">{{ item }}</li></ul>
          </section>
        </div>

        <section v-if="githubUrl || email" class="about-contact" aria-labelledby="about-contact-title">
          <div><p class="about-section-label">05 / 联系方式</p><h2 id="about-contact-title">保持联系。</h2></div>
          <div class="about-contact__links">
            <a v-if="githubUrl" :href="githubUrl" target="_blank" rel="noreferrer">GitHub <span aria-hidden="true">↗</span></a>
            <a v-if="email" :href="`mailto:${email}`">{{ email }}</a>
          </div>
        </section>
      </article>
    </div>
  </PublicLayout>
</template>

<style scoped>
.about-page { width: min(100%, 76rem); margin: 0 auto; padding: clamp(2rem, 5vw, 5rem) 0 5rem; }
.about-kicker, .about-section-label { font-family: 'JetBrains Mono', monospace; font-size: .64rem; text-transform: uppercase; letter-spacing: .15em; color: rgb(var(--color-brand-primary)); }
.about-intro { border-bottom: 1px solid rgb(var(--color-border-subtle) / .68); padding-bottom: clamp(2.5rem, 6vw, 5rem); }
.about-intro h1 { margin-top: 1.15rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(2.8rem, 8vw, 6.5rem); font-weight: 700; line-height: .96; letter-spacing: -.07em; color: rgb(var(--color-text-primary)); }
.about-intro h1 span { color: rgb(var(--color-text-secondary)); }
.about-intro__lede { max-width: 42rem; margin-top: 1.6rem; font-size: clamp(1rem, 2vw, 1.15rem); line-height: 1.9; color: rgb(var(--color-text-secondary)); }
.about-loading { width: min(100%, 54rem); padding: 4rem 0; }
.about-error { margin-top: 1.25rem; border-left: 2px solid rgb(var(--color-danger)); padding: .75rem 1rem; background: rgb(var(--color-danger) / .06); font-size: .82rem; color: rgb(var(--color-danger)); }
.about-content { display: grid; gap: clamp(4rem, 9vw, 7rem); }
.about-intro-grid { display: grid; grid-template-columns: minmax(0, 1.1fr) minmax(18rem, .9fr); gap: clamp(2rem, 6vw, 6rem); align-items: start; padding-top: clamp(2.5rem, 6vw, 5rem); }
.about-lead { max-width: 42rem; margin-top: 1.1rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.65rem, 3.6vw, 2.75rem); font-weight: 500; line-height: 1.28; letter-spacing: -.035em; color: rgb(var(--color-text-primary)); }
.about-record-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: clamp(2rem, 7vw, 6rem); }
.about-record { border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 1.35rem; }
.about-record h2, .about-contact h2 { max-width: 28rem; margin-top: .7rem; font-family: 'Space Grotesk', sans-serif; font-size: clamp(1.5rem, 3.5vw, 2.35rem); font-weight: 650; line-height: 1.15; letter-spacing: -.04em; }
.about-record > p:last-child { max-width: 35rem; margin-top: 1.1rem; font-size: .95rem; line-height: 1.9; color: rgb(var(--color-text-secondary)); }
.about-record ul { display: grid; gap: .8rem; margin-top: 1.2rem; }
.about-record li { display: flex; gap: .75rem; font-size: .9rem; line-height: 1.7; color: rgb(var(--color-text-secondary)); }
.about-record li::before { content: '↳'; color: rgb(var(--color-brand-primary)); }
.about-contact { display: grid; grid-template-columns: minmax(0, 1fr) auto; gap: 2rem; border-top: 1px solid rgb(var(--color-border-subtle) / .68); padding-top: 1.35rem; }
.about-contact__links { display: flex; flex-wrap: wrap; align-items: end; justify-content: end; gap: .7rem 1.2rem; padding-bottom: .25rem; }
.about-contact__links a { font-family: 'JetBrains Mono', monospace; font-size: .68rem; color: rgb(var(--color-brand-primary)); transition: color 180ms; }
.about-contact__links a:hover { color: rgb(var(--color-brand-strong)); }
@media (max-width: 767px) { .about-intro-grid, .about-record-grid, .about-contact { grid-template-columns: 1fr; } .about-contact__links { justify-content: start; align-items: start; } }
</style>
