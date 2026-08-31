<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminNote, fetchAdminNote, updateAdminNote } from '@/api/adminNotes';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import MarkdownEditor from '@/components/admin/MarkdownEditor.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseTextarea from '@/components/common/BaseTextarea.vue';
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard';
import type { NoteSavePayload } from '@/types/note';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import { discardLocalDraft, formatLocalDraftTime, readLocalDraft, type LocalDraftRecord, writeLocalDraft } from '@/utils/localDraft';
import { getErrorMessage } from '@/utils/errors';

interface NoteFormState {
  title: string;
  slug: string;
  summary: string;
  content: string;
  topic: string;
  tagsText: string;
  isPublic: boolean;
  sortOrder: number;
}

const route = useRoute();
const router = useRouter();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const ready = ref(false);
const localDraftSavedAt = ref<number | null>(null);
const recoveryDraft = ref<LocalDraftRecord<NoteFormState> | null>(null);

const noteId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(noteId.value));
const draftKey = computed(() => `note-${noteId.value || 'new'}`);

const form = reactive<NoteFormState>({
  title: '',
  slug: '',
  summary: '',
  content: '',
  topic: '',
  tagsText: '',
  isPublic: true,
  sortOrder: 0,
});

function formData(): NoteFormState {
  return { ...form };
}

function snapshot() {
  return JSON.stringify(formData());
}

const guard = useUnsavedChangesGuard(snapshot, { label: 'Note', isSaving: () => saving.value });

watch(
  form,
  () => {
    if (!ready.value) return;
    const record = writeLocalDraft(draftKey.value, formData());
    localDraftSavedAt.value = record.savedAt;
  },
  { deep: true },
);

function splitText(value: string): string[] {
  return value.split(',').map((item) => item.trim()).filter(Boolean);
}

function applyForm(data: NoteFormState) {
  form.title = data.title || '';
  form.slug = data.slug || '';
  form.summary = data.summary || '';
  form.content = data.content || '';
  form.topic = data.topic || '';
  form.tagsText = data.tagsText || '';
  form.isPublic = data.isPublic !== false;
  form.sortOrder = Number(data.sortOrder) || 0;
}

function validateForm(): string {
  if (!form.title.trim()) return '请填写 Note 标题';
  if (!form.slug.trim()) return '请填写 slug';
  if (!form.content.trim()) return '请填写 Note 内容';
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

async function loadEditor() {
  loading.value = true;
  ready.value = false;
  errorMessage.value = '';
  try {
    const note = noteId.value ? await fetchAdminNote(noteId.value) : null;
    let serverUpdatedAt = 0;
    if (note) {
      applyForm({
        title: note.title,
        slug: note.slug,
        summary: note.summary || '',
        content: note.content || '',
        topic: note.topic || '',
        tagsText: note.tags.join(', '),
        isPublic: note.isPublic !== false,
        sortOrder: note.sortOrder || 0,
      });
      serverUpdatedAt = note.updatedAt ? Date.parse(note.updatedAt) : 0;
    }
    const stored = readLocalDraft<NoteFormState>(draftKey.value);
    if (stored && (isEdit.value ? stored.savedAt > serverUpdatedAt : Boolean(stored.data.title || stored.data.content))) {
      recoveryDraft.value = stored;
      localDraftSavedAt.value = stored.savedAt;
    } else if (stored && isEdit.value) {
      discardLocalDraft(draftKey.value);
    }
    guard.markClean();
    ready.value = true;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, 'Note 编辑器加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

function restoreDraft() {
  if (!recoveryDraft.value) return;
  applyForm(recoveryDraft.value.data);
  recoveryDraft.value = null;
  feedback.info('本地 Note 草稿已恢复。');
}

function discardDraft() {
  clearDraft(true);
}

function clearDraft(showFeedback: boolean) {
  discardLocalDraft(draftKey.value);
  recoveryDraft.value = null;
  localDraftSavedAt.value = null;
  if (showFeedback) feedback.info('本地 Note 草稿已丢弃。');
}

async function saveNote() {
  const validationMessage = validateForm();
  errorMessage.value = validationMessage;
  if (validationMessage) return;
  saving.value = true;
  errorMessage.value = '';
  try {
    if (isEdit.value && noteId.value) {
      await updateAdminNote(noteId.value, toPayload());
      feedback.success('Note 已保存。');
      clearDraft(false);
      guard.markClean();
    } else {
      await createAdminNote(toPayload());
      clearDraft(false);
      guard.markClean();
      await router.push('/admin/notes');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, 'Note 保存失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    saving.value = false;
  }
}

onMounted(loadEditor);
</script>

<template>
  <section class="space-y-5">
    <AdminPageHeader
      :eyebrow="`garden / notes // ${isEdit ? 'edit' : 'new'}`"
      :title="isEdit ? '编辑 Note' : '新建 Note'"
      description="轻量记录，不需要文章级设置；Markdown 预览、本地自动草稿与离开保护保持一致。"
    >
      <template #actions>
        <div class="flex flex-wrap gap-2">
          <span class="admin-editor-state" :class="guard.isDirty ? 'is-dirty' : 'is-clean'"><i aria-hidden="true"></i>{{ guard.isDirty ? 'Unsaved changes' : 'Saved' }}</span>
          <RouterLink to="/admin/notes"><BaseButton variant="secondary" size="sm">返回 Notes</BaseButton></RouterLink>
        </div>
      </template>
    </AdminPageHeader>

    <div v-if="recoveryDraft" class="admin-recovery" role="status">
      <div><strong>发现较新的本地 Note 草稿</strong><p>本地保存于 {{ formatLocalDraftTime(recoveryDraft.savedAt) }}，不会自动覆盖服务器内容。</p></div>
      <div class="flex flex-wrap gap-2"><BaseButton size="sm" @click="restoreDraft">恢复</BaseButton><BaseButton size="sm" variant="secondary" @click="discardDraft">丢弃</BaseButton></div>
    </div>

    <div v-if="loading" class="surface-muted rounded-panel p-8 font-mono text-sm text-brand" role="status">Note 编辑器加载中...</div>
    <div v-else-if="errorMessage && !ready" class="rounded-control border border-danger/40 bg-danger/10 px-4 py-3 text-sm text-danger" role="alert">{{ errorMessage }}</div>

    <form v-else class="admin-editor-form admin-editor-form--note" @submit.prevent="saveNote">
      <div class="admin-editor-form__main">
        <section class="surface-muted rounded-panel p-5 md:p-6">
          <div class="grid gap-4">
            <BaseInput v-model="form.title" label="标题 *" placeholder="记录一个正在形成的想法" />
            <BaseInput v-model="form.slug" label="slug *" />
            <BaseTextarea v-model="form.summary" label="摘要" :rows="3" placeholder="这条记录的上下文或结论。" />
          </div>
        </section>
        <section class="surface-muted rounded-panel p-3 md:p-4">
          <div class="mb-3 flex items-center justify-between gap-3 px-2"><div><p class="admin-eyebrow">garden // markdown</p><h2 class="mt-1 text-lg font-semibold text-text-primary">内容</h2></div><span v-if="localDraftSavedAt" class="font-mono text-[11px] text-text-muted">Saved locally {{ formatLocalDraftTime(localDraftSavedAt) }}</span></div>
          <MarkdownEditor v-model="form.content" @save="saveNote" />
        </section>
      </div>
      <aside class="admin-editor-form__aside">
        <section class="surface-muted rounded-panel p-5">
          <p class="admin-eyebrow">garden // metadata</p>
          <h2 class="mt-1 text-lg font-semibold text-text-primary">记录设置</h2>
          <div class="mt-5 grid gap-4">
            <BaseInput v-model="form.topic" label="主题" hint="例如 NOTE、TIL 或阶段验证。" />
            <BaseInput v-model="form.tagsText" label="标签" hint="用逗号分隔：Linux, MySQL" />
            <label class="admin-native-field"><span>排序</span><input v-model.number="form.sortOrder" type="number" /></label>
            <label class="admin-check-field"><input v-model="form.isPublic" type="checkbox" /><span>公开到 Digital Garden</span></label>
          </div>
        </section>
      </aside>
      <div v-if="errorMessage" class="admin-form-error" role="alert">{{ errorMessage }}</div>
      <div class="admin-editor-form__actions"><span class="text-xs text-text-muted">{{ guard.isDirty ? '修改会自动保存在本地。' : '当前内容与服务器一致。' }}</span><div class="flex flex-wrap gap-2"><BaseButton variant="secondary" :loading="saving" type="submit">保存 Note</BaseButton></div></div>
    </form>
  </section>
</template>

<style scoped>
.admin-editor-state { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: 999px; padding: .45rem .7rem; color: rgb(var(--color-text-muted)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; }
.admin-editor-state i { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-success)); }
.admin-editor-state.is-dirty { border-color: rgb(var(--color-warning) / .5); color: rgb(var(--color-warning)); }
.admin-editor-state.is-dirty i { background: rgb(var(--color-warning)); }
.admin-recovery { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border: 1px solid rgb(var(--color-warning) / .42); border-radius: .75rem; background: rgb(var(--color-warning) / .07); padding: .85rem 1rem; }
.admin-recovery strong { color: rgb(var(--color-text-primary)); font-size: .85rem; }.admin-recovery p { margin-top: .2rem; color: rgb(var(--color-text-muted)); font-size: .75rem; }
.admin-editor-form { display: grid; gap: 1.25rem; grid-template-columns: minmax(0, 1fr) minmax(16rem, 21rem); }.admin-editor-form__main, .admin-editor-form__aside { display: grid; align-content: start; gap: 1.25rem; min-width: 0; }.admin-editor-form__actions, .admin-form-error { grid-column: 1 / -1; }.admin-editor-form__actions { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-top: 1px solid rgb(var(--color-border-subtle) / .72); padding-top: 1rem; }
.admin-form-error { border: 1px solid rgb(var(--color-danger) / .4); border-radius: .65rem; background: rgb(var(--color-danger) / .08); padding: .75rem 1rem; color: rgb(var(--color-danger)); font-size: .82rem; }.admin-native-field { display: grid; gap: .45rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }.admin-native-field input { width: 100%; height: 2.75rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .625rem; background: rgb(var(--color-surface) / .72); padding: 0 .85rem; color: rgb(var(--color-text-primary)); outline: none; }.admin-native-field input:focus { border-color: rgb(var(--color-brand-primary)); }.admin-check-field { display: flex; align-items: center; gap: .6rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }.admin-check-field input { width: 1rem; height: 1rem; accent-color: rgb(var(--color-brand-primary)); }
@media (max-width: 900px) { .admin-editor-form { grid-template-columns: minmax(0, 1fr); } }@media (max-width: 640px) { .admin-recovery, .admin-editor-form__actions { align-items: flex-start; flex-direction: column; } }
</style>
