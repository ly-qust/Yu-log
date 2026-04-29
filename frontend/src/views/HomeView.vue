<template>
  <PublicLayout>
    <GlassPanel>
      <p class="terminal-label text-sm">phase_01 // frontend skeleton</p>
      <h1 class="mt-4 font-display text-4xl font-bold text-cyber-text">你好，我是 Yu。</h1>
      <p class="mt-4 max-w-2xl text-cyber-muted">
        Stitch UI will be migrated into Vue components in later phases. This page is a routing placeholder.
      </p>
    </GlassPanel>
  </PublicLayout>
</template>

<script setup lang="ts">
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
</script>
