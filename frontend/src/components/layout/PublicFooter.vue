<script setup lang="ts">
import BrandMark from '@/components/common/BrandMark.vue';

withDefaults(defineProps<{
  githubUrl?: string;
  email?: string;
}>(), {
  githubUrl: '',
  email: '',
});

const currentYear = new Date().getFullYear();
const footerNavigation = [
  { label: '文章', to: '/articles' },
  { label: '项目', to: '/projects' },
  { label: '笔记', to: '/notes' },
  { label: '时间线', to: '/timeline' },
  { label: '关于我', to: '/about' },
];
</script>

<template>
  <footer class="mt-auto border-t border-border-subtle/65 bg-surface-elevated/42 backdrop-blur-glass">
    <div class="mx-auto w-full max-w-content px-4 py-8 sm:px-6 sm:py-9 lg:px-8">
      <div class="grid gap-8 md:grid-cols-[1.25fr_0.75fr_0.8fr] md:items-start">
        <div>
          <RouterLink aria-label="YU.LOG 首页" class="inline-flex rounded-control" to="/">
            <BrandMark />
          </RouterLink>
          <p class="mt-4 max-w-sm text-sm leading-6 text-text-secondary">
            记录代码，也记录成长。<br />
            一片还在持续生长的数字花园。
          </p>
        </div>

        <nav aria-label="页脚导航">
          <p class="font-mono text-[0.6875rem] uppercase tracking-[0.14em] text-text-muted">浏览</p>
          <div class="mt-3 grid grid-cols-2 gap-x-5 gap-y-2.5 md:grid-cols-1">
            <RouterLink
              v-for="item in footerNavigation"
              :key="item.to"
              class="w-fit text-sm text-text-secondary transition duration-normal hover:text-brand"
              :to="item.to"
            >
              {{ item.label }}
            </RouterLink>
          </div>
        </nav>

        <div>
          <p class="font-mono text-[0.6875rem] uppercase tracking-[0.14em] text-text-muted">联系</p>
          <div class="mt-3 flex flex-wrap gap-x-4 gap-y-2.5">
            <a
              v-if="githubUrl"
              :href="githubUrl"
              class="text-sm text-text-secondary transition duration-normal hover:text-brand"
              rel="noreferrer"
              target="_blank"
            >
              GitHub
            </a>
            <a
              v-if="email"
              :href="'mailto:' + email"
              class="text-sm text-text-secondary transition duration-normal hover:text-brand"
            >
              Email
            </a>
            <RouterLink
              v-if="!githubUrl && !email"
              class="text-sm text-text-secondary transition duration-normal hover:text-brand"
              to="/about"
            >
              联系方式
            </RouterLink>
          </div>

          <div class="mt-5 flex flex-wrap gap-2 font-mono text-[0.65rem] text-text-muted">
            <span>Vue</span><span aria-hidden="true">/</span>
            <span>Spring Boot</span><span aria-hidden="true">/</span>
            <span>Docker</span>
          </div>
        </div>
      </div>

      <div class="mt-8 flex flex-col gap-2 border-t border-border-subtle/55 pt-5 font-mono text-[0.65rem] text-text-muted sm:flex-row sm:items-center sm:justify-between">
        <p>© {{ currentYear }} Yu。系统仍在生长。</p>
        <p>为清晰、学习与长期记忆而写。</p>
      </div>
    </div>
  </footer>
</template>
