<script setup lang="ts">
withDefaults(defineProps<{
  label?: string;
  vertical?: boolean;
}>(), {
  label: '',
  vertical: false,
});
</script>

<template>
  <div
    v-if="vertical"
    aria-hidden="true"
    class="h-full min-h-5 w-px bg-border-subtle/65"
  ></div>
  <div v-else class="flex items-center gap-3" role="separator">
    <span class="h-px flex-1 bg-border-subtle/65"></span>
    <span v-if="label" class="font-mono text-[0.6875rem] uppercase tracking-[0.14em] text-text-muted">{{ label }}</span>
    <span v-if="label" class="h-px flex-1 bg-border-subtle/65"></span>
  </div>
</template>
