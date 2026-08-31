<script setup lang="ts">
withDefaults(defineProps<{
  lines?: number;
  compact?: boolean;
}>(), {
  lines: 3,
  compact: false,
});
</script>

<template>
  <div aria-busy="true" aria-label="内容加载中" class="animate-pulse" role="status">
    <div v-if="!compact" class="h-3 w-24 rounded-full bg-border-subtle/55"></div>
    <div class="mt-4 grid gap-3">
      <div
        v-for="line in lines"
        :key="line"
        class="h-3 rounded-full bg-border-subtle/45"
        :class="line === lines ? 'w-2/3' : 'w-full'"
      ></div>
    </div>
    <span class="sr-only">内容加载中</span>
  </div>
</template>
