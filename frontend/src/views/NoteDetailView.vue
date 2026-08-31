<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { fetchNoteDetail } from '@/api/notes';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { NoteItem } from '@/types/note';
import { getErrorMessage } from '@/utils/errors';
import { formatDateTime } from '@/utils/format';

const route = useRoute();
const note = ref<NoteItem | null>(null);
const loading = ref(false);
const errorMessage = ref('');

const noteId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});

async function loadNote() {
  if (!noteId.value) {
    note.value = null;
    errorMessage.value = '笔记不存在或已下线';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    note.value = await fetchNoteDetail(noteId.value);
  } catch (error) {
    note.value = null;
    errorMessage.value = getErrorMessage(error, '笔记不存在或已下线');
  } finally {
    loading.value = false;
  }
}

onMounted(loadNote);
watch(noteId, loadNote);
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <RouterLink class="font-mono text-xs text-cyber-cyan transition hover:text-cyber-cyanBright" to="/notes">
        返回笔记列表
      </RouterLink>

      <GlassPanel v-if="loading">
        <p class="font-mono text-sm text-cyber-cyan">笔记加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="errorMessage">
        <p class="terminal-label text-sm">note // detail</p>
        <h1 class="mt-4 font-display text-3xl font-semibold">笔记不存在或已下线</h1>
        <p class="mt-3 text-cyber-muted">{{ errorMessage }}</p>
        <RouterLink
          class="mt-6 inline-flex rounded-lg border border-cyber-cyan/60 px-4 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base"
          to="/notes"
        >
          返回笔记列表
        </RouterLink>
      </GlassPanel>

      <article v-else-if="note" class="space-y-6">
        <GlassPanel>
          <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
            <div class="max-w-3xl">
              <p class="terminal-label text-sm">note // {{ note.topic || '未分类主题' }}</p>
              <h1 class="mt-4 font-display text-4xl font-semibold leading-tight text-cyber-text">
                {{ note.title }}
              </h1>
              <p class="mt-4 text-cyber-muted">{{ note.summary || '暂无摘要。' }}</p>

              <div class="mt-5 flex flex-wrap gap-2">
                <span
                  v-for="tag in note.tags"
                  :key="tag"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
                >
                  #{{ tag }}
                </span>
                <span v-if="note.tags.length === 0" class="text-sm text-cyber-muted">暂无标签</span>
              </div>
            </div>

            <div class="grid min-w-60 gap-3 rounded-lg border border-cyber-border bg-cyber-base/50 p-4 sm:grid-cols-2 lg:grid-cols-1">
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">主题</p>
                <p class="mt-1 text-sm text-cyber-text">{{ note.topic || '未分类主题' }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">创建时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(note.createdAt) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">更新时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(note.updatedAt) }}</p>
              </div>
            </div>
          </div>
        </GlassPanel>

        <GlassPanel>
          <p class="terminal-label text-sm">markdown_view // 笔记内容</p>
          <pre class="mt-5 whitespace-pre-wrap break-words font-sans text-base leading-8 text-cyber-text">{{ note.content || '暂无笔记内容。' }}</pre>
        </GlassPanel>
      </article>
    </div>
  </PublicLayout>
</template>
