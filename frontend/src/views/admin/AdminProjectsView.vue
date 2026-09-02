<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';

import { deleteAdminProject, fetchAdminProjects } from '@/api/adminProjects';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { PageResult } from '@/types/api';
import type { AdminProjectQuery, ProjectItem } from '@/types/project';
import { getErrorMessage } from '@/utils/errors';
import { formatBoolean, formatDateTime, formatProjectStatus } from '@/utils/format';

const pageSize = 10;
const feedback = useAdminFeedbackStore();
const loading = ref(false);
const actionLoadingId = ref('');
const errorMessage = ref('');
const successMessage = ref('');
const projectPage = ref<PageResult<ProjectItem>>({
  list: [],
  pageNum: 1,
  pageSize,
  total: 0,
  totalPages: 0,
  hasNext: false,
  hasPrevious: false,
});

const filters = reactive<AdminProjectQuery>({
  keyword: '',
  status: '',
  page: 1,
  size: pageSize,
});

async function loadProjects() {
  loading.value = true;
  errorMessage.value = '';
  try {
    projectPage.value = await fetchAdminProjects({
      keyword: filters.keyword?.trim() || undefined,
      status: filters.status || undefined,
      page: filters.page,
      size: pageSize,
    });
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目列表加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function submitFilters() {
  filters.page = 1;
  await loadProjects();
}

async function changePage(page: number) {
  if (page < 1 || (projectPage.value.totalPages > 0 && page > projectPage.value.totalPages)) {
    return;
  }
  filters.page = page;
  await loadProjects();
}

async function removeProject(project: ProjectItem) {
  const confirmed = await feedback.confirm({
    title: '删除项目',
    message: `确定要删除「${project.name}」吗？此操作不可恢复。`,
    confirmLabel: '删除项目',
    danger: true,
  });
  if (!confirmed) {
    return;
  }

  actionLoadingId.value = project.id;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await deleteAdminProject(project.id);
    successMessage.value = '项目已删除';
    feedback.success('项目已删除');
    await loadProjects();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '项目删除失败，请稍后重试');
    feedback.error(errorMessage.value);
  } finally {
    actionLoadingId.value = '';
  }
}

onMounted(loadProjects);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="terminal-label text-sm">项目 // 管理</p>
          <h2 class="mt-3 font-display text-3xl font-semibold">项目管理</h2>
          <p class="mt-2 text-sm text-cyber-muted">管理项目展示，支持状态筛选、前台可见和技术栈维护。</p>
        </div>

        <RouterLink class="inline-flex items-center justify-center rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan" to="/admin/projects/new">
          新建项目
        </RouterLink>
      </div>

      <form class="mt-6 grid gap-3 lg:grid-cols-[1.4fr_1fr_auto]" @submit.prevent="submitFilters">
        <input
          v-model="filters.keyword"
          class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition placeholder:text-cyber-outline focus:border-cyber-cyan"
          placeholder="搜索项目名称或描述"
          type="search"
        />
        <select v-model="filters.status" class="rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-sm text-cyber-text outline-none transition focus:border-cyber-cyan" @change="submitFilters">
          <option value="">全部状态</option>
          <option value="PLANNING">规划中</option>
          <option value="DEVELOPING">开发中</option>
          <option value="COMPLETED">已完成</option>
        </select>
        <button class="rounded-lg border border-cyber-cyan/60 px-4 py-3 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base" type="submit">
          查询
        </button>
      </form>
    </div>

    <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
    <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

    <div class="glass-panel overflow-x-auto rounded-glass">
      <table class="min-w-[1080px] w-full text-left text-sm">
        <thead class="border-b border-cyber-border bg-cyber-base/40 font-mono text-xs uppercase text-cyber-muted">
          <tr>
            <th class="px-4 py-3">项目名称</th>
            <th class="px-4 py-3">技术栈</th>
            <th class="px-4 py-3">状态</th>
            <th class="px-4 py-3">可见</th>
            <th class="px-4 py-3">排序</th>
            <th class="px-4 py-3">更新时间</th>
            <th class="px-4 py-3">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td class="px-4 py-8 font-mono text-cyber-cyan" colspan="7">项目加载中...</td>
          </tr>
          <tr v-else-if="projectPage.list.length === 0">
            <td class="px-4 py-8 text-cyber-muted" colspan="7">暂无项目。</td>
          </tr>
          <tr v-for="project in projectPage.list" v-else :key="project.id" class="border-t border-cyber-border/60 align-top transition hover:bg-cyber-cyan/5">
            <td class="px-4 py-4">
              <p class="font-semibold text-cyber-text">{{ project.name }}</p>
              <p class="mt-1 max-w-xs truncate text-xs text-cyber-muted">{{ project.description || project.slug }}</p>
            </td>
            <td class="px-4 py-4">
              <div class="flex max-w-56 flex-wrap gap-1">
                <span v-for="tech in project.techStack" :key="tech" class="rounded-full border border-cyber-border px-2 py-0.5 font-mono text-[11px] text-cyber-muted">{{ tech }}</span>
              </div>
            </td>
            <td class="px-4 py-4 font-mono text-xs text-cyber-cyan">{{ formatProjectStatus(project.status) }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ formatBoolean(project.visible) }}</td>
            <td class="px-4 py-4 text-cyber-muted">{{ project.sortOrder ?? 0 }}</td>
            <td class="px-4 py-4 text-xs text-cyber-muted">{{ formatDateTime(project.updatedAt) }}</td>
            <td class="px-4 py-4">
              <div class="flex flex-wrap gap-2">
                <RouterLink class="rounded border border-cyber-border px-2 py-1 font-mono text-[11px] text-cyber-cyan hover:border-cyber-cyan" :to="`/admin/projects/${project.id}/edit`">
                  编辑
                </RouterLink>
                <button class="rounded border border-cyber-danger/60 px-2 py-1 font-mono text-[11px] text-cyber-danger hover:bg-cyber-danger hover:text-cyber-base" type="button" @click="removeProject(project)">
                  删除
                </button>
                <span v-if="actionLoadingId === project.id" class="font-mono text-[11px] text-cyber-cyan">同步中...</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="projectPage.totalPages > 1" class="flex items-center justify-between rounded-lg border border-cyber-border bg-cyber-panel/60 px-4 py-3">
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!projectPage.hasPrevious || loading" type="button" @click="changePage(projectPage.pageNum - 1)">上一页</button>
      <p class="font-mono text-xs text-cyber-muted">第 {{ projectPage.pageNum }} / {{ projectPage.totalPages }} 页</p>
      <button class="rounded-lg border border-cyber-border px-4 py-2 font-mono text-xs text-cyber-muted disabled:opacity-40" :disabled="!projectPage.hasNext || loading" type="button" @click="changePage(projectPage.pageNum + 1)">下一页</button>
    </div>
  </section>
</template>
