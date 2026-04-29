<script setup lang="ts">
import { publicNavigation } from '@/mocks/navigation';
</script>

<template>
  <header class="sticky top-0 z-20 border-b border-cyber-border/40 bg-cyber-bg/75 backdrop-blur-glass">
    <nav class="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
      <RouterLink to="/" class="font-display text-lg font-bold text-cyber-text">YU.LOG</RouterLink>
      <div class="hidden items-center gap-4 md:flex">
        <RouterLink
          v-for="item in publicNavigation"
          :key="item.to"
          :to="item.to"
          class="font-mono text-xs uppercase text-cyber-muted transition hover:text-cyber-cyan"
        >
          {{ item.label }}
        </RouterLink>
      </div>
    </nav>
  </header>
</template>
