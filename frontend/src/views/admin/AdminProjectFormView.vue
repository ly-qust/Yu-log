<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminProject, fetchAdminProject, updateAdminProject } from '@/api/adminProjects';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import MarkdownEditor from '@/components/admin/MarkdownEditor.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseSelect from '@/components/common/BaseSelect.vue';
import BaseTextarea from '@/components/common/BaseTextarea.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { ProjectSavePayload, ProjectStatus } from '@/types/project';
import { discardLocalDraft, formatLocalDraftTime, readLocalDraft, type LocalDraftRecord, writeLocalDraft } from '@/utils/localDraft';
import { getErrorMessage } from '@/utils/errors';

interface ProjectFormState {
  name: string;
  slug: string;
  description: string;
  detailContent: string;
  coverImage: string;
  techStackText: string;
  status: ProjectStatus;
  githubUrl: string;
  demoUrl: string;
  sortOrder: number;
  visible: boolean;
}

const route = useRoute();
const router = useRouter();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const ready = ref(false);
const localDraftSavedAt = ref<number | null>(null);
const recoveryDraft = ref<LocalDraftRecord<ProjectFormState> | null>(null);

const projectId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(projectId.value));
const draftKey = computed(() => `project-${projectId.value || 'new'}`);

const form = reactive<ProjectFormState>({
  name: '',
  slug: '',
  description: '',
  detailContent: '',
  coverImage: '',
  techStackText: '',
  status: 'DEVELOPING',
  githubUrl: '',
  demoUrl: '',
  sortOrder: 0,
  visible: true,
});

function formData(): ProjectFormState { return { ...form }; }
function snapshot() { return JSON.stringify(formData()); }
const guard = useUnsavedChangesGuard(snapshot, { label: '项目', isSaving: () => saving.value });

watch(form, () => {
  if (!ready.value) return;
  const record = writeLocalDraft(draftKey.value, formData());
  localDraftSavedAt.value = record.savedAt;
}, { deep: true });

function splitText(value: string): string[] { return value.split(',').map((item) => item.trim()).filter(Boolean); }

function applyForm(data: ProjectFormState) {
  form.name = data.name || '';
  form.slug = data.slug || '';
  form.description = data.description || '';
  form.detailContent = data.detailContent || '';
  form.coverImage = data.coverImage || '';
  form.techStackText = data.techStackText || '';
  form.status = data.status || 'DEVELOPING';
  form.githubUrl = data.githubUrl || '';
  form.demoUrl = data.demoUrl || '';
  form.sortOrder = Number(data.sortOrder) || 0;
  form.visible = data.visible !== false;
}

function validateForm(): string {
  if (!form.name.trim()) return '请填写项目名称';
  if (!form.slug.trim()) return '请填写项目 slug';
  return '';
}

function toPayload(): ProjectSavePayload {
  return {
    name: form.name.trim(),
    slug: form.slug.trim(),
    description: form.description.trim(),
    detailContent: form.detailContent,
    coverImage: form.coverImage.trim(),
    techStack: splitText(form.techStackText),
    status: form.status,
    githubUrl: form.githubUrl.trim(),
    demoUrl: form.demoUrl.trim(),
    sortOrder: form.sortOrder || 0,
    visible: form.visible,
  };
}

async function loadEditor() {
  loading.value = true;
  ready.value = false;
  errorMessage.value = '';
  try {
    const project = projectId.value ? await fetchAdminProject(projectId.value) : null;
    let serverUpdatedAt = 0;
    if (project) {
      applyForm({
        name: project.name,
        slug: project.slug,
        description: project.description || '',
        detailContent: project.detailContent || '',
        coverImage: project.coverImage || '',
        techStackText: project.techStack.join(', '),
        status: ['PLANNING', 'DEVELOPING', 'COMPLETED'].includes(project.status) ? project.status as ProjectStatus : 'DEVELOPING',
        githubUrl: project.githubUrl || '',
        demoUrl: project.demoUrl || '',
        sortOrder: project.sortOrder || 0,
        visible: project.visible !== false,
      });
      serverUpdatedAt = project.updatedAt ? Date.parse(project.updatedAt) : 0;
    }
    const stored = readLocalDraft<ProjectFormState>(draftKey.value);
    if (stored && (isEdit.value ? stored.savedAt > serverUpdatedAt : Boolean(stored.data.name || stored.data.detailContent))) {
      recoveryDraft.value = stored;
      localDraftSavedAt.value = stored.savedAt;
    } else if (stored && isEdit.value) {
      discardLocalDraft(draftKey.value);
    }
    guard.markClean();
    ready.value = true;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目编辑器加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function restoreDraft() {
  if (!recoveryDraft.value) return;
  applyForm(recoveryDraft.value.data);
  recoveryDraft.value = null;
  feedback.info('本地项目草稿已恢复。');
}

function discardDraft() {
  clearDraft(true);
}

function clearDraft(showFeedback: boolean) {
  discardLocalDraft(draftKey.value);
  recoveryDraft.value = null;
  localDraftSavedAt.value = null;
  if (showFeedback) feedback.info('本地项目草稿已丢弃。');
}

async function saveProject() {
  const validationMessage = validateForm();
  errorMessage.value = validationMessage;
  if (validationMessage) return;
  saving.value = true;
  errorMessage.value = '';
  try {
    if (isEdit.value && projectId.value) {
      await updateAdminProject(projectId.value, toPayload());
      feedback.success('项目已保存。');
      clearDraft(false);
      guard.markClean();
    } else {
      await createAdminProject(toPayload());
      clearDraft(false);
      guard.markClean();
      await router.push('/admin/projects');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目保存失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    saving.value = false;
  }
}

onMounted(loadEditor);
</script>

<template>
  <section class="space-y-5">
    <AdminPageHeader :eyebrow="`garden / projects // ${isEdit ? 'edit' : 'new'}`" :title="isEdit ? '编辑项目' : '新建项目'" description="维护公开 Case Study 目前真实可表达的字段；详情使用与公开端相同的 Markdown 渲染器。">
      <template #actions><div class="flex flex-wrap gap-2"><span class="admin-editor-state" :class="guard.isDirty ? 'is-dirty' : 'is-clean'"><i aria-hidden="true"></i>{{ guard.isDirty ? 'Unsaved changes' : 'Saved' }}</span><RouterLink to="/admin/projects"><BaseButton variant="secondary" size="sm">返回项目</BaseButton></RouterLink></div></template>
    </AdminPageHeader>

    <div v-if="recoveryDraft" class="admin-recovery" role="status"><div><strong>发现较新的本地项目草稿</strong><p>本地保存于 {{ formatLocalDraftTime(recoveryDraft.savedAt) }}，不会自动覆盖服务器内容。</p></div><div class="flex flex-wrap gap-2"><BaseButton size="sm" @click="restoreDraft">恢复</BaseButton><BaseButton size="sm" variant="secondary" @click="discardDraft">丢弃</BaseButton></div></div>
    <div v-if="loading" class="surface-muted rounded-panel p-8 font-mono text-sm text-brand" role="status">项目编辑器加载中...</div>
    <div v-else-if="errorMessage && !ready" class="rounded-control border border-danger/40 bg-danger/10 px-4 py-3 text-sm text-danger" role="alert">{{ errorMessage }}</div>

    <form v-else class="admin-editor-form admin-editor-form--project" @submit.prevent="saveProject">
      <div class="admin-editor-form__main">
        <section class="surface-muted rounded-panel p-5 md:p-6"><div class="grid gap-4"><BaseInput v-model="form.name" label="项目名称 *" /><BaseInput v-model="form.slug" label="slug *" /><BaseTextarea v-model="form.description" label="项目摘要" :rows="3" /></div></section>
        <section class="surface-muted rounded-panel p-3 md:p-4"><div class="mb-3 px-2"><p class="admin-eyebrow">case study // markdown</p><h2 class="mt-1 text-lg font-semibold text-text-primary">项目详情</h2></div><MarkdownEditor v-model="form.detailContent" upload-biz-type="other" /></section>
      </div>
      <aside class="admin-editor-form__aside">
        <section class="surface-muted rounded-panel p-5"><p class="admin-eyebrow">project // metadata</p><h2 class="mt-1 text-lg font-semibold text-text-primary">项目设置</h2><div class="mt-5 grid gap-4"><BaseInput v-model="form.techStackText" label="技术栈" hint="用逗号分隔：Java, Vue3, MySQL" /><BaseSelect v-model="form.status" label="状态"><option value="PLANNING">规划中</option><option value="DEVELOPING">开发中</option><option value="COMPLETED">已完成</option></BaseSelect><label class="admin-native-field"><span>排序</span><input v-model.number="form.sortOrder" type="number" /></label><label class="admin-check-field"><input v-model="form.visible" type="checkbox" /><span>前台可见</span></label></div></section>
        <section class="surface-muted rounded-panel p-5"><p class="admin-eyebrow">links // publish</p><h2 class="mt-1 text-lg font-semibold text-text-primary">链接与封面</h2><div class="mt-5 grid gap-4"><BaseInput v-model="form.githubUrl" label="GitHub 地址" type="url" /><BaseInput v-model="form.demoUrl" label="Demo 地址" type="url" /><ImageUploader v-model="form.coverImage" biz-type="project-cover" label="项目封面图" /></div></section>
      </aside>
      <div v-if="errorMessage" class="admin-form-error" role="alert">{{ errorMessage }}</div>
      <div class="admin-editor-form__actions"><span class="text-xs text-text-muted">{{ guard.isDirty ? '修改会自动保存在本地。' : '当前内容与服务器一致。' }}</span><div class="flex flex-wrap gap-2"><BaseButton variant="secondary" :loading="saving" type="submit">保存项目</BaseButton></div></div>
    </form>
  </section>
</template>

<style scoped>
.admin-editor-state { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: 999px; padding: .45rem .7rem; color: rgb(var(--color-text-muted)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; }.admin-editor-state i { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-success)); }.admin-editor-state.is-dirty { border-color: rgb(var(--color-warning) / .5); color: rgb(var(--color-warning)); }.admin-editor-state.is-dirty i { background: rgb(var(--color-warning)); }.admin-recovery { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border: 1px solid rgb(var(--color-warning) / .42); border-radius: .75rem; background: rgb(var(--color-warning) / .07); padding: .85rem 1rem; }.admin-recovery strong { color: rgb(var(--color-text-primary)); font-size: .85rem; }.admin-recovery p { margin-top: .2rem; color: rgb(var(--color-text-muted)); font-size: .75rem; }.admin-editor-form { display: grid; gap: 1.25rem; grid-template-columns: minmax(0, 1fr) minmax(16rem, 21rem); }.admin-editor-form__main, .admin-editor-form__aside { display: grid; align-content: start; gap: 1.25rem; min-width: 0; }.admin-editor-form__actions, .admin-form-error { grid-column: 1 / -1; }.admin-editor-form__actions { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid rgb(var(--color-border-subtle) / .72); padding-top: 1rem; }.admin-form-error { border: 1px solid rgb(var(--color-danger) / .4); border-radius: .65rem; background: rgb(var(--color-danger) / .08); padding: .75rem 1rem; color: rgb(var(--color-danger)); font-size: .82rem; }.admin-native-field { display: grid; gap: .45rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }.admin-native-field input { width: 100%; height: 2.75rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .625rem; background: rgb(var(--color-surface) / .72); padding: 0 .85rem; color: rgb(var(--color-text-primary)); outline: none; }.admin-native-field input:focus { border-color: rgb(var(--color-brand-primary)); }.admin-check-field { display: flex; align-items: center; gap: .6rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }.admin-check-field input { width: 1rem; height: 1rem; accent-color: rgb(var(--color-brand-primary)); }
@media (max-width: 900px) { .admin-editor-form { grid-template-columns: minmax(0, 1fr); } }@media (max-width: 640px) { .admin-recovery, .admin-editor-form__actions { align-items: flex-start; flex-direction: column; } }
</style>
