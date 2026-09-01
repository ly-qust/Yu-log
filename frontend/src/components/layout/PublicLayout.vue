<script setup lang="ts">
import { nextTick, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { useSiteStore } from '@/stores/site';
import CommandPalette from '@/components/common/CommandPalette.vue';
import MobileNavDrawer from './MobileNavDrawer.vue';
import PublicFooter from './PublicFooter.vue';
import PublicNav from './PublicNav.vue';

const route = useRoute();
const siteStore = useSiteStore();
const mobileOpen = ref(false);
const commandPalette = ref<InstanceType<typeof CommandPalette> | null>(null);

function openSearch() {
  mobileOpen.value = false;
  void nextTick(() => commandPalette.value?.open());
}

watch(
  () => route.fullPath,
  () => {
    mobileOpen.value = false;
  },
);

onMounted(() => {
  void siteStore.loadPublicProfile();
});
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <a
      class="fixed left-4 top-3 z-[90] -translate-y-20 rounded-control bg-brand px-4 py-2 font-mono text-xs font-semibold text-brand-contrast shadow-glow transition focus:translate-y-0"
      href="#main-content"
    >
      跳到主要内容
    </a>

    <PublicNav
      :github-url="siteStore.githubUrl"
      :mobile-open="mobileOpen"
      @open-search="openSearch"
      @toggle-mobile="mobileOpen = !mobileOpen"
    />
    <MobileNavDrawer
      :github-url="siteStore.githubUrl"
      :open="mobileOpen"
      @close="mobileOpen = false"
      @open-search="openSearch"
    />

    <main
      id="main-content"
      class="public-main mx-auto w-full max-w-content flex-1 scroll-mt-20 px-4 py-7 sm:px-6 sm:py-10 lg:px-8 lg:py-12"
      tabindex="-1"
    >
      <slot />
    </main>

    <PublicFooter
      :email="siteStore.email"
      :github-url="siteStore.githubUrl"
    />
    <CommandPalette ref="commandPalette" />
  </div>
</template>
