<template>
  <PublicLayout>
    <GlassPanel>
      <p class="terminal-label text-sm">guest_terminal // placeholder</p>
      <h1 class="mt-4 font-display text-3xl font-semibold">访客终端 // 留言板</h1>
      <p class="mt-3 text-cyber-muted">Message submission stays disabled until the interaction phase.</p>
    </GlassPanel>
  </PublicLayout>
</template>

<script setup lang="ts">
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
</script>
