<script setup lang="ts">
withDefaults(defineProps<{
  title?: string;
  description?: string;
}>(), {
  title: '这里还没有内容',
  description: '新的内容正在生长，稍后再来看看。',
});
</script>

<template>
  <div class="rounded-panel border border-dashed border-border-subtle bg-canvas-subtle/45 px-5 py-10 text-center">
    <div class="mx-auto flex h-11 w-11 items-center justify-center rounded-full border border-brand/25 bg-brand/10 text-brand">
      <slot name="icon">
        <svg aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.7" d="M12 3v18m0-12c-2.6-2.4-5-2.5-7-1 1 3.5 3.4 5.2 7 5m0 1c2.2-2.1 4.5-2.3 7-1-1 3.5-3.3 5.2-7 5" />
        </svg>
      </slot>
    </div>
    <h3 class="mt-4 font-display text-lg font-semibold text-text-primary">{{ title }}</h3>
    <p class="mx-auto mt-2 max-w-md text-sm leading-6 text-text-muted">{{ description }}</p>
    <div v-if="$slots.action" class="mt-5"><slot name="action" /></div>
  </div>
</template>
