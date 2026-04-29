<template>
  <PublicLayout>
    <GlassPanel>
      <p class="terminal-label text-sm">article // {{ route.params.slug }}</p>
      <h1 class="mt-4 font-display text-3xl font-semibold">Article Detail</h1>
      <p class="mt-3 text-cyber-muted">Markdown rendering and comments are reserved for later phases.</p>
    </GlassPanel>
  </PublicLayout>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router';

import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';

const route = useRoute();
</script>
