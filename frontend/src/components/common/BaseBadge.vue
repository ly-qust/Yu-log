<script setup lang="ts">
import { computed } from 'vue';

type BadgeVariant = 'brand' | 'accent' | 'success' | 'warning' | 'danger' | 'neutral';

const props = withDefaults(defineProps<{
  variant?: BadgeVariant;
  dot?: boolean;
}>(), {
  variant: 'neutral',
  dot: false,
});

const badgeClass = computed(() => ({
  'border-brand/35 bg-brand/10 text-brand': props.variant === 'brand',
  'border-accent/35 bg-accent/10 text-accent': props.variant === 'accent',
  'border-success/35 bg-success/10 text-success': props.variant === 'success',
  'border-warning/35 bg-warning/10 text-warning': props.variant === 'warning',
  'border-danger/35 bg-danger/10 text-danger': props.variant === 'danger',
  'border-border-subtle bg-canvas-subtle/65 text-text-secondary': props.variant === 'neutral',
}));
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 font-mono text-[0.6875rem] font-medium leading-none"
    :class="badgeClass"
  >
    <span v-if="dot" aria-hidden="true" class="h-1.5 w-1.5 rounded-full bg-current"></span>
    <slot />
  </span>
</template>
