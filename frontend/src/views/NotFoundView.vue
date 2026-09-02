<template>
  <PublicLayout>
    <GlassPanel>
      <p class="terminal-label text-sm">404 // route_not_found</p>
      <h1 class="mt-4 font-display text-3xl font-semibold">这里还没有长出内容。</h1>
      <p class="mt-3 text-cyber-muted">你访问的路径暂时没有对应页面。</p>
      <RouterLink class="mt-6 inline-block font-mono text-sm text-cyber-cyan" to="/">回到首页</RouterLink>
    </GlassPanel>
  </PublicLayout>
</template>

<script setup lang="ts">
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
</script>
