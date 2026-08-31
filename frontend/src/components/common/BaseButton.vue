<script setup lang="ts">
import { computed } from 'vue';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

const props = withDefaults(defineProps<{
  variant?: ButtonVariant;
  size?: ButtonSize;
  type?: 'button' | 'submit' | 'reset';
  loading?: boolean;
  disabled?: boolean;
  full?: boolean;
  iconOnly?: boolean;
}>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  loading: false,
  disabled: false,
  full: false,
  iconOnly: false,
});

const buttonClass = computed(() => [
  'inline-flex select-none items-center justify-center gap-2 rounded-control border font-mono font-semibold',
  'transition duration-normal ease-standard active:translate-y-px',
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand focus-visible:ring-offset-2 focus-visible:ring-offset-canvas',
  'disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50',
  {
    'border-brand bg-brand text-brand-contrast shadow-glow hover:border-brand-strong hover:bg-brand-strong': props.variant === 'primary',
    'border-border-subtle bg-surface-elevated/72 text-text-primary shadow-soft hover:border-border-active/60 hover:bg-surface-hover': props.variant === 'secondary',
    'border-transparent bg-transparent text-text-secondary hover:border-border-subtle hover:bg-surface-hover/72 hover:text-brand': props.variant === 'ghost',
    'border-danger/70 bg-danger/10 text-danger hover:bg-danger hover:text-brand-contrast': props.variant === 'danger',
    'h-9 px-3 text-xs': props.size === 'sm' && !props.iconOnly,
    'h-11 px-4 text-sm': props.size === 'md' && !props.iconOnly,
    'h-12 px-5 text-sm': props.size === 'lg' && !props.iconOnly,
    'h-9 w-9 p-0': props.size === 'sm' && props.iconOnly,
    'h-11 w-11 p-0': props.size === 'md' && props.iconOnly,
    'h-12 w-12 p-0': props.size === 'lg' && props.iconOnly,
    'w-full': props.full,
  },
]);
</script>

<template>
  <button
    :aria-busy="loading || undefined"
    :class="buttonClass"
    :disabled="disabled || loading"
    :type="type"
  >
    <span
      v-if="loading"
      aria-hidden="true"
      class="h-4 w-4 animate-spin rounded-full border-2 border-current border-r-transparent"
    ></span>
    <slot name="icon"></slot>
    <span v-if="!iconOnly"><slot /></span>
  </button>
</template>
