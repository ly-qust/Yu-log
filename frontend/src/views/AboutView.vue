<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { fetchAbout } from '@/api/about';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { AboutData } from '@/types/site';
import { getErrorMessage } from '@/utils/errors';

const loading = ref(false);
const errorMessage = ref('');
const about = ref<AboutData | null>(null);

function toStringList(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.filter((item): item is string => typeof item === 'string' && item.trim().length > 0);
  }
  return [];
}

const profile = computed(() => about.value?.profile || {});
const careerDirection = computed(() => toStringList(profile.value.careerDirection));

async function loadAbout() {
  loading.value = true;
  errorMessage.value = '';
  try {
    about.value = await fetchAbout();
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '关于我加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

onMounted(loadAbout);
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <GlassPanel>
        <p class="terminal-label text-sm">profile // about_yu</p>
        <p v-if="loading" class="mt-6 font-mono text-sm text-cyber-cyan">关于我加载中...</p>
        <p v-else-if="errorMessage" class="mt-6 rounded-lg border border-cyber-danger/40 bg-cyber-danger/10 px-4 py-3 text-sm text-cyber-danger">
          {{ errorMessage }}
        </p>

        <div v-else class="grid gap-8 lg:grid-cols-[0.85fr_1.15fr] lg:items-start">
          <div>
            <div class="flex h-24 w-24 items-center justify-center rounded-2xl border border-cyber-cyan/40 bg-cyber-cyan/10 font-display text-3xl font-semibold text-cyber-cyanBright">
              {{ String(profile.nickname || 'Yu').slice(0, 1).toUpperCase() }}
            </div>
            <h1 class="mt-5 font-display text-4xl font-semibold text-cyber-text">{{ profile.nickname || 'Yu' }}</h1>
            <p class="mt-2 text-cyber-cyan">{{ profile.role || '计算机科学与技术学习者' }}</p>
            <p class="mt-5 whitespace-pre-wrap break-words text-sm leading-7 text-cyber-muted">
              {{ profile.description || '这里会展示后台配置的个人简介。' }}
            </p>

            <div class="mt-6 grid gap-3">
              <p v-if="profile.location" class="text-sm text-cyber-muted">位置：{{ profile.location }}</p>
              <p v-if="profile.email" class="text-sm text-cyber-muted">邮箱：{{ profile.email }}</p>
              <a
                v-if="profile.githubUrl"
                class="w-fit rounded-lg border border-cyber-border px-3 py-2 font-mono text-xs text-cyber-cyan transition hover:border-cyber-cyan"
                :href="String(profile.githubUrl)"
                rel="noreferrer"
                target="_blank"
              >
                GitHub
              </a>
            </div>
          </div>

          <div class="grid gap-4">
            <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-5">
              <p class="terminal-label text-sm">direction // career</p>
              <h2 class="mt-3 font-display text-2xl font-semibold">职业方向</h2>
              <div class="mt-4 flex flex-wrap gap-2">
                <span
                  v-for="item in careerDirection"
                  :key="item"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-3 py-1 font-mono text-xs text-cyber-muted"
                >
                  {{ item }}
                </span>
                <span v-if="careerDirection.length === 0" class="text-sm text-cyber-muted">暂无职业方向配置。</span>
              </div>
            </div>

            <div class="rounded-lg border border-cyber-border bg-cyber-base/45 p-5">
              <p class="terminal-label text-sm">skills // stack</p>
              <h2 class="mt-3 font-display text-2xl font-semibold">技能列表</h2>
              <div class="mt-4 flex flex-wrap gap-2">
                <span
                  v-for="skill in about?.skills || []"
                  :key="skill"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-3 py-1 font-mono text-xs text-cyber-muted"
                >
                  {{ skill }}
                </span>
                <span v-if="!about?.skills?.length" class="text-sm text-cyber-muted">暂无技能配置。</span>
              </div>
            </div>
          </div>
        </div>
      </GlassPanel>

      <div class="grid gap-6 lg:grid-cols-[1fr_1.1fr]">
        <GlassPanel>
          <p class="terminal-label text-sm">education // path</p>
          <h2 class="mt-3 font-display text-2xl font-semibold">教育经历</h2>
          <div class="mt-5 grid gap-3">
            <div
              v-for="item in about?.education || []"
              :key="item"
              class="rounded-lg border border-cyber-border bg-cyber-base/45 p-4 text-sm leading-7 text-cyber-muted"
            >
              {{ item }}
            </div>
            <p v-if="!about?.education?.length" class="text-sm text-cyber-muted">暂无教育经历配置。</p>
          </div>
        </GlassPanel>

        <GlassPanel>
          <p class="terminal-label text-sm">philosophy // learning</p>
          <h2 class="mt-3 font-display text-2xl font-semibold">学习理念</h2>
          <p class="mt-5 whitespace-pre-wrap break-words text-sm leading-7 text-cyber-muted">
            {{ about?.learningPhilosophy || '暂无学习理念配置。' }}
          </p>
        </GlassPanel>
      </div>
    </div>
  </PublicLayout>
</template>
