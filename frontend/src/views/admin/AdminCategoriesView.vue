<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import {
  createAdminCategory,
  deleteAdminCategory,
  fetchAdminCategories,
  updateAdminCategory,
} from '@/api/adminCategories';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { AdminCategory, CategorySavePayload } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';

const categories = ref<AdminCategory[]>([]);
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const editingId = ref('');

const form = reactive({
  name: '',
  slug: '',
  description: '',
  sortOrder: 0,
  status: 'ENABLED' as 'ENABLED' | 'DISABLED',
});

function resetForm() {
  editingId.value = '';
  form.name = '';
  form.slug = '';
  form.description = '';
  form.sortOrder = 0;
  form.status = 'ENABLED';
}

function editCategory(category: AdminCategory) {
  editingId.value = category.id;
  form.name = category.name;
  form.slug = category.slug;
  form.description = category.description || '';
  form.sortOrder = category.sortOrder || 0;
  form.status = category.status;
}

function payload(): CategorySavePayload {
  return {
    bizType: 'ARTICLE',
    name: form.name.trim(),
    slug: form.slug.trim(),
    description: form.description.trim(),
    sortOrder: form.sortOrder,
    status: form.status,
  };
}

async function loadCategories() {
  loading.value = true;
  errorMessage.value = '';
  try {
    categories.value = await fetchAdminCategories();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '分类加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submit() {
  errorMessage.value = '';
  successMessage.value = '';
  if (!form.name.trim() || !form.slug.trim()) {
    errorMessage.value = '请填写分类名称和分类标识';
    return;
  }

  saving.value = true;
  try {
    if (editingId.value) {
      await updateAdminCategory(editingId.value, payload());
      successMessage.value = '分类已更新';
      feedback.success('分类已更新');
    } else {
      await createAdminCategory(payload());
      successMessage.value = '分类已创建';
      feedback.success('分类已创建');
    }
    resetForm();
    await loadCategories();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '分类保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

async function removeCategory(category: AdminCategory) {
  const confirmed = await feedback.confirm({
    title: '删除分类',
    message: `确定要删除「${category.name}」吗？此操作可能影响文章归类。`,
    confirmLabel: '删除分类',
    danger: true,
  });
  if (!confirmed) {
    return;
  }

  errorMessage.value = '';
  successMessage.value = '';
  try {
    await deleteAdminCategory(category.id);
    successMessage.value = '分类已删除';
    feedback.success('分类已删除');
    await loadCategories();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '分类删除失败，请稍后重试');
    feedback.error(errorMessage.value);
  }
}

onMounted(loadCategories);
</script>

<template>
  <section class="grid gap-5 xl:grid-cols-[380px_1fr]">
    <form class="glass-panel rounded-glass p-6" @submit.prevent="submit">
      <p class="terminal-label text-sm">分类 // {{ editingId ? '编辑' : '新建' }}</p>
      <h2 class="mt-3 font-display text-3xl font-semibold">分类管理</h2>

      <div class="mt-6 grid gap-4">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">分类名称 *</span>
          <input v-model="form.name" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">分类标识 *</span>
          <input v-model="form.slug" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">分类描述</span>
          <textarea v-model="form.description" class="mt-2 min-h-24 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
        </label>
        <div class="grid grid-cols-2 gap-3">
          <label class="block">
            <span class="font-mono text-xs uppercase text-cyber-muted">排序</span>
            <input v-model.number="form.sortOrder" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="number" />
          </label>
          <label class="block">
            <span class="font-mono text-xs uppercase text-cyber-muted">状态</span>
            <select v-model="form.status" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
              <option value="ENABLED">启用</option>
              <option value="DISABLED">停用</option>
            </select>
          </label>
        </div>
      </div>

      <p v-if="errorMessage" class="mt-5 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </p>
      <p v-if="successMessage" class="mt-5 rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
        {{ successMessage }}
      </p>

      <div class="mt-6 flex flex-wrap gap-3">
        <button class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base hover:bg-cyber-cyan disabled:opacity-50" :disabled="saving" type="submit">
          {{ saving ? '保存中...' : editingId ? '保存修改' : '新增分类' }}
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
            <th class="px-4 py-3">分类名称</th>
            <th class="px-4 py-3">分类标识</th>
            <th class="px-4 py-3">文章数量</th>
            <th class="px-4 py-3">排序</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="6">分类加载中...</td>
          </tr>
          <tr v-for="category in categories" v-else :key="category.id" class="border-t border-cyber-border/60">
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ category.name }}</p>
              <p class="mt-1 text-xs text-cyber-muted">{{ category.description || '-' }}</p>
            </td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-muted">{{ category.slug }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ category.articleCount }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ category.sortOrder }}</td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-cyan">{{ category.status === 'ENABLED' ? '启用' : '停用' }}</td>
            <td class="px-4 py-4">
              <div class="flex gap-2">
                <button class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" type="button" @click="editCategory(category)">
                  编辑
                </button>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeCategory(category)">
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
