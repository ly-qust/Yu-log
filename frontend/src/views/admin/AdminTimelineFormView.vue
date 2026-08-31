<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import {
  createAdminTimelineEvent,
  fetchAdminTimelineEvent,
  updateAdminTimelineEvent,
} from '@/api/adminTimeline';
import type { TimelineSavePayload } from '@/types/timeline';
import { getErrorMessage } from '@/utils/errors';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

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
    } else {
      await createAdminTimelineEvent(toPayload());
      await router.push('/admin/timeline');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await loadEvent();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '时间线表单加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="terminal-label text-sm">admin_timeline // {{ isEdit ? 'edit' : 'new' }}</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">{{ isEdit ? '编辑时间线' : '新建时间线' }}</h2>
      </div>
      <RouterLink class="font-mono text-xs text-cyber-cyan hover:text-cyber-cyanBright" to="/admin/timeline">
        返回时间线管理
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
          <span class="font-mono text-xs uppercase text-cyber-muted">事件日期 *</span>
          <input v-model="form.eventDate" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="date" />
        </label>
      </div>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">描述</span>
        <textarea v-model="form.description" class="mt-2 min-h-32 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <div class="grid gap-5 lg:grid-cols-3">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">类型</span>
          <input v-model="form.type" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" placeholder="PROJECT / STUDY" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">标签</span>
          <input v-model="form.tagsText" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" placeholder="课程设计, 实习准备" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">排序</span>
          <input v-model.number="form.sortOrder" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="number" />
        </label>
      </div>

      <label class="flex items-center gap-3 text-sm text-cyber-muted">
        <input v-model="form.visible" class="h-4 w-4 accent-cyber-cyan" type="checkbox" />
        是否前台可见
      </label>

      <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
      <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

      <div class="flex flex-wrap gap-3">
        <button class="rounded-lg bg-cyber-cyanBright px-5 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50" :disabled="saving" type="submit">
          {{ saving ? '保存中...' : '保存时间线' }}
        </button>
        <RouterLink class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" to="/admin/timeline">
          取消
        </RouterLink>
      </div>
    </form>
  </section>
</template>
