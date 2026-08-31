<script setup lang="ts">
import { computed, useId } from 'vue';

defineOptions({ inheritAttrs: false });

const model = defineModel<string>({ default: '' });
const generatedId = useId();
const props = withDefaults(defineProps<{
  id?: string;
  label?: string;
  hint?: string;
  error?: string;
  rows?: number;
  disabled?: boolean;
}>(), {
  id: '',
  label: '',
  hint: '',
  error: '',
  rows: 5,
  disabled: false,
});

const controlId = computed(() => props.id || 'textarea-' + generatedId);
const descriptionId = computed(() => props.error || props.hint ? controlId.value + '-description' : undefined);
</script>

<template>
  <label class="grid gap-2" :for="controlId">
    <span v-if="label" class="font-mono text-xs font-medium text-text-secondary">{{ label }}</span>
    <textarea
      :id="controlId"
      v-model="model"
      v-bind="$attrs"
      :aria-describedby="descriptionId"
      :aria-invalid="Boolean(error)"
      class="w-full resize-y rounded-control border border-border-subtle bg-surface/72 px-3.5 py-3 text-sm leading-6 text-text-primary shadow-sm outline-none transition duration-normal placeholder:text-text-muted/75 hover:border-border-active/45 focus:border-brand focus:ring-2 focus:ring-brand/18 disabled:cursor-not-allowed disabled:opacity-55"
      :class="{ 'border-danger focus:border-danger focus:ring-danger/15': error }"
      :disabled="disabled"
      :rows="rows"
    ></textarea>
    <span
      v-if="error || hint"
      :id="descriptionId"
      class="text-xs"
      :class="error ? 'text-danger' : 'text-text-muted'"
    >
      {{ error || hint }}
    </span>
  </label>
</template>
