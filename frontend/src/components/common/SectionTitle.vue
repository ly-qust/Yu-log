<script setup lang="ts">
withDefaults(defineProps<{
  eyebrow?: string;
  title: string;
  description?: string;
  align?: 'left' | 'center';
  level?: 1 | 2 | 3;
}>(), {
  eyebrow: '',
  description: '',
  align: 'left',
  level: 2,
});
</script>

<template>
  <div :class="align === 'center' ? 'mx-auto max-w-2xl text-center' : 'max-w-2xl'">
    <p v-if="eyebrow" class="terminal-label font-mono text-xs uppercase tracking-[0.14em]">{{ eyebrow }}</p>
    <component
      :is="'h' + level"
      class="mt-2 font-display text-h2 text-text-primary"
    >
      {{ title }}
    </component>
    <p v-if="description" class="mt-3 text-sm leading-7 text-text-secondary sm:text-base">
      {{ description }}
    </p>
    <div v-if="$slots.action" class="mt-4"><slot name="action" /></div>
  </div>
</template>
