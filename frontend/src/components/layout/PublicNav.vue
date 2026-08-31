<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';

import BrandMark from '@/components/common/BrandMark.vue';
import IconButton from '@/components/common/IconButton.vue';
import ThemeToggle from '@/components/common/ThemeToggle.vue';
import { publicNavigation } from '@/mocks/navigation';

withDefaults(defineProps<{
  githubUrl?: string;
  mobileOpen?: boolean;
}>(), {
  githubUrl: '',
  mobileOpen: false,
});

const emit = defineEmits<{
  toggleMobile: [];
}>();

const isScrolled = ref(false);

function updateScrollState() {
  isScrolled.value = window.scrollY > 16;
}

onMounted(() => {
  updateScrollState();
  window.addEventListener('scroll', updateScrollState, { passive: true });
});

onUnmounted(() => {
  window.removeEventListener('scroll', updateScrollState);
});
</script>

<template>
  <header
    class="sticky top-0 z-50 border-b transition duration-normal ease-standard"
    :class="isScrolled
      ? 'border-border-subtle/72 bg-surface-elevated/82 shadow-soft backdrop-blur-elevated'
      : 'border-transparent bg-canvas/58 backdrop-blur-glass'"
  >
    <div class="mx-auto flex h-16 w-full max-w-content items-center gap-4 px-4 sm:px-6 lg:px-8">
      <RouterLink
        aria-label="YU.LOG 首页"
        class="shrink-0 rounded-control focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
        to="/"
      >
        <BrandMark />
      </RouterLink>

      <nav aria-label="主要导航" class="ml-auto hidden items-center gap-1 lg:flex">
        <RouterLink
          v-for="item in publicNavigation"
          :key="item.to"
          :active-class="item.exact ? '' : 'border-brand/22 bg-brand/8 text-brand'"
          class="rounded-control border border-transparent px-3 py-2 font-mono text-[0.72rem] font-medium tracking-[0.04em] text-text-secondary transition duration-normal hover:border-border-subtle hover:bg-surface-hover/68 hover:text-text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
          :exact-active-class="item.exact ? 'border-brand/22 bg-brand/8 text-brand' : ''"
          :to="item.to"
        >
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="ml-auto hidden items-center gap-1 lg:ml-2 lg:flex">
        <a
          v-if="githubUrl"
          :href="githubUrl"
          aria-label="访问 GitHub 主页"
          class="flex h-9 w-9 items-center justify-center rounded-control border border-transparent text-text-secondary transition duration-normal hover:border-border-subtle hover:bg-surface-hover/68 hover:text-brand focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand"
          rel="noreferrer"
          target="_blank"
          title="GitHub"
        >
          <svg aria-hidden="true" class="h-[1.125rem] w-[1.125rem]" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 .7a11.5 11.5 0 0 0-3.64 22.41c.58.1.79-.25.79-.56v-2.23c-3.22.7-3.9-1.37-3.9-1.37-.52-1.34-1.28-1.7-1.28-1.7-1.05-.72.08-.71.08-.71 1.16.08 1.77 1.2 1.77 1.2 1.03 1.77 2.7 1.26 3.36.96.1-.75.4-1.26.73-1.55-2.57-.29-5.27-1.29-5.27-5.73 0-1.27.45-2.3 1.19-3.11-.12-.29-.52-1.47.11-3.07 0 0 .97-.31 3.16 1.19a10.9 10.9 0 0 1 5.76 0c2.19-1.5 3.15-1.19 3.15-1.19.64 1.6.24 2.78.12 3.07.74.81 1.19 1.84 1.19 3.11 0 4.46-2.71 5.43-5.29 5.72.42.36.79 1.07.79 2.16v3.2c0 .31.21.67.8.56A11.5 11.5 0 0 0 12 .7Z"/>
          </svg>
        </a>
        <ThemeToggle />
      </div>

      <div class="ml-auto flex items-center gap-1 lg:hidden">
        <ThemeToggle />
        <IconButton
          :aria-controls="'mobile-navigation'"
          :aria-expanded="mobileOpen"
          :label="mobileOpen ? '关闭导航菜单' : '打开导航菜单'"
          @click="emit('toggleMobile')"
        >
          <svg v-if="!mobileOpen" aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path d="M5 7h14M5 12h14M5 17h14" stroke-linecap="round" stroke-width="1.7" />
          </svg>
          <svg v-else aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path d="m6.5 6.5 11 11m0-11-11 11" stroke-linecap="round" stroke-width="1.7" />
          </svg>
        </IconButton>
      </div>
    </div>
  </header>
</template>
