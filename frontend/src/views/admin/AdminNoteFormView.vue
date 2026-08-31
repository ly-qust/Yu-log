<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminNote, fetchAdminNote, updateAdminNote } from '@/api/adminNotes';
import type { NoteSavePayload } from '@/types/note';
import { getErrorMessage } from '@/utils/errors';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const noteId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(noteId.value));

const form = reactive({
  title: '',
  slug: '',
  summary: '',
  content: '',
  topic: '',
  tagsText: '',
  isPublic: true,
  sortOrder: 0,
});

function splitText(value: string): string[] {
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function validateForm(): string {
  if (!form.title.trim()) {
    return '请填写标题';
  }
  if (!form.slug.trim()) {
    return '请填写 slug';
  }
  if (!form.content.trim()) {
    return '请填写内容';
  }
  return '';
}

function toPayload(): NoteSavePayload {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    summary: form.summary.trim(),
    content: form.content,
    topic: form.topic.trim(),
    tags: splitText(form.tagsText),
    isPublic: form.isPublic,
    sortOrder: form.sortOrder || 0,
  };
}

async function loadNote() {
  if (!noteId.value) {
    return;
  }

  const note = await fetchAdminNote(noteId.value);
  form.title = note.title;
  form.slug = note.slug;
  form.summary = note.summary || '';
  form.content = note.content || '';
  form.topic = note.topic || '';
  form.tagsText = note.tags.join(', ');
  form.isPublic = note.isPublic !== false;
  form.sortOrder = note.sortOrder || 0;
}

async function submit() {
  const validationMessage = validateForm();
  errorMessage.value = validationMessage;
  successMessage.value = '';
  if (validationMessage) {
    return;
  }

  saving.value = true;
  try {
    if (isEdit.value && noteId.value) {
      await updateAdminNote(noteId.value, toPayload());
      successMessage.value = '笔记已保存';
    } else {
      await createAdminNote(toPayload());
      await router.push('/admin/notes');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '笔记保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await loadNote();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '笔记表单加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="terminal-label text-sm">admin_notes // {{ isEdit ? 'edit' : 'new' }}</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">{{ isEdit ? '编辑笔记' : '新建笔记' }}</h2>
      </div>
      <RouterLink class="font-mono text-xs text-cyber-cyan hover:text-cyber-cyanBright" to="/admin/notes">
        返回笔记管理
      </RouterLink>
    </div>

    <p v-if="loading" class="mt-8 font-mono text-sm text-cyber-cyan">表单加载中...</p>

    <form v-else class="mt-8 grid gap-5" @submit.prevent="submit">
      <div class="grid gap-5 lg:grid-cols-2">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标题 *</span>
          <input v-model="form.title" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">slug *</span>
          <input v-model="form.slug" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
      </div>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">摘要</span>
        <textarea v-model="form.summary" class="mt-2 min-h-24 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <div class="grid gap-5 lg:grid-cols-3">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">主题</span>
          <input v-model="form.topic" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签</span>
          <input v-model="form.tagsText" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" placeholder="Linux, MySQL, Redis" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">排序</span>
          <input v-model.number="form.sortOrder" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="number" />
        </label>
      </div>

      <label class="flex items-center gap-3 text-sm text-cyber-muted">
        <input v-model="form.isPublic" class="h-4 w-4 accent-cyber-cyan" type="checkbox" />
        是否公开
      </label>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">内容 *</span>
        <textarea v-model="form.content" class="mt-2 min-h-[360px] w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 font-mono text-sm leading-7 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
      <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

      <div class="flex flex-wrap gap-3">
        <button class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50" :disabled="saving" type="submit">
          {{ saving ? '保存中...' : '保存笔记' }}
        </button>
        <RouterLink class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" to="/admin/notes">
          取消
        </RouterLink>
      </div>
    </form>
  </section>
</template>
