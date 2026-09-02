<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminArticle, fetchAdminArticle, updateAdminArticle } from '@/api/adminArticles';
import { fetchAdminCategories } from '@/api/adminCategories';
import { fetchAdminTags } from '@/api/adminTags';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import MarkdownEditor from '@/components/admin/MarkdownEditor.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseSelect from '@/components/common/BaseSelect.vue';
import BaseTextarea from '@/components/common/BaseTextarea.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard';
import type { AdminCategory, AdminTag, ArticleSavePayload, ArticleStatus } from '@/types/content';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import { discardLocalDraft, formatLocalDraftTime, readLocalDraft, type LocalDraftRecord, writeLocalDraft } from '@/utils/localDraft';
import { getErrorMessage } from '@/utils/errors';

interface ArticleFormState {
  title: string;
  slug: string;
  summary: string;
  content: string;
  coverImage: string;
  categoryId: string;
  tagIds: string[];
  status: ArticleStatus;
  isTop: boolean;
  readingTime: number;
}

const route = useRoute();
const router = useRouter();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const categories = ref<AdminCategory[]>([]);
const tags = ref<AdminTag[]>([]);
const localDraftSavedAt = ref<number | null>(null);
const recoveryDraft = ref<LocalDraftRecord<ArticleFormState> | null>(null);
const ready = ref(false);

const articleId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(articleId.value));
const draftKey = computed(() => `article-${articleId.value || 'new'}`);

const form = reactive<ArticleFormState>({
  title: '',
  slug: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: '',
  tagIds: [],
  status: 'DRAFT',
  isTop: false,
  readingTime: 1,
});

function snapshot() {
  return JSON.stringify(formData());
}

function formData(): ArticleFormState {
  return { ...form, tagIds: [...form.tagIds] };
}

const guard = useUnsavedChangesGuard(snapshot, { label: '文章', isSaving: () => saving.value });

watch(
  form,
  () => {
    if (!ready.value) return;
    const record = writeLocalDraft(draftKey.value, formData());
    localDraftSavedAt.value = record.savedAt;
  },
  { deep: true },
);

function applyForm(data: ArticleFormState) {
  form.title = data.title || '';
  form.slug = data.slug || '';
  form.summary = data.summary || '';
  form.content = data.content || '';
  form.coverImage = data.coverImage || '';
  form.categoryId = data.categoryId || '';
  form.tagIds = Array.isArray(data.tagIds) ? [...data.tagIds] : [];
  form.status = data.status || 'DRAFT';
  form.isTop = Boolean(data.isTop);
  form.readingTime = Number(data.readingTime) > 0 ? Number(data.readingTime) : 1;
}

function validateForm(): string {
  if (!form.title.trim()) return '请填写文章标题';
  if (!form.slug.trim()) return '请填写访问标识 slug';
  if (!form.content.trim()) return '请填写正文内容';
  if (!form.categoryId) return '请选择所属分类';
  return '';
}

function toPayload(status: ArticleStatus): ArticleSavePayload {
  return {
    title: form.title.trim(),
    slug: form.slug.trim(),
    summary: form.summary.trim(),
    content: form.content,
    coverImage: form.coverImage.trim(),
    categoryId: form.categoryId,
    tagIds: [...form.tagIds],
    status,
    isTop: form.isTop,
    readingTime: form.readingTime > 0 ? form.readingTime : undefined,
  };
}

async function loadEditor() {
  loading.value = true;
  errorMessage.value = '';
  ready.value = false;
  try {
    const optionPromise = Promise.all([fetchAdminCategories(), fetchAdminTags()]);
    const articlePromise = articleId.value ? fetchAdminArticle(articleId.value) : Promise.resolve(null);
    const [options, article] = await Promise.all([optionPromise, articlePromise]);
    categories.value = options[0];
    tags.value = options[1];

    let serverUpdatedAt: number | undefined;
    if (article) {
      applyForm({
        title: article.title,
        slug: article.slug,
        summary: article.summary || '',
        content: article.content || '',
        coverImage: article.coverImage || '',
        categoryId: article.categoryId || '',
        tagIds: article.tags.map((tag) => tag.id),
        status: article.status,
        isTop: Boolean(article.isTop),
        readingTime: article.readingTime || 1,
      });
      serverUpdatedAt = article.updatedAt ? Date.parse(article.updatedAt) : undefined;
    }

    const stored = readLocalDraft<ArticleFormState>(draftKey.value);
    if (stored && (isEdit.value ? stored.savedAt > (serverUpdatedAt || 0) : Boolean(stored.data.title || stored.data.content))) {
      recoveryDraft.value = stored;
      localDraftSavedAt.value = stored.savedAt;
    } else if (stored && isEdit.value) {
      discardLocalDraft(draftKey.value);
    }
    guard.markClean();
    ready.value = true;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章编辑器加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function restoreDraft() {
  if (!recoveryDraft.value) return;
  applyForm(recoveryDraft.value.data);
  localDraftSavedAt.value = recoveryDraft.value.savedAt;
  recoveryDraft.value = null;
  feedback.info('本地草稿已恢复，保存后才会写入服务器。');
}

function discardDraft() {
  clearDraft(true);
}

function clearDraft(showFeedback: boolean) {
  discardLocalDraft(draftKey.value);
  recoveryDraft.value = null;
  localDraftSavedAt.value = null;
  if (showFeedback) feedback.info('本地草稿已丢弃。');
}

async function saveArticle(nextStatus: ArticleStatus) {
  const validationMessage = validateForm();
  errorMessage.value = validationMessage;
  if (validationMessage) return;

  saving.value = true;
  errorMessage.value = '';
  try {
    form.status = nextStatus;
    if (isEdit.value && articleId.value) {
      await updateAdminArticle(articleId.value, toPayload(nextStatus));
      feedback.success(nextStatus === 'PUBLISHED' ? '文章已发布。' : '文章草稿已保存。');
      clearDraft(false);
      guard.markClean();
    } else {
      await createAdminArticle(toPayload(nextStatus));
      clearDraft(false);
      guard.markClean();
      await router.push('/admin/articles');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '文章保存失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    saving.value = false;
  }
}

function saveShortcut() {
  void saveArticle(form.status === 'PUBLISHED' ? 'PUBLISHED' : 'DRAFT');
}

onMounted(loadEditor);
</script>

<template>
  <section class="space-y-5">
    <AdminPageHeader
      :eyebrow="`content / articles // ${isEdit ? 'edit' : 'new'}`"
      :title="isEdit ? '编辑文章' : '新建文章'"
      description="用 Markdown 写作，随时预览；本地草稿会自动保存，服务器保存与发布始终分开。"
    >
      <template #actions>
        <div class="flex flex-wrap gap-2">
          <span class="admin-editor-state" :class="guard.isDirty ? 'is-dirty' : 'is-clean'">
            <i aria-hidden="true"></i>{{ guard.isDirty ? '有未保存修改' : '已保存' }}
          </span>
          <RouterLink to="/admin/articles"><BaseButton variant="secondary" size="sm">返回文章</BaseButton></RouterLink>
        </div>
      </template>
    </AdminPageHeader>

    <div v-if="recoveryDraft" class="admin-recovery" role="status">
      <div>
        <strong>发现较新的本地草稿</strong>
        <p>本地保存于 {{ formatLocalDraftTime(recoveryDraft.savedAt) }}，不会自动覆盖服务器文章。</p>
      </div>
      <div class="flex flex-wrap gap-2">
        <BaseButton size="sm" @click="restoreDraft">恢复</BaseButton>
        <BaseButton size="sm" variant="secondary" @click="discardDraft">丢弃</BaseButton>
      </div>
    </div>

    <div v-if="loading" class="surface-muted rounded-panel p-8 font-mono text-sm text-brand" role="status">编辑器加载中...</div>
    <div v-else-if="errorMessage && !ready" class="rounded-control border border-danger/40 bg-danger/10 px-4 py-3 text-sm text-danger" role="alert">{{ errorMessage }}</div>

    <form v-else class="admin-editor-form" @submit.prevent="saveArticle('DRAFT')">
      <div class="admin-editor-form__main">
        <section class="surface-muted rounded-panel p-5 md:p-6">
          <div class="grid gap-4">
            <BaseInput v-model="form.title" label="标题 *" placeholder="写一个清晰的标题" />
            <BaseInput v-model="form.slug" label="访问标识 slug *" hint="用于公开文章地址，建议使用英文短横线。" />
            <BaseTextarea v-model="form.summary" label="摘要" :rows="3" placeholder="用一两句话说明这篇文章解决什么问题。" />
          </div>
        </section>

        <section class="surface-muted rounded-panel p-3 md:p-4">
          <div class="mb-3 flex items-center justify-between gap-3 px-2">
            <div>
              <p class="admin-eyebrow">正文 // MARKDOWN</p>
              <h2 class="mt-1 text-lg font-semibold text-text-primary">正文</h2>
            </div>
            <span v-if="localDraftSavedAt" class="font-mono text-[11px] text-text-muted">已在本地保存 · {{ formatLocalDraftTime(localDraftSavedAt) }}</span>
          </div>
          <MarkdownEditor v-model="form.content" upload-biz-type="other" @save="saveShortcut" />
        </section>
      </div>

      <aside class="admin-editor-form__aside">
        <section class="surface-muted rounded-panel p-5">
          <p class="admin-eyebrow">发布设置 // PUBLISH</p>
          <h2 class="mt-1 text-lg font-semibold text-text-primary">发布设置</h2>
          <div class="mt-5 grid gap-4">
            <BaseSelect v-model="form.categoryId" label="分类 *">
              <option value="">请选择分类</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option>
            </BaseSelect>
            <BaseSelect v-model="form.status" label="当前状态">
              <option value="DRAFT">草稿</option>
              <option value="PUBLISHED">已发布</option>
              <option value="HIDDEN">隐藏</option>
            </BaseSelect>
            <label class="admin-native-field">
              <span>阅读时间（分钟）</span>
              <input v-model.number="form.readingTime" min="1" type="number" />
            </label>
            <label class="admin-check-field">
              <input v-model="form.isTop" type="checkbox" />
              <span>置顶这篇文章</span>
            </label>
          </div>
        </section>

        <section class="surface-muted rounded-panel p-5">
          <p class="admin-eyebrow">分类与标签 // ORGANIZE</p>
          <h2 class="mt-1 text-lg font-semibold text-text-primary">标签</h2>
          <div class="mt-4 grid gap-2">
            <label v-for="tag in tags" :key="tag.id" class="admin-check-field admin-check-field--chip">
              <input v-model="form.tagIds" :value="tag.id" type="checkbox" />
              <span>{{ tag.name }}</span>
            </label>
            <p v-if="!tags.length" class="text-sm text-text-muted">暂无可用标签，请先在标签管理中创建。</p>
          </div>
        </section>

        <section class="surface-muted rounded-panel p-5">
          <p class="admin-eyebrow">媒体 // COVER</p>
          <h2 class="mt-1 text-lg font-semibold text-text-primary">封面</h2>
          <div class="mt-4">
            <ImageUploader v-model="form.coverImage" biz-type="article-cover" label="文章封面图" />
          </div>
        </section>
      </aside>

      <div v-if="errorMessage" class="admin-form-error" role="alert">{{ errorMessage }}</div>
      <div class="admin-editor-form__actions">
        <div class="text-xs text-text-muted">{{ guard.isDirty ? '修改会先保存在本地，离开页面会提醒。' : '当前内容与服务器一致。' }}</div>
        <div class="flex flex-wrap gap-2">
          <BaseButton variant="secondary" :loading="saving" @click="saveArticle('DRAFT')">保存草稿</BaseButton>
          <BaseButton :loading="saving" @click="saveArticle('PUBLISHED')">发布文章</BaseButton>
        </div>
      </div>
    </form>
  </section>
</template>

<style scoped>
.admin-editor-state { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: 999px; padding: .45rem .7rem; color: rgb(var(--color-text-muted)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; }
.admin-editor-state i { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-success)); }
.admin-editor-state.is-dirty { border-color: rgb(var(--color-warning) / .5); color: rgb(var(--color-warning)); }
.admin-editor-state.is-dirty i { background: rgb(var(--color-warning)); }
.admin-recovery { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border: 1px solid rgb(var(--color-warning) / .42); border-radius: .75rem; background: rgb(var(--color-warning) / .07); padding: .85rem 1rem; }
.admin-recovery strong { color: rgb(var(--color-text-primary)); font-size: .85rem; }
.admin-recovery p { margin-top: .2rem; color: rgb(var(--color-text-muted)); font-size: .75rem; }
.admin-editor-form { display: grid; gap: 1.25rem; grid-template-columns: minmax(0, 1fr) minmax(16rem, 21rem); }
.admin-editor-form__main, .admin-editor-form__aside { display: grid; align-content: start; gap: 1.25rem; min-width: 0; }
.admin-editor-form__actions, .admin-form-error { grid-column: 1 / -1; }
.admin-editor-form__actions { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid rgb(var(--color-border-subtle) / .72); padding-top: 1rem; }
.admin-form-error { border: 1px solid rgb(var(--color-danger) / .4); border-radius: .65rem; background: rgb(var(--color-danger) / .08); padding: .75rem 1rem; color: rgb(var(--color-danger)); font-size: .82rem; }
.admin-native-field { display: grid; gap: .45rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }
.admin-native-field input { width: 100%; height: 2.75rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .625rem; background: rgb(var(--color-surface) / .72); padding: 0 .85rem; color: rgb(var(--color-text-primary)); outline: none; }
.admin-native-field input:focus { border-color: rgb(var(--color-brand-primary)); box-shadow: 0 0 0 3px rgb(var(--color-brand-primary) / .15); }
.admin-check-field { display: flex; align-items: center; gap: .6rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }
.admin-check-field input { width: 1rem; height: 1rem; accent-color: rgb(var(--color-brand-primary)); }
.admin-check-field--chip { border: 1px solid rgb(var(--color-border-subtle) / .65); border-radius: .55rem; padding: .55rem .65rem; }
.admin-check-field--chip:has(input:checked) { border-color: rgb(var(--color-brand-primary) / .55); background: rgb(var(--color-brand-primary) / .08); color: rgb(var(--color-brand-primary)); }
@media (max-width: 900px) { .admin-editor-form { grid-template-columns: minmax(0, 1fr); } }
@media (max-width: 640px) { .admin-recovery, .admin-editor-form__actions { align-items: flex-start; flex-direction: column; } .admin-editor-form__actions > :last-child { width: 100%; } }
</style>
