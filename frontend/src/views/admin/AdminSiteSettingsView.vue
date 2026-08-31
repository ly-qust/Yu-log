<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';

import { fetchAdminSiteSettings, updateAdminSiteSetting, updateAdminSiteSettingsBatch } from '@/api/adminSiteSettings';
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue';
import BaseButton from '@/components/common/BaseButton.vue';
import BaseInput from '@/components/common/BaseInput.vue';
import BaseTextarea from '@/components/common/BaseTextarea.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard';
import { useAdminFeedbackStore } from '@/stores/adminFeedback';
import type { SiteSetting, SiteSettingBatchItem } from '@/types/site';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime } from '@/utils/format';

interface ProfileFields {
  nickname: string;
  role: string;
  avatar: string;
  description: string;
  location: string;
  email: string;
  githubUrl: string;
  careerDirection: string[];
}

const feedback = useAdminFeedbackStore();
const loading = ref(false);
const savingKey = ref('');
const savingAll = ref(false);
const errorMessage = ref('');
const settings = ref<SiteSetting[]>([]);
const editableValues = reactive<Record<string, string>>({});
const structuredLists = reactive<Record<string, string[]>>({});
const profileExtras = ref<Record<string, unknown>>({});
const profile = reactive<ProfileFields>({ nickname: '', role: '', avatar: '', description: '', location: '', email: '', githubUrl: '', careerDirection: [] });

const groupLabels: Record<string, string> = { home: '首页配置', about: '关于我', social: '社交链接', site: '站点配置', general: '通用配置' };
const keyLabels: Record<string, string> = {
  'site.hero.title': '首页标题', 'site.hero.subtitle': '首页副标题', 'site.hero.description': '首页描述', 'site.hero.status_text': '首页状态文字',
  'site.currently_learning': '当前学习内容', 'site.about.profile': '关于我简介', 'site.about.skills': '技能列表', 'site.about.education': '教育经历',
  'site.about.philosophy': '学习理念', 'site.social.github': 'GitHub 链接',
};
const profileFields: Array<{ key: keyof Omit<ProfileFields, 'careerDirection'>; label: string }> = [
  { key: 'nickname', label: '昵称' }, { key: 'role', label: '身份 / 角色' }, { key: 'location', label: '所在位置' },
  { key: 'email', label: '邮箱' }, { key: 'githubUrl', label: 'GitHub 地址' },
];
const listSettingKeys = new Set(['site.about.skills', 'site.about.education', 'site.currently_learning']);

const groupedSettings = computed(() => {
  const groups = new Map<string, SiteSetting[]>();
  for (const setting of settings.value) {
    const groupName = setting.groupName || 'general';
    groups.set(groupName, [...(groups.get(groupName) || []), setting]);
  }
  return Array.from(groups.entries()).map(([groupName, items]) => ({ groupName, label: groupLabels[groupName] || groupName, items }));
});

function isJsonSetting(setting: SiteSetting) {
  return setting.settingType === 'JSON' || setting.settingValue.trim().startsWith('{') || setting.settingValue.trim().startsWith('[');
}
function isProfileSetting(setting: SiteSetting) { return setting.settingKey === 'site.about.profile'; }
function isListSetting(setting: SiteSetting) { return listSettingKeys.has(setting.settingKey); }

function parseStructuredSetting(setting: SiteSetting) {
  if (!isProfileSetting(setting) && !isListSetting(setting)) return;
  try {
    const value = JSON.parse(setting.settingValue || (isProfileSetting(setting) ? '{}' : '[]')) as unknown;
    if (isProfileSetting(setting) && value && typeof value === 'object' && !Array.isArray(value)) {
      const object = value as Record<string, unknown>;
      profileExtras.value = { ...object };
      for (const field of profileFields) profile[field.key] = typeof object[field.key] === 'string' ? object[field.key] as string : '';
      profile.description = typeof object.description === 'string' ? object.description : '';
      profile.avatar = typeof object.avatar === 'string' ? object.avatar : '';
      profile.careerDirection = Array.isArray(object.careerDirection) ? object.careerDirection.filter((item): item is string => typeof item === 'string') : [];
    } else if (isListSetting(setting)) {
      structuredLists[setting.settingKey] = Array.isArray(value) ? value.filter((item): item is string => typeof item === 'string') : [];
    }
  } catch {
    errorMessage.value = `${keyLabels[setting.settingKey] || setting.settingKey} 的 JSON 数据无法解析，请使用 Advanced JSON 修复。`;
  }
}

function initEditableValues(data: SiteSetting[]) {
  for (const key of Object.keys(editableValues)) delete editableValues[key];
  for (const setting of data) {
    editableValues[setting.settingKey] = setting.settingValue || '';
    parseStructuredSetting(setting);
  }
}

function listValue(key: string) { return structuredLists[key] || []; }
function addListItem(key: string) { if (!structuredLists[key]) structuredLists[key] = []; structuredLists[key].push(''); }
function removeListItem(key: string, index: number) { const items = listValue(key); items.splice(index, 1); if (!items.length) items.push(''); }
function addCareerDirection() { profile.careerDirection.push(''); }
function removeCareerDirection(index: number) { profile.careerDirection.splice(index, 1); }

function settingValue(setting: SiteSetting) {
  if (isProfileSetting(setting)) return JSON.stringify({ ...profileExtras.value, ...profile, careerDirection: profile.careerDirection.filter(Boolean) }, null, 2);
  if (isListSetting(setting)) return JSON.stringify(listValue(setting.settingKey).map((item) => item.trim()).filter(Boolean));
  return editableValues[setting.settingKey] || '';
}

function validateJson(setting: SiteSetting, value: string) {
  if (!isJsonSetting(setting) || isProfileSetting(setting) || isListSetting(setting)) return true;
  try { JSON.parse(value); return true; } catch { errorMessage.value = `${keyLabels[setting.settingKey] || setting.settingKey} 不是合法 JSON，已阻止保存。`; return false; }
}

function payloadFor(setting: SiteSetting): SiteSettingBatchItem | null {
  const value = settingValue(setting);
  if (!validateJson(setting, value)) return null;
  return { settingKey: setting.settingKey, settingValue: value, settingType: setting.settingType || (isJsonSetting(setting) ? 'JSON' : 'TEXT'), groupName: setting.groupName || 'general', description: setting.description || '' };
}

function snapshot() { return JSON.stringify({ editableValues, structuredLists, profile }); }
const guard = useUnsavedChangesGuard(snapshot, { label: '站点配置', isSaving: () => savingAll.value || Boolean(savingKey.value) });

async function loadSettings() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data = await fetchAdminSiteSettings();
    settings.value = data;
    initEditableValues(data);
    guard.markClean();
  } catch (error) { errorMessage.value = getErrorMessage(error, '站点配置加载失败，请稍后重试'); }
  finally { loading.value = false; }
}

async function saveSetting(setting: SiteSetting) {
  const payload = payloadFor(setting);
  if (!payload) return;
  savingKey.value = setting.settingKey;
  errorMessage.value = '';
  try {
    await updateAdminSiteSetting(setting.settingKey, payload);
    feedback.success(`${keyLabels[setting.settingKey] || setting.settingKey} 已保存。`);
    await loadSettings();
  } catch (error) { errorMessage.value = getErrorMessage(error, '配置保存失败，请稍后重试'); feedback.error(errorMessage.value); }
  finally { savingKey.value = ''; }
}

async function saveAll() {
  const payloads = settings.value.map(payloadFor);
  if (payloads.some((item) => item === null)) return;
  savingAll.value = true;
  errorMessage.value = '';
  try {
    await updateAdminSiteSettingsBatch(payloads as SiteSettingBatchItem[]);
    feedback.success('站点配置已批量保存。');
    await loadSettings();
  } catch (error) { errorMessage.value = getErrorMessage(error, '批量保存失败，请稍后重试'); feedback.error(errorMessage.value); }
  finally { savingAll.value = false; }
}

onMounted(loadSettings);
</script>

<template>
  <section class="space-y-5">
    <AdminPageHeader eyebrow="system // site settings" title="站点配置" description="常用配置使用结构化表单维护；只有真正开放的未知 JSON 才进入 Advanced JSON，保存前会校验格式。">
      <template #actions><div class="flex flex-wrap gap-2"><span class="admin-editor-state" :class="guard.isDirty ? 'is-dirty' : 'is-clean'"><i aria-hidden="true"></i>{{ guard.isDirty ? 'Unsaved changes' : 'Saved' }}</span><BaseButton :loading="savingAll" :disabled="loading" size="sm" @click="saveAll">批量保存</BaseButton></div></template>
    </AdminPageHeader>

    <div v-if="errorMessage" class="admin-form-error" role="alert">{{ errorMessage }}</div>
    <div v-if="loading" class="surface-muted rounded-panel p-8 font-mono text-sm text-brand" role="status">站点配置加载中...</div>

    <div v-else class="space-y-5">
      <section v-if="profile.nickname || profile.role || settings.some(isProfileSetting)" class="surface-muted rounded-panel p-5 md:p-6">
        <div class="flex flex-wrap items-start justify-between gap-4"><div><p class="admin-eyebrow">about // profile</p><h2 class="mt-1 text-xl font-semibold text-text-primary">关于我资料</h2><p class="mt-1 text-sm text-text-muted">字段直接映射现有 About 配置，未知字段会被保留。</p></div><BaseButton size="sm" :loading="savingKey === 'site.about.profile'" @click="saveSetting(settings.find(isProfileSetting)!)">保存资料</BaseButton></div>
        <div class="mt-5 grid gap-4 md:grid-cols-2"><BaseInput v-for="field in profileFields" :key="field.key" v-model="profile[field.key]" :label="field.label" :type="field.key === 'email' ? 'email' : field.key === 'githubUrl' ? 'url' : 'text'" /><BaseTextarea v-model="profile.description" class="md:col-span-2" label="个人简介" :rows="3" /></div>
        <div class="mt-4"><ImageUploader v-model="profile.avatar" biz-type="avatar" label="头像" /></div>
        <div class="mt-5"><div class="flex items-center justify-between"><p class="text-sm font-medium text-text-secondary">职业方向</p><BaseButton size="sm" variant="ghost" @click="addCareerDirection">+ 添加</BaseButton></div><div class="mt-2 grid gap-2 sm:grid-cols-2"><div v-for="(_, index) in profile.careerDirection" :key="index" class="flex gap-2"><input v-model="profile.careerDirection[index]" class="admin-structured-input" type="text" placeholder="Java 后端开发" /><BaseButton size="sm" variant="ghost" aria-label="移除职业方向" @click="removeCareerDirection(index)">×</BaseButton></div></div></div>
      </section>

      <section v-for="group in groupedSettings" :key="group.groupName" class="surface-muted rounded-panel p-5 md:p-6">
        <p class="admin-eyebrow">{{ group.groupName }} // config</p><h2 class="mt-1 text-xl font-semibold text-text-primary">{{ group.label }}</h2>
        <div class="mt-5 grid gap-4">
          <template v-for="setting in group.items" :key="setting.settingKey">
          <div v-if="!isProfileSetting(setting)" class="admin-setting-card">
            <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between"><div><p class="font-medium text-text-primary">{{ keyLabels[setting.settingKey] || setting.settingKey }}</p><p class="mt-1 font-mono text-[11px] text-text-muted">{{ setting.settingKey }} · {{ setting.settingType || 'TEXT' }}</p><p v-if="setting.description" class="mt-2 text-xs text-text-muted">{{ setting.description }}</p><p class="mt-2 text-xs text-text-muted">更新时间：{{ formatDateTime(setting.updatedAt) }}</p></div><BaseButton size="sm" variant="secondary" :loading="savingKey === setting.settingKey" @click="saveSetting(setting)">保存</BaseButton></div>
            <div v-if="isListSetting(setting)" class="mt-4 grid gap-2"><div v-for="(_, index) in listValue(setting.settingKey)" :key="index" class="flex gap-2"><input v-model="listValue(setting.settingKey)[index]" class="admin-structured-input" type="text" /><BaseButton size="sm" variant="ghost" aria-label="移除条目" @click="removeListItem(setting.settingKey, index)">×</BaseButton></div><BaseButton class="w-fit" size="sm" variant="ghost" @click="addListItem(setting.settingKey)">+ 添加条目</BaseButton></div>
            <BaseTextarea v-else-if="isJsonSetting(setting)" v-model="editableValues[setting.settingKey]" class="mt-4" label="Advanced JSON" hint="仅开放 JSON 配置使用；保存前会校验语法。" :rows="6" />
            <BaseInput v-else v-model="editableValues[setting.settingKey]" class="mt-4" label="配置值" />
          </div>
          </template>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.admin-editor-state { display: inline-flex; align-items: center; gap: .45rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: 999px; padding: .45rem .7rem; color: rgb(var(--color-text-muted)); font-family: 'JetBrains Mono', monospace; font-size: .65rem; }.admin-editor-state i { width: .42rem; height: .42rem; border-radius: 50%; background: rgb(var(--color-success)); }.admin-editor-state.is-dirty { border-color: rgb(var(--color-warning) / .5); color: rgb(var(--color-warning)); }.admin-editor-state.is-dirty i { background: rgb(var(--color-warning)); }.admin-setting-card { border: 1px solid rgb(var(--color-border-subtle) / .7); border-radius: .75rem; background: rgb(var(--color-surface) / .38); padding: 1rem; }.admin-structured-input { min-width: 0; width: 100%; height: 2.7rem; border: 1px solid rgb(var(--color-border-subtle)); border-radius: .6rem; background: rgb(var(--color-surface) / .72); padding: 0 .75rem; color: rgb(var(--color-text-primary)); outline: none; }.admin-structured-input:focus { border-color: rgb(var(--color-brand-primary)); }.admin-form-error { border: 1px solid rgb(var(--color-danger) / .4); border-radius: .65rem; background: rgb(var(--color-danger) / .08); padding: .75rem 1rem; color: rgb(var(--color-danger)); font-size: .82rem; }
</style>
