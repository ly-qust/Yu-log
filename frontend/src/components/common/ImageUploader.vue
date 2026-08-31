<script setup lang="ts">
import { computed, ref, watch } from 'vue';

import { uploadFile } from '@/api/files';
import type { FileBizType } from '@/types/file';
import { getErrorMessage } from '@/utils/errors';

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    bizType?: FileBizType;
    label?: string;
  }>(),
  {
    modelValue: '',
    bizType: 'other',
    label: '上传图片',
  },
);

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const fileInput = ref<HTMLInputElement | null>(null);
const uploading = ref(false);
const errorMessage = ref('');
const previewFailed = ref(false);
const imageUrl = computed(() => props.modelValue || '');

watch(imageUrl, () => {
  previewFailed.value = false;
});

function openFilePicker() {
  fileInput.value?.click();
}

function clearImage() {
  emit('update:modelValue', '');
  errorMessage.value = '';
  previewFailed.value = false;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) {
    return;
  }

  uploading.value = true;
  errorMessage.value = '';
  try {
    const result = await uploadFile(file, props.bizType);
    emit('update:modelValue', result.url);
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '图片上传失败，请稍后重试');
  } finally {
    uploading.value = false;
    input.value = '';
  }
}
</script>

<template>
  <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4">
    <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <p class="font-mono text-xs uppercase text-cyber-muted">{{ label }}</p>
        <p class="mt-1 text-xs text-cyber-outline">支持 jpg、png、webp、gif，单文件不超过 5MB。</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <button
          class="rounded-lg border border-cyber-cyan/60 px-3 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="uploading"
          type="button"
          @click="openFilePicker"
        >
          {{ uploading ? '上传中...' : '选择图片' }}
        </button>
        <button
          v-if="imageUrl"
          class="rounded-lg border border-cyber-border px-3 py-2 font-mono text-xs text-cyber-muted transition hover:border-cyber-cyan hover:text-cyber-cyan"
          type="button"
          @click="clearImage"
        >
          清空
        </button>
      </div>
    </div>

    <input ref="fileInput" accept="image/jpeg,image/png,image/webp,image/gif" class="hidden" type="file" @change="handleFileChange" />

    <div v-if="imageUrl" class="mt-4 grid gap-3 lg:grid-cols-[220px_1fr] lg:items-start">
      <div class="overflow-hidden rounded-lg border border-cyber-border bg-cyber-base/70">
        <img
          v-if="!previewFailed"
          :src="imageUrl"
          alt="图片预览"
          class="h-36 w-full object-cover"
          @error="previewFailed = true"
        />
        <div v-else class="flex h-36 items-center justify-center px-4 text-center text-sm text-cyber-muted">
          图片预览加载失败
        </div>
      </div>
      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">图片 URL</span>
        <input
          :value="imageUrl"
          class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 font-mono text-xs text-cyber-text outline-none focus:border-cyber-cyan"
          readonly
          type="text"
        />
      </label>
    </div>

    <p v-if="errorMessage" class="mt-4 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
      {{ errorMessage }}
    </p>
  </div>
</template>
