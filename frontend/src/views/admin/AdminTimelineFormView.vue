<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import {
  createAdminTimelineEvent,
  fetchAdminTimelineEvent,
  fetchAdminTimeline,
  updateAdminTimelineEvent,
} from '@/api/adminTimeline';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseSelect from '@/components/common/BaseSelect.vue';
import BaseTextarea from '@/components/common/BaseTextarea.vue';
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { TimelineSavePayload } from '@/types/timeline';
import { getErrorMessage } from '@/utils/errors';
import { formatTimelineType } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const typeOptions = ref<string[]>([]);

const eventId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(eventId.value));

const form = reactive({
  title: '',
  description: '',
  eventDate: '',
  type: '',
  tagsText: '',
  sortOrder: 0,
  visible: true,
});

function snapshot() {
  return JSON.stringify({ ...form });
}

const guard = useUnsavedChangesGuard(snapshot, { label: '时间线节点', isSaving: () => saving.value });

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
  if (!form.eventDate) {
    return '请选择事件日期';
  }
  return '';
}

function toPayload(): TimelineSavePayload {
  return {
    title: form.title.trim(),
    description: form.description.trim(),
    eventDate: form.eventDate,
    type: form.type.trim(),
    tags: splitText(form.tagsText),
    sortOrder: form.sortOrder || 0,
    visible: form.visible,
  };
}

async function loadEvent() {
  if (!eventId.value) {
    return;
  }

  const event = await fetchAdminTimelineEvent(eventId.value);
  form.title = event.title;
  form.description = event.description || '';
  form.eventDate = event.eventDate || '';
  form.type = event.type || '';
  form.tagsText = event.tags.join(', ');
  form.sortOrder = event.sortOrder || 0;
  form.visible = event.visible !== false;
}

async function loadTypeOptions() {
  const page = await fetchAdminTimeline({ page: 1, size: 100 });
  typeOptions.value = Array.from(new Set(page.list.map((event) => event.type).filter((type): type is string => Boolean(type))));
  if (!typeOptions.value.includes(form.type)) typeOptions.value.push(form.type || 'MILESTONE');
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
    if (isEdit.value && eventId.value) {
      await updateAdminTimelineEvent(eventId.value, toPayload());
      successMessage.value = '时间线已保存';
      feedback.success('时间线已保存。');
      guard.markClean();
    } else {
      await createAdminTimelineEvent(toPayload());
      guard.markClean();
      await router.push('/admin/timeline');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线保存失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await Promise.all([loadEvent(), loadTypeOptions()]);
    if (form.type && !typeOptions.value.includes(form.type)) typeOptions.value.push(form.type);
    guard.markClean();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线表单加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <AdminPageHeader :eyebrow="`时间线 // ${isEdit ? '编辑' : '新建'} · TIMELINE`" :title="isEdit ? '编辑时间线' : '新建时间线'" description="时间线使用结构化字段记录成长节点，不引入不属于当前模型的长文内容。">
      <template #actions><div class="flex flex-wrap gap-2"><span class="admin-editor-state" :class="guard.isDirty ? 'is-dirty' : 'is-clean'"><i aria-hidden="true"></i>{{ guard.isDirty ? '有未保存修改' : '已保存' }}</span><RouterLink to="/admin/timeline"><BaseButton variant="secondary" size="sm">返回时间线</BaseButton></RouterLink></div></template>
    </AdminPageHeader>

    <p v-if="loading" class="mt-8 font-mono text-sm text-cyber-cyan">表单加载中...</p>

    <form v-else class="mt-8 grid gap-5" @submit.prevent="submit">
      <div class="grid gap-5 lg:grid-cols-2">
        <BaseInput v-model="form.title" label="标题 *" />
        <BaseInput v-model="form.eventDate" label="事件日期 *" type="date" />
      </div>

      <BaseTextarea v-model="form.description" label="描述" :rows="4" />

      <div class="grid gap-5 lg:grid-cols-3">
        <BaseSelect v-model="form.type" label="类型"><option v-for="type in typeOptions" :key="type" :value="type">{{ formatTimelineType(type) }}</option></BaseSelect>
        <BaseInput v-model="form.tagsText" label="标签" placeholder="课程设计, 实习准备" />
        <label class="admin-native-field"><span>排序</span><input v-model.number="form.sortOrder" type="number" /></label>
      </div>

      <label class="flex items-center gap-3 text-sm text-cyber-muted">
        <input v-model="form.visible" class="h-4 w-4 accent-cyber-cyan" type="checkbox" />
        是否前台可见
      </label>

      <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
      <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

      <div class="flex flex-wrap gap-3"><BaseButton :loading="saving" type="submit">保存时间线</BaseButton><RouterLink to="/admin/timeline"><BaseButton variant="secondary">取消</BaseButton></RouterLink></div>
    </form>
  </section>
</template>

<style scoped>
.admin-editor-state { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: 999px; padding: .45rem .7rem; color: rgb(var(--color-text-muted)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; }.admin-editor-state i { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-success)); }.admin-editor-state.is-dirty { border-color: rgb(var(--color-warning) / .5); color: rgb(var(--color-warning)); }.admin-editor-state.is-dirty i { background: rgb(var(--color-warning)); }.admin-native-field { display: grid; gap: .45rem; color: rgb(var(--color-text-secondary)); font-size: .8rem; }.admin-native-field input { width: 100%; height: 2.75rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .625rem; background: rgb(var(--color-surface) / .72); padding: 0 .85rem; color: rgb(var(--color-text-primary)); outline: none; }.admin-native-field input:focus { border-color: rgb(var(--color-brand-primary)); }
</style>
