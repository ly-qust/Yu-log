<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { createAdminTag, deleteAdminTag, fetchAdminTags, updateAdminTag } from '@/api/adminTags';
import type { AdminTag, TagSavePayload } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';

const tags = ref<AdminTag[]>([]);
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const editingId = ref('');

const form = reactive({
  name: '',
  slug: '',
  color: '#38debb',
  description: '',
  status: 'ENABLED' as 'ENABLED' | 'DISABLED',
});

function resetForm() {
  editingId.value = '';
  form.name = '';
  form.slug = '';
  form.color = '#38debb';
  form.description = '';
  form.status = 'ENABLED';
}

function editTag(tag: AdminTag) {
  editingId.value = tag.id;
  form.name = tag.name;
  form.slug = tag.slug;
  form.color = tag.color || '#38debb';
  form.description = tag.description || '';
  form.status = tag.status;
}

function payload(): TagSavePayload {
  return {
    name: form.name.trim(),
    slug: form.slug.trim(),
    color: form.color.trim(),
    description: form.description.trim(),
    status: form.status,
  };
}

async function loadTags() {
  loading.value = true;
  errorMessage.value = '';
  try {
    tags.value = await fetchAdminTags();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '标签加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submit() {
  errorMessage.value = '';
  successMessage.value = '';
  if (!form.name.trim() || !form.slug.trim()) {
    errorMessage.value = '请填写标签名称和标签标识';
    return;
  }

  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminTag(editingId.value, payload());
      successMessage.value = '标签已更新';
    } else {
      await createAdminTag(payload());
      successMessage.value = '标签已创建';
    }
    resetForm();
    await loadTags();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '标签保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

async function removeTag(tag: AdminTag) {
  if (!window.confirm(`确认删除标签「${tag.name}」吗？`)) {
    return;
  }

  errorMessage.value = '';
  successMessage.value = '';
  try {
    await deleteAdminTag(tag.id);
    successMessage.value = '标签已删除';
    await loadTags();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '标签删除失败，请稍后重试');
  }
}

onMounted(loadTags);
</script>

<template>
  <section class="grid gap-5 xl:grid-cols-[380px_1fr]">
    <form class="glass-panel rounded-glass p-6" @submit.prevent="submit">
      <p class="terminal-label text-sm">admin_tags // {{ editingId ? 'edit' : 'new' }}</p>
      <h2 class="mt-3 font-display text-3xl font-semibold">标签管理</h2>

      <div class="mt-6 grid gap-4">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签名称 *</span>
          <input v-model="form.name" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签标识 *</span>
          <input v-model="form.slug" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签颜色</span>
          <div class="mt-2 flex gap-3">
            <input v-model="form.color" class="h-12 w-16 rounded-lg border border-cyber-border bg-cyber-base" type="color" />
            <input v-model="form.color" class="min-w-0 flex-1 rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
          </div>
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签描述</span>
          <textarea v-model="form.description" class="mt-2 min-h-24 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">状态</span>
          <select v-model="form.status" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
            <option value="ENABLED">启用</option>
            <option value="DISABLED">停用</option>
          </select>
        </label>
      </div>

      <p v-if="errorMessage" class="mt-5 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </p>
      <p v-if="successMessage" class="mt-5 rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
        {{ successMessage }}
      </p>

      <div class="mt-6 flex flex-wrap gap-3">
        <button class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base hover:bg-cyber-cyan disabled:opacity-50" :disabled="saving" type="submit">
          {{ saving ? '保存中...' : editingId ? '保存修改' : '新增标签' }}
        </button>
        <button class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" type="button" @click="resetForm">
          重置
        </button>
      </div>
    </form>

    <div class="glass-panel overflow-x-auto rounded-glass">
      <table class="min-w-[760px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">标签名称</th>
            <th class="px-4 py-3">标签标识</th>
            <th class="px-4 py-3">标签颜色</th>
            <th class="px-4 py-3">文章数量</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="6">标签加载中...</td>
          </tr>
          <tr v-for="tag in tags" v-else :key="tag.id" class="border-t border-cyber-border/60">
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ tag.name }}</p>
              <p class="mt-1 text-xs text-cyber-muted">{{ tag.description || '-' }}</p>
            </td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-muted">{{ tag.slug }}</td>
            <td class="px-4 py-4">
              <div class="flex items-center gap-2">
                <span class="h-4 w-4 rounded-full border border-cyber-border" :style="{ backgroundColor: tag.color || '#38debb' }"></span>
                <span class="font-mono text-xs text-cyber-muted">{{ tag.color || '-' }}</span>
              </div>
            </td>
            <td class="px-4 py-4 text-cyber-muted">{{ tag.articleCount }}</td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-cyan">{{ tag.status === 'ENABLED' ? '启用' : '停用' }}</td>
            <td class="px-4 py-4">
              <div class="flex gap-2">
                <button class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" type="button" @click="editTag(tag)">
                  编辑
                </button>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeTag(tag)">
                  删除
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
