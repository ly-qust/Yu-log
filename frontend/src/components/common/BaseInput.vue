<script setup lang="ts">
import { computed, useId } from 'vue';

defineOptions({ inheritAttrs: false });

const model = defineModel<string | number>({ default: '' });
const generatedId = useId();
const props = withDefaults(defineProps<{
  id?: string;
  label?: string;
  hint?: string;
  error?: string;
  type?: string;
  disabled?: boolean;
}>(), {
  id: '',
  label: '',
  hint: '',
  error: '',
  type: 'text',
  disabled: false,
});

const controlId = computed(() => props.id || 'input-' + generatedId);
const descriptionId = computed(() => props.error || props.hint ? controlId.value + '-description' : undefined);
</script>

<template>
  <label class="grid gap-2" :for="controlId">
    <span v-if="label" class="font-mono text-xs font-medium text-text-secondary">{{ label }}</span>
    <input
      :id="controlId"
      v-model="model"
      v-bind="$attrs"
      :aria-describedby="descriptionId"
      :aria-invalid="Boolean(error)"
      class="h-11 w-full rounded-control border border-border-subtle bg-surface/72 px-3.5 text-sm text-text-primary shadow-sm outline-none transition duration-normal placeholder:text-text-muted/75 hover:border-border-active/45 focus:border-brand focus:ring-2 focus:ring-brand/18 disabled:cursor-not-allowed disabled:opacity-55"
      :class="{ 'border-danger focus:border-danger focus:ring-danger/15': error }"
      :disabled="disabled"
      :type="type"
    />
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
