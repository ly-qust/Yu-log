<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import {
  fetchAdminSiteSettings,
  updateAdminSiteSetting,
  updateAdminSiteSettingsBatch,
} from '@/api/adminSiteSettings';
import ImageUploader from '@/components/common/ImageUploader.vue';
import type { SiteSetting, SiteSettingBatchItem } from '@/types/site';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime } from '@/utils/format';

const loading = ref(false);
const savingKey = ref('');
const savingAll = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const uploadedImageUrl = ref('');
const settings = ref<SiteSetting[]>([]);
const editableValues = ref<Record<string, string>>({});

const groupLabels: Record<string, string> = {
  home: '首页配置',
  about: '关于我',
  social: '社交链接',
  site: '站点配置',
};

const keyLabels: Record<string, string> = {
  'site.hero.title': '首页标题',
  'site.hero.subtitle': '首页副标题',
  'site.hero.description': '首页描述',
  'site.hero.status_text': '首页状态文字',
  'site.currently_learning': '当前学习内容',
  'site.about.profile': '关于我简介',
  'site.about.skills': '技能列表',
  'site.about.education': '教育经历',
  'site.about.philosophy': '学习理念',
  'site.social.github': 'GitHub 链接',
};

const groupedSettings = computed(() => {
  const groups = new Map<string, SiteSetting[]>();
  for (const setting of settings.value) {
    const groupName = setting.groupName || 'site';
    groups.set(groupName, [...(groups.get(groupName) || []), setting]);
  }
  return Array.from(groups.entries()).map(([groupName, items]) => ({
    groupName,
    label: groupLabels[groupName] || groupName,
    items,
  }));
});

function isJsonSetting(setting: SiteSetting): boolean {
  return setting.settingType === 'JSON' || setting.settingValue.trim().startsWith('{') || setting.settingValue.trim().startsWith('[');
}

function initEditableValues(data: SiteSetting[]) {
  editableValues.value = Object.fromEntries(data.map((setting) => [setting.settingKey, setting.settingValue || '']));
}

async function loadSettings() {
  loading.value = true;
  errorMessage.value = '';
  try {
    const data = await fetchAdminSiteSettings();
    settings.value = data;
    initEditableValues(data);
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '站点配置加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function saveSetting(setting: SiteSetting) {
  savingKey.value = setting.settingKey;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    await updateAdminSiteSetting(setting.settingKey, {
      settingValue: editableValues.value[setting.settingKey] || '',
      settingType: setting.settingType || 'TEXT',
      groupName: setting.groupName || 'site',
      description: setting.description || '',
    });
    successMessage.value = `${keyLabels[setting.settingKey] || setting.settingKey} 已保存`;
    await loadSettings();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '配置保存失败，请稍后重试');
  } finally {
    savingKey.value = '';
  }
}

async function saveAll() {
  savingAll.value = true;
  errorMessage.value = '';
  successMessage.value = '';
  try {
    const payload: SiteSettingBatchItem[] = settings.value.map((setting) => ({
      settingKey: setting.settingKey,
      settingValue: editableValues.value[setting.settingKey] || '',
      settingType: setting.settingType || 'TEXT',
      groupName: setting.groupName || 'site',
      description: setting.description || '',
    }));
    await updateAdminSiteSettingsBatch(payload);
    successMessage.value = '站点配置已批量保存';
    await loadSettings();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '批量保存失败，请稍后重试');
  } finally {
    savingAll.value = false;
  }
}

onMounted(loadSettings);
</script>

<template>
  <section class="space-y-5">
    <div class="glass-panel rounded-glass p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <p class="terminal-label text-sm">admin_site // settings</p>
          <h2 class="mt-3 font-display text-3xl font-semibold">站点配置</h2>
          <p class="mt-2 text-sm text-cyber-muted">编辑首页、关于我、当前学习和社交链接配置；JSON 配置先使用原文编辑。</p>
        </div>
        <button
          class="rounded-lg bg-cyber-cyanBright px-4 py-3 font-mono text-xs font-semibold text-cyber-base transition hover:bg-cyber-cyan disabled:opacity-50"
          :disabled="savingAll || loading"
          type="button"
          @click="saveAll"
        >
          {{ savingAll ? '保存中...' : '批量保存' }}
        </button>
      </div>
    </div>

    <p v-if="successMessage" class="rounded-lg border border-cyber-cyan/40 bg-cyber-cyan/10 px-4 py-3 text-sm text-cyber-cyan">{{ successMessage }}</p>
    <p v-if="errorMessage" class="rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">{{ errorMessage }}</p>

    <div v-if="loading" class="glass-panel rounded-glass p-6">
      <p class="font-mono text-sm text-cyber-cyan">配置加载中...</p>
    </div>

    <div v-else class="space-y-5">
      <section class="glass-panel rounded-glass p-6">
        <p class="terminal-label text-sm">asset_tool // upload</p>
        <h3 class="mt-3 font-display text-2xl font-semibold">上传图片获取 URL</h3>
        <p class="mt-2 text-sm text-cyber-muted">
          可用于关于我头像、首页图片或站点配置中的图片字段。复杂 JSON 配置仍使用原文编辑，把上传后的 URL 填入对应 JSON 字段即可。
        </p>
        <div class="mt-5">
          <ImageUploader v-model="uploadedImageUrl" biz-type="site" label="站点图片资源" />
        </div>
      </section>

      <section v-for="group in groupedSettings" :key="group.groupName" class="glass-panel rounded-glass p-6">
        <p class="terminal-label text-sm">{{ group.groupName }} // config</p>
        <h3 class="mt-3 font-display text-2xl font-semibold">{{ group.label }}</h3>

        <div class="mt-6 grid gap-4">
          <div
            v-for="setting in group.items"
            :key="setting.settingKey"
            class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4"
          >
            <div class="flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <p class="font-semibold text-cyber-text">{{ keyLabels[setting.settingKey] || setting.settingKey }}</p>
                <p class="mt-1 font-mono text-[11px] text-cyber-outline">{{ setting.settingKey }} · {{ setting.settingType || 'TEXT' }}</p>
                <p v-if="setting.description" class="mt-2 text-xs text-cyber-muted">{{ setting.description }}</p>
                <p class="mt-2 text-xs text-cyber-outline">更新时间：{{ formatDateTime(setting.updatedAt) }}</p>
              </div>
              <button
                class="w-fit rounded-lg border border-cyber-cyan/60 px-3 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:opacity-50"
                :disabled="savingKey === setting.settingKey"
                type="button"
                @click="saveSetting(setting)"
              >
                {{ savingKey === setting.settingKey ? '保存中...' : '保存' }}
              </button>
            </div>

            <textarea
              v-if="isJsonSetting(setting)"
              v-model="editableValues[setting.settingKey]"
              class="mt-4 min-h-40 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 font-mono text-sm leading-7 text-cyber-text outline-none focus:border-cyber-cyan"
            ></textarea>
            <input
              v-else
              v-model="editableValues[setting.settingKey]"
              class="mt-4 w-full rounded-lg border border-cyber-border bg-cyber-base/70 px-4 py-3 text-cyber-text outline-none focus:border-cyber-cyan"
              type="text"
            />
          </div>
        </div>
      </section>
    </div>
  </section>
</template>
