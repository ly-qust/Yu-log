<script setup lang="ts">
withDefaults(defineProps<{
  compact?: boolean;
}>(), {
  compact: false,
});
</script>

<template>
  <span class="inline-flex items-center gap-2.5">
    <span class="relative flex h-9 w-9 items-center justify-center rounded-control border border-brand/28 bg-brand/8 text-brand shadow-glow">
      <svg aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path d="M4.5 5.5 12 13l7.5-7.5M12 13v6" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.8" />
        <circle cx="4.5" cy="5.5" r="1.35" fill="currentColor" stroke="none" />
        <circle cx="19.5" cy="5.5" r="1.35" fill="currentColor" stroke="none" />
        <circle cx="12" cy="19" r="1.35" fill="currentColor" stroke="none" />
      </svg>
      <span aria-hidden="true" class="absolute -right-0.5 -top-0.5 h-1.5 w-1.5 rounded-full bg-success shadow-[0_0_10px_rgb(var(--color-success)/0.7)]"></span>
    </span>
    <span v-if="!compact" class="flex items-baseline font-display text-base font-bold tracking-[-0.02em] text-text-primary">
      YU<span class="text-brand">.</span>LOG
    </span>
  </span>
</template>
