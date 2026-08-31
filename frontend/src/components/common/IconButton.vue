<script setup lang="ts">
import BaseButton, { type ButtonSize, type ButtonVariant } from './BaseButton.vue';

defineOptions({ inheritAttrs: false });

withDefaults(defineProps<{
  label: string;
  title?: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  disabled?: boolean;
}>(), {
  title: '',
  variant: 'ghost',
  size: 'md',
  disabled: false,
});
</script>

<template>
  <BaseButton
    v-bind="$attrs"
    :aria-label="label"
    :disabled="disabled"
    icon-only
    :size="size"
    :title="title || label"
    :variant="variant"
  >
    <template #icon><slot /></template>
  </BaseButton>
</template>
