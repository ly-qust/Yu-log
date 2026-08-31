<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
  as?: string;
  variant?: 'glass' | 'surface' | 'elevated';
  interactive?: boolean;
  padded?: boolean;
}>(), {
  as: 'section',
  variant: 'glass',
  interactive: false,
  padded: true,
});

const panelClass = computed(() => [
  'rounded-glass',
  {
    'glass-panel': props.variant === 'glass' || props.variant === 'elevated',
    'border border-border-subtle bg-surface shadow-soft': props.variant === 'surface',
    'shadow-elevated backdrop-blur-elevated': props.variant === 'elevated',
    'interactive-surface': props.interactive,
    'p-5 sm:p-6': props.padded,
  },
]);
</script>

<template>
  <component :is="as" :class="panelClass">
    <slot />
  </component>
</template>
