<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { createAdminProject, fetchAdminProject, updateAdminProject } from '@/api/adminProjects';
import ImageUploader from '@/components/common/ImageUploader.vue';
import type { ProjectSavePayload, ProjectStatus } from '@/types/project';
import { getErrorMessage } from '@/utils/errors';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const successMessage = ref('');

const projectId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});
const isEdit = computed(() => Boolean(projectId.value));

const form = reactive({
  name: '',
  slug: '',
  description: '',
  detailContent: '',
  coverImage: '',
  techStackText: '',
  status: 'DEVELOPING' as ProjectStatus,
  githubUrl: '',
  demoUrl: '',
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
  if (!form.name.trim()) {
    return '请填写项目名称';
  }
  if (!form.slug.trim()) {
    return '请填写 slug';
  }
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

async function loadProject() {
  if (!projectId.value) {
    return;
  }

  const project = await fetchAdminProject(projectId.value);
  form.name = project.name;
  form.slug = project.slug;
  form.description = project.description || '';
  form.detailContent = project.detailContent || '';
  form.coverImage = project.coverImage || '';
  form.techStackText = project.techStack.join(', ');
  form.status = ['PLANNING', 'DEVELOPING', 'COMPLETED'].includes(project.status) ? (project.status as ProjectStatus) : 'DEVELOPING';
  form.githubUrl = project.githubUrl || '';
  form.demoUrl = project.demoUrl || '';
  form.sortOrder = project.sortOrder || 0;
  form.visible = project.visible !== false;
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
    if (isEdit.value && projectId.value) {
      await updateAdminProject(projectId.value, toPayload());
      successMessage.value = '项目已保存';
    } else {
      await createAdminProject(toPayload());
      await router.push('/admin/projects');
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
}

onMounted(async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    await loadProject();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目表单加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <section class="glass-panel rounded-glass p-6">
    <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="terminal-label text-sm">admin_projects // {{ isEdit ? 'edit' : 'new' }}</p>
        <h2 class="mt-3 font-display text-3xl font-semibold">{{ isEdit ? '编辑项目' : '新建项目' }}</h2>
      </div>
      <RouterLink class="font-mono text-xs text-cyber-cyan hover:text-cyber-cyanBright" to="/admin/projects">
        返回项目管理
      </RouterLink>
    </div>

    <p v-if="loading" class="mt-8 font-mono text-sm text-cyber-cyan">表单加载中...</p>

    <form v-else class="mt-8 grid gap-5" @submit.prevent="submit">
      <div class="grid gap-5 lg:grid-cols-2">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">项目名称 *</span>
          <input v-model="form.name" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">slug *</span>
          <input v-model="form.slug" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="text" />
        </label>
      </div>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">项目摘要</span>
        <textarea v-model="form.description" class="mt-2 min-h-24 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <label class="block">
        <span class="font-mono text-xs uppercase text-cyber-muted">项目详情</span>
        <textarea v-model="form.detailContent" class="mt-2 min-h-48 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 font-mono text-sm leading-7 text-cyber-text outline-none focus:border-cyber-cyan"></textarea>
      </label>

      <div class="grid gap-5 lg:grid-cols-3">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">技术栈</span>
          <input v-model="form.techStackText" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" placeholder="Java, Vue3, MySQL" type="text" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">状态</span>
          <select v-model="form.status" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan">
            <option value="PLANNING">规划中</option>
            <option value="DEVELOPING">开发中</option>
            <option value="COMPLETED">已完成</option>
          </select>
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">排序</span>
          <input v-model.number="form.sortOrder" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="number" />
        </label>
      </div>

      <div class="grid gap-5 lg:grid-cols-3">
        <div class="lg:col-span-3">
          <ImageUploader v-model="form.coverImage" biz-type="project-cover" label="项目封面图" />
          <input v-model="form.coverImage" class="mt-3 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" placeholder="也可以手动填写封面 URL" type="text" />
        </div>
      </div>

      <div class="grid gap-5 lg:grid-cols-2">
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">GitHub 地址</span>
          <input v-model="form.githubUrl" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="url" />
        </label>
        <label class="block">
          <span class="font-mono text-xs uppercase text-cyber-muted">Demo 地址</span>
          <input v-model="form.demoUrl" class="mt-2 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan" type="url" />
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
          {{ saving ? '保存中...' : '保存项目' }}
        </button>
        <RouterLink class="rounded-lg border border-cyber-border px-5 py-3 font-mono text-xs text-cyber-muted hover:border-cyber-cyan hover:text-cyber-cyan" to="/admin/projects">
          取消
        </RouterLink>
      </div>
    </form>
  </section>
</template>
