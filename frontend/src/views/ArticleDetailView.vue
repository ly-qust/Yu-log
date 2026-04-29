<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { fetchArticleDetail, likeArticle } from '@/api/articles';
import GlassPanel from '@/components/common/GlassPanel.vue';
import PublicLayout from '@/components/layout/PublicLayout.vue';
import type { ArticleDetail } from '@/types/content';
import { getErrorMessage } from '@/utils/errors';
import { formatCount, formatDateTime } from '@/utils/format';

const route = useRoute();
const router = useRouter();
const article = ref<ArticleDetail | null>(null);
const loading = ref(false);
const liking = ref(false);
const errorMessage = ref('');

const articleId = computed(() => {
  const value = route.params.id;
  return Array.isArray(value) ? value[0] : value;
});

async function loadArticle() {
  if (!articleId.value) {
    errorMessage.value = '文章不存在或已下线';
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    article.value = await fetchArticleDetail(articleId.value);
  } catch (error) {
    article.value = null;
    errorMessage.value = getErrorMessage(error, '加载失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

async function likeCurrentArticle() {
  if (!article.value || liking.value) {
    return;
  }

  liking.value = true;
  errorMessage.value = '';
  try {
    const likeCount = await likeArticle(article.value.id);
    article.value = {
      ...article.value,
      likeCount,
    };
  } catch (error) {
    errorMessage.value = getErrorMessage(error, '点赞失败，请稍后重试');
  } finally {
    liking.value = false;
  }
}

onMounted(loadArticle);
watch(articleId, loadArticle);
</script>

<template>
  <PublicLayout>
    <div class="space-y-6">
      <button
        class="font-mono text-xs text-cyber-cyan transition hover:text-cyber-cyanBright"
        type="button"
        @click="router.push('/articles')"
      >
        返回文章列表
      </button>

      <GlassPanel v-if="loading">
        <p class="font-mono text-sm text-cyber-cyan">文章加载中...</p>
      </GlassPanel>

      <GlassPanel v-else-if="errorMessage">
        <p class="terminal-label text-sm">article // detail</p>
        <h1 class="mt-4 font-display text-3xl font-semibold">文章不存在或已下线</h1>
        <p class="mt-3 text-cyber-danger">{{ errorMessage }}</p>
      </GlassPanel>

      <article v-else-if="article" class="space-y-6">
        <GlassPanel>
          <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
            <div class="max-w-3xl">
              <p class="terminal-label text-sm">
                article // {{ article.categoryName || '未分类' }}
              </p>
              <h1 class="mt-4 font-display text-4xl font-semibold leading-tight text-cyber-text">
                {{ article.title }}
              </h1>
              <p class="mt-4 text-cyber-muted">{{ article.summary || '暂无摘要。' }}</p>

              <div class="mt-5 flex flex-wrap gap-2">
                <span
                  v-for="tag in article.tags"
                  :key="tag.id"
                  class="rounded-full border border-cyber-border bg-cyber-base/60 px-2.5 py-1 font-mono text-[11px] text-cyber-muted"
                >
                  #{{ tag.name }}
                </span>
              </div>
            </div>

            <div class="grid min-w-60 grid-cols-2 gap-3 rounded-lg border border-cyber-border bg-cyber-base/50 p-4">
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">发布时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(article.publishedAt) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">更新时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatDateTime(article.updatedAt) }}</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">阅读时间</p>
                <p class="mt-1 text-sm text-cyber-text">{{ article.readingTime }} 分钟</p>
              </div>
              <div>
                <p class="font-mono text-[11px] uppercase text-cyber-outline">浏览量</p>
                <p class="mt-1 text-sm text-cyber-text">{{ formatCount(article.viewCount) }}</p>
              </div>
            </div>
          </div>

          <button
            class="mt-6 rounded-lg border border-cyber-cyan/60 px-4 py-2 font-mono text-xs text-cyber-cyan transition hover:bg-cyber-cyan hover:text-cyber-base disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="liking"
            type="button"
            @click="likeCurrentArticle"
          >
            {{ liking ? '同步中...' : `点赞 / ${formatCount(article.likeCount)}` }}
          </button>
        </GlassPanel>

        <GlassPanel>
          <p class="terminal-label text-sm">markdown_view // 正文内容</p>
          <pre class="mt-5 whitespace-pre-wrap break-words font-sans text-base leading-8 text-cyber-text">{{ article.content }}</pre>
        </GlassPanel>
      </article>
    </div>
  </PublicLayout>
</template>
