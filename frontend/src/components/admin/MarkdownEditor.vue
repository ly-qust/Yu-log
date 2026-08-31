<script setup lang="ts">
import { nextTick, ref } from 'vue';

import { uploadFile } from '@/api/files';
import MarkdownRenderer from '@/components/article/MarkdownRenderer.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import type { FileBizType } from '@/types/file';
import { getErrorMessage } from '@/utils/errors';

type EditorMode = 'editor' | 'split' | 'preview';

const props = withDefaults(defineProps<{
  modelValue: string;
  placeholder?: string;
  disabled?: boolean;
  uploadBizType?: FileBizType;
}>(), {
  placeholder: '开始写 Markdown…',
  disabled: false,
  uploadBizType: 'other',
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
  save: [];
}>();

const textarea = ref<HTMLTextAreaElement | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const mode = ref<EditorMode>('split');
const uploading = ref(false);
const uploadError = ref('');

function update(value: string) {
  emit('update:modelValue', value);
}

function insertAround(before: string, after = before, fallback = 'text') {
  const element = textarea.value;
  if (!element) return;
  const start = element.selectionStart;
  const end = element.selectionEnd;
  const selected = props.modelValue.slice(start, end) || fallback;
  update(props.modelValue.slice(0, start) + before + selected + after + props.modelValue.slice(end));
  nextTick(() => {
    element.focus();
    const nextStart = start + before.length;
    element.setSelectionRange(nextStart, nextStart + selected.length);
  });
}

function insertHeading() {
  const element = textarea.value;
  if (!element) return;
  const start = element.selectionStart;
  const lineStart = props.modelValue.lastIndexOf('\n', start - 1) + 1;
  update(props.modelValue.slice(0, lineStart) + '## ' + props.modelValue.slice(lineStart));
  nextTick(() => {
    element.focus();
    element.setSelectionRange(start + 3, start + 3);
  });
}

function handleKeydown(event: KeyboardEvent) {
  if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
    event.preventDefault();
    emit('save');
    return;
  }
  if (event.key !== 'Tab') return;
  event.preventDefault();
  const element = textarea.value;
  if (!element) return;
  const start = element.selectionStart;
  update(props.modelValue.slice(0, start) + '  ' + props.modelValue.slice(element.selectionEnd));
  nextTick(() => element.setSelectionRange(start + 2, start + 2));
}

function openUpload() {
  fileInput.value?.click();
}

async function handleUpload(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  uploading.value = true;
  uploadError.value = '';
  try {
    const result = await uploadFile(file, props.uploadBizType);
    const element = textarea.value;
    const position = element?.selectionStart ?? props.modelValue.length;
    const alt = file.name.replace(/\.[^.]+$/, '') || 'image';
    const markdown = `![${alt}](${result.url})`;
    update(props.modelValue.slice(0, position) + markdown + props.modelValue.slice(element?.selectionEnd ?? position));
    await nextTick();
    element?.focus();
    const nextPosition = position + markdown.length;
    element?.setSelectionRange(nextPosition, nextPosition);
  } catch (error) {
    uploadError.value = getErrorMessage(error, '正文图片上传失败，请稍后重试');
  } finally {
    uploading.value = false;
    input.value = '';
  }
}
</script>

<template>
  <section class="admin-editor" aria-label="Markdown 编辑器">
    <div class="admin-editor__toolbar">
      <div class="admin-editor__tools" role="toolbar" aria-label="Markdown 快捷工具">
        <button type="button" title="插入二级标题" @click="insertHeading">H2</button>
        <button type="button" title="加粗" @click="insertAround('**', '**', '重点')"><strong>B</strong></button>
        <button type="button" title="链接" @click="insertAround('[', '](https://)', '链接文字')">↗</button>
        <button type="button" title="代码" @click="insertAround('`', '`', 'code')">&lt;/&gt;</button>
        <button type="button" :disabled="uploading || disabled" title="上传正文图片" @click="openUpload">{{ uploading ? '上传中' : '图片' }}</button>
        <input ref="fileInput" class="sr-only" type="file" accept="image/jpeg,image/png,image/webp,image/gif" @change="handleUpload" />
      </div>
      <div class="admin-editor__modes" role="tablist" aria-label="编辑预览模式">
        <button v-for="item in [['editor', 'Editor'], ['split', 'Split'], ['preview', 'Preview']] as const" :key="item[0]" type="button" role="tab" :aria-selected="mode === item[0]" :class="{ 'is-active': mode === item[0] }" @click="mode = item[0]">
          {{ item[1] }}
        </button>
      </div>
    </div>
    <p v-if="uploadError" class="admin-editor__error" role="alert">{{ uploadError }}</p>
    <div class="admin-editor__workspace" :class="`admin-editor__workspace--${mode}`">
      <label v-if="mode !== 'preview'" class="admin-editor__input-wrap">
        <span class="sr-only">Markdown 正文</span>
        <textarea
          ref="textarea"
          :value="modelValue"
          :placeholder="placeholder"
          :disabled="disabled"
          spellcheck="false"
          @input="update(($event.target as HTMLTextAreaElement).value)"
          @keydown="handleKeydown"
        ></textarea>
      </label>
      <div v-if="mode !== 'editor'" class="admin-editor__preview">
        <div class="admin-editor__preview-label">Rendered preview</div>
        <MarkdownRenderer :content="modelValue" />
      </div>
    </div>
    <div class="admin-editor__footer">
      <span>Markdown · sanitized · shared public renderer</span>
      <span>Tab = indent · Ctrl/Cmd + S = save draft</span>
    </div>
  </section>
</template>
