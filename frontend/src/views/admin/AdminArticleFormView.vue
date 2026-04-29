<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminArticle, fetchAdminArticle, updateAdminArticle } from '@/api/adminArticles';
import { fetchAdminCategories } from '@/api/adminCategories';
import { fetchAdminTags } from '@/api/adminTags';
import type { AdminCategory, AdminTag, ArticleSavePayload, ArticleStatus } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const categories = ref<AdminCategory[]>([]);
const tags = ref<AdminTag[]>([]);

const articleId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(articleId.value));
const saveButtonLabel = computed(() => {
  if (saving.value) {
    return '保存中...';
  }
  if (form.status === 'PUBLISHED') {
    return '发布';
  }
  if (form.status === 'DRAFT') {
    return '保存草稿';
  }
  return '保存';
});

const form = reactive({
  title: '',
  slug: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: '',
  tagIds: [] as string[],
  status: 'DRAFT' as ArticleStatus,
  isTop: false,
  readingTime: 1,
});

function validateForm(): string {
  if (!form.title.trim()) {
    return '请填写标题';
  }
  if (!form.slug.trim()) {
    return '请填写访问标识 slug';
  }
  if (!form.content.trim()) {
    return '请填写正文内容';
  }
  if (!form.categoryId) {
    return '请选择所属分类';
  }
  return '';
}

function toPayload(): ArticleSavePayload {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    summary: form.summary.trim(),
    content: form.content,
    coverImage: form.coverImage.trim(),
    categoryId: form.categoryId,
    tagIds: form.tagIds,
    status: form.status,
    isTop: form.isTop,
    readingTime: form.readingTime > 0 ? form.readingTime : undefined,
  };
}

async function loadOptions() {
  const [categoryData, tagData] = await Promise.all([fetchAdminCategories(), fetchAdminTags()]);
  categories.value = categoryData;
  tags.value = tagData;
}

async function loadArticle() {
  if (!articleId.value) {
    return;
  }

  const article = await fetchAdminArticle(articleId.value);
  form.title = article.title;
  form.slug = article.slug;
  form.summary = article.summary || '';
  form.content = article.content || '';
  form.coverImage = article.coverImage || '';
  form.categoryId = article.categoryId || '';
  form.tagIds = article.tags.map((tag) => tag.id);
  form.status = article.status;
  form.isTop = Boolean(article.isTop);
  form.readingTime = article.readingTime || 1;
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
    if (isEdit.value && articleId.value) {
      await updateAdminArticle(articleId.value, toPayload());
      successMessage.value = '文章已保存';
    } else {
      await createAdminArticle(toPayload());
      await router.push('/admin/articles');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await loadOptions();
    await loadArticle();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章表单加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="terminal-label text-sm">admin_articles // {{ isEdit ? 'edit' : 'new' }}</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">{{ isEdit ? '编辑文章' : '新建文章' }}</h2>
      </div>
      <RouterLink class="font-mono text-xs text-cyber-cyan hover:text-cyber-cyanBright" to="/admin/articles">
        返回文章管理
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
          <span class="font-mono text-xs uppercase text-cyber-muted">访问标识 slug *</span>
          <input v-model="form.slug" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
      </div>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">摘要</span>
        <textarea v-model="form.summary" class="mt-2 min-h-24 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <div class="grid gap-5 lg:grid-cols-3">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">所属分类 *</span>
          <select v-model="form.categoryId" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
            <option value="">请选择分类</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.name }}
            </option>
          </select>
        </label>

        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">文章状态</span>
          <select v-model="form.status" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
            <option value="DRAFT">草稿</option>
            <option value="PUBLISHED">已发布</option>
            <option value="HIDDEN">已隐藏</option>
          </select>
        </label>

        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">阅读时间</span>
          <input v-model.number="form.readingTime" min="1" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="number" />
        </label>
      </div>

      <div class="grid gap-5 lg:grid-cols-[1fr_2fr]">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">文章标签</span>
          <select v-model="form.tagIds" multiple class="mt-2 h-44 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
            <option v-for="tag in tags" :key="tag.id" :value="tag.id">
              {{ tag.name }}
            </option>
          </select>
        </label>

        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">封面图</span>
          <input v-model="form.coverImage" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
          <label class="mt-4 flex items-center gap-3 text-sm text-cyber-muted">
            <input v-model="form.isTop" class="h-4 w-4 accent-cyber-cyan" type="checkbox" />
            是否置顶
          </label>
        </label>
      </div>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">正文内容 *</span>
        <textarea v-model="form.content" class="mt-2 min-h-[360px] w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 font-mono text-sm leading-7 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">
        {{ successMessage }}
      </p>
      <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
        {{ errorMessage }}
      </p>

      <div class="flex flex-wrap gap-3">
        <button
          class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="saving"
          type="submit"
        >
          {{ saveButtonLabel }}
        </button>
        <RouterLink class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" to="/admin/articles">
          取消
        </RouterLink>
      </div>
    </form>
  </section>
</template>
