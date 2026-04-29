<template>
  <PublicLayout>
    <GlassPanel>
      <p class="terminal-label text-sm">404 // route_not_found</p>
      <h1 class="mt-4 font-display text-3xl font-semibold">Page Not Found</h1>
      <RouterLink class="mt-6 inline-block font-mono text-sm text-cyber-cyan" to="/">Return Home</RouterLink>
    </GlassPanel>
  </PublicLayout>
</template>

<script setup lang="ts">
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
</script>
